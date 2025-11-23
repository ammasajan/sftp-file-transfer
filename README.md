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

## License

Apache License, Version 2.0
