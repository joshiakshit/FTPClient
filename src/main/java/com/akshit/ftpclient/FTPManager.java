package com.akshit.ftpclient;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import javax.swing.*;
import java.io.*;

public class FTPManager {

    private final JTextArea logArea;
    private final FTPClient ftpClient;

    public FTPManager(JTextArea logArea) {
        this.logArea = logArea;
        this.ftpClient = new FTPClient();
    }

    public boolean connect(String server, String user, String pass) {
        try {
            ftpClient.connect(server);
            show("Connected to " + server);

            boolean login = ftpClient.login(user, pass);
            if (login) {
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                show("Login successful!");
                return true;
            } else {
                show("Login failed. Please check your credentials.");
                ftpClient.disconnect();
                return false;
            }
        } catch (IOException e) {
            show("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    public String[] listFiles() {
        try {
            return ftpClient.listNames();
        } catch (IOException e) {
            show("Error retrieving file list: " + e.getMessage());
            return null;
        }
    }

    public void uploadFile(File file) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try (FileInputStream fis = new FileInputStream(file)) {
                    boolean success = ftpClient.storeFile(file.getName(), fis);
                    if (success) show("Uploaded: " + file.getName());
                    else show("Failed to upload: " + file.getName());
                } catch (IOException e) {
                    show("Upload error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                show("Upload complete.");
            }
        }.execute();
    }

    public void downloadFile(String remoteFile, File localFile) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try (FileOutputStream fos = new FileOutputStream(localFile)) {
                    boolean success = ftpClient.retrieveFile(remoteFile, fos);
                    if (success) show("Downloaded: " + remoteFile);
                    else show("Failed to download: " + remoteFile);
                } catch (IOException e) {
                    show("Download error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                show("Download complete.");
            }
        }.execute();
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                show("🔌 Disconnected from server.");
            }
        } catch (IOException e) {
            show("Error disconnecting: " + e.getMessage());
        }
    }

    private void show(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}