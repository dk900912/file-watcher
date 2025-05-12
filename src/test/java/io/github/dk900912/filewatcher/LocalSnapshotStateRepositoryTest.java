package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.model.DirectorySnapshot;
import io.github.dk900912.filewatcher.model.FileSnapshot;
import io.github.dk900912.filewatcher.utils.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author dukui
 */
public class LocalSnapshotStateRepositoryTest {
    private static final int MIN_DIRECTORIES = 3;
    private static final int FILES_PER_DIR = 2;
    private static Path testRoot;
    private static LocalSnapshotStateRepository repository;

    @BeforeAll
    static void setup() throws IOException {
        testRoot = Files.createTempDirectory("snapshot-test");
        repository = new LocalSnapshotStateRepository(testRoot.resolve("state.ser"));

        for (int i = 0; i < MIN_DIRECTORIES; i++) {
            Path dir = Files.createDirectory(testRoot.resolve("dir" + i));
            for (int j = 0; j < FILES_PER_DIR; j++) {
                Files.createFile(dir.resolve("file" + j + ".txt"));
            }
        }
    }

    @AfterAll
    static void cleanup() throws IOException {
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

    private Map<File, DirectorySnapshot> createSnapshotMap() {
        Map<File, DirectorySnapshot> snapshots = new LinkedHashMap<>();
        try (Stream<Path> dirStream = Files.list(testRoot)) {
            dirStream
                    .filter(Files::isDirectory)
                    .forEach(dir -> {
                        File dirFile = dir.toFile();
                        snapshots.put(dirFile, new DirectorySnapshot(dirFile));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return snapshots;
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSaveAndRestoreConsistency() {
        Map<File, DirectorySnapshot> original = createSnapshotMap();

        repository.save(original);
        Map<File, DirectorySnapshot> restored = (Map<File, DirectorySnapshot>) repository.restore();

        compareSnapshotMaps(original, restored);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSaveAndRestoreAfterModification() throws IOException {
        Path firstFile = testRoot.resolve("dir0/file0.txt");
        Files.writeString(firstFile, "modified content");

        Files.createFile(testRoot.resolve("dir1/new_file.txt"));

        Map<File, DirectorySnapshot> modified = createSnapshotMap();

        repository.save(modified);
        Map<File, DirectorySnapshot> restored = (Map<File, DirectorySnapshot>) repository.restore();

        compareSnapshotMaps(modified, restored);
    }

    private void compareSnapshotMaps(Map<File, DirectorySnapshot> expected, Map<File, DirectorySnapshot> actual) {
        Assert.isTrue(expected.size() == actual.size(), "Map size mismatch");

        expected.forEach((dir, expectedSnapshot) -> {
            DirectorySnapshot actualSnapshot = actual.get(dir);
            Assert.notNull(actualSnapshot, "Missing directory: " + dir);

            Assert.isTrue(expectedSnapshot.getDirectory().equals(actualSnapshot.getDirectory()),
                    "Directory mismatch");
            Assert.isTrue(expectedSnapshot.getTime().equals(actualSnapshot.getTime()),
                    "Snapshot time mismatch");

            Set<FileSnapshot> expectedFiles = expectedSnapshot.getFiles();
            Set<FileSnapshot> actualFiles = actualSnapshot.getFiles();
            Assert.isTrue(expectedFiles.size() == actualFiles.size(),
                    "File count mismatch in " + dir);

            expectedFiles.forEach(expectedFile -> {
                FileSnapshot actualFile = actualFiles.stream()
                        .filter(f -> f.getFile().equals(expectedFile.getFile()))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("File not found: " + expectedFile.getFile()));

                compareFileSnapshots(expectedFile, actualFile);
            });
        });
    }

    private void compareFileSnapshots(FileSnapshot expected, FileSnapshot actual) {
        Assert.isTrue(expected.getFile().equals(actual.getFile()), "File path mismatch");
        Assert.isTrue(expected.exists() == actual.exists(), "Existence mismatch");
        Assert.isTrue(expected.getLength() == actual.getLength(), "File size mismatch");
        Assert.isTrue(expected.getLastModified() == actual.getLastModified(),
                "Last modified time mismatch");
    }
}