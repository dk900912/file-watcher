package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.FileWatcherProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileFilter;
import java.util.Set;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;
import static io.github.dk900912.filewatcher.filter.MatchingStrategy.REGEX;

/**
 * @author dukui
 */
public class FileFilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileFilterFactory.class);

    public static FileFilter create(FileWatcherProperties fileWatcherProperties) {
        MatchingStrategy acceptedStrategy = fileWatcherProperties.getAcceptedStrategy();
        if (acceptedStrategy == null || acceptedStrategy == ANY) {
            logger.warn("FileWatcherProperties.acceptedStrategy is null or ANY, use AnyFilter");
            return new AnyFilter();
        }
        Set<String> acceptedFilePatterns = fileWatcherProperties.getAcceptedFilePatterns();
        if (acceptedFilePatterns == null || acceptedFilePatterns.isEmpty()) {
            logger.warn("FileWatcherProperties.acceptedFilePatterns is null or empty when acceptedStrategy is SUFFIX or REGEX, use AnyFilter");
            return new AnyFilter();
        }
        if (REGEX == acceptedStrategy) {
            if (acceptedFilePatterns.size() > 1) {
                logger.warn("FileWatcherProperties.acceptedFilePatterns should contain a single regex pattern when acceptedStrategy is REGEX. Only the first element will be utilized");
            }
            logger.info("FileWatcherProperties.acceptedStrategy is REGEX, use RegexFilter");
            return new RegexFilter(acceptedFilePatterns.stream().findFirst().get());
        }
        logger.info("FileWatcherProperties.acceptedStrategy is SUFFIX, use SuffixFilter");
        return new SuffixFilter(acceptedFilePatterns);
    }
}
