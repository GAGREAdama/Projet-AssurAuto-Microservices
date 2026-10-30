package com.assurauto.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assurauto.client.api.dto.ClientDto;
import com.assurauto.client.api.dto.ClientDto.AdresseDto;
import com.assurauto.client.api.dto.ClientDto.PermisDto;
import com.assurauto.client.application.exception.DuplicateResourceException;
import com.assurauto.client.application.service.ClientService;
import com.assurauto.client.domain.exception.BusinessRuleException;
import com.assurauto.client.domain.model.Client;
import com.assurauto.client.domain.repository.ClientRepository;
import com.assurauto.client.domain.valueobject.Genre;
import com.assurauto.client.domain.valueobject.SituationFamiliale;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    ClientRepository repository;
    @InjectMocks
    ClientService service;

    private ClientDto dto(LocalDate naissance, LocalDate permis) {
        return new ClientDto(null, "Kabre", "Awa", naissance, Genre.FEMININ,
                new AdresseDto("12 rue du Marche", "Ouagadougou", "Burkina Faso"), "awa@mail.com",
                List.of("+22670000000"), "CNIB123456", new PermisDto("P-1", "B", permis),
                SituationFamiliale.MARIE, "Ingenieure", null, null, null);
    }

    @Test
    void refuseUnClientMineur() {
        ClientDto d = dto(LocalDate.now().minusYears(16), LocalDate.now());
        assertThrows(BusinessRuleException.class, () -> service.creer(d, true));
    }

    @Test
    void refuseUnPermisAvantLaMajorite() {
        ClientDto d = dto(LocalDate.of(1990, 5, 1), LocalDate.of(2000, 1, 1));
        assertThrows(BusinessRuleException.class, () -> service.creer(d, true));
    }

    @Test
    void refuseUnEmailDejaUtilise() {
        ClientDto d = dto(LocalDate.of(1990, 5, 1), LocalDate.of(2012, 1, 1));
        when(repository.findByEmail("awa@mail.com")).thenReturn(Optional.of(new Client()));
        assertThrows(DuplicateResourceException.class, () -> service.creer(d, true));
    }

    @Test
    void creeUnClientValideEtMasqueLaPiece() {
        ClientDto d = dto(LocalDate.of(1990, 5, 1), LocalDate.of(2012, 1, 1));
        when(repository.findByEmail(any())).thenReturn(Optional.empty());
        when(repository.findByNumeroPieceIdentite(any())).thenReturn(Optional.empty());
        when(repository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));
        ClientDto cree = service.creer(d, true);
        assertEquals("awa@mail.com", cree.email());
        assertEquals(100, cree.scoreFidelite());
        verify(repository).save(any(Client.class));
    }
}
