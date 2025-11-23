# SFTP File Transfer

A robust, multi-threaded Java application for automating file transfers to an SFTP server. Designed for reliability and ease of use in standalone environments.

## Features

- **Recursive Directory Monitoring**: Continuously watches a local directory for new files.
- **Multi-threaded Transfers**: Uploads multiple files in parallel for high throughput.
- **Conditional Overwrite**: Option to skip uploads if a file with the same name and size already exists on the server.
- **Smart File Handling**:
  - Moves successfully transferred files to a `completed` directory.
  - Moves skipped (duplicate) files to an `overlap` directory.
- **File Stability Check**: Ensures files are fully written before attempting transfer.
- **Encrypted Configuration**: Supports encrypted passwords for enhanced security.

## Prerequisites

- Java 8 or higher
- Maven 3.x (for building)

## Installation

Clone the repository and build the project using Maven:

```bash
mvn clean package
```

This will generate an executable JAR file in the `target` directory (e.g., `sftp-file-transfer-2.1-SNAPSHOT.jar`).

## Configuration

The application is configured using the `config.properties` file located in `src/main/resources`. You can also provide an external configuration file at runtime (if supported by the code, otherwise modify the resource file before building).

### Property Reference

| Property | Description | Default / Example |
|----------|-------------|-------------------|
| **Server Settings** | | |
| `sftp.host` | IP address or hostname of the SFTP server. | `192.168.1.10` |
| `sftp.port` | Port number for SFTP. | `22` |
| `sftp.user` | Username for authentication. | `sftpuser` |
| `sftp.password.encrypted` | Encrypted password (see below). | `...` |
| **Directory Settings** | | |
| `sftp.local.dir` | Local directory to monitor for files. | `D:/data/input/` |
| `sftp.remote.dir` | Destination directory on the server. | `/remote/data/` |
| `sftp.local.completed.dir` | Directory to move files after success. | `D:/data/completed/` |
| `sftp.local.overlap.dir` | Directory to move skipped/duplicate files. | `D:/data/overlap/` |
| **Transfer Logic** | | |
| `sftp.parallel.max` | Maximum number of concurrent transfers. | `8` |
| `sftp.overwrite.if.same` | If `false`, skips upload if remote file exists with same size. | `false` |
| `sftp.file.extension` | Filter files by extension (empty for all). | `.pdf` |
| **Monitoring** | | |
| `sftp.monitor.enable` | Enable continuous monitoring (`true`) or one-time scan (`false`). | `true` |
| `sftp.monitor.interval` | Interval in seconds between scans. | `10` |
| `sftp.stable.check.delay` | Seconds to wait to ensure file is not changing size. | `5` |

## Usage

### 1. Generate Encrypted Password
For security, the password in `config.properties` must be encrypted. Use the included `CryptoUtil` class:

```bash
# Compile the utility
javac -cp src/main/java src/main/java/CryptoUtil.java

# Run with your plain text password
java -cp src/main/java CryptoUtil "YourPasswordHere"
```

Copy the output string and paste it into `sftp.password.encrypted` in `config.properties`.

### 2. Run the Application
Execute the built JAR file:

```bash
java -jar target/sftp-file-transfer-2.1-SNAPSHOT.jar
```

The application will start, load the configuration, and begin monitoring or scanning the specified directory.

## Application Logs

The application will generate logs in the `logs` directory. The log file is named `app.log`.

```bash
2025-11-23 22:28:22 [main] INFO  SftpFileTransfer - Starting SFTP File Transfer Application
2025-11-23 22:28:22 [main] INFO  SftpFileTransfer - Monitoring enabled for directory: D:/temp/
2025-11-23 22:28:33 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_100_18608.bin
2025-11-23 22:28:33 [pool-2-thread-1] INFO  TransferTask - Starting transfer for file: File_100_18608.bin
2025-11-23 22:28:38 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_10_19042.bin
2025-11-23 22:28:38 [pool-2-thread-2] INFO  TransferTask - Starting transfer for file: File_10_19042.bin
2025-11-23 22:28:39 [pool-2-thread-1] INFO  TransferTask - Transfer completed: File_100_18608.bin | Size: 0.28 MB | Time: 5535 ms | Bandwidth: 0.05 MB/s
2025-11-23 22:28:39 [pool-2-thread-1] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_100_18608.bin
2025-11-23 22:28:43 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_11_20640.bin
2025-11-23 22:28:43 [pool-2-thread-3] INFO  TransferTask - Starting transfer for file: File_11_20640.bin
2025-11-23 22:28:48 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_12_26562.bin
2025-11-23 22:28:48 [pool-2-thread-4] INFO  TransferTask - Starting transfer for file: File_12_26562.bin
2025-11-23 22:28:48 [pool-2-thread-2] INFO  TransferTask - Transfer completed: File_10_19042.bin | Size: 0.56 MB | Time: 10262 ms | Bandwidth: 0.05 MB/s
2025-11-23 22:28:48 [pool-2-thread-2] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_10_19042.bin
2025-11-23 22:28:49 [pool-2-thread-3] INFO  TransferTask - Transfer completed: File_11_20640.bin | Size: 0.92 MB | Time: 6058 ms | Bandwidth: 0.15 MB/s
2025-11-23 22:28:49 [pool-2-thread-3] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_11_20640.bin
2025-11-23 22:28:53 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_13_1755.bin
2025-11-23 22:28:53 [pool-2-thread-5] INFO  TransferTask - Starting transfer for file: File_13_1755.bin
2025-11-23 22:28:58 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_14_8569.bin
2025-11-23 22:28:58 [pool-2-thread-6] INFO  TransferTask - Starting transfer for file: File_14_8569.bin
2025-11-23 22:29:02 [main] INFO  SftpFileTransfer - Starting SFTP File Transfer Application
2025-11-23 22:29:02 [main] INFO  SftpFileTransfer - Monitoring enabled for directory: D:/temp/
2025-11-23 22:29:13 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_12_26562.bin
2025-11-23 22:29:13 [pool-2-thread-1] INFO  TransferTask - Starting transfer for file: File_12_26562.bin
2025-11-23 22:29:18 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_13_1755.bin
2025-11-23 22:29:18 [pool-2-thread-2] INFO  TransferTask - Starting transfer for file: File_13_1755.bin
2025-11-23 22:29:19 [pool-2-thread-1] INFO  TransferTask - Transfer completed: File_12_26562.bin | Size: 1.17 MB | Time: 6329 ms | Bandwidth: 0.18 MB/s
2025-11-23 22:29:19 [pool-2-thread-1] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_12_26562.bin
2025-11-23 22:29:23 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_14_8569.bin
2025-11-23 22:29:23 [pool-2-thread-3] INFO  TransferTask - Starting transfer for file: File_14_8569.bin
2025-11-23 22:29:24 [pool-2-thread-2] INFO  TransferTask - Transfer completed: File_13_1755.bin | Size: 2.45 MB | Time: 6802 ms | Bandwidth: 0.36 MB/s
2025-11-23 22:29:24 [pool-2-thread-2] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_13_1755.bin
2025-11-23 22:29:28 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_15_18946.bin
2025-11-23 22:29:28 [pool-2-thread-4] INFO  TransferTask - Starting transfer for file: File_15_18946.bin
2025-11-23 22:29:33 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_16_23581.bin
2025-11-23 22:29:33 [pool-2-thread-5] INFO  TransferTask - Starting transfer for file: File_16_23581.bin
2025-11-23 22:29:34 [pool-2-thread-4] INFO  TransferTask - Transfer completed: File_15_18946.bin | Size: 0.64 MB | Time: 5977 ms | Bandwidth: 0.11 MB/s
2025-11-23 22:29:34 [pool-2-thread-4] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_15_18946.bin
2025-11-23 22:29:38 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_17_6005.bin
2025-11-23 22:29:38 [pool-2-thread-6] INFO  TransferTask - Starting transfer for file: File_17_6005.bin
2025-11-23 22:29:43 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_18_5442.bin
2025-11-23 22:29:43 [pool-2-thread-7] INFO  TransferTask - Starting transfer for file: File_18_5442.bin
2025-11-23 22:29:44 [pool-2-thread-6] INFO  TransferTask - Transfer completed: File_17_6005.bin | Size: 1.90 MB | Time: 6172 ms | Bandwidth: 0.31 MB/s
2025-11-23 22:29:44 [pool-2-thread-6] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_17_6005.bin
2025-11-23 22:29:44 [pool-2-thread-3] INFO  TransferTask - Transfer completed: File_14_8569.bin | Size: 2.64 MB | Time: 21744 ms | Bandwidth: 0.12 MB/s
2025-11-23 22:29:44 [pool-2-thread-3] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_14_8569.bin
2025-11-23 22:29:48 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_19_6024.bin
2025-11-23 22:29:48 [pool-2-thread-8] INFO  TransferTask - Starting transfer for file: File_19_6024.bin
2025-11-23 22:29:48 [pool-2-thread-5] INFO  TransferTask - Transfer completed: File_16_23581.bin | Size: 1.74 MB | Time: 15040 ms | Bandwidth: 0.12 MB/s
2025-11-23 22:29:48 [pool-2-thread-5] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_16_23581.bin
2025-11-23 22:29:53 [pool-2-thread-7] INFO  TransferTask - Transfer completed: File_18_5442.bin | Size: 1.86 MB | Time: 9845 ms | Bandwidth: 0.19 MB/s
2025-11-23 22:29:53 [pool-2-thread-7] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_18_5442.bin
2025-11-23 22:29:53 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_1_18729.bin
2025-11-23 22:29:53 [pool-2-thread-1] INFO  TransferTask - Starting transfer for file: File_1_18729.bin
2025-11-23 22:29:58 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_20_26350.bin
2025-11-23 22:29:58 [pool-2-thread-2] INFO  TransferTask - Starting transfer for file: File_20_26350.bin
2025-11-23 22:30:00 [main] INFO  SftpFileTransfer - Starting SFTP File Transfer Application
2025-11-23 22:30:00 [main] INFO  SftpFileTransfer - Monitoring enabled for directory: D:/temp/
2025-11-23 22:30:23 [main] INFO  SftpFileTransfer - Starting SFTP File Transfer Application
2025-11-23 22:30:23 [main] INFO  SftpFileTransfer - Monitoring enabled for directory: D:/temp/
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_100_18608.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_100_18608.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_10_19042.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_10_19042.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_11_20640.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_11_20640.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_12_26562.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_12_26562.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_13_1755.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_13_1755.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_14_8569.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_14_8569.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_15_18946.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_15_18946.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_16_23581.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_16_23581.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_17_6005.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_17_6005.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Skipping transfer (File exists with same size): File_18_5442.bin
2025-11-23 22:30:29 [main] INFO  SftpFileTransfer - Moved overlapping file to: D:\temp\overlap\File_18_5442.bin
2025-11-23 22:30:34 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_19_6024.bin
2025-11-23 22:30:34 [pool-2-thread-1] INFO  TransferTask - Starting transfer for file: File_19_6024.bin
2025-11-23 22:30:39 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_1_18729.bin
2025-11-23 22:30:39 [pool-2-thread-2] INFO  TransferTask - Starting transfer for file: File_1_18729.bin
2025-11-23 22:30:41 [pool-2-thread-1] INFO  TransferTask - Transfer completed: File_19_6024.bin | Size: 2.10 MB | Time: 6797 ms | Bandwidth: 0.31 MB/s
2025-11-23 22:30:41 [pool-2-thread-1] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_19_6024.bin
2025-11-23 22:30:44 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_20_26350.bin
2025-11-23 22:30:44 [pool-2-thread-3] INFO  TransferTask - Starting transfer for file: File_20_26350.bin
2025-11-23 22:30:49 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_21_23697.bin
2025-11-23 22:30:49 [pool-2-thread-4] INFO  TransferTask - Starting transfer for file: File_21_23697.bin
2025-11-23 22:30:50 [pool-2-thread-3] INFO  TransferTask - Transfer completed: File_20_26350.bin | Size: 0.49 MB | Time: 5720 ms | Bandwidth: 0.08 MB/s
2025-11-23 22:30:50 [pool-2-thread-3] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_20_26350.bin
2025-11-23 22:30:54 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_22_24397.bin
2025-11-23 22:30:54 [pool-2-thread-5] INFO  TransferTask - Starting transfer for file: File_22_24397.bin
2025-11-23 22:30:56 [pool-2-thread-4] INFO  TransferTask - Transfer completed: File_21_23697.bin | Size: 2.06 MB | Time: 6387 ms | Bandwidth: 0.32 MB/s
2025-11-23 22:30:56 [pool-2-thread-4] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_21_23697.bin
2025-11-23 22:30:58 [pool-2-thread-2] INFO  TransferTask - Transfer completed: File_1_18729.bin | Size: 1.96 MB | Time: 18440 ms | Bandwidth: 0.11 MB/s
2025-11-23 22:30:58 [pool-2-thread-2] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_1_18729.bin
2025-11-23 22:30:59 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_23_6208.bin
2025-11-23 22:30:59 [pool-2-thread-6] INFO  TransferTask - Starting transfer for file: File_23_6208.bin
2025-11-23 22:31:01 [pool-2-thread-5] INFO  TransferTask - Transfer completed: File_22_24397.bin | Size: 2.80 MB | Time: 6597 ms | Bandwidth: 0.42 MB/s
2025-11-23 22:31:01 [pool-2-thread-5] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_22_24397.bin
2025-11-23 22:31:04 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_24_165.bin
2025-11-23 22:31:04 [pool-2-thread-7] INFO  TransferTask - Starting transfer for file: File_24_165.bin
2025-11-23 22:31:09 [main] INFO  SftpFileTransfer - Submitted file for transfer: File_25_15151.bin
2025-11-23 22:31:09 [pool-2-thread-8] INFO  TransferTask - Starting transfer for file: File_25_15151.bin
2025-11-23 22:31:10 [pool-2-thread-7] INFO  TransferTask - Transfer completed: File_24_165.bin | Size: 0.75 MB | Time: 5848 ms | Bandwidth: 0.13 MB/s
2025-11-23 22:31:10 [pool-2-thread-7] INFO  TransferTask - Moved file to completed directory: D:\temp\completed\File_24_165.bin
```

> [!NOTE]
> This java library is build for special usecase to transfer files reliably from local directory to SFTP Server.

## License

Apache License, Version 2.0
