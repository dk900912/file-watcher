package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.filter.MatchingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author dukui
 */
public class FileWatcherPropertiesTest {

    private FileWatcherProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileWatcherProperties();
    }

    @Test
    void testDefaultValues() {
        assertTrue(properties.getDaemon());
        assertEquals("File Watcher", properties.getName());
        assertEquals(-1, properties.getRemainingScans());
        assertFalse(properties.getSnapshotEnabled());
        assertNull(properties.getDirectories());
        assertNull(properties.getAcceptedStrategy());
        assertEquals(Duration.ofMillis(1000), properties.getPollInterval());
        assertEquals(Duration.ofMillis(400), properties.getQuietPeriod());
    }

    @Test
    void testSetDirectories() {
        List<String> dirs = Arrays.asList("/path1", "/path2");
        assertThrows(IllegalArgumentException.class, () ->
                properties.setDirectories(dirs)
        );
    }

    @Test
    void testSetAcceptedStrategy_ValidInput() {
        Map<MatchingStrategy, Set<String>> strategy = Map.of(
            MatchingStrategy.REGEX, Set.of(".*\\.txt", ".*\\.xml")
        );
        properties.setAcceptedStrategy(strategy);
        assertEquals(strategy, properties.getAcceptedStrategy());
    }

    @Test
    void testSetAcceptedStrategy_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setAcceptedStrategy(null)
        );
    }

    @Test
    void testSetAcceptedStrategy_EmptyMap() {
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setAcceptedStrategy(new HashMap<>())
        );
    }

    @Test
    void testSetAcceptedStrategy_EmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = Map.of(
            MatchingStrategy.REGEX, Set.of()
        );
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setAcceptedStrategy(strategy)
        );
    }

    @Test
    void testSetAcceptedStrategy_SingleNonAnyStrategyWithEmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.SUFFIX, Set.of());
        final IllegalArgumentException acceptedStrategyMustNotBeEmpty = assertThrows(IllegalArgumentException.class, () ->
            properties.setAcceptedStrategy(strategy)
        );
        assertEquals("AcceptedStrategy must contain non-empty value for the key", acceptedStrategyMustNotBeEmpty.getMessage());
    }

    @Test
    void testSetAcceptedStrategy_SingleAnyStrategyWithEmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.ANY, Set.of());
        properties.setAcceptedStrategy(strategy);
        assertEquals(strategy, properties.getAcceptedStrategy());
    }

    @Test
    void testSetAcceptedStrategy_MultipleStrategiesWithNonEmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.SUFFIX, Set.of(".jpg", ".txt"));
        strategy.put(MatchingStrategy.REGEX, Set.of(".*\\.txt"));
        strategy.put(MatchingStrategy.ANY, Set.of("**/*.xml"));
        final IllegalArgumentException acceptedStrategyMustBeOnlyOne = assertThrows(IllegalArgumentException.class, () ->
                properties.setAcceptedStrategy(strategy)
        );
        assertEquals("AcceptedStrategy must contain exactly one key after filtering out ANY", acceptedStrategyMustBeOnlyOne.getMessage());
    }

    @Test
    void testSetPollInterval_ValidDuration() {
        Duration newInterval = Duration.ofMillis(2000);
        properties.setPollInterval(newInterval);
        assertEquals(newInterval, properties.getPollInterval());
    }

    @Test
    void testSetPollInterval_ZeroDuration() {
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setPollInterval(Duration.ZERO)
        );
    }

    @Test
    void testSetPollInterval_NegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setPollInterval(Duration.ofMillis(-1000))
        );
    }

    @Test
    void testSetPollInterval_LessThanQuietPeriod() {
        Duration quietPeriod = Duration.ofMillis(800);
        properties.setQuietPeriod(quietPeriod);
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setPollInterval(Duration.ofMillis(500))
        );
    }

    @Test
    void testSetQuietPeriod_ValidDuration() {
        Duration newQuietPeriod = Duration.ofMillis(200);
        properties.setQuietPeriod(newQuietPeriod);
        assertEquals(newQuietPeriod, properties.getQuietPeriod());
    }

    @Test
    void testSetQuietPeriod_ZeroDuration() {
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setQuietPeriod(Duration.ZERO)
        );
    }

    @Test
    void testSetQuietPeriod_NegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setQuietPeriod(Duration.ofMillis(-400))
        );
    }

    @Test
    void testSetQuietPeriod_GreaterThanPollInterval() {
        Duration pollInterval = Duration.ofMillis(1000);
        properties.setPollInterval(pollInterval);
        assertThrows(IllegalArgumentException.class, () -> 
            properties.setQuietPeriod(Duration.ofMillis(2000))
        );
    }
}
