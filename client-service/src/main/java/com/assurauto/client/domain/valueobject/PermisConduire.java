package com.assurauto.client.domain.valueobject;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Objet valeur : permis de conduire. */
@Embeddable
public class PermisConduire {

    @Column(name = "permis_numero", nullable = false)
    private String numero;
    @Column(name = "permis_categorie", nullable = false, length = 10)
    private String categorie;
    @Column(name = "permis_date_obtention", nullable = false)
    private LocalDate dateObtention;

    public PermisConduire() {}

    public PermisConduire(String numero, String categorie, LocalDate dateObtention) {
        this.numero = numero;
        this.categorie = categorie;
        this.dateObtention = dateObtention;
    }

    public String getNumero() { return numero; }
    public String getCategorie() { return categorie; }
    public LocalDate getDateObtention() { return dateObtention; }
}
