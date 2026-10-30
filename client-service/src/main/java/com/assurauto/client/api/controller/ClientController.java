package com.assurauto.client.api.controller;

import com.assurauto.client.api.dto.ClientDto;
import com.assurauto.client.api.dto.ClientDto.EvenementDto;
import com.assurauto.client.api.dto.EvenementRequest;
import com.assurauto.client.application.service.ClientService;
import com.assurauto.client.domain.valueobject.CategorieClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Gestion des assurés")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Créer un client")
    public ResponseEntity<ClientDto> creer(@Valid @RequestBody ClientDto dto, Authentication auth) {
        ClientDto cree = service.creer(dto, admin(auth));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @GetMapping
    @Operation(summary = "Rechercher les clients (nom/prénom, ville, catégorie)")
    public List<ClientDto> rechercher(@RequestParam(required = false) String nom,
                                      @RequestParam(required = false) String ville,
                                      @RequestParam(required = false) CategorieClient categorie,
                                      Authentication auth) {
        return service.rechercher(nom, ville, categorie, admin(auth));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un client")
    public ClientDto trouver(@PathVariable Long id, Authentication auth) {
        return service.trouver(id, admin(auth));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client")
    public ClientDto modifier(@PathVariable Long id, @Valid @RequestBody ClientDto dto, Authentication auth) {
        return service.modifier(id, dto, admin(auth));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un client (ADMIN)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historique")
    @Operation(summary = "Historique commercial d'un client")
    public List<EvenementDto> historique(@PathVariable Long id) {
        return service.historique(id);
    }

    @PostMapping("/{id}/historique")
    @Operation(summary = "Ajouter un événement commercial (met à jour score de fidélité et catégorie)")
    public ClientDto ajouterEvenement(@PathVariable Long id, @Valid @RequestBody EvenementRequest request,
                                      Authentication auth) {
        return service.ajouterEvenement(id, request, admin(auth));
    }

    /** Seul un ADMIN voit le numéro de pièce d'identité en clair. */
    private boolean admin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
