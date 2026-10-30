package com.assurauto.contrat.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RenouvellementRequest(@NotNull @Future LocalDate nouvelleDateFin) {
}
