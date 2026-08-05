package com.akshit.ftpclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FTPManagerTest {

    private FTPManager newManager() {
        return new FTPManager(new JTextArea(), false);
    }

    @Test
    void isConnectedIsFalseBeforeConnecting() {
        assertFalse(newManager().isConnected());
    }

    @Test
    void disconnectWithoutConnectingIsANoOp() {
        assertDoesNotThrow(() -> newManager().disconnect());
    }

    @Test
    void listFilesWithoutConnectingReturnsNullInsteadOfThrowing() {
        assertNull(newManager().listFiles());
    }

    @Test
    void uploadFileWithoutConnectingFailsGracefully(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("sample.txt");
        Files.writeString(file, "hello");

        FTPManager manager = newManager();
        boolean result = assertDoesNotThrow(() -> manager.uploadFile(file.toFile(), (transferred, total) -> { }));
        assertFalse(result);
    }

    @Test
    void downloadFileWithoutConnectingFailsGracefully(@TempDir Path tempDir) {
        FTPManager manager = newManager();
        File destination = tempDir.resolve("out.txt").toFile();
        boolean result = assertDoesNotThrow(() -> manager.downloadFile("remote.txt", destination, (transferred, total) -> { }));
        assertFalse(result);
    }
}
