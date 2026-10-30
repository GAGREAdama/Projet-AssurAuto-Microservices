package com.assurauto.contrat.domain.valueobject;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Objet valeur : garantie et plafond de couverture. */
@Embeddable
public class Garantie {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_garantie", nullable = false)
    private TypeGarantie type;
    @Column(name = "plafond_couverture", nullable = false, precision = 14, scale = 2)
    private BigDecimal plafond;

    public Garantie() {}

    public Garantie(TypeGarantie type, BigDecimal plafond) {
        this.type = type;
        this.plafond = plafond;
    }

    public TypeGarantie getType() { return type; }
    public BigDecimal getPlafond() { return plafond; }
}
