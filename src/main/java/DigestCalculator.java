package main.java;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DigestCalculator {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java DigestCalculator <tipoDigest> <caminhoPasta> <caminhoXml>");
            return;
        }
        String tipoDigest = args[0];
        String caminhoPasta = args[1];
        String caminhoXml = args[2];

        final XMLHandler xmlHandler;
        try {
            xmlHandler = new XMLHandler(caminhoXml);
        } catch (Exception e) {
            System.out.println("Erro ao carregar o arquivo XML: " + e.getMessage());
            return;
        }

        try {
            Files.walk(Paths.get(caminhoPasta))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String digest = DigestFile.digest(path.toFile(), tipoDigest);
                            String fileName = path.getFileName().toString();

                            EnumStatus status = xmlHandler.getStatus(fileName, tipoDigest, digest);
                            System.out.println(String.format(
                                    "%s %s %s (%s)", fileName, tipoDigest, digest, status.toString()));

                            if (status == EnumStatus.NOT_FOUND) {
                                System.out.println("Arquivo não encontrado no XML, adicionando...");
                                xmlHandler.addNewDigestForFile(fileName, tipoDigest, digest);
                            }
                        } catch (Exception e) {
                            System.err.println("Erro ao calcular digest de " + path + ": " + e.getMessage());
                        }
                    });

            System.out.println("Digest calculado com sucesso. Resultado salvo em: " + caminhoXml);

        } catch (Exception e) {
            System.err.println("Erro ao acessar os arquivos da pasta: " + e.getMessage());
        }

    }
}