package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.FileWatcherProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileFilter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;

/**
 * @author dukui
 */
public class FileFilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileFilterFactory.class);

    public static FileFilter create(FileWatcherProperties fileWatcherProperties) {
        Map<MatchingStrategy, Set<String>> acceptedStrategy =
                Optional.ofNullable(fileWatcherProperties.getAcceptedStrategy())
                        .orElse(Map.of(ANY, Set.of()));
        MatchingStrategy matchingStrategy = (MatchingStrategy) acceptedStrategy.keySet().toArray()[0];
        Set<String> acceptedStrategyPatterns = acceptedStrategy.get(matchingStrategy);
        return switch (matchingStrategy) {
            case ANY -> {
                logger.info("FileWatcherProperties.acceptedStrategy is ANY, use AnyFilter");
                yield new AnyFilter();
            }
            case SUFFIX -> {
                logger.info("FileWatcherProperties.acceptedStrategy is SUFFIX, use SuffixFilter");
                yield new SuffixFilter(acceptedStrategyPatterns);
            }
            case REGEX -> {
                logger.info("FileWatcherProperties.acceptedStrategy is REGEX, use RegexFilter");
                yield new RegexFilter(acceptedStrategyPatterns);
            }
        };
    }
}
