package app.models;

public class Personne {

    private String nom;
    private int age;
    private Adresse adresse;

    public Personne() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Adresse getAdresse() { return adresse; }
    public void setAdresse(Adresse adresse) { this.adresse = adresse; }

    @Override
    public String toString() {
        return "Personne{nom='" + nom + "', age=" + age + ", adresse=" + adresse + "}";
    }
}
