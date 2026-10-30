package com.assurauto.vehicule.application.service;

import com.assurauto.vehicule.api.dto.VehiculeDto;
import com.assurauto.vehicule.application.exception.DuplicateResourceException;
import com.assurauto.vehicule.application.exception.ResourceNotFoundException;
import com.assurauto.vehicule.application.mapper.VehiculeMapper;
import com.assurauto.vehicule.domain.exception.BusinessRuleException;
import com.assurauto.vehicule.domain.model.Vehicule;
import com.assurauto.vehicule.domain.repository.VehiculeRepository;
import com.assurauto.vehicule.infrastructure.client.ContratGateway;
import com.assurauto.vehicule.infrastructure.client.ContratGateway.ContratView;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculeService {

    private final VehiculeRepository repository;
    private final ContratGateway contratGateway;

    public VehiculeService(VehiculeRepository repository, ContratGateway contratGateway) {
        this.repository = repository;
        this.contratGateway = contratGateway;
    }

    // ---------- Commandes ----------
    @Transactional
    public VehiculeDto creer(VehiculeDto dto) {
        verifierContrat(dto.contratId());
        String immat = dto.immatriculation().trim().toUpperCase();
        String vin = dto.vin().trim().toUpperCase();
        if (repository.findByImmatriculation(immat).isPresent()) {
            throw new DuplicateResourceException("Immatriculation déjà enregistrée : " + immat);
        }
        if (repository.findByVin(vin).isPresent()) {
            throw new DuplicateResourceException("VIN déjà enregistré : " + vin);
        }
        Vehicule vehicule = new Vehicule();
        VehiculeMapper.apply(dto, vehicule);
        vehicule.verifierCoherence();
        vehicule.mettreAJourKilometrage(dto.kilometrageDeclare());
        return VehiculeMapper.toDto(repository.save(vehicule));
    }

    @Transactional
    public VehiculeDto modifier(Long id, VehiculeDto dto) {
        Vehicule vehicule = charger(id);
        if (!vehicule.getContratId().equals(dto.contratId())) {
            verifierContrat(dto.contratId());
        }
        String immat = dto.immatriculation().trim().toUpperCase();
        String vin = dto.vin().trim().toUpperCase();
        repository.findByImmatriculation(immat).filter(v -> !v.getId().equals(id)).ifPresent(v -> {
            throw new DuplicateResourceException("Immatriculation déjà utilisée par un autre véhicule");
        });
        repository.findByVin(vin).filter(v -> !v.getId().equals(id)).ifPresent(v -> {
            throw new DuplicateResourceException("VIN déjà utilisé par un autre véhicule");
        });
        VehiculeMapper.apply(dto, vehicule);
        vehicule.verifierCoherence();
        vehicule.mettreAJourKilometrage(dto.kilometrageDeclare());
        return VehiculeMapper.toDto(repository.save(vehicule));
    }

    @Transactional
    public VehiculeDto mettreAJourKilometrage(Long id, int kilometrage) {
        Vehicule vehicule = charger(id);
        vehicule.mettreAJourKilometrage(kilometrage);
        return VehiculeMapper.toDto(repository.save(vehicule));
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(charger(id));
    }

    // ---------- Requêtes ----------
    @Transactional(readOnly = true)
    public VehiculeDto trouver(Long id) {
        return VehiculeMapper.toDto(charger(id));
    }

    /** Priorité des critères : immatriculation, puis contratId, puis marque ; sinon tout. */
    @Transactional(readOnly = true)
    public List<VehiculeDto> rechercher(Long contratId, String marque, String immatriculation) {
        List<Vehicule> resultat;
        if (immatriculation != null && !immatriculation.isBlank()) {
            resultat = repository.findByImmatriculation(immatriculation.trim().toUpperCase()).map(List::of).orElse(List.of());
        } else if (contratId != null) {
            resultat = repository.findByContratId(contratId);
        } else if (marque != null && !marque.isBlank()) {
            resultat = repository.findByMarqueContainingIgnoreCase(marque.trim());
        } else {
            resultat = repository.findAll();
        }
        return resultat.stream().map(VehiculeMapper::toDto).toList();
    }

    /** Vérification inter-services : le contrat doit exister dans contrat-service et ne pas être résilié. */
    private void verifierContrat(Long contratId) {
        ContratView contrat = contratGateway.trouver(contratId)
                .orElseThrow(() -> new BusinessRuleException("Le contrat " + contratId + " n'existe pas"));
        if ("RESILIE".equals(contrat.statut())) {
            throw new BusinessRuleException("Le contrat " + contratId + " est résilié : impossible d'y rattacher un véhicule");
        }
    }

    private Vehicule charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule " + id + " introuvable"));
    }
}
