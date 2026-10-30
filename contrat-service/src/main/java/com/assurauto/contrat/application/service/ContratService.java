package com.assurauto.contrat.application.service;

import com.assurauto.contrat.api.dto.ContratDto;
import com.assurauto.contrat.application.exception.ResourceNotFoundException;
import com.assurauto.contrat.application.mapper.ContratMapper;
import com.assurauto.contrat.domain.exception.BusinessRuleException;
import com.assurauto.contrat.domain.model.Contrat;
import com.assurauto.contrat.domain.repository.ContratRepository;
import com.assurauto.contrat.domain.valueobject.StatutContrat;
import com.assurauto.contrat.infrastructure.client.ClientGateway;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratService {

    private final ContratRepository repository;
    private final ClientGateway clientGateway;

    public ContratService(ContratRepository repository, ClientGateway clientGateway) {
        this.repository = repository;
        this.clientGateway = clientGateway;
    }

    // ---------- Commandes ----------
    @Transactional
    public ContratDto creer(ContratDto dto) {
        // Vérification inter-services : le client doit exister dans client-service
        if (!clientGateway.clientExiste(dto.clientId())) {
            throw new BusinessRuleException("Le client " + dto.clientId() + " n'existe pas");
        }
        Contrat contrat = new Contrat();
        contrat.setClientId(dto.clientId());
        appliquer(contrat, dto);
        Contrat sauve = repository.save(contrat);
        clientGateway.enregistrerEvenement(sauve.getClientId(), "SOUSCRIPTION",
                "Souscription du contrat " + sauve.getNumeroPolice());
        return ContratMapper.toDto(sauve);
    }

    @Transactional
    public ContratDto modifier(Long id, ContratDto dto) {
        Contrat contrat = charger(id);
        if (!contrat.getClientId().equals(dto.clientId())) {
            throw new BusinessRuleException("Le client d'un contrat ne peut pas être modifié");
        }
        appliquer(contrat, dto);
        return ContratMapper.toDto(repository.save(contrat));
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(charger(id));
    }

    @Transactional
    public ContratDto suspendre(Long id) {
        Contrat c = charger(id);
        c.suspendre();
        return ContratMapper.toDto(repository.save(c));
    }

    @Transactional
    public ContratDto reactiver(Long id) {
        Contrat c = charger(id);
        c.reactiver();
        return ContratMapper.toDto(repository.save(c));
    }

    @Transactional
    public ContratDto resilier(Long id) {
        Contrat c = charger(id);
        c.resilier();
        return ContratMapper.toDto(repository.save(c));
    }

    @Transactional
    public ContratDto renouveler(Long id, LocalDate nouvelleDateFin) {
        Contrat c = charger(id);
        c.renouveler(nouvelleDateFin);
        Contrat sauve = repository.save(c);
        clientGateway.enregistrerEvenement(sauve.getClientId(), "RENOUVELLEMENT",
                "Renouvellement du contrat " + sauve.getNumeroPolice());
        return ContratMapper.toDto(sauve);
    }

    // ---------- Requêtes ----------
    @Transactional(readOnly = true)
    public ContratDto trouver(Long id) {
        return ContratMapper.toDto(charger(id));
    }

    @Transactional(readOnly = true)
    public List<ContratDto> rechercher(Long clientId, StatutContrat statut) {
        List<Contrat> resultat;
        if (clientId != null && statut != null) {
            resultat = repository.findByClientIdAndStatut(clientId, statut);
        } else if (clientId != null) {
            resultat = repository.findByClientId(clientId);
        } else if (statut != null) {
            resultat = repository.findByStatut(statut);
        } else {
            resultat = repository.findAll();
        }
        return resultat.stream().map(ContratMapper::toDto).toList();
    }

    private void appliquer(Contrat contrat, ContratDto dto) {
        contrat.modifierConditions(dto.typeContrat(), dto.dateDebut(), dto.dateFin(), dto.renouvellementAutomatique(),
                dto.primeAnnuelle());
        contrat.definirCouverture(ContratMapper.garanties(dto), ContratMapper.franchises(dto), ContratMapper.options(dto));
    }

    private Contrat charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat " + id + " introuvable"));
    }
}
