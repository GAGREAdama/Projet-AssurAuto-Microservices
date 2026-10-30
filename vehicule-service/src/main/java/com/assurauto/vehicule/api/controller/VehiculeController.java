package com.assurauto.vehicule.api.controller;

import com.assurauto.vehicule.api.dto.KilometrageRequest;
import com.assurauto.vehicule.api.dto.VehiculeDto;
import com.assurauto.vehicule.application.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/vehicules")
@Tag(name = "Véhicules", description = "Gestion des véhicules assurés")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Enregistrer un véhicule (vérifie le contrat via contrat-service)")
    public ResponseEntity<VehiculeDto> creer(@Valid @RequestBody VehiculeDto dto) {
        VehiculeDto cree = service.creer(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @GetMapping
    @Operation(summary = "Rechercher les véhicules (contratId, marque, immatriculation)")
    public List<VehiculeDto> rechercher(@RequestParam(required = false) Long contratId,
                                        @RequestParam(required = false) String marque,
                                        @RequestParam(required = false) String immatriculation) {
        return service.rechercher(contratId, marque, immatriculation);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un véhicule")
    public VehiculeDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un véhicule")
    public VehiculeDto modifier(@PathVariable Long id, @Valid @RequestBody VehiculeDto dto) {
        return service.modifier(id, dto);
    }

    @PatchMapping("/{id}/kilometrage")
    @Operation(summary = "Mettre à jour le kilométrage déclaré (ne peut pas diminuer)")
    public VehiculeDto kilometrage(@PathVariable Long id, @Valid @RequestBody KilometrageRequest request) {
        return service.mettreAJourKilometrage(id, request.kilometrage());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule (ADMIN)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
