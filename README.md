# 🖥️ GUI-Based FTP Client (Java + Maven)

A fully functional **graphical FTP client** built using **Java Swing** and **Apache Commons Net**.
It connects to FTP servers, lists remote files, and performs upload/download operations — all in a modern, responsive GUI.

This project combines **network programming**, **concurrency**, and **desktop GUI development**, forming a real-world equivalent of lightweight tools like FileZilla.

---

## 🧠 Overview

The **FTP Client** enables users to connect to remote FTP servers, authenticate, and manage files seamlessly.
It supports file listing, uploading, and downloading, with detailed logs visible inside the GUI.
Transfers happen in the background via `SwingWorker`, keeping the interface responsive.

Built as part of Akshit’s *Java Projects Series*, this project demonstrates practical use of **FTP protocol**, **multithreading**, and **Swing UI design**.

---

## 🚀 Features

✅ **Connect to FTP Servers** — Enter hostname, username, and password  
✅ **List Remote Files** — Displays files and folders from the connected server  
✅ **Upload & Download** — Supports bidirectional file transfer  
✅ **Background Tasks** — Uses `SwingWorker` for non-blocking operations  
✅ **Real-time Logs** — Shows live connection and file transfer updates  
✅ **Maven-based Build** — Simple to compile and run across any system

---

## 🧩 Tech Stack

| Component       | Description                    |
| --------------- | ------------------------------ |
| **Language**    | Java 17                        |
| **GUI**         | Java Swing                     |
| **Networking**  | Apache Commons Net (FTPClient) |
| **Concurrency** | SwingWorker Threads            |
| **Build Tool**  | Apache Maven                   |
| **Logging**     | GUI-integrated console output  |

---

## 📁 Project Structure

```
FTP-Client/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/akshit/ftpclient/
│   │   │   ├── FTPClientApp.java
│   │   │   └── FTPManager.java
│   │   └── resources/
│   └── test/
│       └── java/
└── logs/
```

---

## ⚙️ How It Works

1. Launch the GUI — a login screen appears.
2. Enter **Server**, **Username**, and **Password** → Click **Connect**.
3. The client connects using `FTPClient` from Apache Commons Net.
4. Once connected:

   * Files are listed in the main panel.
   * You can **upload** local files or **download** remote ones.
5. Logs (connection status, upload/download progress) appear in the console panel.

All operations run asynchronously, ensuring the GUI never freezes during large transfers.

---

## ⚡ Quick Start

### ✅ Prerequisites

* Java 17+
* Apache Maven
* IntelliJ IDEA (recommended)

---

### 🛠️ Build

```bash
mvn clean compile
```

---

### ▶️ Run

```bash
mvn exec:java "-Dexec.mainClass=com.akshit.ftpclient.FTPClientApp"
```

---

### 🌍 Test Server (Public)

Use this reliable demo FTP server for testing:

| Field        | Value            |
| ------------ | ---------------- |
| **Server**   | `test.rebex.net` |
| **Username** | `demo`           |
| **Password** | `password`       |

> ⚠️ Note: Some free public FTP servers like `ftp.dlptest.com` may intermittently block connections due to rate limits.
> `test.rebex.net` is stable and recommended for testing uploads/downloads.

---

## 🧾 Example Log Output

```
Connecting to test.rebex.net ...
Connected to test.rebex.net
✅ Login successful!
🔁 Listing files...
📥 Downloaded: readme.txt
📤 Uploaded: sample.txt
✅ Transfer complete!
```

---

## 🖼️ GUI Preview (Structure)

```
+-------------------------------------------------------+
|   FTP Client GUI - Akshit Joshi                       |
|-------------------------------------------------------|
|  Server: [ test.rebex.net ]                           |
|  Username: [ demo ]   Password: [ ******** ]          |
|  [ Connect ]  [ Refresh ]  [ Upload ]  [ Download ]   |
|-------------------------------------------------------|
|  Remote Files:                                        |
|  [ readme.txt ]                                       |
|  [ pub/ ]                                             |
|  [ example.png ]                                      |
|-------------------------------------------------------|
|  Console Log:                                         |
|  ✅ Connected to test.rebex.net                       |
|  📥 Download complete!                                |
+-------------------------------------------------------+
```

---

## 🧠 Learning Outcomes

✅ Mastered **FTP communication** using Apache Commons Net  
✅ Built a **responsive Swing GUI** for network operations  
✅ Implemented **multithreading with SwingWorker**  
✅ Integrated **live logging and background execution**  
✅ Learned how to combine UI + Networking + Concurrency cleanly

---

## 🧩 Future Enhancements

🚀 Add progress bars for upload/download tasks  
📂 Enable directory navigation (double-click folders)  
💾 Export session logs to `ftp.log`  
☁️ Support SFTP via Apache Commons VFS  
🌐 Add “Saved Profiles” for frequently used FTP servers

---

## 👨‍💻 Author

**Akshit Joshi**  
💻 Java Developer
