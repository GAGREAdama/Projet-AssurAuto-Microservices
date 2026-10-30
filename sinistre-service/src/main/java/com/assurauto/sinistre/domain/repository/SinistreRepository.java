package com.assurauto.sinistre.domain.repository;

import com.assurauto.sinistre.domain.model.Sinistre;
import com.assurauto.sinistre.domain.valueobject.StatutSinistre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    List<Sinistre> findByContratId(Long contratId);

    List<Sinistre> findByVehiculeId(Long vehiculeId);

    List<Sinistre> findByStatut(StatutSinistre statut);
}
