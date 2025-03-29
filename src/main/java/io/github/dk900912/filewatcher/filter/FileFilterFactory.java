package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.FileWatcherProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileFilter;
import java.util.Set;

import static io.github.dk900912.filewatcher.filter.MatchingType.ANY;
import static io.github.dk900912.filewatcher.filter.MatchingType.REGEX;

/**
 * @author dk900912
 */
public class FileFilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileFilterFactory.class);

    public static FileFilter create(FileWatcherProperties fileWatcherProperties) {
        MatchingType acceptedStrategy = fileWatcherProperties.getAcceptedStrategy();
        if (acceptedStrategy == null || acceptedStrategy == ANY) {
            logger.warn("FileWatcherProperties.acceptedStrategy is null or ANY, use AnyFilter");
            return new AnyFilter();
        }
        Set<String> acceptedFileFormats = fileWatcherProperties.getAcceptedFileFormats();
        if (acceptedFileFormats == null || acceptedFileFormats.isEmpty()) {
            logger.warn("FileWatcherProperties.acceptedFileFormats is null or empty when acceptedStrategy is SUFFIX or REGEX, use AnyFilter");
            return new AnyFilter();
        }
        if (REGEX == acceptedStrategy) {
            if (acceptedFileFormats.size() > 1) {
                logger.warn("FileWatcherProperties.acceptedFileFormats should contain a single regex pattern when acceptedStrategy is REGEX. Only the first element will be utilized.");
            }
            logger.info("FileWatcherProperties.acceptedStrategy is REGEX, use RegexFilter");
            return new RegexFilter(acceptedFileFormats.stream().findFirst().get());
        }
        logger.info("FileWatcherProperties.acceptedStrategy is SUFFIX, use SuffixFilter");
        return new SuffixFilter(acceptedFileFormats);
    }
}
