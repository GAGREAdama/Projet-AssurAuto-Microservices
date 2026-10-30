package com.assurauto.contrat.domain.repository;

import com.assurauto.contrat.domain.model.Contrat;
import com.assurauto.contrat.domain.valueobject.StatutContrat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    List<Contrat> findByClientId(Long clientId);

    List<Contrat> findByStatut(StatutContrat statut);

    List<Contrat> findByClientIdAndStatut(Long clientId, StatutContrat statut);
}
