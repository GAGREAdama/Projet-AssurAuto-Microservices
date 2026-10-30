package com.assurauto.vehicule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.assurauto.vehicule.domain.exception.BusinessRuleException;
import com.assurauto.vehicule.domain.model.Vehicule;
import com.assurauto.vehicule.domain.valueobject.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class VehiculeDomainTest {

    private Vehicule vehicule(TypeGarage garage, UsageVehicule usage, int kmAnnuel, boolean alarme, boolean geo) {
        Vehicule v = new Vehicule();
        v.setTypeGarage(garage);
        v.setUsagePrincipal(usage);
        v.setKilometrageAnnuelEstime(kmAnnuel);
        v.setEquipements(new EquipementsSecurite(alarme, geo));
        v.setAnneeFabrication(2020);
        v.setDatePremiereMiseEnCirculation(LocalDate.of(2020, 6, 1));
        return v;
    }

    @Test
    void scoreRisqueTientCompteDuGarageDeLUsageEtDesEquipements() {
        assertEquals(25, vehicule(TypeGarage.PRIVE, UsageVehicule.PERSONNEL, 5000, true, true).scoreRisque());
        assertEquals(80, vehicule(TypeGarage.RUE, UsageVehicule.PROFESSIONNEL, 30000, false, false).scoreRisque());
    }

    @Test
    void leKilometrageNeDiminuePas() {
        Vehicule v = vehicule(TypeGarage.PRIVE, UsageVehicule.PERSONNEL, 10000, false, false);
        v.mettreAJourKilometrage(50000);
        assertThrows(BusinessRuleException.class, () -> v.mettreAJourKilometrage(40000));
        assertEquals(50000, v.getKilometrageDeclare());
    }

    @Test
    void refuseDesDatesIncoherentes() {
        Vehicule v = vehicule(TypeGarage.PRIVE, UsageVehicule.PERSONNEL, 10000, false, false);
        v.verifierCoherence();
        v.setDatePremiereMiseEnCirculation(LocalDate.of(2018, 1, 1));
        assertThrows(BusinessRuleException.class, v::verifierCoherence);
        v.setAnneeFabrication(LocalDate.now().getYear() + 1);
        v.setDatePremiereMiseEnCirculation(LocalDate.now());
        assertThrows(BusinessRuleException.class, v::verifierCoherence);
        assertTrue(true);
    }
}
