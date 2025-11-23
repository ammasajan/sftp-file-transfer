import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class TransferTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(TransferTask.class);
    private final File file;
    private final Set<String> activeTransfers;

    public TransferTask(File file, Set<String> activeTransfers) {
        this.file = file;
        this.activeTransfers = activeTransfers;
    }

    @Override
    public void run() {
        String host = ConfigLoader.get("sftp.host");
        int port = ConfigLoader.getInt("sftp.port", 22);
        String user = ConfigLoader.get("sftp.user");
        String password = ConfigLoader.getDecryptedPassword();
        String remoteDir = ConfigLoader.get("sftp.remote.dir");
        String completedDir = ConfigLoader.get("sftp.local.completed.dir");

        Session session = null;
        ChannelSftp channelSftp = null;
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Starting transfer for file: " + file.getName());

            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            session.connect();
            
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            
            channelSftp.cd(remoteDir);
            
            channelSftp.cd(remoteDir);
            
            boolean shouldTransfer = true;
            // Optimization: File existence check is now done in SftpFileTransfer before submission.
            // We proceed with transfer assuming it's needed.

            if (shouldTransfer) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    channelSftp.put(fis, file.getName());
                }
            } else {
                // If skipped, we might still want to move it to completed or just leave it?
                // The requirement implies we treat it as "handled" if we skip it because it's already there.
                // But if we don't transfer, maybe we shouldn't move it to completed?
                // Let's assume we treat it as success (idempotent) and move it to completed so it doesn't get picked up again immediately/repeatedly if we are monitoring.
                // However, if we don't move it, the monitor will pick it up again and skip it again.
                // Let's proceed to "completion" steps even if skipped.
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double sizeInMb = file.length() / (1024.0 * 1024.0);
            double speed = sizeInMb / (duration / 1000.0); // MB/s

            logger.info(String.format("Transfer completed: %s | Size: %.2f MB | Time: %d ms | Bandwidth: %.2f MB/s",
                    file.getName(), sizeInMb, duration, speed));

            // Move to completed directory
            if (completedDir != null && !completedDir.isEmpty()) {
                Path source = file.toPath();
                Path targetDir = Paths.get(completedDir);
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                Path target = targetDir.resolve(file.getName());
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Moved file to completed directory: " + target);
            }

        } catch (Exception e) {
            logger.error("Failed to transfer file: " + file.getName(), e);
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
            
            // Remove from active transfers
            synchronized (activeTransfers) {
                activeTransfers.remove(file.getAbsolutePath());
            }
        }
    }
}
