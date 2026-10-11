package app.models;

public class Adresse {

    private String ville;
    private String rue;

    public Adresse() {}

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getRue() { return rue; }
    public void setRue(String rue) { this.rue = rue; }

    @Override
    public String toString() {
        return rue + ", " + ville;
    }
}
