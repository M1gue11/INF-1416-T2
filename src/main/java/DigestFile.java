package main.java;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.*;

public class DigestFile {
    private static final int BUFFER_SIZE = 1024 * 1024; // 1 MB

    private static String digestToHex(byte[] digest) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    public static String digest(File file, String tipoDigest) {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            MessageDigest messageDigest = MessageDigest.getInstance(tipoDigest);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }

            byte[] digest = messageDigest.digest();

            return digestToHex(digest);

        } catch (Exception e) {
            System.err.println("Erro ao calcular digest: " + e.getMessage());
            return null;
        }
    }
}
