package app.models;

import dto.MapFile;

/**
 * Exemple de modèle avec un attribut MapFile pour l'upload.
 * Le champ "photo" sera automatiquement rempli par le framework
 * si le formulaire envoie un fichier dans un champ nommé "p.photo".
 */
public class Produit {

    private String nom;
    private double prix;
    private MapFile photo;

    public Produit() {}

    // ---- Getters / Setters ----

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public MapFile getPhoto() { return photo; }
    public void setPhoto(MapFile photo) { this.photo = photo; }

    @Override
    public String toString() {
        return "Produit{nom='" + nom + "', prix=" + prix
                + ", photo=" + (photo != null ? photo : "aucune") + "}";
    }
}
