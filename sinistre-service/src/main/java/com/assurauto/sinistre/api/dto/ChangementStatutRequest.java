package com.assurauto.sinistre.api.dto;

import com.assurauto.sinistre.domain.valueobject.StatutSinistre;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ChangementStatutRequest(@NotNull StatutSinistre statut, @Size(max = 500) String commentaire,
                                      BigDecimal montantIndemnisation) {
}
