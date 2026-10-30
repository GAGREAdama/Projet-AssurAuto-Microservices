package com.assurauto.contrat.application.mapper;

import com.assurauto.contrat.api.dto.ContratDto;
import com.assurauto.contrat.api.dto.ContratDto.*;
import com.assurauto.contrat.domain.model.Contrat;
import com.assurauto.contrat.domain.valueobject.*;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class ContratMapper {

    private ContratMapper() {
    }

    public static ContratDto toDto(Contrat c) {
        return new ContratDto(c.getId(), c.getNumeroPolice(), c.getClientId(), c.getTypeContrat(), c.getDateDebut(),
                c.getDateFin(), c.isRenouvellementAutomatique(), c.getStatut(), c.getPrimeAnnuelle(),
                c.getGaranties().stream().map(g -> new GarantieDto(g.getType(), g.getPlafond())).toList(),
                c.getFranchises().stream().map(f -> new FranchiseDto(f.getGarantie(), f.getMontant())).toList(),
                new TreeSet<>(c.getOptions()), c.getDateCreation());
    }

    public static List<Garantie> garanties(ContratDto d) {
        return d.garanties().stream().map(g -> new Garantie(g.type(), g.plafond())).toList();
    }

    public static List<Franchise> franchises(ContratDto d) {
        if (d.franchises() == null) {
            return List.of();
        }
        return d.franchises().stream().map(f -> new Franchise(f.garantie(), f.montant())).toList();
    }

    public static Set<OptionSupplementaire> options(ContratDto d) {
        return d.options() == null ? Set.of() : d.options();
    }
}
