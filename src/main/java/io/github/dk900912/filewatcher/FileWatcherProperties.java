package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.filter.MatchingStrategy;
import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;

/**
 * @author dukui
 */
public class FileWatcherProperties {

    // Daemon thread by default
    private Boolean daemon = true;

    // Thread name by default
    private String name = "File Watcher";

    // Scan forever by default
    private AtomicInteger remainingScans = new AtomicInteger(-1);

    private Boolean snapshotEnabled = false;

    private List<String> directories;

    private Map<MatchingStrategy, Set<String>> acceptedStrategy;

    private Duration pollInterval = Duration.ofMillis(1000);

    private Duration quietPeriod = Duration.ofMillis(400);

    public Boolean getDaemon() {
        return daemon;
    }

    public void setDaemon(Boolean daemon) {
        this.daemon = daemon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AtomicInteger getRemainingScans() {
        return remainingScans;
    }

    public void setRemainingScans(AtomicInteger remainingScans) {
        this.remainingScans = remainingScans;
    }

    public Boolean getSnapshotEnabled() {
        return snapshotEnabled;
    }

    public void setSnapshotEnabled(Boolean snapshotEnabled) {
        this.snapshotEnabled = snapshotEnabled;
    }

    public List<String> getDirectories() {
        return directories;
    }

    public void setDirectories(List<String> directories) {
        Assert.isTrue(directories != null && !directories.isEmpty(), "Directories must not be null or empty");
        this.directories = directories.stream()
                .map(String::trim)
                .distinct()
                .peek(path -> {
                    File dir = new File(path);
                    Assert.isTrue(dir.isDirectory(), () -> "Directory '" + dir + "' must be a directory");
                })
                .toList();
    }

    public Map<MatchingStrategy, Set<String>> getAcceptedStrategy() {
        return acceptedStrategy;
    }

    /**
     * Sets the accepted strategy for file matching.
     * If the accepted strategy contains the {@link io.github.dk900912.filewatcher.filter.MatchingStrategy#ANY} key,
     * it will be filtered out first. After filtering, the accepted strategy must contain exactly one key, and the
     * corresponding value must not be empty.
     *
     * @param acceptedStrategy the accepted strategy map
     * @throws IllegalArgumentException if the accepted strategy is null, empty, contains more than one key after filtering,
     *                                  or the value for the key is empty
     */
    public void setAcceptedStrategy(Map<MatchingStrategy, Set<String>> acceptedStrategy) {
        Assert.isTrue(acceptedStrategy != null && !acceptedStrategy.isEmpty(),
                "AcceptedStrategy must not be null or empty");
        boolean onlyAny = acceptedStrategy.keySet().stream().allMatch(ANY::equals);
        if (!onlyAny) {
            Map<MatchingStrategy, Set<String>> filteredStrategy = acceptedStrategy.entrySet().stream()
                    .filter(entry -> !ANY.equals(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Assert.isTrue(filteredStrategy.size() == 1,
                    "AcceptedStrategy must contain exactly one key after filtering out ANY");
            Assert.isTrue(filteredStrategy.values().stream().noneMatch(Set::isEmpty),
                    "AcceptedStrategy must contain non-empty value for the key");
            this.acceptedStrategy = filteredStrategy;
        } else {
            this.acceptedStrategy = acceptedStrategy;
        }
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        Assert.isTrue(pollInterval.toMillis() > 0, "PollInterval must be positive");
        Assert.isTrue(pollInterval.toMillis() > getQuietPeriod().toMillis(), "PollInterval must be greater than QuietPeriod");
        this.pollInterval = pollInterval;
    }

    public Duration getQuietPeriod() {
        return quietPeriod;
    }

    public void setQuietPeriod(Duration quietPeriod) {
        Assert.isTrue(quietPeriod.toMillis() > 0, "QuietPeriod must be positive");
        Assert.isTrue(getPollInterval().toMillis() > quietPeriod.toMillis(), "PollInterval must be greater than QuietPeriod");
        this.quietPeriod = quietPeriod;
    }
}

