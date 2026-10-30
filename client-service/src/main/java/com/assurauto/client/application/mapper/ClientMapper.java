package com.assurauto.client.application.mapper;

import com.assurauto.client.api.dto.ClientDto;
import com.assurauto.client.api.dto.ClientDto.*;
import com.assurauto.client.domain.model.Client;
import com.assurauto.client.domain.valueobject.*;
import java.util.List;

public final class ClientMapper {

    private ClientMapper() {
    }

    /** @param complet false => le numéro de pièce d'identité est masqué (protection des données personnelles). */
    public static ClientDto toDto(Client c, boolean complet) {
        return new ClientDto(c.getId(), c.getNom(), c.getPrenom(), c.getDateNaissance(), c.getGenre(),
                new AdresseDto(c.getAdresse().getRue(), c.getAdresse().getVille(), c.getAdresse().getPays()),
                c.getEmail(), List.copyOf(c.getTelephones()),
                complet ? c.getNumeroPieceIdentite() : masquer(c.getNumeroPieceIdentite()),
                new PermisDto(c.getPermisConduire().getNumero(), c.getPermisConduire().getCategorie(),
                        c.getPermisConduire().getDateObtention()),
                c.getSituationFamiliale(), c.getProfession(), c.getCategorie(), c.getScoreFidelite(),
                c.getHistoriqueCommercial().stream().map(ClientMapper::toDto).toList());
    }

    public static EvenementDto toDto(EvenementCommercial e) {
        return new EvenementDto(e.getDateEvenement(), e.getType(), e.getDescription(), e.getPoints());
    }

    public static void apply(ClientDto d, Client c) {
        c.setNom(d.nom().trim());
        c.setPrenom(d.prenom().trim());
        c.setDateNaissance(d.dateNaissance());
        c.setGenre(d.genre());
        c.setAdresse(new Adresse(d.adresse().rue(), d.adresse().ville(), d.adresse().pays()));
        c.setEmail(d.email().trim().toLowerCase());
        c.getTelephones().clear();
        c.getTelephones().addAll(d.telephones());
        c.setNumeroPieceIdentite(d.numeroPieceIdentite().trim());
        c.setPermisConduire(new PermisConduire(d.permisConduire().numero(), d.permisConduire().categorie(),
                d.permisConduire().dateObtention()));
        c.setSituationFamiliale(d.situationFamiliale());
        c.setProfession(d.profession().trim());
    }

    static String masquer(String valeur) {
        if (valeur == null || valeur.length() <= 3) {
            return "***";
        }
        return "*".repeat(valeur.length() - 3) + valeur.substring(valeur.length() - 3);
    }
}
