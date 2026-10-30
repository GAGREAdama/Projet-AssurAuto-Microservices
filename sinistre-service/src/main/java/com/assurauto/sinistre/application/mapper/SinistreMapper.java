package com.assurauto.sinistre.application.mapper;

import com.assurauto.sinistre.api.dto.SinistreDto;
import com.assurauto.sinistre.api.dto.SinistreDto.SuiviDto;
import com.assurauto.sinistre.domain.model.Sinistre;
import com.assurauto.sinistre.domain.valueobject.SuiviTraitement;

public final class SinistreMapper {

    private SinistreMapper() {
    }

    public static SinistreDto toDto(Sinistre s) {
        return new SinistreDto(s.getId(), s.getContratId(), s.getVehiculeId(), s.getDateSinistre(),
                s.getDateDeclaration(), s.getDescription(), s.getTypeIncident(), s.getLieu(), s.getStatut(),
                s.getMontantEstime(), s.getMontantIndemnisation(),
                s.getHistorique().stream().map(SinistreMapper::toDto).toList());
    }

    public static SuiviDto toDto(SuiviTraitement t) {
        return new SuiviDto(t.getStatut(), t.getDateChangement(), t.getCommentaire());
    }
}
