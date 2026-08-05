package com.akshit.ftpclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;

public class FTPClient extends JFrame {

    private JTextField serverField, userField;
    private JPasswordField passField;
    private JCheckBox ftpsCheckBox;
    private JButton connectBtn, uploadBtn, downloadBtn, refreshBtn;
    private JTextArea logArea;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JProgressBar progressBar;

    private FTPManager ftpManager;

    public FTPClient() {
        setTitle("Akshit's FTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Connection Details"));

        serverField = new JTextField("test.rebex.net");
        userField = new JTextField("demo");
        passField = new JPasswordField("password");
        ftpsCheckBox = new JCheckBox("Use FTPS (secure)");

        connectBtn = new JButton("Connect");
        connectBtn.addActionListener(this::connectToServer);

        topPanel.add(new JLabel("Server:"));
        topPanel.add(serverField);
        topPanel.add(new JLabel("Username:"));
        topPanel.add(userField);
        topPanel.add(new JLabel("Password:"));
        topPanel.add(passField);
        topPanel.add(ftpsCheckBox);
        topPanel.add(connectBtn);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Remote Files"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        uploadBtn = new JButton("Upload");
        downloadBtn = new JButton("Download");
        refreshBtn = new JButton("Refresh");

        uploadBtn.addActionListener(this::uploadFile);
        downloadBtn.addActionListener(this::downloadFile);
        refreshBtn.addActionListener(this::refreshFiles);

        buttonPanel.add(uploadBtn);
        buttonPanel.add(downloadBtn);
        buttonPanel.add(refreshBtn);
        southPanel.add(buttonPanel, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        southPanel.add(progressBar, BorderLayout.SOUTH);

        centerPanel.add(southPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Logs"));
        logPanel.add(logScroll, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (ftpManager != null && ftpManager.isConnected()) {
                    ftpManager.disconnect();
                }
            }
        });

        setVisible(true);
        setLocationRelativeTo(null);

        log("Welcome to Akshit's FTP Client!");
        log("Enter credentials and click Connect.");
        updateControlState(false);
    }

    private void connectToServer(ActionEvent e) {
        String server = serverField.getText();
        String user = userField.getText();
        char[] passChars = passField.getPassword();
        String pass = new String(passChars);
        Arrays.fill(passChars, '\0');

        log("Connecting to " + server + " ...");
        ftpManager = new FTPManager(logArea, ftpsCheckBox.isSelected());
        updateControlState(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return ftpManager.connect(server, user, pass);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        log("Connected successfully!");
                        refreshFiles(null);
                    } else {
                        log("Failed to connect.");
                    }
                } catch (Exception ex) {
                    log("Connection error: " + ex.getMessage());
                } finally {
                    updateControlState(false);
                }
            }
        };
        worker.execute();
    }

    private void uploadFile(ActionEvent e) {
        if (ftpManager == null || !ftpManager.isConnected()) {
            log("Not connected to any FTP server.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select file to upload");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        log("Uploading " + file.getName() + "...");
        updateControlState(true);
        resetProgress();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return ftpManager.uploadFile(file, this::onProgress);
            }

            private void onProgress(long transferred, long total) {
                SwingUtilities.invokeLater(() -> updateProgress(transferred, total));
            }

            @Override
            protected void done() {
                try {
                    log(get() ? "Upload complete." : "Upload failed.");
                } catch (Exception ex) {
                    log("Upload error: " + ex.getMessage());
                } finally {
                    updateControlState(false);
                }
            }
        };
        worker.execute();
    }

    private void downloadFile(ActionEvent e) {
        if (ftpManager == null || !ftpManager.isConnected()) {
            log("Not connected to any FTP server.");
            return;
        }

        String selectedFile = fileList.getSelectedValue();
        if (selectedFile == null) {
            log("Please select a file to download.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save file as");
        chooser.setSelectedFile(new File(selectedFile));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File saveFile = chooser.getSelectedFile();
        log("Downloading " + selectedFile + "...");
        updateControlState(true);
        resetProgress();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return ftpManager.downloadFile(selectedFile, saveFile, this::onProgress);
            }

            private void onProgress(long transferred, long total) {
                SwingUtilities.invokeLater(() -> updateProgress(transferred, total));
            }

            @Override
            protected void done() {
                try {
                    log(get() ? "Download complete." : "Download failed.");
                } catch (Exception ex) {
                    log("Download error: " + ex.getMessage());
                } finally {
                    updateControlState(false);
                }
            }
        };
        worker.execute();
    }

    private void refreshFiles(ActionEvent e) {
        if (ftpManager == null || !ftpManager.isConnected()) {
            log("Not connected to any FTP server.");
            return;
        }

        updateControlState(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String[] files = ftpManager.listFiles();
                listModel.clear();
                if (files != null) {
                    for (String f : files) listModel.addElement(f);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    log("File list refreshed.");
                } catch (Exception ex) {
                    log("Error refreshing file list: " + ex.getMessage());
                } finally {
                    updateControlState(false);
                }
            }
        };
        worker.execute();
    }

    private void resetProgress() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString(null);
    }

    private void updateProgress(long transferred, long total) {
        if (total <= 0) {
            progressBar.setIndeterminate(true);
            return;
        }
        progressBar.setIndeterminate(false);
        int percent = (int) Math.min(100, (transferred * 100) / total);
        progressBar.setValue(percent);
        progressBar.setString(percent + "%");
    }

    /** Enables/disables the action buttons; {@code busy} disables everything, otherwise availability follows connection state. */
    private void updateControlState(boolean busy) {
        boolean connected = ftpManager != null && ftpManager.isConnected();
        connectBtn.setEnabled(!busy);
        uploadBtn.setEnabled(!busy && connected);
        downloadBtn.setEnabled(!busy && connected);
        refreshBtn.setEnabled(!busy && connected);
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FTPClient::new);
    }
}
