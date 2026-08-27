package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates and verifies testimonios of a signed escritura (CU07, CU08).
 */
@Service
@Transactional
public class TestimonioGeneracionVerificacionService {

    private static final Logger log = LoggerFactory.getLogger(TestimonioGeneracionVerificacionService.class);

    private final EscrituraRepository escrituraRepository;
    private final TestimonioRepository testimonioRepository;

    public TestimonioGeneracionVerificacionService(EscrituraRepository escrituraRepository,
            TestimonioRepository testimonioRepository) {
        this.escrituraRepository = escrituraRepository;
        this.testimonioRepository = testimonioRepository;
    }

    /**
     * Generates a testimonio from a signed escritura. The testimonio number is system-assigned.
     *
     * @param idEscritura the escritura ID
     * @return the generated testimonio
     * @throws ResourceNotFoundException if no escritura with the given ID exists
     * @throws BusinessValidationException if the escritura is not "Firmada"
     */
    public Testimonio generar(Integer idEscritura) {
        Escritura escritura = escrituraRepository.findById(idEscritura)
                .orElseThrow(() -> new ResourceNotFoundException("Escritura no encontrada con ID: " + idEscritura));

        if (!ConstantesNegocio.ESCRITURA_FIRMADA.equals(escritura.getEstado())) {
            throw new BusinessValidationException(
                    "La escritura debe estar en estado 'Firmada' para generar su testimonio");
        }

        Testimonio testimonio = new Testimonio();
        testimonio.setNumero((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        testimonio.setFkIdEscritura(escritura);
        testimonio.setVerificado(false);
        testimonio.setObservado(false);

        log.info("Generando testimonio para escritura id: {}", idEscritura);
        return testimonioRepository.save(testimonio);
    }

    /**
     * Verifies a testimonio, recording whether it was observed and why.
     *
     * @param idTestimonio the testimonio ID
     * @param observado whether the testimonio was found to have observations
     * @param observaciones the reason, when observado is true
     * @return the verified testimonio
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     */
    public Testimonio verificar(Integer idTestimonio, boolean observado, String observaciones) {
        Testimonio testimonio = testimonioRepository.findById(idTestimonio)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimonio));

        testimonio.setVerificado(true);
        testimonio.setObservado(observado);
        testimonio.setObservaciones(observaciones);

        log.info("Verificando testimonio id: {}, observado: {}", idTestimonio, observado);
        return testimonioRepository.save(testimonio);
    }
}
