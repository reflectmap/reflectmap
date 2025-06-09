package com.reflectmap.internal.compiler.metafactory;

import java.lang.invoke.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A high-performance factory for creating SAM instances via LambdaMetafactory.
 * Internally caches one CallSite per (interface, method name, signature).
 */
public final class LambdaFactory {
    private LambdaFactory() {}

    // Cache of one CallSite per SAM signature.
    private static final ConcurrentMap<CacheKey, CallSite> CACHE = new ConcurrentHashMap<>();

    // Lookup for the current class. Grants private access for lambda bootstrap.
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Creates a new SAM implementation for a given functional interface.
     * The returned instance will delegate all calls of its SAM to the provided MethodHandle.
     *
     * @param iface      the functional interface class (e.g. {@code Function})
     * @param samName    the SAM name (e.g. {@code apply})
     * @param samType    the SAM type (return type + parameters)
     * @param target     the handle to invoke the SAM is called
     * @return           an instance of the functional interface bound to the target handle
     * @throws Throwable if creation or invocation fails
     */
    @SuppressWarnings("unchecked")
    public static <F> F create(Class<F> iface, String samName, MethodType samType, MethodHandle target) throws Throwable {
        Objects.requireNonNull(iface,   "iface");
        Objects.requireNonNull(samName, "samName");
        Objects.requireNonNull(samType, "samType");
        Objects.requireNonNull(target,  "target");

        CacheKey key = new CacheKey(iface, samName, samType);

        // Only compute the call-site once per SAM type.
        CallSite cs = CACHE.computeIfAbsent(key, k -> {
            try {
                return LambdaMetafactory.metafactory(
                        LOOKUP,
                        k.samName,
                        MethodType.methodType(key.iface, MethodHandle.class),
                        k.samType.erase(),
                        MethodHandles.exactInvoker(key.samType),
                        k.samType
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        // Retype the factory to (MethodHandle) -> Object so invokeExact matches.
        MethodHandle factory = cs.getTarget().asType(MethodType.methodType(Object.class, MethodHandle.class));

        // Adapt the target handle to the precise SAM signature.
        MethodHandle adapted = target.asType(samType);

        // Call invokeExact on the factory handle with our adapted target.
        // This returns the newly-created SAM instance, typed as Object.
        Object raw = factory.invokeExact(adapted);

        // Cast the SAM instance back to the original functional interface type.
        return (F) raw;
    }

    // Key for caching per SAM signature (interface, method name, signature)
    private record CacheKey(Class<?> iface, String samName, MethodType samType) {}

}