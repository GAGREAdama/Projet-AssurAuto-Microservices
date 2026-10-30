package com.assurauto.contrat.api.controller;

import com.assurauto.contrat.api.dto.ContratDto;
import com.assurauto.contrat.api.dto.RenouvellementRequest;
import com.assurauto.contrat.application.service.ContratService;
import com.assurauto.contrat.domain.valueobject.StatutContrat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/contrats")
@Tag(name = "Contrats", description = "Gestion des contrats d'assurance automobile")
public class ContratController {

    private final ContratService service;

    public ContratController(ContratService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Créer un contrat (vérifie l'existence du client via client-service)")
    public ResponseEntity<ContratDto> creer(@Valid @RequestBody ContratDto dto) {
        ContratDto cree = service.creer(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @GetMapping
    @Operation(summary = "Lister / rechercher les contrats (clientId, statut)")
    public List<ContratDto> rechercher(@RequestParam(required = false) Long clientId,
                                       @RequestParam(required = false) StatutContrat statut) {
        return service.rechercher(clientId, statut);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un contrat")
    public ContratDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un contrat")
    public ContratDto modifier(@PathVariable Long id, @Valid @RequestBody ContratDto dto) {
        return service.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un contrat (ADMIN)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/suspendre")
    @Operation(summary = "Suspendre un contrat")
    public ContratDto suspendre(@PathVariable Long id) {
        return service.suspendre(id);
    }

    @PostMapping("/{id}/reactiver")
    @Operation(summary = "Réactiver un contrat suspendu")
    public ContratDto reactiver(@PathVariable Long id) {
        return service.reactiver(id);
    }

    @PostMapping("/{id}/resilier")
    @Operation(summary = "Résilier un contrat (définitif)")
    public ContratDto resilier(@PathVariable Long id) {
        return service.resilier(id);
    }

    @PostMapping("/{id}/renouveler")
    @Operation(summary = "Renouveler un contrat (nouvelle date de fin)")
    public ContratDto renouveler(@PathVariable Long id, @Valid @RequestBody RenouvellementRequest request) {
        return service.renouveler(id, request.nouvelleDateFin());
    }
}
