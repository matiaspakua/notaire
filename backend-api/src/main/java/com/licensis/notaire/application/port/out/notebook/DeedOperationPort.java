package com.licensis.notaire.application.port.out.notebook;

import com.licensis.notaire.business.Deed;

/**
 * Outbound port for deed operations within notebook context.
 */
public interface DeedOperationPort {

    Deed save(Deed entity);
}
