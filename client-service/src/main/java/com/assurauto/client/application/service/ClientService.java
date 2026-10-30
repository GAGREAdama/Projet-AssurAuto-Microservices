package com.assurauto.client.application.service;

import com.assurauto.client.api.dto.ClientDto;
import com.assurauto.client.api.dto.ClientDto.EvenementDto;
import com.assurauto.client.api.dto.EvenementRequest;
import com.assurauto.client.application.exception.DuplicateResourceException;
import com.assurauto.client.application.exception.ResourceNotFoundException;
import com.assurauto.client.application.mapper.ClientMapper;
import com.assurauto.client.domain.exception.BusinessRuleException;
import com.assurauto.client.domain.model.Client;
import com.assurauto.client.domain.repository.ClientRepository;
import com.assurauto.client.domain.valueobject.CategorieClient;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service applicatif : commandes (créer/modifier/supprimer) et requêtes (consulter/rechercher) séparées (CQRS léger). */
@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    // ---------- Commandes ----------
    @Transactional
    public ClientDto creer(ClientDto dto, boolean complet) {
        verifierRegles(dto);
        String email = dto.email().trim().toLowerCase();
        if (repository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Un client avec l'email " + email + " existe déjà");
        }
        if (repository.findByNumeroPieceIdentite(dto.numeroPieceIdentite().trim()).isPresent()) {
            throw new DuplicateResourceException("Ce numéro de pièce d'identité est déjà enregistré");
        }
        Client client = new Client();
        ClientMapper.apply(dto, client);
        return ClientMapper.toDto(repository.save(client), complet);
    }

    @Transactional
    public ClientDto modifier(Long id, ClientDto dto, boolean complet) {
        Client client = charger(id);
        verifierRegles(dto);
        String email = dto.email().trim().toLowerCase();
        repository.findByEmail(email).filter(c -> !c.getId().equals(id)).ifPresent(c -> {
            throw new DuplicateResourceException("Un autre client utilise déjà l'email " + email);
        });
        repository.findByNumeroPieceIdentite(dto.numeroPieceIdentite().trim()).filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Ce numéro de pièce d'identité appartient à un autre client");
                });
        ClientMapper.apply(dto, client);
        return ClientMapper.toDto(repository.save(client), complet);
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(charger(id));
    }

    @Transactional
    public ClientDto ajouterEvenement(Long id, EvenementRequest request, boolean complet) {
        Client client = charger(id);
        client.ajouterEvenement(request.type(), request.description());
        return ClientMapper.toDto(repository.save(client), complet);
    }

    // ---------- Requêtes ----------
    @Transactional(readOnly = true)
    public ClientDto trouver(Long id, boolean complet) {
        return ClientMapper.toDto(charger(id), complet);
    }

    @Transactional(readOnly = true)
    public List<ClientDto> rechercher(String nom, String ville, CategorieClient categorie, boolean complet) {
        Specification<Client> spec = (root, query, cb) -> cb.conjunction();
        if (nom != null && !nom.isBlank()) {
            String motif = "%" + nom.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.<String>get("nom")), motif),
                    cb.like(cb.lower(root.<String>get("prenom")), motif)));
        }
        if (ville != null && !ville.isBlank()) {
            String v = ville.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("adresse").<String>get("ville")), v));
        }
        if (categorie != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categorie"), categorie));
        }
        return repository.findAll(spec).stream().map(c -> ClientMapper.toDto(c, complet)).toList();
    }

    @Transactional(readOnly = true)
    public List<EvenementDto> historique(Long id) {
        return charger(id).getHistoriqueCommercial().stream().map(ClientMapper::toDto).toList();
    }

    // ---------- Règles métier ----------
    private void verifierRegles(ClientDto dto) {
        LocalDate aujourdhui = LocalDate.now();
        if (Period.between(dto.dateNaissance(), aujourdhui).getYears() < 18) {
            throw new BusinessRuleException("Le client doit être majeur (18 ans minimum)");
        }
        if (dto.permisConduire().dateObtention().isBefore(dto.dateNaissance().plusYears(18))) {
            throw new BusinessRuleException("La date d'obtention du permis est antérieure aux 18 ans du client");
        }
    }

    private Client charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client " + id + " introuvable"));
    }
}
