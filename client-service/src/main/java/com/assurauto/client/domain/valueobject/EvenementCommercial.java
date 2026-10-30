package com.assurauto.client.domain.valueobject;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Objet valeur : ligne de l'historique commercial. */
@Embeddable
public class EvenementCommercial {

    @Column(name = "date_evenement", nullable = false)
    private LocalDateTime dateEvenement;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false)
    private TypeEvenement type;
    @Column(length = 500)
    private String description;
    private int points;

    public EvenementCommercial() {}

    public EvenementCommercial(LocalDateTime dateEvenement, TypeEvenement type, String description, int points) {
        this.dateEvenement = dateEvenement;
        this.type = type;
        this.description = description;
        this.points = points;
    }

    public LocalDateTime getDateEvenement() { return dateEvenement; }
    public TypeEvenement getType() { return type; }
    public String getDescription() { return description; }
    public int getPoints() { return points; }
}
