package com.assurauto.sinistre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.assurauto.sinistre.domain.exception.BusinessRuleException;
import com.assurauto.sinistre.domain.model.Sinistre;
import com.assurauto.sinistre.domain.valueobject.StatutSinistre;
import com.assurauto.sinistre.domain.valueobject.TypeIncident;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SinistreDomainTest {

    private Sinistre sinistre() {
        return Sinistre.declarer(1L, 2L, LocalDate.now(), "Choc arriere", TypeIncident.COLLISION, "Ouagadougou",
                new BigDecimal("1500"));
    }

    @Test
    void workflowCompletJusquALaCloture() {
        Sinistre s = sinistre();
        assertEquals(StatutSinistre.DECLARE, s.getStatut());
        s.changerStatut(StatutSinistre.EN_EXPERTISE, "Expert mandate", null);
        s.changerStatut(StatutSinistre.ACCEPTE, "Garantie acquise", null);
        s.changerStatut(StatutSinistre.INDEMNISE, "Paiement", new BigDecimal("1200"));
        s.changerStatut(StatutSinistre.CLOTURE, "Termine", null);
        assertEquals(new BigDecimal("1200"), s.getMontantIndemnisation());
        assertEquals(5, s.getHistorique().size());
        assertFalse(s.supprimable());
    }

    @Test
    void refuseLesTransitionsInterdites() {
        Sinistre s = sinistre();
        assertThrows(BusinessRuleException.class, () -> s.changerStatut(StatutSinistre.INDEMNISE, "x", BigDecimal.TEN));
        assertThrows(BusinessRuleException.class, () -> s.changerStatut(StatutSinistre.CLOTURE, "x", null));
        assertTrue(s.supprimable());
    }

    @Test
    void indemnisationExigeUnMontantPositif() {
        Sinistre s = sinistre();
        s.changerStatut(StatutSinistre.EN_EXPERTISE, null, null);
        s.changerStatut(StatutSinistre.ACCEPTE, null, null);
        assertThrows(BusinessRuleException.class, () -> s.changerStatut(StatutSinistre.INDEMNISE, null, null));
        assertThrows(BusinessRuleException.class, () -> s.changerStatut(StatutSinistre.INDEMNISE, null, BigDecimal.ZERO));
    }

    @Test
    void modificationInterditeApresDecision() {
        Sinistre s = sinistre();
        s.changerStatut(StatutSinistre.REFUSE, "Hors garantie", null);
        assertThrows(BusinessRuleException.class, () -> s.modifier(LocalDate.now(), "x", TypeIncident.VOL, "y", BigDecimal.ONE));
    }
}
