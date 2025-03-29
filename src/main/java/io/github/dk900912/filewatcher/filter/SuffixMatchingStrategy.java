package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.util.Set;

import static io.github.dk900912.filewatcher.filter.MatchingType.SUFFIX;

/**
 * @author dk900912
 */
public class SuffixMatchingStrategy implements MatchingStrategy {

    private final Set<String> suffixes;

    public SuffixMatchingStrategy(Set<String> suffixes) {
        Assert.notNull(suffixes, "Suffixes must not be null");
        this.suffixes = suffixes;
    }

    @Override
    public MatchingType supports() {
        return SUFFIX;
    }

    @Override
    public boolean matches(String fileName) {
        return suffixes.stream().anyMatch(fileName.toLowerCase()::endsWith);
    }
}
