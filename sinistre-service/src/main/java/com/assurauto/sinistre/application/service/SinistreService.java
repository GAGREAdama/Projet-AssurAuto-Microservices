package com.assurauto.sinistre.application.service;

import com.assurauto.sinistre.api.dto.ChangementStatutRequest;
import com.assurauto.sinistre.api.dto.SinistreDto;
import com.assurauto.sinistre.api.dto.SinistreDto.SuiviDto;
import com.assurauto.sinistre.application.exception.ResourceNotFoundException;
import com.assurauto.sinistre.application.mapper.SinistreMapper;
import com.assurauto.sinistre.domain.exception.BusinessRuleException;
import com.assurauto.sinistre.domain.model.Sinistre;
import com.assurauto.sinistre.domain.repository.SinistreRepository;
import com.assurauto.sinistre.domain.valueobject.StatutSinistre;
import com.assurauto.sinistre.infrastructure.client.ClientGateway;
import com.assurauto.sinistre.infrastructure.client.ContratGateway;
import com.assurauto.sinistre.infrastructure.client.ContratGateway.ContratView;
import com.assurauto.sinistre.infrastructure.client.VehiculeGateway;
import com.assurauto.sinistre.infrastructure.client.VehiculeGateway.VehiculeView;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SinistreService {

    private final SinistreRepository repository;
    private final ContratGateway contratGateway;
    private final VehiculeGateway vehiculeGateway;
    private final ClientGateway clientGateway;

    public SinistreService(SinistreRepository repository, ContratGateway contratGateway,
                           VehiculeGateway vehiculeGateway, ClientGateway clientGateway) {
        this.repository = repository;
        this.contratGateway = contratGateway;
        this.vehiculeGateway = vehiculeGateway;
        this.clientGateway = clientGateway;
    }

    // ---------- Commandes ----------
    @Transactional
    public SinistreDto declarer(SinistreDto dto) {
        ContratView contrat = verifierReferences(dto, true);
        Sinistre sinistre = Sinistre.declarer(dto.contratId(), dto.vehiculeId(), dto.dateSinistre(), dto.description(),
                dto.typeIncident(), dto.lieu(), dto.montantEstime());
        Sinistre sauve = repository.save(sinistre);
        clientGateway.enregistrerEvenement(contrat.clientId(), "SINISTRE", "Sinistre déclaré sur le contrat " + contrat.id());
        return SinistreMapper.toDto(sauve);
    }

    @Transactional
    public SinistreDto modifier(Long id, SinistreDto dto) {
        Sinistre sinistre = charger(id);
        if (!sinistre.getContratId().equals(dto.contratId()) || !sinistre.getVehiculeId().equals(dto.vehiculeId())) {
            throw new BusinessRuleException("Le contrat et le véhicule d'un sinistre ne peuvent pas être modifiés");
        }
        verifierReferences(dto, false);
        sinistre.modifier(dto.dateSinistre(), dto.description(), dto.typeIncident(), dto.lieu(), dto.montantEstime());
        return SinistreMapper.toDto(repository.save(sinistre));
    }

    @Transactional
    public SinistreDto changerStatut(Long id, ChangementStatutRequest request) {
        Sinistre sinistre = charger(id);
        sinistre.changerStatut(request.statut(), request.commentaire(), request.montantIndemnisation());
        return SinistreMapper.toDto(repository.save(sinistre));
    }

    @Transactional
    public void supprimer(Long id) {
        Sinistre sinistre = charger(id);
        if (!sinistre.supprimable()) {
            throw new BusinessRuleException("Seul un sinistre au statut DECLARE peut être supprimé");
        }
        repository.delete(sinistre);
    }

    // ---------- Requêtes ----------
    @Transactional(readOnly = true)
    public SinistreDto trouver(Long id) {
        return SinistreMapper.toDto(charger(id));
    }

    @Transactional(readOnly = true)
    public List<SuiviDto> historique(Long id) {
        return charger(id).getHistorique().stream().map(SinistreMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SinistreDto> rechercher(Long contratId, Long vehiculeId, StatutSinistre statut) {
        List<Sinistre> resultat;
        if (contratId != null) {
            resultat = repository.findByContratId(contratId);
        } else if (vehiculeId != null) {
            resultat = repository.findByVehiculeId(vehiculeId);
        } else if (statut != null) {
            resultat = repository.findByStatut(statut);
        } else {
            resultat = repository.findAll();
        }
        return resultat.stream()
                .filter(s -> vehiculeId == null || s.getVehiculeId().equals(vehiculeId))
                .filter(s -> statut == null || s.getStatut() == statut)
                .map(SinistreMapper::toDto).toList();
    }

    /** Vérifications inter-services : contrat, période de couverture et appartenance du véhicule au contrat. */
    private ContratView verifierReferences(SinistreDto dto, boolean nouveau) {
        ContratView contrat = contratGateway.trouver(dto.contratId())
                .orElseThrow(() -> new BusinessRuleException("Le contrat " + dto.contratId() + " n'existe pas"));
        if (nouveau && !"EN_COURS".equals(contrat.statut())) {
            throw new BusinessRuleException("Le contrat " + contrat.id() + " n'est pas EN_COURS (statut : " + contrat.statut() + ")");
        }
        if (dto.dateSinistre().isBefore(contrat.dateDebut()) || dto.dateSinistre().isAfter(contrat.dateFin())) {
            throw new BusinessRuleException("La date du sinistre est hors de la période de couverture du contrat");
        }
        VehiculeView vehicule = vehiculeGateway.trouver(dto.vehiculeId())
                .orElseThrow(() -> new BusinessRuleException("Le véhicule " + dto.vehiculeId() + " n'existe pas"));
        if (!dto.contratId().equals(vehicule.contratId())) {
            throw new BusinessRuleException("Le véhicule " + vehicule.id() + " n'est pas rattaché au contrat " + dto.contratId());
        }
        return contrat;
    }

    private Sinistre charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sinistre " + id + " introuvable"));
    }
}
