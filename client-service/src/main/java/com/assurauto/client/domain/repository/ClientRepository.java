package com.assurauto.client.domain.repository;

import com.assurauto.client.domain.model.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByNumeroPieceIdentite(String numeroPieceIdentite);
}
