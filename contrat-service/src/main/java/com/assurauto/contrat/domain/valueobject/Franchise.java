package com.assurauto.contrat.domain.valueobject;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Objet valeur : franchise applicable à une garantie. */
@Embeddable
public class Franchise {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_garantie", nullable = false)
    private TypeGarantie garantie;
    @Column(name = "montant_franchise", nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    public Franchise() {}

    public Franchise(TypeGarantie garantie, BigDecimal montant) {
        this.garantie = garantie;
        this.montant = montant;
    }

    public TypeGarantie getGarantie() { return garantie; }
    public BigDecimal getMontant() { return montant; }
}
