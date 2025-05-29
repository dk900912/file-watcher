package io.github.dk900912.filewatcher;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author dukui
 */
public class FileWatcherPropertiesFactoryTest {

    private static Path testRoot;

    @BeforeAll
    public static void setup() throws IOException {
        testRoot = Files.createTempDirectory("snapshot-test");
    }

    @AfterAll
    public static void cleanup() throws IOException {
        try (Stream<Path> pathStream = Files.walk(testRoot)) {
            pathStream
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @Test
    public void testCreateFromMapWithEmpty() {
        assertNull(FileWatcherPropertiesFactory.createFromMap(Map.of(), null));
    }

    @Test
    public void testCreateFromMapWithAllParameters() {
        Path validDir1 = testRoot.resolve("dir1");
        Path validDir2 = testRoot.resolve("dir2");
        Path repository = null;

        try {
            Files.createDirectory(validDir1);
            Files.createDirectory(validDir2);
            repository = Files.createTempFile(testRoot, "file-watcher", ".snapshot", new FileAttribute[0]);
        } catch (IOException e) {
            fail("Failed to create test directories: " + e.getMessage());
        }

        Map<String, Object> properties = Map.of(
            "directories", Arrays.asList(validDir1.toString(), validDir2.toString()),
            "daemon", false,
            "name", "Custom Watcher",
            "acceptedStrategy", Map.of(),
            "snapshotState", new FileWatcherProperties.SnapshotState(true, repository.toString()),
            "remainingScans", 5,
            "pollInterval", Duration.ofMillis(2000),
            "quietPeriod", Duration.ofMillis(100)
        );

        FileWatcherPropertiesFactory.PropertyFunction mockConverter = (sourceType, targetType, value) -> {
            if (value instanceof Long && targetType == Duration.class) {
                return Duration.ofMillis((Long) value);
            }
            return value;
        };

        FileWatcherProperties propertiesInstance = FileWatcherPropertiesFactory.createFromMap(properties, mockConverter);
        assertNotNull(propertiesInstance);
        assertEquals(false, propertiesInstance.getDaemon());
        assertEquals("Custom Watcher", propertiesInstance.getName());
        assertEquals(2, propertiesInstance.getDirectories().size());
        assertEquals(Map.of(ANY, Set.of()), propertiesInstance.getAcceptedStrategy());
        assertEquals(true, propertiesInstance.getSnapshotState().getEnabled());
        assertEquals(repository.toString(), propertiesInstance.getSnapshotState().getRepository());
        assertEquals(5, propertiesInstance.getRemainingScans().get());
        assertEquals(Duration.ofMillis(2000), propertiesInstance.getPollInterval().get());
    }

    @Test
    public void testCreateFromMapWithAllParameters_InvalidSnapshotState() {
        Path validDir3 = testRoot.resolve("dir3");
        Path validDir4 = testRoot.resolve("dir4");
        Path repository = validDir3;

        try {
            Files.createDirectory(validDir3);
            Files.createDirectory(validDir4);
        } catch (IOException e) {
            fail("Failed to create test directories: " + e.getMessage());
        }

        Map<String, Object> properties = Map.of(
                "directories", Arrays.asList(validDir3.toString(), validDir4.toString()),
                "daemon", false,
                "name", "Custom Watcher",
                "acceptedStrategy", Map.of(),
                "snapshotState", new FileWatcherProperties.SnapshotState(true, repository.toString()),
                "remainingScans", 5,
                "pollInterval", Duration.ofMillis(2000),
                "quietPeriod", Duration.ofMillis(100)
        );

        FileWatcherPropertiesFactory.PropertyFunction mockConverter = (sourceType, targetType, value) -> {
            if (value instanceof Long && targetType == Duration.class) {
                return Duration.ofMillis((Long) value);
            }
            return value;
        };

        assertNull(FileWatcherPropertiesFactory.createFromMap(properties, mockConverter));
    }



    @Test
    public void testCreateFromMapWithDefaultValues() {
        Path validDir5 = testRoot.resolve("dir5");
        
        try {
            Files.createDirectory(validDir5);
        } catch (IOException e) {
            fail("Failed to create test directory: " + e.getMessage());
        }

        Map<String, Object> properties = Map.of("directories", List.of(validDir5.toString()));
        FileWatcherProperties propertiesInstance = FileWatcherPropertiesFactory.createFromMap(properties, null);
        assertNotNull(propertiesInstance);
        
        try {
            Field defaultDaemon = FileWatcherProperties.class.getDeclaredField("DEFAULT_DAEMON");
            defaultDaemon.setAccessible(true);
            assertEquals(defaultDaemon.get(null), propertiesInstance.getDaemon());
            
            Field defaultName = FileWatcherProperties.class.getDeclaredField("DEFAULT_NAME");
            defaultName.setAccessible(true);
            assertEquals(defaultName.get(null), propertiesInstance.getName());
            
        } catch (Exception e) {
            fail("Failed to access private fields: " + e.getMessage());
        }
    }
}