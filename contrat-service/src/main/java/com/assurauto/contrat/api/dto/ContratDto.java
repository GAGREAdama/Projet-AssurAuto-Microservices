package com.assurauto.contrat.api.dto;

import com.assurauto.contrat.domain.valueobject.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ContratDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) String numeroPolice,
        @NotNull Long clientId,
        @NotNull TypeContrat typeContrat,
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,
        boolean renouvellementAutomatique,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) StatutContrat statut,
        @DecimalMin("0.0") BigDecimal primeAnnuelle,
        @NotEmpty List<@Valid GarantieDto> garanties,
        List<@Valid FranchiseDto> franchises,
        Set<OptionSupplementaire> options,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) LocalDateTime dateCreation) {

    public record GarantieDto(@NotNull TypeGarantie type, @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal plafond) {
    }

    public record FranchiseDto(@NotNull TypeGarantie garantie, @NotNull @DecimalMin("0.0") BigDecimal montant) {
    }
}
