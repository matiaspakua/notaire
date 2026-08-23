package com.licensis.notaire.service.mappers;

import com.licensis.notaire.dto.DtoPagoResponse;
import com.licensis.notaire.negocio.Pago;

public final class PagoMapper {

    private PagoMapper() {}

    public static DtoPagoResponse toDto(Pago pago) {
        return new DtoPagoResponse(
                pago.getIdPago(),
                pago.getPresupuesto() != null ? pago.getPresupuesto().getIdPresupuesto() : null,
                pago.getMonto(),
                pago.getFecha(),
                pago.getMetodoPago(),
                pago.getObservaciones()
        );
    }
}
