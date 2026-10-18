package com.framework.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class UploadedFile {

    private String fileName;
    private String contentType;
    private byte[] bytes;

    public UploadedFile(String fileName, String contentType, byte[] bytes) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public long getSize() {
        return bytes.length;
    }

    // Enregistrer le fichier dans un répertoire local
    public Path saveTo(Path directory) throws IOException {

        Files.createDirectories(directory);

        // Nettoyer le nom de fichier pour éviter les problèmes de sécurité
        String safeName = fileName.replace('\\', '/');

        // Supprimer les parties de chemin pour ne garder que le nom du fichier
        safeName = safeName.substring(safeName.lastIndexOf('/') + 1);

        if (safeName.isBlank() || safeName.equals(".") || safeName.equals("..")) {
            throw new IOException("Nom de fichier invalide.");
        }

        // Déterminer le chemin de destination
        Path dir = directory.toAbsolutePath().normalize();
        Path destination = dir.resolve(safeName).normalize();

        if (!destination.getParent().equals(dir)) {
            throw new IOException("Chemin de fichier invalide.");
        }

        // Enregistrer les octets sans écraser un fichier existant
        // Files.write(destination,bytes,StandardOpenOption.CREATE_NEW,
        // StandardOpenOption.WRITE);
        Files.write(destination,bytes,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
        
        return destination;
    }
}