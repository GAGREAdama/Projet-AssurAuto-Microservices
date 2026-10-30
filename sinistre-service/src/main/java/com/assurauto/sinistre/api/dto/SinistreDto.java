package com.assurauto.sinistre.api.dto;

import com.assurauto.sinistre.domain.valueobject.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SinistreDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @NotNull Long contratId,
        @NotNull Long vehiculeId,
        @NotNull @PastOrPresent LocalDate dateSinistre,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) LocalDate dateDeclaration,
        @NotBlank @Size(max = 2000) String description,
        @NotNull TypeIncident typeIncident,
        @NotBlank String lieu,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) StatutSinistre statut,
        @NotNull @DecimalMin("0.0") BigDecimal montantEstime,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) BigDecimal montantIndemnisation,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) List<SuiviDto> historique) {

    public record SuiviDto(StatutSinistre statut, LocalDateTime date, String commentaire) {
    }
}
