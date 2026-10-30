package com.assurauto.sinistre.domain.valueobject;

/** Statut de traitement d'un sinistre et transitions autorisées. */
public enum StatutSinistre {
    DECLARE, EN_EXPERTISE, ACCEPTE, REFUSE, INDEMNISE, CLOTURE;

    public boolean transitionAutorisee(StatutSinistre cible) {
        return switch (this) {
            case DECLARE -> cible == EN_EXPERTISE || cible == REFUSE;
            case EN_EXPERTISE -> cible == ACCEPTE || cible == REFUSE;
            case ACCEPTE -> cible == INDEMNISE;
            case INDEMNISE, REFUSE -> cible == CLOTURE;
            case CLOTURE -> false;
        };
    }
}
