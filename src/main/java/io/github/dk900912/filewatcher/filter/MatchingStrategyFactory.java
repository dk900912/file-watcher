package io.github.dk900912.filewatcher.filter;

import java.util.Collections;
import java.util.Set;

/**
 * @author dk900912
 */
public class MatchingStrategyFactory {
    public static SuffixStrategyBuilder forSuffix() {
        return new SuffixStrategyBuilder();
    }

    public static RegexStrategyBuilder forRegex() {
        return new RegexStrategyBuilder();
    }

    public static class SuffixStrategyBuilder {
        private Set<String> suffixes;

        public SuffixStrategyBuilder withSuffixes(String... suffixes) {
            this.suffixes = Set.of(suffixes);
            return this;
        }

        public SuffixStrategyBuilder withSuffixes(Set<String> suffixes) {
            this.suffixes = Collections.unmodifiableSet(suffixes);
            return this;
        }

        public SuffixMatchingStrategy build() {
            return new SuffixMatchingStrategy(suffixes);
        }
    }

    public static class RegexStrategyBuilder {
        private String regex;

        public RegexStrategyBuilder withPattern(String regex) {
            this.regex = regex;
            return this;
        }

        public RegexMatchingStrategy build() {
            return new RegexMatchingStrategy(regex);
        }
    }
}
