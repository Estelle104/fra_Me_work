package com.framework.upload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

public class MultipartParser {

    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;

    public MultipartData parse(HttpServletRequest request) throws IOException {

        String contentType = request.getContentType();

        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) {
            throw new IOException("La requête doit être de type multipart/form-data.");
        }

        // Récupération du boundary.
        String boundary = extraireParametre(contentType, "boundary");

        if (boundary == null || boundary.isEmpty()) {
            throw new IOException("Boundary multipart introuvable.");
        }

        // Vérification de la taille annoncée, lorsqu'elle est connue.
        long longueur = request.getContentLengthLong();

        if (longueur > MAX_REQUEST_SIZE) {
            throw new IOException("La requête dépasse la taille maximale autorisée.");
        }

        // Lecture du corps sous forme binaire.
        byte[] body;

        try (InputStream input = request.getInputStream()) {
            body = lireLimite(input, MAX_REQUEST_SIZE);
        }

        // Délimiteurs multipart.
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);

        byte[] separateur = ("\r\n--" + boundary).getBytes(StandardCharsets.ISO_8859_1);

        if (!commencePar(body, boundaryBytes, 0)) {
            throw new IOException("Le début du corps multipart est invalide.");
        }

        MultipartData resultat = new MultipartData();

        int position = boundaryBytes.length;

        while (true) {

            // Fin du formulaire multipart.
            if (commencePar(body, "--".getBytes(StandardCharsets.US_ASCII), position)) {
                break;
            }

            // Chaque partie doit commencer après CRLF.
            if (!commencePar( body,"\r\n".getBytes(StandardCharsets.US_ASCII), position)) {
                throw new IOException("Délimiteur multipart invalide.");
            }

            position += 2;

            // Recherche de la fin des en-têtes.
            byte[] finEntetes =
                    "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);

            int finHeaders = indexOf(body, finEntetes, position);

            if (finHeaders < 0) {
                throw new IOException("En-têtes multipart incomplets.");
            }

            // Les en-têtes sont textuels.
            String headers = new String(
                    body,
                    position,
                    finHeaders - position,
                    StandardCharsets.ISO_8859_1
            );

            int debutContenu = finHeaders + finEntetes.length;

            // Recherche de la prochaine frontière sans convertir
            // le contenu binaire en texte.
            int finContenu = indexOf(body, separateur, debutContenu);

            if (finContenu < 0) {
                throw new IOException("Fin de partie multipart introuvable.");
            }

            // Extraction exacte du contenu de cette partie.
            byte[] contenu = Arrays.copyOfRange(body,debutContenu,finContenu );

            traiterPartie(headers, contenu, resultat);

            // Position après CRLF--boundary.
            position = finContenu + separateur.length;
        }

        return resultat;
    }

    // Traite une partie : fichier ou champ texte.
    private void traiterPartie(String headers,byte[] contenu, MultipartData resultat) throws IOException {

        String disposition = obtenirEntete(headers, "Content-Disposition");

        if (disposition == null) {
            throw new IOException("En-tête Content-Disposition manquant.");
        }

        String nomChamp = extraireParametre(disposition, "name");

        if (nomChamp == null || nomChamp.isEmpty()) {
            throw new IOException("Nom du champ multipart manquant.");
        }

        String nomFichier = extraireParametre(disposition, "filename");

        if (nomFichier != null) {
            if (nomFichier.isEmpty()) {
                return;
            }

            String typeFichier = obtenirEntete(headers, "Content-Type");

            if (typeFichier == null) {
                typeFichier = "application/octet-stream";
            }

            UploadedFile fichier = new UploadedFile(nomFichier,typeFichier,contenu);

            resultat.ajouterFichier(nomChamp, fichier);

        } else {

            // Les champs texte sont décodés en UTF-8.
            String valeur = new String(contenu,StandardCharsets.UTF_8);

            resultat.ajouterChamp(nomChamp, valeur);
        }
    }

    // Lit le flux sans dépasser la taille maximale autorisée.
    private byte[] lireLimite(InputStream input,int limite) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];

        int total = 0;
        int lus;

        while ((lus = input.read(buffer)) != -1) {
            if (lus > limite - total) {
                throw new IOException("La requête dépasse la taille maximale autorisée.");
            }

            output.write(buffer, 0, lus);
            total += lus;
        }

        return output.toByteArray();
    }

    
     //Recherche un en-tête HTTP dans les en-têtes d'une partie.
    private String obtenirEntete(String headers, String nom) {

        for (String ligne : headers.split("\r\n")) {

            int separateur = ligne.indexOf(':');

            if (separateur < 0) {
                continue;
            }

            String cle = ligne.substring(0, separateur).trim();

            if (cle.equalsIgnoreCase(nom)) {
                return ligne.substring(separateur + 1).trim();
            }
        }

        return null;
    }

    private String extraireParametre(String texte, String nom) {

        Pattern pattern = Pattern.compile(
                "(?:^|;)\\s*" + Pattern.quote(nom)
                + "\\s*=\\s*(?:\"([^\"]*)\"|([^;]*))",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texte);

        if (!matcher.find()) {
            return null;
        }

        if (matcher.group(1) != null) {
            return matcher.group(1);
        }

        return matcher.group(2).trim();
    }

    private boolean commencePar(byte[] source,byte[] recherche,int position) {

        if (position < 0 || position + recherche.length > source.length) {
            return false;
        }

        for (int i = 0; i < recherche.length; i++) {
            if (source[position + i] != recherche[i]) {
                return false;
            }
        }

        return true;
    }

    private int indexOf(
            byte[] source,
            byte[] recherche,
            int debut) {

        if (recherche.length == 0) {
            return debut;
        }

        for (int i = debut; i <= source.length - recherche.length; i++) {

            boolean trouve = true;

            for (int j = 0; j < recherche.length; j++) {

                if (source[i + j] != recherche[j]) {
                    trouve = false;
                    break;
                }
            }

            if (trouve) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Résultat de l'analyse multipart.
     *
     * Un formulaire peut contenir plusieurs champs texte
     * et plusieurs fichiers.
     */
    public static class MultipartData {

        private final Map<String, List<String>> champs =
                new LinkedHashMap<>();

        private final Map<String, List<UploadedFile>> fichiers =
                new LinkedHashMap<>();

        private void ajouterChamp(String nom, String valeur) {
            champs.computeIfAbsent(nom, k -> new ArrayList<>())
                    .add(valeur);
        }

        private void ajouterFichier(String nom, UploadedFile fichier) {
            fichiers.computeIfAbsent(nom, k -> new ArrayList<>())
                    .add(fichier);
        }

        public String getChamp(String nom) {

            List<String> valeurs = champs.get(nom);

            if (valeurs == null || valeurs.isEmpty()) {
                return null;
            }

            return valeurs.get(0);
        }

        public List<String> getChamps(String nom) {
            return champs.getOrDefault(nom, List.of());
        }

        public UploadedFile getFichier(String nom) {

            List<UploadedFile> valeurs = fichiers.get(nom);

            if (valeurs == null || valeurs.isEmpty()) {
                return null;
            }

            return valeurs.get(0);
        }

        public List<UploadedFile> getFichiers(String nom) {
            return fichiers.getOrDefault(nom, List.of());
        }

        public Map<String, List<String>> getTousLesChamps() {
            return champs;
        }

        public Map<String, List<UploadedFile>> getTousLesFichiers() {
            return fichiers;
        }
    }
}