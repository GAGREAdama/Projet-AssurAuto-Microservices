package com.assurauto.sinistre.api.controller;

import com.assurauto.sinistre.api.dto.ChangementStatutRequest;
import com.assurauto.sinistre.api.dto.SinistreDto;
import com.assurauto.sinistre.api.dto.SinistreDto.SuiviDto;
import com.assurauto.sinistre.application.service.SinistreService;
import com.assurauto.sinistre.domain.valueobject.StatutSinistre;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/sinistres")
@Tag(name = "Sinistres", description = "Déclaration et suivi des sinistres")
public class SinistreController {

    private final SinistreService service;

    public SinistreController(SinistreService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Déclarer un sinistre (vérifie contrat et véhicule via contrat-service / vehicule-service)")
    public ResponseEntity<SinistreDto> declarer(@Valid @RequestBody SinistreDto dto) {
        SinistreDto cree = service.declarer(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @GetMapping
    @Operation(summary = "Rechercher les sinistres par contrat, véhicule ou statut")
    public List<SinistreDto> rechercher(@RequestParam(required = false) Long contratId,
                                        @RequestParam(required = false) Long vehiculeId,
                                        @RequestParam(required = false) StatutSinistre statut) {
        return service.rechercher(contratId, vehiculeId, statut);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un sinistre")
    public SinistreDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un sinistre (tant qu'il est DECLARE ou EN_EXPERTISE)")
    public SinistreDto modifier(@PathVariable Long id, @Valid @RequestBody SinistreDto dto) {
        return service.modifier(id, dto);
    }

    @PostMapping("/{id}/statut")
    @Operation(summary = "Suivre le traitement : changer le statut (DECLARE > EN_EXPERTISE > ACCEPTE > INDEMNISE > CLOTURE)")
    public SinistreDto changerStatut(@PathVariable Long id, @Valid @RequestBody ChangementStatutRequest request) {
        return service.changerStatut(id, request);
    }

    @GetMapping("/{id}/historique")
    @Operation(summary = "Historique de traitement d'un sinistre")
    public List<SuiviDto> historique(@PathVariable Long id) {
        return service.historique(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un sinistre DECLARE (ADMIN)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
