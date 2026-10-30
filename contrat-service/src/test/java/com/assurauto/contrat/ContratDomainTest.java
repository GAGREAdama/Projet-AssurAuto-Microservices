package com.assurauto.contrat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.assurauto.contrat.domain.exception.BusinessRuleException;
import com.assurauto.contrat.domain.model.Contrat;
import com.assurauto.contrat.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContratDomainTest {

    private Contrat contratRc() {
        Contrat c = new Contrat();
        c.setClientId(1L);
        c.modifierConditions(TypeContrat.RESPONSABILITE_CIVILE, LocalDate.now(), LocalDate.now().plusYears(1), false,
                new BigDecimal("300"));
        c.definirCouverture(List.of(new Garantie(TypeGarantie.RESPONSABILITE_CIVILE, new BigDecimal("1000000"))),
                List.of(new Franchise(TypeGarantie.RESPONSABILITE_CIVILE, new BigDecimal("50"))),
                Set.of(OptionSupplementaire.ASSISTANCE));
        return c;
    }

    @Test
    void cycleDeVieSuspensionReactivationResiliation() {
        Contrat c = contratRc();
        assertEquals(StatutContrat.EN_COURS, c.getStatut());
        c.suspendre();
        assertEquals(StatutContrat.SUSPENDU, c.getStatut());
        c.reactiver();
        c.resilier();
        assertEquals(StatutContrat.RESILIE, c.getStatut());
        assertThrows(BusinessRuleException.class, c::suspendre);
        assertThrows(BusinessRuleException.class, c::resilier);
    }

    @Test
    void laResponsabiliteCiviletEstObligatoire() {
        Contrat c = contratRc();
        assertThrows(BusinessRuleException.class, () -> c.definirCouverture(
                List.of(new Garantie(TypeGarantie.VOL, BigDecimal.TEN)), List.of(), Set.of()));
    }

    @Test
    void tousRisquesExigeDommagesCollision() {
        Contrat c = contratRc();
        c.modifierConditions(TypeContrat.TOUS_RISQUES, LocalDate.now(), LocalDate.now().plusYears(1), false, null);
        assertThrows(BusinessRuleException.class, () -> c.definirCouverture(
                List.of(new Garantie(TypeGarantie.RESPONSABILITE_CIVILE, BigDecimal.TEN)), List.of(), Set.of()));
    }

    @Test
    void refuseUneFranchiseSansGarantieEtLesPlafondsNonPositifs() {
        Contrat c = contratRc();
        assertThrows(BusinessRuleException.class, () -> c.definirCouverture(
                List.of(new Garantie(TypeGarantie.RESPONSABILITE_CIVILE, BigDecimal.TEN)),
                List.of(new Franchise(TypeGarantie.VOL, BigDecimal.ONE)), Set.of()));
        assertThrows(BusinessRuleException.class, () -> c.definirCouverture(
                List.of(new Garantie(TypeGarantie.RESPONSABILITE_CIVILE, BigDecimal.ZERO)), List.of(), Set.of()));
    }

    @Test
    void renouvellementEtCouverture() {
        Contrat c = contratRc();
        LocalDate nouvelle = LocalDate.now().plusYears(2);
        c.renouveler(nouvelle);
        assertEquals(nouvelle, c.getDateFin());
        assertTrue(c.couvre(LocalDate.now().plusMonths(3)));
        assertThrows(BusinessRuleException.class, () -> c.renouveler(LocalDate.now()));
    }

    @Test
    void datesIncoherentesRefusees() {
        Contrat c = contratRc();
        assertThrows(BusinessRuleException.class, () -> c.modifierConditions(TypeContrat.RESPONSABILITE_CIVILE,
                LocalDate.now(), LocalDate.now().minusDays(1), false, null));
    }
}
