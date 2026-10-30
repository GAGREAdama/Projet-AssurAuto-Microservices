package com.assurauto.vehicule.application.mapper;

import com.assurauto.vehicule.api.dto.VehiculeDto;
import com.assurauto.vehicule.domain.model.Vehicule;
import com.assurauto.vehicule.domain.valueobject.EquipementsSecurite;
import java.util.HashSet;
import java.util.TreeSet;

public final class VehiculeMapper {

    private VehiculeMapper() {
    }

    public static VehiculeDto toDto(Vehicule v) {
        return new VehiculeDto(v.getId(), v.getContratId(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                v.getAnneeFabrication(), v.getVin(), v.getDatePremiereMiseEnCirculation(), v.getKilometrageDeclare(),
                v.getZoneStationnement(), v.getTypeGarage(), v.getUsagePrincipal(), v.getKilometrageAnnuelEstime(),
                v.getEquipements().isAlarme(), v.getEquipements().isGeolocalisation(),
                new TreeSet<>(v.getAutresEquipements()), v.scoreRisque());
    }

    /** Copie les champs du DTO ; le kilométrage est géré à part (règle : il ne diminue jamais). */
    public static void apply(VehiculeDto d, Vehicule v) {
        v.setContratId(d.contratId());
        v.setImmatriculation(d.immatriculation().trim().toUpperCase());
        v.setMarque(d.marque().trim());
        v.setModele(d.modele().trim());
        v.setAnneeFabrication(d.anneeFabrication());
        v.setVin(d.vin().trim().toUpperCase());
        v.setDatePremiereMiseEnCirculation(d.datePremiereMiseEnCirculation());
        v.setZoneStationnement(d.zoneStationnement().trim());
        v.setTypeGarage(d.typeGarage());
        v.setUsagePrincipal(d.usagePrincipal());
        v.setKilometrageAnnuelEstime(d.kilometrageAnnuelEstime());
        v.setEquipements(new EquipementsSecurite(d.systemeAlarme(), d.dispositifGeolocalisation()));
        v.getAutresEquipements().clear();
        if (d.autresEquipements() != null) {
            v.getAutresEquipements().addAll(new HashSet<>(d.autresEquipements()));
        }
    }
}
