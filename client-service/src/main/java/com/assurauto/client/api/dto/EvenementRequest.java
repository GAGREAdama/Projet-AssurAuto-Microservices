package com.assurauto.client.api.dto;

import com.assurauto.client.domain.valueobject.TypeEvenement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvenementRequest(@NotNull TypeEvenement type, @Size(max = 500) String description) {
}
