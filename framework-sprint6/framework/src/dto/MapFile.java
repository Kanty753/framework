package dto;

/**
 * Représente un fichier uploadé via un formulaire multipart.
 * Utilisé comme type d'attribut dans les modèles pour le binding automatique.
 */
public class MapFile {

    /** Nom du fichier d'origine (nettoyé, sans chemin dangereux) */
    private String nomFichier;

    /** Type MIME du fichier (ex. "image/png", "application/pdf") */
    private String type;

    /** Contenu brut du fichier en octets */
    private byte[] contenu;

    // ---- Constructeurs ----

    public MapFile() {}

    public MapFile(String nomFichier, String type, byte[] contenu) {
        this.nomFichier = nomFichier;
        this.type = type;
        this.contenu = contenu;
    }

    // ---- Getters / Setters ----

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }

    /**
     * Retourne la taille du fichier en octets, ou 0 si aucun contenu.
     */
    public long getTaille() {
        return contenu != null ? contenu.length : 0;
    }

    /**
     * Indique si ce MapFile contient effectivement un fichier.
     */
    public boolean estVide() {
        return contenu == null || contenu.length == 0;
    }

    @Override
    public String toString() {
        return "MapFile{nom='" + nomFichier + "', type='" + type
                + "', taille=" + getTaille() + " octets}";
    }
}
