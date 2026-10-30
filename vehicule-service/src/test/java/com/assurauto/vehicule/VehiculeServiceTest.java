package com.assurauto.vehicule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assurauto.vehicule.api.dto.VehiculeDto;
import com.assurauto.vehicule.application.exception.DuplicateResourceException;
import com.assurauto.vehicule.application.service.VehiculeService;
import com.assurauto.vehicule.domain.exception.BusinessRuleException;
import com.assurauto.vehicule.domain.model.Vehicule;
import com.assurauto.vehicule.domain.repository.VehiculeRepository;
import com.assurauto.vehicule.domain.valueobject.TypeGarage;
import com.assurauto.vehicule.domain.valueobject.UsageVehicule;
import com.assurauto.vehicule.infrastructure.client.ContratGateway;
import com.assurauto.vehicule.infrastructure.client.ContratGateway.ContratView;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    @Mock
    VehiculeRepository repository;
    @Mock
    ContratGateway contratGateway;
    @InjectMocks
    VehiculeService service;

    private VehiculeDto dto() {
        return new VehiculeDto(null, 3L, "ab-123-cd", "Toyota", "Corolla", 2021, "JTDKB20U093123456",
                LocalDate.of(2021, 3, 1), 42000, "Ouagadougou", TypeGarage.PRIVE, UsageVehicule.PERSONNEL, 12000,
                true, false, Set.of("Anti-demarrage"), null);
    }

    @Test
    void refuseUnContratInexistant() {
        when(contratGateway.trouver(3L)).thenReturn(Optional.empty());
        assertThrows(BusinessRuleException.class, () -> service.creer(dto()));
        verify(repository, never()).save(any());
    }

    @Test
    void refuseUnContratResilie() {
        when(contratGateway.trouver(3L)).thenReturn(Optional.of(new ContratView(3L, 1L, "RESILIE")));
        assertThrows(BusinessRuleException.class, () -> service.creer(dto()));
    }

    @Test
    void refuseUneImmatriculationDejaConnue() {
        when(contratGateway.trouver(3L)).thenReturn(Optional.of(new ContratView(3L, 1L, "EN_COURS")));
        when(repository.findByImmatriculation("AB-123-CD")).thenReturn(Optional.of(new Vehicule()));
        assertThrows(DuplicateResourceException.class, () -> service.creer(dto()));
    }

    @Test
    void creeUnVehiculeRattacheAUnContratActif() {
        when(contratGateway.trouver(3L)).thenReturn(Optional.of(new ContratView(3L, 1L, "EN_COURS")));
        when(repository.findByImmatriculation(any())).thenReturn(Optional.empty());
        when(repository.findByVin(any())).thenReturn(Optional.empty());
        when(repository.save(any(Vehicule.class))).thenAnswer(i -> i.getArgument(0));
        VehiculeDto cree = service.creer(dto());
        assertEquals("AB-123-CD", cree.immatriculation());
        assertEquals(42000, cree.kilometrageDeclare());
        assertEquals(40, cree.scoreRisque());
    }
}
