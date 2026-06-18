package com.licensis.notaire.service.mappers;

import com.licensis.notaire.dto.DtoHistorialSummary;
import com.licensis.notaire.negocio.Historial;

public final class HistorialMapper {

    private HistorialMapper() {}

    public static DtoHistorialSummary toDto(Historial h) {
        return new DtoHistorialSummary(
                h.getIdHistorial(),
                h.getFecha(),
                h.getObservaciones(),
                h.getFkIdGestion() != null ? h.getFkIdGestion().getIdGestion() : null,
                h.getFkIdEstadoGestion() != null ? h.getFkIdEstadoGestion().getIdEstadoGestion() : null,
                h.getFkIdEstadoGestion() != null ? h.getFkIdEstadoGestion().getNombre() : null
        );
    }
}
