package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.FileWatcherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

/**
 * @author dukui
 */
public class FileFilterFactoryTest {

    private FileWatcherProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileWatcherProperties();
    }

    @Test
    void testCreate_NullAcceptedStrategy() {
        FileFilterFactory.create(properties);
    }

    @Test
    void testCreate_ValidAcceptedStrategy() {
        properties.setAcceptedStrategy(Map.of(MatchingStrategy.ANY, Set.of()));
        FileFilterFactory.create(properties);
    }

}
