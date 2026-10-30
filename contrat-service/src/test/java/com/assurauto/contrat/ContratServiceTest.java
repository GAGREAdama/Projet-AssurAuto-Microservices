package com.assurauto.contrat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assurauto.contrat.api.dto.ContratDto;
import com.assurauto.contrat.api.dto.ContratDto.GarantieDto;
import com.assurauto.contrat.application.exception.ServiceUnavailableException;
import com.assurauto.contrat.application.service.ContratService;
import com.assurauto.contrat.domain.exception.BusinessRuleException;
import com.assurauto.contrat.domain.model.Contrat;
import com.assurauto.contrat.domain.repository.ContratRepository;
import com.assurauto.contrat.domain.valueobject.*;
import com.assurauto.contrat.infrastructure.client.ClientGateway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContratServiceTest {

    @Mock
    ContratRepository repository;
    @Mock
    ClientGateway clientGateway;
    @InjectMocks
    ContratService service;

    private ContratDto dto() {
        return new ContratDto(null, null, 7L, TypeContrat.RESPONSABILITE_CIVILE, LocalDate.now(),
                LocalDate.now().plusYears(1), true, null, new BigDecimal("250"),
                List.of(new GarantieDto(TypeGarantie.RESPONSABILITE_CIVILE, new BigDecimal("500000"))), List.of(),
                Set.of(OptionSupplementaire.ASSISTANCE), null);
    }

    @Test
    void refuseUnContratPourUnClientInexistant() {
        when(clientGateway.clientExiste(7L)).thenReturn(false);
        assertThrows(BusinessRuleException.class, () -> service.creer(dto()));
        verify(repository, never()).save(any());
    }

    @Test
    void propageLIndisponibiliteDeClientService() {
        when(clientGateway.clientExiste(7L)).thenThrow(new ServiceUnavailableException("client-service injoignable"));
        assertThrows(ServiceUnavailableException.class, () -> service.creer(dto()));
    }

    @Test
    void creeUnContratEtNotifieLHistoriqueClient() {
        when(clientGateway.clientExiste(7L)).thenReturn(true);
        when(repository.save(any(Contrat.class))).thenAnswer(i -> i.getArgument(0));
        ContratDto cree = service.creer(dto());
        assertEquals(StatutContrat.EN_COURS, cree.statut());
        assertEquals(7L, cree.clientId());
        verify(clientGateway).enregistrerEvenement(any(), any(), any());
    }
}
