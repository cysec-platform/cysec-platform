/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.utils;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtilsTest {

    @TempDir
    Path tempDir;

    private Path testFile;
    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("test.txt");
        testDir = tempDir.resolve("testdir");
        Files.createFile(testFile);
        Files.createDirectory(testDir);
    }

    @Test
    void getFileName_shouldReturnCorrectFilename() {
        Path path = Paths.get("/path/to/file.txt");
        assertThat(FileUtils.getFileName(path)).isEqualTo("file.txt");
    }

    @Test
    void getFileName_shouldReturnDirectoryName() {
        Path path = Paths.get("/path/to/directory");
        assertThat(FileUtils.getFileName(path)).isEqualTo("directory");
    }

    @Test
    void getFileExt_shouldReturnExtension() {
        Path path = Paths.get("file.txt");
        assertThat(FileUtils.getFileExt(path)).isEqualTo("txt");
    }

    @Test
    void getFileExt_shouldReturnNullForNoExtension() {
        Path path = Paths.get("file");
        assertThat(FileUtils.getFileExt(path)).isNull();
    }

    @Test
    void getFileExt_shouldReturnNullForDotAtStart() {
        Path path = Paths.get(".hidden");
        assertThat(FileUtils.getFileExt(path)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.java", "document.pdf", "archive.tar.gz"})
    void getFileExt_shouldHandleVariousExtensions(String filename) {
        Path path = Paths.get(filename);
        String ext = FileUtils.getFileExt(path);
        assertThat(ext).isNotNull();
        assertThat(filename).endsWith("." + ext);
    }

    @Test
    void asTemp_shouldReplaceExtensionWithTmp() {
        Path path = Paths.get("/parent/file.txt");
        Path temp = FileUtils.asTemp(path);
        
        assertThat(temp.toString()).isEqualTo("/parent/file.tmp");
        assertThat(temp.getParent()).isEqualTo(path.getParent());
    }

    @Test
    void asTemp_shouldHandleMultipleDotsCorrectly() {
        Path path = Paths.get("/parent/file.tar.gz");
        Path temp = FileUtils.asTemp(path);
        
        assertThat(temp.toString()).isEqualTo("/parent/file.tar.tmp");
    }

    @Test
    void getNameExt_shouldSeparateNameAndExtension() {
        String[] result = FileUtils.getNameExt("file.txt");
        
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo("file");
        assertThat(result[1]).isEqualTo("txt");
    }

    @Test
    void getNameExt_shouldThrowForNoDot() {
        assertThatThrownBy(() -> FileUtils.getNameExt("filename"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("file name doesn't contain dot");
    }

    @Test
    void getNameExt_shouldHandleMultipleDots() {
        String[] result = FileUtils.getNameExt("file.tar.gz");
        
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo("file.tar");
        assertThat(result[1]).isEqualTo("gz");
    }

    @Test
    void deleteDir_shouldDeleteEmptyDirectory() throws IOException {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);
        
        FileUtils.deleteDir(emptyDir);
        
        assertThat(Files.exists(emptyDir)).isFalse();
    }

    @Test
    void deleteDir_shouldDeleteDirectoryWithFiles() throws IOException {
        Path dirWithFiles = tempDir.resolve("withfiles");
        Files.createDirectory(dirWithFiles);
        Files.createFile(dirWithFiles.resolve("file1.txt"));
        Files.createFile(dirWithFiles.resolve("file2.txt"));
        
        FileUtils.deleteDir(dirWithFiles);
        
        assertThat(Files.exists(dirWithFiles)).isFalse();
    }

    @Test
    void deleteDir_shouldDeleteNestedDirectories() throws IOException {
        Path nested = tempDir.resolve("parent").resolve("child");
        Files.createDirectories(nested);
        Files.createFile(nested.resolve("file.txt"));
        
        FileUtils.deleteDir(tempDir.resolve("parent"));
        
        assertThat(Files.exists(tempDir.resolve("parent"))).isFalse();
    }

    @Test
    void copyDir_shouldCopyDirectoryStructure() throws IOException {
        Path source = tempDir.resolve("source");
        Path target = tempDir.resolve("target");
        
        Files.createDirectory(source);
        Files.createFile(source.resolve("file1.txt"));
        Path subdir = source.resolve("subdir");
        Files.createDirectory(subdir);
        Files.createFile(subdir.resolve("file2.txt"));
        
        FileUtils.copyDir(source, target);
        
        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.exists(target.resolve("file1.txt"))).isTrue();
        assertThat(Files.exists(target.resolve("subdir"))).isTrue();
        assertThat(Files.exists(target.resolve("subdir/file2.txt"))).isTrue();
    }

    @Test
    void copyDir_shouldThrowForNullSource() {
        assertThatThrownBy(() -> FileUtils.copyDir(null, tempDir))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void copyDir_shouldThrowForNullTarget() {
        assertThatThrownBy(() -> FileUtils.copyDir(tempDir, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void moveDir_shouldMoveDirectoryStructure() throws IOException {
        Path source = tempDir.resolve("source");
        Path target = tempDir.resolve("target");
        
        Files.createDirectory(source);
        Files.write(source.resolve("file1.txt"), "content".getBytes());
        Path subdir = source.resolve("subdir");
        Files.createDirectory(subdir);
        Files.write(subdir.resolve("file2.txt"), "content2".getBytes());
        
        FileUtils.moveDir(source, target);
        
        assertThat(Files.exists(source)).isFalse();
        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.exists(target.resolve("file1.txt"))).isTrue();
        assertThat(Files.exists(target.resolve("subdir/file2.txt"))).isTrue();
        assertThat(Files.readAllLines(target.resolve("file1.txt"))).containsExactly("content");
    }

    @Test
    void moveDir_shouldThrowForNullParameters() {
        assertThatThrownBy(() -> FileUtils.moveDir(null, tempDir))
            .isInstanceOf(NullPointerException.class);
        
        assertThatThrownBy(() -> FileUtils.moveDir(tempDir, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void zip_shouldCreateZipArchive() throws IOException {
        Path source = tempDir.resolve("source");
        Path zipFile = tempDir.resolve("archive.zip");
        
        Files.createDirectory(source);
        Files.write(source.resolve("file1.txt"), "content1".getBytes());
        Files.write(source.resolve("file2.txt"), "content2".getBytes());
        
        FileUtils.zip(source, zipFile);
        
        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.size(zipFile)).isGreaterThan(0);
    }

    @Test
    void zip_shouldExcludeSpecifiedFiles() throws IOException {
        Path source = tempDir.resolve("source");
        Path zipFile = tempDir.resolve("archive.zip");
        
        Files.createDirectory(source);
        Files.write(source.resolve("file1.txt"), "content1".getBytes());
        Files.write(source.resolve("excluded.txt"), "excluded".getBytes());
        
        FileUtils.zip(source, zipFile, "excluded.txt");
        
        assertThat(Files.exists(zipFile)).isTrue();
    }

    @Test
    void zip_shouldThrowForNullParameters() {
        assertThatThrownBy(() -> FileUtils.zip(null, tempDir.resolve("test.zip")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid source directory or destination archive");
        
        assertThatThrownBy(() -> FileUtils.zip(tempDir, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid source directory or destination archive");
    }

    @Test
    void unzip_shouldExtractZipArchive() throws IOException {
        Path zipFile = tempDir.resolve("test.zip");
        Path extractDir = tempDir.resolve("extract");
        
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry("file1.txt"));
            zos.write("content1".getBytes());
            zos.closeEntry();
            
            zos.putNextEntry(new ZipEntry("subdir/file2.txt"));
            zos.write("content2".getBytes());
            zos.closeEntry();
        }
        
        FileUtils.unzip(zipFile, extractDir);
        
        assertThat(Files.exists(extractDir.resolve("file1.txt"))).isTrue();
        assertThat(Files.exists(extractDir.resolve("subdir/file2.txt"))).isTrue();
        assertThat(Files.readAllLines(extractDir.resolve("file1.txt"))).containsExactly("content1");
        assertThat(Files.readAllLines(extractDir.resolve("subdir/file2.txt"))).containsExactly("content2");
    }

    @Test
    void unzip_shouldThrowForNullParameters() {
        assertThatThrownBy(() -> FileUtils.unzip((Path) null, tempDir))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid source archive or destination directory");
        
        assertThatThrownBy(() -> FileUtils.unzip(tempDir.resolve("test.zip"), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid source archive or destination directory");
    }

    @Test
    void unzipInputStream_shouldExtractFromInputStream() throws IOException {
        Path extractDir = tempDir.resolve("extract");
        
        byte[] zipData = createTestZipData();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipData);
        
        FileUtils.unzip(inputStream, extractDir);
        
        assertThat(Files.exists(extractDir.resolve("test.txt"))).isTrue();
        assertThat(Files.readAllLines(extractDir.resolve("test.txt"))).containsExactly("test content");
    }

    private byte[] createTestZipData() throws IOException {
        Path tempZip = tempDir.resolve("temp.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZip))) {
            zos.putNextEntry(new ZipEntry("test.txt"));
            zos.write("test content".getBytes());
            zos.closeEntry();
        }
        return Files.readAllBytes(tempZip);
    }
}
