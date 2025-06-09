package com.reflectmap.internal.compiler.metafactory;

import java.lang.invoke.*;
import java.lang.reflect.Field;

public final class MethodHandleFactory {
    private MethodHandleFactory() {}

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final ClassValue<MethodHandles.Lookup> LOOKUPS = new ClassValue<>() {
        @Override
        protected MethodHandles.Lookup computeValue(Class<?> type) {
            try {
                return MethodHandles.privateLookupIn(type, LOOKUP);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static MethodHandles.Lookup lookup(Class<?> owner) {
        return LOOKUPS.get(owner);
    }

    public static MethodHandle findStatic(Class<?> owner, String methodName, MethodType type) {
        try {
            return lookup(owner).findStatic(owner, methodName, type);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MethodHandle findGetter(Class<?> owner, Field field) {
        try {
            return lookup(owner).findGetter(owner, field.getName(), field.getType());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MethodHandle findSetter(Class<?> owner, Field field) {
        try {
            return lookup(owner).findSetter(owner, field.getName(), field.getType());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

}