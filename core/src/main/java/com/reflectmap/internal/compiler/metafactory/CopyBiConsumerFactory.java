package com.reflectmap.internal.compiler.metafactory;

import com.reflectmap.internal.compiler.LambdaCompilerInstruction;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;

public final class CopyBiConsumerFactory {
    private CopyBiConsumerFactory() {}

    private static final String     NAME = "accept";
    private static final MethodType MT   = MethodType.methodType(void.class, Object.class, Object.class);

    /**
     * Compile a copying consumer from a resolved instruction.
     */
    @SuppressWarnings("unchecked")
    public static BiConsumer<Object, Object> of(LambdaCompilerInstruction instruction) throws Throwable {
        // Get the raw getter / setter handles and adapt them.
        MethodHandle getter = instruction.getter().asType(MethodType.methodType(Object.class, Object.class));
        MethodHandle setter = instruction.setter().asType(MT);

        // First apply getter(src) -> value, then setter(dst, value).
        MethodHandle filtered = MethodHandles.filterArguments(setter, 1, getter);

        // Filtered is (dst, src) -> void; permute to (src, dst) -> void.
        MethodHandle composite = MethodHandles.permuteArguments(filtered, MT, 1, 0);

        // Produce a single BiConsumer<Object,Object> that invokes that composite MH.
        return LambdaFactory.create(BiConsumer.class, NAME, MT, composite);
    }
}