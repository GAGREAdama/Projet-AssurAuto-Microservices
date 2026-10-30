package com.assurauto.vehicule.domain.valueobject;

import jakarta.persistence.*;

/** Objet valeur : équipements de sécurité principaux. */
@Embeddable
public class EquipementsSecurite {

    @Column(name = "alarme_installee", nullable = false)
    private boolean alarme;
    @Column(name = "dispositif_geolocalisation", nullable = false)
    private boolean geolocalisation;

    public EquipementsSecurite() {}

    public EquipementsSecurite(boolean alarme, boolean geolocalisation) {
        this.alarme = alarme;
        this.geolocalisation = geolocalisation;
    }

    public boolean isAlarme() { return alarme; }
    public boolean isGeolocalisation() { return geolocalisation; }
}
