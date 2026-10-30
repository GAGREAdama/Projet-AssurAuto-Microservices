package com.assurauto.sinistre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assurauto.sinistre.api.dto.SinistreDto;
import com.assurauto.sinistre.application.service.SinistreService;
import com.assurauto.sinistre.domain.exception.BusinessRuleException;
import com.assurauto.sinistre.domain.model.Sinistre;
import com.assurauto.sinistre.domain.repository.SinistreRepository;
import com.assurauto.sinistre.domain.valueobject.StatutSinistre;
import com.assurauto.sinistre.domain.valueobject.TypeIncident;
import com.assurauto.sinistre.infrastructure.client.ClientGateway;
import com.assurauto.sinistre.infrastructure.client.ContratGateway;
import com.assurauto.sinistre.infrastructure.client.ContratGateway.ContratView;
import com.assurauto.sinistre.infrastructure.client.VehiculeGateway;
import com.assurauto.sinistre.infrastructure.client.VehiculeGateway.VehiculeView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SinistreServiceTest {

    @Mock
    SinistreRepository repository;
    @Mock
    ContratGateway contratGateway;
    @Mock
    VehiculeGateway vehiculeGateway;
    @Mock
    ClientGateway clientGateway;
    @InjectMocks
    SinistreService service;

    private SinistreDto dto(LocalDate date) {
        return new SinistreDto(null, 5L, 9L, date, null, "Collision au carrefour", TypeIncident.COLLISION,
                "Ouagadougou", null, new BigDecimal("2000"), null, null);
    }

    private ContratView contratActif() {
        return new ContratView(5L, 1L, "EN_COURS", LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(9));
    }

    @Test
    void refuseUnSinistreHorsPeriodeDeCouverture() {
        when(contratGateway.trouver(5L)).thenReturn(Optional.of(contratActif()));
        assertThrows(BusinessRuleException.class, () -> service.declarer(dto(LocalDate.now().minusYears(2))));
        verify(repository, never()).save(any());
    }

    @Test
    void refuseUnContratSuspendu() {
        when(contratGateway.trouver(5L)).thenReturn(Optional.of(
                new ContratView(5L, 1L, "SUSPENDU", LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(9))));
        assertThrows(BusinessRuleException.class, () -> service.declarer(dto(LocalDate.now())));
    }

    @Test
    void refuseUnVehiculeQuiNAppartientPasAuContrat() {
        when(contratGateway.trouver(5L)).thenReturn(Optional.of(contratActif()));
        when(vehiculeGateway.trouver(9L)).thenReturn(Optional.of(new VehiculeView(9L, 77L)));
        assertThrows(BusinessRuleException.class, () -> service.declarer(dto(LocalDate.now())));
    }

    @Test
    void declareUnSinistreValideEtNotifieLeClient() {
        when(contratGateway.trouver(5L)).thenReturn(Optional.of(contratActif()));
        when(vehiculeGateway.trouver(9L)).thenReturn(Optional.of(new VehiculeView(9L, 5L)));
        when(repository.save(any(Sinistre.class))).thenAnswer(i -> i.getArgument(0));
        SinistreDto cree = service.declarer(dto(LocalDate.now()));
        assertEquals(StatutSinistre.DECLARE, cree.statut());
        assertEquals(LocalDate.now(), cree.dateDeclaration());
        verify(clientGateway).enregistrerEvenement(any(), any(), any());
    }
}
