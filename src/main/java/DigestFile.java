package main.java;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

public class DigestFile {

    public static byte[] readFileToByteArray(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public static String digest(File file, String tipoDigest) {
        try {
            byte[] content = readFileToByteArray(file.toPath().toString());
            MessageDigest messageDigest = MessageDigest.getInstance(tipoDigest);

            messageDigest.update(content);
            byte[] digest = messageDigest.digest();

            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1);
                buf.append((hex.length() < 2 ? "0" : "") + hex);
            }
            return buf.toString();

        } catch (Exception e) {
            System.err.println("Erro ao calcular digest: " + e.getMessage());
            return null;
        }

    }
}
