package com.assurauto.client.domain.valueobject;

/** Type d'événement de l'historique commercial et impact sur le score de fidélité. */
public enum TypeEvenement {
    SOUSCRIPTION(50), RENOUVELLEMENT(30), PARRAINAGE(40), RECLAMATION(-10), SINISTRE(-20), AUTRE(0);

    private final int points;

    TypeEvenement(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
