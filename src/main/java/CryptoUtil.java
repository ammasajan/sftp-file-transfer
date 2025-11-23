import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.Key;

public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    // 16-byte key for AES-128. In production, store this securely (e.g., env var, keystore).
    // For this task, we use a hardcoded key as requested/implied for simplicity.
    private static final byte[] KEY = "MySuperSecretKey".getBytes(); 

    public static String encrypt(String value) throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        return Base64.getEncoder().encodeToString(encryptedByteValue);
    }

    public static String decrypt(String value) throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.getDecoder().decode(value);
        byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue, "utf-8");
    }

    // Main method to generate encrypted password for config
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                System.out.println("Encrypted: " + encrypt(args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java CryptoUtil <password_to_encrypt>");
        }
    }
}
