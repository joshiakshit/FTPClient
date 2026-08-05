package com.akshit.ftpclient;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import javax.net.ssl.SSLException;
import javax.swing.*;
import java.io.*;
import java.time.Duration;

public class FTPManager {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final Duration DATA_TIMEOUT = Duration.ofSeconds(30);

    /** Reports transfer progress. {@code total} is -1 when the size of the transfer isn't known ahead of time. */
    public interface ProgressListener {
        void onProgress(long bytesTransferred, long total);
    }

    private final JTextArea logArea;
    private final FTPClient ftpClient;
    private final boolean useFtps;

    public FTPManager(JTextArea logArea, boolean useFtps) {
        this.logArea = logArea;
        this.useFtps = useFtps;
        this.ftpClient = useFtps ? new FTPSClient() : new FTPClient();
        this.ftpClient.setConnectTimeout(CONNECT_TIMEOUT_MS);
    }

    /** Blocking; call from a background thread. */
    public boolean connect(String server, String user, String pass) {
        try {
            ftpClient.connect(server);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                show("Server refused the connection: " + ftpClient.getReplyString());
                ftpClient.disconnect();
                return false;
            }
            show("Connected to " + server + (useFtps ? " (FTPS)" : ""));

            boolean login = ftpClient.login(user, pass);
            if (!login) {
                show("Login failed. Please check your credentials.");
                ftpClient.disconnect();
                return false;
            }

            if (useFtps) {
                // Protect the data channel too; AUTH TLS alone only secures the control channel.
                FTPSClient ftpsClient = (FTPSClient) ftpClient;
                ftpsClient.execPBSZ(0);
                ftpsClient.execPROT("P");
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setDataTimeout(DATA_TIMEOUT);
            show("Login successful!");
            return true;
        } catch (SSLException e) {
            show("TLS negotiation failed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            show("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    /** Blocking; call from a background thread. */
    public String[] listFiles() {
        if (!ftpClient.isConnected()) {
            show("Not connected to any FTP server.");
            return null;
        }
        try {
            return ftpClient.listNames();
        } catch (IOException e) {
            show("Error retrieving file list: " + e.getMessage());
            return null;
        }
    }

    /** Blocking; call from a background thread. */
    public boolean uploadFile(File file, ProgressListener progressListener) {
        if (!ftpClient.isConnected()) {
            show("Not connected to any FTP server.");
            return false;
        }
        long total = file.length();
        ftpClient.setCopyStreamListener(toCopyStreamListener(progressListener, total));
        try (FileInputStream fis = new FileInputStream(file)) {
            boolean success = ftpClient.storeFile(file.getName(), fis);
            show(success ? "Uploaded: " + file.getName() : "Failed to upload: " + file.getName());
            return success;
        } catch (IOException e) {
            show("Upload error: " + e.getMessage());
            return false;
        } finally {
            ftpClient.setCopyStreamListener(null);
        }
    }

    /** Blocking; call from a background thread. */
    public boolean downloadFile(String remoteFile, File localFile, ProgressListener progressListener) {
        if (!ftpClient.isConnected()) {
            show("Not connected to any FTP server.");
            return false;
        }
        long total = -1;
        try {
            String size = ftpClient.getSize(remoteFile);
            if (size != null) total = Long.parseLong(size);
        } catch (IOException | NumberFormatException e) {
            // Server doesn't support SIZE, or returned something unparsable; fall back to indeterminate progress.
        }
        ftpClient.setCopyStreamListener(toCopyStreamListener(progressListener, total));
        try (FileOutputStream fos = new FileOutputStream(localFile)) {
            boolean success = ftpClient.retrieveFile(remoteFile, fos);
            show(success ? "Downloaded: " + remoteFile : "Failed to download: " + remoteFile);
            return success;
        } catch (IOException e) {
            show("Download error: " + e.getMessage());
            return false;
        } finally {
            ftpClient.setCopyStreamListener(null);
        }
    }

    private CopyStreamListener toCopyStreamListener(ProgressListener progressListener, long total) {
        if (progressListener == null) return null;
        return new CopyStreamListener() {
            @Override
            public void bytesTransferred(CopyStreamEvent event) {
                bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
            }

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                progressListener.onProgress(totalBytesTransferred, total);
            }
        };
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    /** Blocking; call from a background thread if already connected. */
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
