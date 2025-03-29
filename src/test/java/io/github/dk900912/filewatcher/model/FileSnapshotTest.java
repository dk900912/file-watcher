package io.github.dk900912.filewatcher.model;

import io.github.dk900912.filewatcher.utils.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author dk900912
 */
public class FileSnapshotTest {
    private static File testFile;
    private static final String TEST_FILE_NAME = "FileSnapshotTest";

    @BeforeAll
    static void setup() throws IOException {
        // 1. Create test file before all tests
        Path path = Files.createTempFile(TEST_FILE_NAME, null);
        testFile = path.toFile();
    }

    @AfterAll
    static void cleanup() {
        // 2. Delete test file after all tests
        if (testFile != null && testFile.exists()) {
            Assert.isTrue(testFile.delete(), "Failed to delete test file");
        }
    }

    @Test
    void testConstructorWithFileOnly() {
        // 3.1 Test constructor with File parameter
        FileSnapshot snapshot = new FileSnapshot(testFile);

        Assert.isTrue(snapshot.exists(), "File should exist");
        Assert.isTrue(snapshot.getFile().equals(testFile), "File path should match");
        Assert.isTrue(snapshot.getLength() == testFile.length(), "File length should match");
        Assert.isTrue(snapshot.getLastModified() == testFile.lastModified(),
                "Last modified time should match");
    }

    @Test
    void testConstructorEquivalence() {
        // 3.2 Verify equivalence between two constructors
        FileSnapshot autoSnapshot = new FileSnapshot(testFile);
        FileSnapshot manualSnapshot = new FileSnapshot(
                testFile,
                testFile.exists(),
                testFile.length(),
                testFile.lastModified()
        );

        Assert.isTrue(autoSnapshot.equals(manualSnapshot),
                "Snapshots from different constructors should be logically equal");

        Assert.isTrue(autoSnapshot.getFile().equals(manualSnapshot.getFile()),
                "File references should match");
        Assert.isTrue(autoSnapshot.exists() == manualSnapshot.exists(),
                "Existence status should be consistent");
        Assert.isTrue(autoSnapshot.getLength() == manualSnapshot.getLength(),
                "File lengths should be identical");
        Assert.isTrue(autoSnapshot.getLastModified() == manualSnapshot.getLastModified(),
                "Timestamps should be synchronized");
    }
}
