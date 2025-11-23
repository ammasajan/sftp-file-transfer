import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SftpFileTransfer {
    private static final Logger logger = LogManager.getLogger(SftpFileTransfer.class);
    private static final Set<String> activeTransfers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        logger.info("Starting SFTP File Transfer Application");

        String localDir = ConfigLoader.get("sftp.local.dir");
        int maxParallel = ConfigLoader.getInt("sftp.parallel.max", 4);
        boolean monitorEnable = ConfigLoader.getBoolean("sftp.monitor.enable", false);
        int monitorInterval = ConfigLoader.getInt("sftp.monitor.interval", 10);
        int stableCheckDelay = ConfigLoader.getInt("sftp.stable.check.delay", 5);
        String extension = ConfigLoader.get("sftp.file.extension");

        ExecutorService executor = Executors.newFixedThreadPool(maxParallel);

        File folder = new File(localDir);
        if (!folder.exists() || !folder.isDirectory()) {
            logger.error("Invalid local directory: " + localDir);
            return;
        }

        if (monitorEnable) {
            logger.info("Monitoring enabled for directory: " + localDir);
            while (true) {
                Map<String, Long> remoteFiles = fetchRemoteFiles();
                scanAndSubmit(folder, executor, extension, stableCheckDelay, remoteFiles);
                try {
                    Thread.sleep(monitorInterval * 1000L);
                } catch (InterruptedException e) {
                    logger.error("Monitor interrupted", e);
                    break;
                }
            }
        } else {
            logger.info("One-time scan for directory: " + localDir);
            Map<String, Long> remoteFiles = fetchRemoteFiles();
            scanAndSubmit(folder, executor, extension, stableCheckDelay, remoteFiles);
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Executor interrupted", e);
            }
        }
        
        logger.info("Application finished.");
    }


    private static void scanAndSubmit(File folder, ExecutorService executor, String extension, int stableCheckDelay, Map<String, Long> remoteFiles) {
        File[] files = folder.listFiles();
        if (files == null) return;

        boolean overwriteIfSame = ConfigLoader.getBoolean("sftp.overwrite.if.same", false);

        for (File file : files) {
            if (file.isFile()) {
                if (extension != null && !extension.isEmpty() && !file.getName().endsWith(extension)) {
                    continue;
                }

                String filePath = file.getAbsolutePath();
                
                // Check if already active
                synchronized (activeTransfers) {
                    if (activeTransfers.contains(filePath)) {
                        continue;
                    }
                }

                // Check against remote files if overwrite is disabled
                if (!overwriteIfSame && remoteFiles != null) {
                    Long remoteSize = remoteFiles.get(file.getName());
                    if (remoteSize != null && remoteSize == file.length()) {
                        logger.info("Skipping transfer (File exists with same size): " + file.getName());
                        
                        String overlapDir = ConfigLoader.get("sftp.local.overlap.dir");
                        if (overlapDir != null && !overlapDir.isEmpty()) {
                            try {
                                java.nio.file.Path source = file.toPath();
                                java.nio.file.Path targetDir = java.nio.file.Paths.get(overlapDir);
                                if (!java.nio.file.Files.exists(targetDir)) {
                                    java.nio.file.Files.createDirectories(targetDir);
                                }
                                java.nio.file.Path target = targetDir.resolve(file.getName());
                                java.nio.file.Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                logger.info("Moved overlapping file to: " + target);
                            } catch (java.io.IOException e) {
                                logger.error("Failed to move overlapping file: " + file.getName(), e);
                            }
                        }
                        continue;
                    }
                }

                // Check stability
                if (isStable(file, stableCheckDelay)) {
                    synchronized (activeTransfers) {
                        if (!activeTransfers.contains(filePath)) {
                            activeTransfers.add(filePath);
                            executor.submit(new TransferTask(file, activeTransfers));
                            logger.info("Submitted file for transfer: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    private static boolean isStable(File file, int delaySeconds) {
        long initialSize = file.length();
        long initialTime = file.lastModified();
        
        try {
            Thread.sleep(delaySeconds * 1000L);
        } catch (InterruptedException e) {
            return false;
        }

        long finalSize = file.length();
        long finalTime = file.lastModified();

        return initialSize == finalSize && initialTime == finalTime;
    }

    private static Map<String, Long> fetchRemoteFiles() {
        String host = ConfigLoader.get("sftp.host");
        int port = ConfigLoader.getInt("sftp.port", 22);
        String user = ConfigLoader.get("sftp.user");
        String password = ConfigLoader.getDecryptedPassword();
        String remoteDir = ConfigLoader.get("sftp.remote.dir");

        Map<String, Long> fileMap = new java.util.HashMap<>();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            
            @SuppressWarnings("unchecked")
            java.util.Vector<ChannelSftp.LsEntry> list = channelSftp.ls(remoteDir);
            for (ChannelSftp.LsEntry entry : list) {
                if (!entry.getAttrs().isDir()) {
                    fileMap.put(entry.getFilename(), entry.getAttrs().getSize());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch remote file list", e);
            return null; // Return null to indicate failure, maybe fallback to individual checks or retry?
                         // For now, if we fail to get list, we might assume empty or just log error.
                         // If we return null, the check in scanAndSubmit will be skipped (remoteFiles != null),
                         // effectively falling back to "try to transfer" (which might fail or overwrite depending on server).
                         // Actually, if we return null, we skip the optimization check, so we proceed to submit.
                         // TransferTask will then overwrite. This is acceptable fallback.
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }
        return fileMap;
    }
}
