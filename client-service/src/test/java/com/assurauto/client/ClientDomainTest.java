package com.assurauto.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.assurauto.client.domain.model.Client;
import com.assurauto.client.domain.valueobject.CategorieClient;
import com.assurauto.client.domain.valueobject.TypeEvenement;
import org.junit.jupiter.api.Test;

class ClientDomainTest {

    @Test
    void nouveauClientEstStandardAvecScore100() {
        Client c = new Client();
        assertEquals(CategorieClient.STANDARD, c.getCategorie());
        assertEquals(100, c.getScoreFidelite());
    }

    @Test
    void evenementsFontEvoluerScoreEtCategorie() {
        Client c = new Client();
        for (int i = 0; i < 6; i++) {
            c.ajouterEvenement(TypeEvenement.SOUSCRIPTION, "Souscription " + i);
        }
        assertEquals(400, c.getScoreFidelite());
        assertEquals(CategorieClient.PREMIUM, c.getCategorie());
        for (int i = 0; i < 8; i++) {
            c.ajouterEvenement(TypeEvenement.SOUSCRIPTION, "Souscription " + i);
        }
        assertEquals(800, c.getScoreFidelite());
        assertEquals(CategorieClient.VIP, c.getCategorie());
        assertEquals(14, c.getHistoriqueCommercial().size());
    }

    @Test
    void scoreNeDescendJamaisSousZero() {
        Client c = new Client();
        for (int i = 0; i < 20; i++) {
            c.ajouterEvenement(TypeEvenement.SINISTRE, "Sinistre");
        }
        assertEquals(0, c.getScoreFidelite());
        assertEquals(CategorieClient.STANDARD, c.getCategorie());
    }
}
