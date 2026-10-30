package com.assurauto.vehicule.domain.repository;

import com.assurauto.vehicule.domain.model.Vehicule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findByContratId(Long contratId);

    List<Vehicule> findByMarqueContainingIgnoreCase(String marque);

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    Optional<Vehicule> findByVin(String vin);
}
