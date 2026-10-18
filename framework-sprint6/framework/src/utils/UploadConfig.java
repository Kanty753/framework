package utils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

/**
 * Charge la configuration d'upload depuis le fichier "upload.properties"
 * situé dans le classpath. Le fichier est lu une seule fois (lazy init).
 *
 * Clé attendue : upload.path = /chemin/vers/dossier/uploads
 */
public class UploadConfig {

    private static final String FICHIER_CONFIG = "upload.properties";
    private static final String CLE_CHEMIN = "upload.path";

    /** Instance unique (lazy, thread-safe via holder) */
    private static class Holder {
        static final UploadConfig INSTANCE = new UploadConfig();
    }

    private final String cheminUpload;

    /**
     * Constructeur privé : charge le fichier .properties et valide le dossier.
     */
    private UploadConfig() {
        Properties props = new Properties();

        // Lecture du fichier depuis le classpath
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(FICHIER_CONFIG)) {

            if (is == null) {
                throw new RuntimeException(
                    "Fichier de configuration introuvable dans le classpath : "
                    + FICHIER_CONFIG
                    + ". Créez-le avec la clé '" + CLE_CHEMIN + "'.");
            }

            props.load(is);

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur lors de la lecture de " + FICHIER_CONFIG, e);
        }

        // Lecture de la clé
        String chemin = props.getProperty(CLE_CHEMIN);
        if (chemin == null || chemin.trim().isEmpty()) {
            throw new RuntimeException(
                "Clé '" + CLE_CHEMIN + "' manquante ou vide dans "
                + FICHIER_CONFIG);
        }

        chemin = chemin.trim();

        // Vérification / création du dossier
        File dossier = new File(chemin);
        if (!dossier.exists()) {
            boolean cree = dossier.mkdirs();
            if (!cree) {
                throw new RuntimeException(
                    "Impossible de créer le dossier d'upload : " + chemin);
            }
            System.out.println("[UploadConfig] Dossier créé : " + chemin);
        }

        if (!dossier.isDirectory()) {
            throw new RuntimeException(
                "Le chemin d'upload n'est pas un dossier : " + chemin);
        }

        this.cheminUpload = chemin;
        System.out.println("[UploadConfig] Dossier d'upload configuré : " + chemin);
    }

    /** Retourne l'instance unique. */
    public static UploadConfig getInstance() {
        return Holder.INSTANCE;
    }

    /** Retourne le chemin absolu du dossier d'upload. */
    public String getCheminUpload() {
        return cheminUpload;
    }

    /**
     * Nettoie un nom de fichier pour empêcher le path traversal.
     * - Supprime tout chemin parent (../, ..\, /, \)
     * - Ne garde que le nom de base du fichier
     * - Remplace les caractères spéciaux dangereux
     *
     * @param nomOriginal le nom brut fourni par le navigateur
     * @return le nom nettoyé, ou "fichier_inconnu" si vide
     */
    public static String nettoyerNomFichier(String nomOriginal) {
        if (nomOriginal == null || nomOriginal.trim().isEmpty()) {
            return "fichier_inconnu";
        }

        // Extraire uniquement le nom de base (après le dernier séparateur)
        String nom = nomOriginal;
        int dernierSlash = Math.max(
            nom.lastIndexOf('/'),
            nom.lastIndexOf('\\')
        );
        if (dernierSlash >= 0) {
            nom = nom.substring(dernierSlash + 1);
        }

        // Supprimer les séquences de traversal restantes
        nom = nom.replace("..", "");

        // Supprimer les caractères dangereux (garder lettres, chiffres, ., -, _)
        nom = nom.replaceAll("[^a-zA-Z0-9._\\-]", "_");

        if (nom.isEmpty() || nom.equals("_")) {
            return "fichier_inconnu";
        }

        return nom;
    }
}
