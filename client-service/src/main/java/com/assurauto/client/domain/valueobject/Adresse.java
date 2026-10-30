package com.assurauto.client.domain.valueobject;

import jakarta.persistence.*;

/** Objet valeur : adresse postale. */
@Embeddable
public class Adresse {

    @Column(name = "adresse_rue", nullable = false)
    private String rue;
    @Column(name = "adresse_ville", nullable = false)
    private String ville;
    @Column(name = "adresse_pays", nullable = false)
    private String pays;

    public Adresse() {}

    public Adresse(String rue, String ville, String pays) {
        this.rue = rue;
        this.ville = ville;
        this.pays = pays;
    }

    public String getRue() { return rue; }
    public String getVille() { return ville; }
    public String getPays() { return pays; }
}
