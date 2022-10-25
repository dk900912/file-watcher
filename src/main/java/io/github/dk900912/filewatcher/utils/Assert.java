package io.github.dk900912.filewatcher.utils;

import java.util.function.Supplier;

/**
 * Assertion utility class that assists in validating arguments.
 *
 * @author dukui
 */
public final class Assert {

    private Assert() {}

    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void state(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalStateException(nullSafeGet(messageSupplier));
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isNull(Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }


    public static void hasLength(String text, String message) {
        if (!StringUtil.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasLength(String text, Supplier<String> messageSupplier) {
        if (!StringUtil.hasLength(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void hasText(String text, String message) {
        if (!StringUtil.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text, Supplier<String> messageSupplier) {
        if (!StringUtil.hasText(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    private static String nullSafeGet(Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }

}