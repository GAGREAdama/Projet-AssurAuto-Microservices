package com.assurauto.vehicule.api.dto;

import com.assurauto.vehicule.domain.valueobject.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

public record VehiculeDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @NotNull Long contratId,
        @NotBlank @Size(max = 20) String immatriculation,
        @NotBlank String marque,
        @NotBlank String modele,
        @NotNull @Min(1950) Integer anneeFabrication,
        @NotBlank @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "le VIN doit comporter 17 caractères alphanumériques (sans I, O, Q)") String vin,
        @NotNull @PastOrPresent LocalDate datePremiereMiseEnCirculation,
        @NotNull @Min(0) Integer kilometrageDeclare,
        @NotBlank String zoneStationnement,
        @NotNull TypeGarage typeGarage,
        @NotNull UsageVehicule usagePrincipal,
        @NotNull @Min(0) Integer kilometrageAnnuelEstime,
        boolean systemeAlarme,
        boolean dispositifGeolocalisation,
        Set<String> autresEquipements,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Integer scoreRisque) {
}
