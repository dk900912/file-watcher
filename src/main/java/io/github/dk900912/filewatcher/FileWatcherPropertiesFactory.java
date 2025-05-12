package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates a new {@link FileWatcherProperties} instance by mapping configuration properties
 * from the provided {@code Map<String, Object>} to constructor parameters.
 *
 * <p>This method is designed with two key considerations:
 *
 * <ol>
 *     <li><b>Property Value Conversion via {@link PropertyFunction}</b>:<br>
 *         The {@link PropertyFunction} interface allows custom conversion of property values
 *         (e.g., converting a String like "1000ms" to a Java {@code Duration} object).
 *         This design decouples the core logic from Spring's {@code ConversionService} while
 *         enabling integration with Spring's type conversion capabilities for business use cases.
 *         By accepting a functional interface, it provides extensibility for users to implement
 *         their own conversion strategies without requiring Spring dependencies in the core library.
 *     </li>
 *
 *     <li><b>Preserving Parameter Names via Compiler Flag</b>:<br>
 *         To resolve constructor parameter names at runtime via reflection, the Java compiler must
 *         retain parameter names (i.e., avoid "parameter name erasure"). This requires adding the
 *         {@code --parameters} flag during compilation. Without this flag, parameter names would
 *         be inaccessible, breaking the mapping between configuration keys and constructor arguments.
 *         As an alternative to the compiler flag, you can use Spring's
 *         {@code StandardReflectionParameterNameDiscoverer} to infer parameter names at runtime.
 *     </li>
 * </ol>
 *
 * @author dukui
 */
public class FileWatcherPropertiesFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherPropertiesFactory.class);

    /**
     * @param properties The configuration properties to map to constructor parameters
     * @param propertyFunction A custom converter for property value transformations
     * @return A new {@link FileWatcherProperties} instance with mapped values
     */
    public static FileWatcherProperties createFromMap(Map<String, Object> properties, PropertyFunction propertyFunction) {
        Assert.notNull(properties, "Properties must not be null");

        Map<String, Object> normalizedProperties = properties.entrySet().stream()
                .map(entry -> {
                    String normalizedKey = normalizePropertyName(entry.getKey());
                    return Map.entry(normalizedKey, entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Constructor<?> mostParameterizedConstructor = getMostParameterizedConstructor();
        Assert.notNull(mostParameterizedConstructor, "No suitable constructor found for FileWatcherProperties");

        List<Object> constructorArguments = new ArrayList<>();
        for (Parameter parameter : mostParameterizedConstructor.getParameters()) {
            String parameterName = normalizePropertyName(parameter.getName());
            Object rawValue = normalizedProperties.get(parameterName);
            Object convertedValue = propertyFunction == null ? rawValue : propertyFunction.apply(rawValue.getClass(), parameter.getType(), rawValue);
            constructorArguments.add(convertedValue);
        }

        FileWatcherProperties fileWatcherProperties = null;
        try {
            mostParameterizedConstructor.setAccessible(true);
            fileWatcherProperties = (FileWatcherProperties) mostParameterizedConstructor.newInstance(constructorArguments.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to create FileWatcherProperties instance", e);
        }
        return fileWatcherProperties;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getMostParameterizedConstructor() {
        Constructor<T>[] constructors = (Constructor<T>[]) FileWatcherProperties.class.getDeclaredConstructors();
        if (constructors.length == 0) {
            return null;
        }

        // Group constructors by parameter count
        Map<Integer, List<Constructor<T>>> groupedByParameterCount =
                Arrays.stream(constructors)
                        .collect(Collectors.groupingBy(Constructor::getParameterCount));

        // Find the entry with the maximum key (parameter count)
        Map.Entry<Integer, List<Constructor<T>>> maxEntry = groupedByParameterCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .orElseThrow(() -> new IllegalStateException("No constructors found"));

        // Check for ambiguity: multiple constructors with the same max parameter count
        if (maxEntry.getValue().size() > 1) {
            throw new IllegalStateException(
                    String.format("Ambiguous constructor(s) with %d parameters found: %s",
                            maxEntry.getKey(),
                            maxEntry.getValue()
                                    .stream()
                                    .map(Constructor::toString)
                                    .collect(Collectors.joining(", "))
                    ));
        }
        return maxEntry.getValue().getFirst();
    }

    private static String normalizePropertyName(String input) {
        if (input == null) {
            return null;
        }
        String lowerCase = input.toLowerCase();
        return lowerCase.replaceAll("[\\-_]", "");
    }

    /**
     * A functional interface that represents a function which accepts source type, target type,
     * and an object value to produce a converted result of the specified target type.
     */
    @FunctionalInterface
    public interface PropertyFunction {

        /**
         * Applies this converter to the given arguments.
         *
         * @param sourceType the type of the source object
         * @param targetType the target type to convert to
         * @param value      the object to be converted
         * @return the converted object
         */
        Object apply(Class<?> sourceType, Class<?> targetType, Object value);
    }
}
