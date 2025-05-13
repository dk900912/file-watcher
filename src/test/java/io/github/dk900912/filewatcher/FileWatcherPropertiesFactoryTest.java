package io.github.dk900912.filewatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author dukui
 */
public class FileWatcherPropertiesFactoryTest {

    @TempDir
    static Path tempDir;

    @Test
    void testCreateFromMapWithEmpty() {
        assertNull(FileWatcherPropertiesFactory.createFromMap(Map.of(), null));
    }

    @Test
    void testCreateFromMapWithAllParameters() {
        String validDir1 = tempDir.resolve("dir1").toString();
        String validDir2 = tempDir.resolve("dir2").toString();
        
        try {
            Files.createDirectory(tempDir.resolve("dir1"));
            Files.createDirectory(tempDir.resolve("dir2"));
        } catch (IOException e) {
            fail("Failed to create test directories: " + e.getMessage());
        }

        Map<String, Object> properties = Map.of(
            "directories", Arrays.asList(validDir1, validDir2),
            "daemon", false,
            "name", "Custom Watcher",
            "acceptedStrategy", Map.of(),
            "snapshotEnabled", true,
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
        assertEquals(true, propertiesInstance.getSnapshotEnabled());
        assertEquals(5, propertiesInstance.getRemainingScans().get());
        assertEquals(Duration.ofMillis(2000), propertiesInstance.getPollInterval().get());
    }

    @Test
    void testCreateFromMapWithDefaultValues() {
        String validDir = tempDir.resolve("default_dir").toString();
        
        try {
            Files.createDirectory(tempDir.resolve("default_dir"));
        } catch (IOException e) {
            fail("Failed to create test directory: " + e.getMessage());
        }

        Map<String, Object> properties = Map.of("directories", List.of(validDir));
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