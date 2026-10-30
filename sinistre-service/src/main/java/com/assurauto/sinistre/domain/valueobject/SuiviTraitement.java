package com.assurauto.sinistre.domain.valueobject;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Objet valeur : étape du suivi de traitement. */
@Embeddable
public class SuiviTraitement {

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutSinistre statut;
    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;
    @Column(length = 500)
    private String commentaire;

    public SuiviTraitement() {}

    public SuiviTraitement(StatutSinistre statut, LocalDateTime dateChangement, String commentaire) {
        this.statut = statut;
        this.dateChangement = dateChangement;
        this.commentaire = commentaire;
    }

    public StatutSinistre getStatut() { return statut; }
    public LocalDateTime getDateChangement() { return dateChangement; }
    public String getCommentaire() { return commentaire; }
}
