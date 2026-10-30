package com.assurauto.client.api.dto;

import com.assurauto.client.domain.valueobject.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Représentation d'un client (entrée et sortie). Les champs en lecture seule sont ignorés en entrée. */
public record ClientDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotNull @Past LocalDate dateNaissance,
        @NotNull Genre genre,
        @NotNull @Valid AdresseDto adresse,
        @NotBlank @Email String email,
        @NotEmpty List<@NotBlank @Pattern(regexp = "^\\+?[0-9 ]{8,20}$") String> telephones,
        @NotBlank @Size(max = 50) String numeroPieceIdentite,
        @NotNull @Valid PermisDto permisConduire,
        @NotNull SituationFamiliale situationFamiliale,
        @NotBlank String profession,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) CategorieClient categorie,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Integer scoreFidelite,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) List<EvenementDto> historiqueCommercial) {

    public record AdresseDto(@NotBlank String rue, @NotBlank String ville, @NotBlank String pays) {
    }

    public record PermisDto(@NotBlank String numero, @NotBlank @Size(max = 10) String categorie,
                            @NotNull @PastOrPresent LocalDate dateObtention) {
    }

    public record EvenementDto(LocalDateTime date, TypeEvenement type, String description, int points) {
    }
}
