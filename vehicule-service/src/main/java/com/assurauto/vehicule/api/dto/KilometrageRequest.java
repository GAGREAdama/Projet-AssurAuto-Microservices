package com.assurauto.vehicule.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record KilometrageRequest(@NotNull @Min(0) Integer kilometrage) {
}
