package com.licensis.notaire.negocio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.client.RestClient;
import com.licensis.notaire.dto.*;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.GenericRestClient;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase controller de la capa de negocio. Versión frontend que delega en API
 * REST.
 * Métodos no migrados retornan valores por defecto para permitir compilación.
 */
public class ControllerNegocio {

    private static ControllerNegocio instancia = null;
    private static final Logger LOGGER = Logger.getLogger(ControllerNegocio.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GenericRestClient tipoIdentificacionClient;
    private GenericRestClient personaClient;
    private GenericRestClient tramiteClient;
    private GenericRestClient presupuestoClient;
    private GenericRestClient estadoGestionClient;
    private GenericRestClient gestionClient;
    private GenericRestClient usuarioClient;
    private GenericRestClient escrituraClient;
    private GenericRestClient testimonioClient;
    private GenericRestClient movimientoTestimonioClient;
    private GenericRestClient copiaClient;
    private GenericRestClient inmuebleClient;
    private GenericRestClient folioClient;
    private GenericRestClient tipoFolioClient;
    private GenericRestClient tipoTramiteClient;
    private GenericRestClient tipoDocumentoClient;
    private GenericRestClient pagoClient;
    private GenericRestClient itemClient;
    private GenericRestClient conceptoClient;
    private GenericRestClient documentoPresentadoClient;
    private GenericRestClient historialClient;

    private ControllerNegocio() {
        AdministradorJpa admin = AdministradorJpa.getInstancia();
        tipoIdentificacionClient = admin.getTipoIdentificacionJpa();
        personaClient = admin.getPersonaJpa();
        tramiteClient = admin.getTramiteJpa();
        presupuestoClient = admin.getPresupuestoJpa();
        estadoGestionClient = admin.getEstadoDeGestionJpa();
        gestionClient = new GenericRestClient("/gestiones");
        usuarioClient = admin.getUsuarioJpa();
        escrituraClient = admin.getEscrituraJpa();
        testimonioClient = admin.getTestimonioJpa();
        movimientoTestimonioClient = admin.getMovimientoTestimonioJpa();
        copiaClient = admin.getCopiaJpa();
        inmuebleClient = admin.getInmuebleJpa();
        folioClient = admin.getFolioJpa();
        tipoFolioClient = admin.getTipoDeFolioJpa();
        tipoTramiteClient = admin.getTipoDeTramiteJpa();
        tipoDocumentoClient = admin.getTipoDeDocumentoJpa();
        pagoClient = admin.getPagoJpa();
        itemClient = admin.getItemJpa();
        conceptoClient = admin.getConceptoJpa();
        documentoPresentadoClient = admin.getDocumentoPresentadoJpa();
        historialClient = admin.getHistorialJpa();
    }

    public static ControllerNegocio getInstancia() {
        if (instancia == null) {
            instancia = new ControllerNegocio();
        }
        return instancia;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private static GenericDto asGenericDto(Object value) {
        if (value instanceof GenericDto gd) {
            return gd;
        }
        if (value instanceof java.util.Map<?, ?> map) {
            GenericDto dto = new GenericDto();
            dto.setData((java.util.Map<String, Object>) map);
            return dto;
        }
        return null;
    }

    private static DtoTipoIdentificacion toDtoTipoIdentificacion(GenericDto dto) {
        DtoTipoIdentificacion result = new DtoTipoIdentificacion();
        if (dto == null) {
            return result;
        }
        result.setIdTipoIdentificacion(dto.getInt("idTipoIdentificacion"));
        result.setNombre(dto.getString("nombre"));
        return result;
    }

    private static DtoEstadoDeGestion toDtoEstado(GenericDto dto) {
        DtoEstadoDeGestion estado = new DtoEstadoDeGestion();
        if (dto == null) {
            return estado;
        }
        estado.setIdEstadoGestion(dto.getInt("idEstadoGestion"));
        estado.setNombre(dto.getString("nombre"));
        estado.setObservaciones(dto.getString("observaciones"));
        return estado;
    }

    private static DtoPersona toDtoPersona(GenericDto dto) {
        DtoPersona persona = new DtoPersona();
        if (dto == null) {
            return persona;
        }
        persona.setIdPersona(dto.getInt("idPersona"));
        persona.setNombre(dto.getString("nombre"));
        persona.setApellido(dto.getString("apellido"));
        persona.setEsCliente(Boolean.TRUE.equals(dto.getBoolean("esCliente")));
        persona.setNumeroIdentificacion(dto.getString("numeroIdentificacion"));
        persona.setTelefono(dto.getString("telefono"));
        persona.setDomicilio(dto.getString("domicilio"));
        String email = dto.getString("eMail");
        if (email == null) {
            email = dto.getString("email");
        }
        persona.setEmail(email);

        Object tipo = dto.get("fkIdTipoIdentificacion");
        if (tipo instanceof GenericDto) {
            persona.setDtoTipoIdentificacion(toDtoTipoIdentificacion((GenericDto) tipo));
        } else if (tipo instanceof java.util.Map<?, ?> map) {
            GenericDto tipoDto = new GenericDto();
            tipoDto.setData((java.util.Map<String, Object>) map);
            persona.setDtoTipoIdentificacion(toDtoTipoIdentificacion(tipoDto));
        }
        return persona;
    }

    private static DtoPresupuesto toDtoPresupuesto(GenericDto dto) {
        DtoPresupuesto presupuesto = new DtoPresupuesto();
        if (dto == null) {
            return presupuesto;
        }
        presupuesto.setIdPresupuesto(dto.getInt("idPresupuesto"));
        presupuesto.setTotal(dto.getFloat("total"));
        presupuesto.setSaldo(dto.getFloat("saldo"));
        presupuesto.setObservaciones(dto.getString("observaciones"));
        return presupuesto;
    }

    private static DtoTramite toDtoTramite(GenericDto dto) {
        DtoTramite tramite = new DtoTramite();
        if (dto == null) {
            return tramite;
        }
        tramite.setIdTramite(dto.getInt("idTramite"));
        tramite.setObservaciones(dto.getString("observaciones"));
        Object presupuesto = dto.get("fkIdPresupuesto");
        GenericDto presupuestoDto = asGenericDto(presupuesto);
        if (presupuestoDto != null) {
            tramite.setPresupuesto(toDtoPresupuesto(presupuestoDto));
        }
        GenericDto tipoTramite = asGenericDto(dto.get("fkIdTipoTramite"));
        if (tipoTramite != null) {
            tramite.setTiposDeTramite(toDtoTipoTramite(tipoTramite));
        }
        GenericDto inmueble = asGenericDto(dto.get("fkIdInmueble"));
        if (inmueble != null) {
            DtoInmueble dtoInmueble = new DtoInmueble();
            dtoInmueble.setIdInmueble(inmueble.getInt("idInmueble"));
            dtoInmueble.setNomenclaturaCatastral(inmueble.getString("nomenclaturaCatastral"));
            dtoInmueble.setValuacionFiscal(inmueble.getString("valuacionFiscal"));
            dtoInmueble.setDomicilio(inmueble.getString("domicilio"));
            dtoInmueble.setTipoInmueble(inmueble.getString("tipoInmueble"));
            dtoInmueble.setObservaciones(inmueble.getString("observaciones"));
            tramite.setInmueble(dtoInmueble);
        }
        GenericDto escritura = asGenericDto(dto.get("fkIdEscritura"));
        if (escritura != null) {
            tramite.setEscritura(toDtoEscritura(escritura));
        }
        GenericDto gestion = asGenericDto(dto.get("fkIdGestion"));
        if (gestion != null) {
            DtoGestionDeEscritura dtoGestion = new DtoGestionDeEscritura();
            dtoGestion.setIdGestion(gestion.getInt("idGestion"));
            Integer numero = gestion.getInt("numero");
            if (numero != null) {
                dtoGestion.setNumero(numero);
            }
            dtoGestion.setEncabezado(gestion.getString("encabezado"));
            dtoGestion.setObservaciones(gestion.getString("observaciones"));
            tramite.setGestionDeEscritura(dtoGestion);
        }
        Object personasObj = dto.get("personaList");
        if (personasObj instanceof List<?> personas) {
            for (Object personaObj : personas) {
                GenericDto personaDto = asGenericDto(personaObj);
                if (personaDto != null) {
                    tramite.getListaPersonas().add(toDtoPersona(personaDto));
                }
            }
        }
        return tramite;
    }

    private static DtoGestionDeEscritura toDtoGestion(GenericDto dto) {
        DtoGestionDeEscritura gestion = new DtoGestionDeEscritura();
        if (dto == null) {
            return gestion;
        }
        gestion.setIdGestion(dto.getInt("idGestion"));
        Integer numero = dto.getInt("numero");
        if (numero != null) {
            gestion.setNumero(numero);
        }
        gestion.setEncabezado(dto.getString("encabezado"));
        gestion.setObservaciones(dto.getString("observaciones"));
        gestion.setNumeroBibliorato(dto.getInt("numeroBibliorato"));
        gestion.setNumeroArchivo(dto.getInt("numeroArchivo"));
        Object fecha = dto.get("fechaInicio");
        if (fecha instanceof Date date) {
            gestion.setFechaInicio(date);
        }
        Object estado = dto.get("fkIdEstadoDeGestion");
        GenericDto estadoDto = asGenericDto(estado);
        if (estadoDto != null) {
            gestion.setEstado(toDtoEstado(estadoDto));
        }
        GenericDto escribano = asGenericDto(dto.get("fkIdPersonaEscribano"));
        if (escribano != null) {
            gestion.setPersonaEscribano(toDtoPersona(escribano));
        }
        Object tramitesObj = dto.get("tramiteList");
        if (tramitesObj instanceof List<?> tramites) {
            for (Object tramiteObj : tramites) {
                GenericDto tramite = asGenericDto(tramiteObj);
                if (tramite != null) {
                    gestion.getListaTramitesAsociados().add(toDtoTramite(tramite));
                }
            }
        }
        return gestion;
    }

    private static DtoEscritura toDtoEscritura(GenericDto dto) {
        DtoEscritura escritura = new DtoEscritura();
        if (dto == null) {
            return escritura;
        }
        Integer id = dto.getInt("idEscritura");
        if (id != null) {
            escritura.setIdEscritura(id);
        }
        Integer numero = dto.getInt("numero");
        if (numero != null) {
            escritura.setNumero(numero);
        }
        escritura.setEstado(dto.getString("estado"));
        Object fecha = dto.get("fechaEscrituracion");
        if (fecha instanceof Date d) {
            escritura.setFechaEscrituracion(d);
        }
        Object fechaIns = dto.get("fechaInscripcion");
        if (fechaIns instanceof Date d) {
            escritura.setFechaInscripcion(d);
        }
        escritura.setCuerpo(dto.getString("cuerpo"));
        escritura.setMatriculaInscripcion(dto.getString("matriculaInscripcion"));
        escritura.setObservaciones(dto.getString("observaciones"));
        return escritura;
    }

    private static DtoTestimonio toDtoTestimonio(GenericDto dto) {
        DtoTestimonio testimonio = new DtoTestimonio();
        if (dto == null) {
            return testimonio;
        }
        Integer id = dto.getInt("idTestimonio");
        if (id != null) {
            testimonio.setIdTestimonio(id);
        }
        Integer numero = dto.getInt("numero");
        if (numero != null) {
            testimonio.setNumero(numero);
        }
        Boolean observado = dto.getBoolean("observado");
        testimonio.setObservado(Boolean.TRUE.equals(observado));
        testimonio.setObservaciones(dto.getString("observaciones"));
        Object escrituraObj = dto.get("fkIdEscritura");
        if (escrituraObj instanceof GenericDto gd) {
            testimonio.setEscritura(toDtoEscritura(gd));
        }
        return testimonio;
    }

    private static DtoMovimientoTestimonio toDtoMovimientoTestimonio(GenericDto dto) {
        DtoMovimientoTestimonio movimiento = new DtoMovimientoTestimonio();
        if (dto == null) {
            return movimiento;
        }
        Integer id = dto.getInt("idMovimientoTestimonio");
        if (id != null) {
            movimiento.setIdMovimientoTestimonio(id);
        }
        Object in = dto.get("fechaIngreso");
        if (in instanceof Date d) {
            movimiento.setFechaIngreso(d);
        }
        Object out = dto.get("fechaSalida");
        if (out instanceof Date d) {
            movimiento.setFechaSalida(d);
        }
        Object ins = dto.get("fechaInscripcion");
        if (ins instanceof Date d) {
            movimiento.setFechaInscripcion(d);
        }
        Boolean inscripta = dto.getBoolean("inscripta");
        movimiento.setInscripta(Boolean.TRUE.equals(inscripta));
        Integer carton = dto.getInt("numeroCarton");
        if (carton != null) {
            movimiento.setNumeroCarton(carton);
        }
        movimiento.setObservaciones(dto.getString("observaciones"));
        Object testimonioObj = dto.get("testimonio");
        if (testimonioObj instanceof GenericDto gd) {
            movimiento.setTestimonio(toDtoTestimonio(gd));
        }
        return movimiento;
    }

    private static DtoCopia toDtoCopia(GenericDto dto) {
        DtoCopia copia = new DtoCopia();
        if (dto == null) {
            return copia;
        }
        Integer id = dto.getInt("idCopia");
        if (id != null) {
            copia.setIdCopia(id);
        }
        Integer numero = dto.getInt("numero");
        if (numero != null) {
            copia.setNumero(numero);
        }
        Object imp = dto.get("fechaImpresion");
        if (imp instanceof Date d) {
            copia.setFechaImpresion(d);
        }
        Object ret = dto.get("fechaRetiro");
        if (ret instanceof Date d) {
            copia.setFechaRetiro(d);
        }
        copia.setObservaciones(dto.getString("observaciones"));
        Object personaObj = dto.get("fkIdPersona");
        if (personaObj instanceof GenericDto gd) {
            copia.setPersona(toDtoPersona(gd));
        }
        Object testimonioObj = dto.get("fkIdTestimonio");
        if (testimonioObj instanceof GenericDto gd) {
            copia.setTestimonio(toDtoTestimonio(gd));
        }
        return copia;
    }

    private static DtoTipoDeTramite toDtoTipoTramite(GenericDto dto) {
        DtoTipoDeTramite tipo = new DtoTipoDeTramite();
        if (dto == null) {
            return tipo;
        }
        tipo.setIdTipoTramite(dto.getInt("idTipoTramite"));
        tipo.setNombre(dto.getString("nombre"));
        tipo.setSeArchiva(Boolean.TRUE.equals(dto.getBoolean("seArchiva")));
        tipo.setSeInscribe(Boolean.TRUE.equals(dto.getBoolean("seInscribe")));
        tipo.setAsociaInmuebles(Boolean.TRUE.equals(dto.getBoolean("asociaInmuebles")));
        tipo.setHabilitado(Boolean.TRUE.equals(dto.getBoolean("habilitado")));
        tipo.setObservaciones(dto.getString("observaciones"));
        return tipo;
    }

    private static DtoConcepto toDtoConcepto(GenericDto dto) {
        DtoConcepto concepto = new DtoConcepto();
        if (dto == null) {
            return concepto;
        }
        concepto.setIdConcepto(dto.getInt("idConcepto"));
        concepto.setNombre(dto.getString("nombre"));
        concepto.setValor(dto.getFloat("valor"));
        concepto.setPorcentaje(dto.getInt("porcentaje"));
        concepto.setHabilitado(Boolean.TRUE.equals(dto.getBoolean("habilitado")));
        return concepto;
    }

    private static DtoItem toDtoItem(GenericDto dto) {
        DtoItem item = new DtoItem();
        if (dto == null) {
            return item;
        }
        item.setIdItem(dto.getInt("idItem"));
        item.setNombre(dto.getString("nombre"));
        item.setValor(dto.getFloat("valor"));
        item.setPorcentaje(dto.getInt("porcentaje"));
        item.setObservaciones(dto.getString("observaciones"));
        item.setConceptoFijo(Boolean.TRUE.equals(dto.getBoolean("conceptoFijo")));
        Object pres = dto.get("fkIdPresupuesto");
        if (pres instanceof GenericDto gd) {
            item.setPresupuestos(toDtoPresupuesto(gd));
        }
        return item;
    }

    private static DtoPago toDtoPago(GenericDto dto) {
        DtoPago pago = new DtoPago();
        if (dto == null) {
            return pago;
        }
        pago.setIdPago(dto.getInt("idPago"));
        pago.setMonto(dto.getFloat("monto"));
        Object fecha = dto.get("fecha");
        if (fecha instanceof Date d) {
            pago.setFecha(d);
        }
        pago.setObservaciones(dto.getString("observaciones"));
        Object pres = dto.get("fkIdPresupuesto");
        if (pres instanceof GenericDto gd) {
            pago.setPresupuesto(toDtoPresupuesto(gd));
        }
        return pago;
    }

    private static DtoTipoDeDocumento toDtoTipoDocumento(GenericDto dto) {
        DtoTipoDeDocumento tipo = new DtoTipoDeDocumento();
        if (dto == null) {
            return tipo;
        }
        tipo.setIdTipoDocumento(dto.getInt("idTipoDocumento"));
        tipo.setNombre(dto.getString("nombre"));
        tipo.setVence(Boolean.TRUE.equals(dto.getBoolean("vence")));
        tipo.setDiasVencimiento(dto.getInt("diasVencimiento"));
        tipo.setQuienEntrega(dto.getString("quienEntrega"));
        tipo.setHabilitado(Boolean.TRUE.equals(dto.getBoolean("habilitado")));
        return tipo;
    }

    private static DtoDocumentoPresentado toDtoDocumentoPresentado(GenericDto dto) {
        DtoDocumentoPresentado documento = new DtoDocumentoPresentado();
        if (dto == null) {
            return documento;
        }
        documento.setIdDocumentoPresentado(dto.getInt("idDocumentoPresentado"));
        documento.setNombre(dto.getString("nombre"));
        documento.setNumeroCarton(dto.getInt("numeroCarton"));
        documento.setPreparado(Boolean.TRUE.equals(dto.getBoolean("preparado")));
        documento.setVence(Boolean.TRUE.equals(dto.getBoolean("vence")));
        documento.setDiasVencimiento(dto.getInt("diasVencimiento"));
        documento.setImporteApagar(dto.getFloat("importeAPagar"));
        documento.setLiberado(Boolean.TRUE.equals(dto.getBoolean("liberado")));
        documento.setObservado(Boolean.TRUE.equals(dto.getBoolean("observado")));
        documento.setEntregado(Boolean.TRUE.equals(dto.getBoolean("entregado")));
        documento.setReingresado(Boolean.TRUE.equals(dto.getBoolean("reingresado")));
        documento.setQuienEntrega(dto.getString("quienEntrega"));
        documento.setObservaciones(dto.getString("observaciones"));
        Object fechaIngreso = dto.get("fechaIngreso");
        if (fechaIngreso instanceof Date d) {
            documento.setFechaIngreso(d);
        }
        Object fechaSalida = dto.get("fechaSalida");
        if (fechaSalida instanceof Date d) {
            documento.setFechaSalida(d);
        }
        Object fechaPago = dto.get("fechaPago");
        if (fechaPago instanceof Date d) {
            documento.setFechaPago(d);
        }
        Object fechaLiberado = dto.get("fechaLiberado");
        if (fechaLiberado instanceof Date d) {
            documento.setFechaLiberado(d);
        }
        Object fechaVencimiento = dto.get("fechaVencimiento");
        if (fechaVencimiento instanceof Date d) {
            documento.setFechaVencimiento(d);
        }
        GenericDto tramite = asGenericDto(dto.get("fkIdTramite"));
        if (tramite != null) {
            documento.setFkTramite(toDtoTramite(tramite));
        }
        return documento;
    }

    private static GenericDto toGenericPersonaRef(Integer idPersona) {
        GenericDto persona = new GenericDto();
        persona.put("idPersona", idPersona);
        return persona;
    }

    private static GenericDto toGenericEstadoRef(Integer idEstado) {
        GenericDto estado = new GenericDto();
        estado.put("idEstadoGestion", idEstado);
        return estado;
    }

    private static GenericDto toGenericTramiteRef(Integer idTramite) {
        GenericDto tramite = new GenericDto();
        tramite.put("idTramite", idTramite);
        return tramite;
    }

    private static GenericDto toGenericEscrituraRef(Integer idEscritura) {
        GenericDto escritura = new GenericDto();
        escritura.put("idEscritura", idEscritura);
        return escritura;
    }

    private static GenericDto toGenericPersona(DtoPersona dto) {
        GenericDto payload = new GenericDto();
        payload.put("idPersona", dto.getIdPersona());
        payload.put("nombre", dto.getNombre());
        payload.put("apellido", dto.getApellido());
        payload.put("nacionalidad", dto.getNacionalidad());
        payload.put("cuit", dto.getCuit());
        payload.put("sexo", dto.getSexo());
        payload.put("fechaNacimiento", dto.getFechaNacimiento());
        payload.put("estadoCivil", dto.getEstadoCivil());
        payload.put("numeroNupcias", dto.getNumeroNupcias());
        payload.put("ocupacion", dto.getOcupacion());
        payload.put("domicilio", dto.getDomicilio());
        payload.put("telefono", dto.getTelefono());
        payload.put("registroEscribano", dto.getRegistroEscribano());
        payload.put("esCliente", dto.getEsCliente());
        payload.put("numeroIdentificacion", dto.getNumeroIdentificacion());
        payload.put("eMail", dto.getEmail());
        if (dto.getDtoTipoIdentificacion() != null) {
            GenericDto tipo = new GenericDto();
            tipo.put("idTipoIdentificacion", dto.getDtoTipoIdentificacion().getIdTipoIdentificacion());
            tipo.put("nombre", dto.getDtoTipoIdentificacion().getNombre());
            payload.put("fkIdTipoIdentificacion", tipo);
        }
        return payload;
    }

    private ArrayList<DtoPersona> buscarPersonaNombreApellidoConFiltros(DtoPersona dto, boolean requiereGestion) {
        ArrayList<DtoPersona> result = new ArrayList<>();
        try {
            String path = "buscar?nombre=" + encode(dto.getNombre()) + "&apellido=" + encode(dto.getApellido());
            List<GenericDto> personas = personaClient.findAllByPath(path);
            for (GenericDto persona : personas) {
                DtoPersona mapped = toDtoPersona(persona);
                if (!requiereGestion || personaTieneGestion(mapped.getIdPersona())) {
                    result.add(mapped);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando personas por nombre/apellido", e);
        }
        return result;
    }

    private boolean personaTieneGestion(Integer idPersona) {
        if (idPersona == null) {
            return false;
        }
        try {
            List<GenericDto> tramites = tramiteClient.findAll();
            for (GenericDto tramite : tramites) {
                Object personasObj = tramite.get("personaList");
                if (personasObj instanceof List<?> personaList) {
                    for (Object persona : personaList) {
                        if (persona instanceof java.util.Map<?, ?> map) {
                            Object personaId = map.get("idPersona");
                            if (personaId instanceof Number && Objects.equals(((Number) personaId).intValue(), idPersona)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error validando relacion persona-gestion", e);
        }
        return false;
    }

    /** Stub: delegar a API cuando se implemente. */
    public List<DtoUsuario> buscarUsuariosDisponibles() {
        List<DtoUsuario> result = new ArrayList<>();
        try {
            for (GenericDto usuario : usuarioClient.findAll()) {
                DtoUsuario mapped = new DtoUsuario();
                mapped.setIdUsuario(usuario.getInt("idUsuario"));
                mapped.setNombre(usuario.getString("nombre"));
                mapped.setContrasenia(usuario.getString("contrasenia"));
                mapped.setTipo(usuario.getString("tipo"));
                mapped.setEstado(Boolean.TRUE.equals(usuario.getBoolean("estado")));
                result.add(mapped);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error listando usuarios", e);
        }
        return result;
    }

    /** Stub: buscar usuario por persona; delegar a API. */
    public DtoUsuario buscarUsuario(DtoUsuario dto) {
        if (dto == null) {
            return null;
        }
        try {
            List<GenericDto> usuarios = usuarioClient.findAll();
            for (GenericDto usuario : usuarios) {
                boolean sameId = dto.getIdUsuario() != null && Objects.equals(dto.getIdUsuario(), usuario.getInt("idUsuario"));
                boolean sameName = dto.getNombre() != null
                        && dto.getNombre().equalsIgnoreCase(usuario.getString("nombre"));
                if (sameId || sameName) {
                    DtoUsuario mapped = new DtoUsuario();
                    mapped.setIdUsuario(usuario.getInt("idUsuario"));
                    mapped.setNombre(usuario.getString("nombre"));
                    mapped.setContrasenia(usuario.getString("contrasenia"));
                    mapped.setTipo(usuario.getString("tipo"));
                    mapped.setEstado(Boolean.TRUE.equals(usuario.getBoolean("estado")));
                    return mapped;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando usuario", e);
        }
        return null;
    }

    /**
     * Stub: asociar tipo identificación a persona.
     *
     * @return
     */
    public int asociarFkTipoIdentificacion(DtoPersona dto) {
        if (dto == null || dto.getDtoTipoIdentificacion() == null || dto.getDtoTipoIdentificacion().getNombre() == null) {
            return 0;
        }
        ArrayList<DtoTipoIdentificacion> tipos = listarTiposIdentificacion();
        for (DtoTipoIdentificacion tipo : tipos) {
            if (dto.getDtoTipoIdentificacion().getNombre().equalsIgnoreCase(tipo.getNombre())) {
                return tipo.getIdTipoIdentificacion() != null ? tipo.getIdTipoIdentificacion() : 0;
            }
        }
        return 0;
    }

    /** Stub: buscar persona por tipo y número identificación. */
    public DtoPersona buscarPersonaTipoNumeroIdentificacion(DtoPersona dto) {
        try {
            String path = "buscar?numeroIdentificacion=" + encode(dto.getNumeroIdentificacion());
            List<GenericDto> personas = personaClient.findAllByPath(path);
            Integer tipoId = dto.getDtoTipoIdentificacion() != null ? dto.getDtoTipoIdentificacion().getIdTipoIdentificacion() : null;
            for (GenericDto persona : personas) {
                DtoPersona mapped = toDtoPersona(persona);
                if (tipoId == null
                        || (mapped.getDtoTipoIdentificacion() != null
                        && Objects.equals(tipoId, mapped.getDtoTipoIdentificacion().getIdTipoIdentificacion()))) {
                    return mapped;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando persona por tipo/numero", e);
        }
        DtoPersona noValida = new DtoPersona();
        noValida.setIdPersona(DtoPersona.ID_DTO_INICIALIZADO);
        return noValida;
    }

    /** Stub: control modificación persona. */
    public boolean controlModificacionPersona(DtoPersona dtoAntes, DtoPersona dtoDespues) {
        if (dtoAntes == null || dtoDespues == null) {
            return true;
        }
        return !Objects.equals(dtoAntes.getNombre(), dtoDespues.getNombre())
                || !Objects.equals(dtoAntes.getApellido(), dtoDespues.getApellido())
                || !Objects.equals(dtoAntes.getNumeroIdentificacion(), dtoDespues.getNumeroIdentificacion())
                || !Objects.equals(dtoAntes.getTelefono(), dtoDespues.getTelefono())
                || !Objects.equals(dtoAntes.getEmail(), dtoDespues.getEmail())
                || !Objects.equals(dtoAntes.getDomicilio(), dtoDespues.getDomicilio())
                || !Objects.equals(dtoAntes.getEsCliente(), dtoDespues.getEsCliente());
    }

    /** Stub: modificar persona; delegar a API. */
    public void modificarPersona(DtoPersona dto) throws Exception {
        GenericDto payload = toGenericPersona(dto);
        personaClient.edit(payload);
    }

    /** Stub: buscar registros auditoría por usuario. */
    public List<com.licensis.notaire.dto.DtoRegistroAuditoria> buscarRegistrosAuditoria(DtoUsuario dto) {
        return new ArrayList<>();
    }

    /**
     * Metodo que permite encriptar un string usando MD5
     *
     * @param stringAEncriptar El string a encriptar
     * @return El string encriptado en MD5
     */
    public String encriptaEnMD5(String stringAEncriptar) {
        char[] CONSTS_HEX = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(stringAEncriptar.getBytes());
            StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                int bajo = (int) (bytes[i] & 0x0f);
                int alto = (int) ((bytes[i] & 0xf0) >> 4);
                strbCadenaMD5.append(CONSTS_HEX[alto]);
                strbCadenaMD5.append(CONSTS_HEX[bajo]);
            }
            return strbCadenaMD5.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Metodo que permite verificar si dos contraseñas (char[]) son iguales
     *
     * @param j1 Primera contraseña
     * @param j2 Segunda contraseña
     * @return DtoFlag con flag=true si son iguales, false en caso contrario
     */
    public DtoFlag isPasswordCorrect(char[] j1, char[] j2) {
        com.licensis.notaire.dto.DtoFlag dtoFlag = new com.licensis.notaire.dto.DtoFlag();
        boolean valor = true;
        int puntero = 0;
        if (j1.length != j2.length) {
            valor = false;
        } else {
            while ((valor) && (puntero < j1.length)) {
                if (j1[puntero] != j2[puntero]) {
                    valor = false;
                }
                puntero++;
            }
        }
        dtoFlag.setFlag(valor);
        return dtoFlag;
    }

    /** Stub: listar tipos identificación. */
    public ArrayList<com.licensis.notaire.dto.DtoTipoIdentificacion> listarTiposIdentificacion() {
        ArrayList<DtoTipoIdentificacion> result = new ArrayList<>();
        try {
            List<GenericDto> tipos = tipoIdentificacionClient.findAll();
            for (GenericDto tipo : tipos) {
                result.add(toDtoTipoIdentificacion(tipo));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error listando tipos de identificacion", e);
        }
        return result;
    }

    /** Stub: verificar si existe persona. */
    public boolean siExistePersona(DtoPersona dto) {
        DtoPersona persona = buscarPersonaTipoNumeroIdentificacion(dto);
        return persona != null && persona.getIdPersona() != null && !persona.getIdPersona().equals(DtoPersona.ID_DTO_INICIALIZADO);
    }

    /** Stub: dar alta persona. */
    public com.licensis.notaire.dto.DtoPersona darAltaPersona(DtoPersona dto) {
        try {
            GenericDto created = personaClient.create(toGenericPersona(dto));
            return toDtoPersona(created);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error dando alta persona", e);
            return dto;
        }
    }

    /** Stub: obtener documentación de gestión. */
    public DtoGestionDeEscritura obtenerDocNecesarioEntregadosNoEntregadosDeGestion(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null) {
            return null;
        }
        DtoGestionDeEscritura gestion = buscarDtoGestion(dto);
        if (gestion == null) {
            return dto;
        }
        try {
            List<GenericDto> documentos = documentoPresentadoClient.findAll();
            for (DtoTramite tramite : gestion.getListaTramitesAsociados()) {
                tramite.getListaDocumentosGestion().clear();
                tramite.getListaDocumentosNoPrecentados().clear();
                tramite.getListaDocumentosNecesarios().clear();
                for (GenericDto documentoRaw : documentos) {
                    DtoDocumentoPresentado documento = toDtoDocumentoPresentado(documentoRaw);
                    if (documento.getFkTramite() != null
                            && documento.getFkTramite().getIdTramite() != null
                            && Objects.equals(documento.getFkTramite().getIdTramite(), tramite.getIdTramite())) {
                        tramite.getListaDocumentosGestion().add(documento);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo documentacion de gestion", e);
        }
        return gestion;
    }

    /** Stub: buscar gestión. */
    public com.licensis.notaire.dto.DtoGestionDeEscritura buscarDtoGestion(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null) {
            return null;
        }
        try {
            GenericDto gestionRaw = null;
            if (dto.getIdGestion() != null && dto.getIdGestion() > 0) {
                gestionRaw = gestionClient.find(dto.getIdGestion());
            } else if (dto.getNumero() > 0) {
                for (GenericDto gestion : gestionClient.findAll()) {
                    if (Objects.equals(gestion.getInt("numero"), dto.getNumero())) {
                        gestionRaw = gestion;
                        break;
                    }
                }
            }
            if (gestionRaw == null) {
                return dto;
            }
            DtoGestionDeEscritura gestion = toDtoGestion(gestionRaw);
            if (gestion.getListaTramitesAsociados().isEmpty()) {
                for (GenericDto tramiteRaw : tramiteClient.findAll()) {
                    DtoTramite tramite = toDtoTramite(tramiteRaw);
                    if (tramite.getGestion() != null
                            && Objects.equals(tramite.getGestion().getIdGestion(), gestion.getIdGestion())) {
                        gestion.getListaTramitesAsociados().add(tramite);
                    }
                }
            }
            return gestion;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando gestion", e);
            return dto;
        }
    }

    /** Stub: modificar documentación. */
    public com.licensis.notaire.dto.DtoFlag modificarDocumentacion(
            java.util.ArrayList<com.licensis.notaire.dto.DtoDocumentoPresentado> lista,
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        DtoFlag resultado = new DtoFlag();
        resultado.setFlag(false);
        if (lista == null || lista.isEmpty()) {
            return resultado;
        }
        boolean ok = true;
        for (DtoDocumentoPresentado documento : lista) {
            try {
                GenericDto payload = new GenericDto();
                payload.put("idDocumentoPresentado", documento.getIdDocumentoPresentado());
                payload.put("nombre", documento.getNombre());
                payload.put("numeroCarton", documento.getNumeroCarton());
                payload.put("fechaIngreso", documento.getFechaIngreso());
                payload.put("fechaSalida", documento.getFechaSalida());
                payload.put("preparado", documento.isPreparado());
                payload.put("vence", documento.isVence());
                payload.put("fechaVencimiento", documento.getFechaVencimiento());
                payload.put("diasVencimiento", documento.getDiasVencimiento());
                payload.put("importeAPagar", documento.getImporteAPagar());
                payload.put("fechaPago", documento.getFechaPago());
                payload.put("liberado", documento.isLiberado());
                payload.put("fechaLiberado", documento.getFechaLiberado());
                payload.put("observado", documento.isObservado());
                payload.put("observaciones", documento.getObservaciones());
                payload.put("reingresado", documento.isReingresado());
                payload.put("quienEntrega", documento.getQuienEntrega());
                payload.put("entregado", documento.isEntregado());
                if (documento.getFkTramite() != null && documento.getFkTramite().getIdTramite() != null) {
                    payload.put("fkIdTramite", toGenericTramiteRef(documento.getFkTramite().getIdTramite()));
                }
                if (documento.getIdDocumentoPresentado() != null && documento.getIdDocumentoPresentado() > 0) {
                    documentoPresentadoClient.edit(payload);
                } else {
                    documentoPresentadoClient.create(payload);
                }
            } catch (Exception e) {
                ok = false;
                LOGGER.log(Level.WARNING, "Error modificando documentacion", e);
            }
        }
        resultado.setFlag(ok);
        if (ok && dto != null) {
            iscompletaDocumentacion(dto);
        }
        return resultado;
    }

    /** Stub: consultar documentos próximos a vencer. */
    public List<com.licensis.notaire.dto.DtoDocumentoPresentado> consultarDocumentosProximosVencer() {
        List<DtoDocumentoPresentado> resultado = new ArrayList<>();
        Date hoy = new Date();
        try {
            for (GenericDto documentoRaw : documentoPresentadoClient.findAll()) {
                DtoDocumentoPresentado documento = toDtoDocumentoPresentado(documentoRaw);
                if (documento.getFechaVencimiento() != null
                        && documento.getFechaVencimiento().after(hoy)
                        && documento.getDiasVencimiento() != null
                        && documento.getDiasVencimiento() <= 30) {
                    resultado.add(documento);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error consultando documentos proximos a vencer", e);
        }
        return resultado;
    }

    /** Stub: verificar documentación completa externa. */
    public static boolean documentacionCompletaExterna(com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null) {
            return false;
        }
        for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
            for (DtoDocumentoPresentado documento : tramite.getListaDocumentosGestion()) {
                if (ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA.equals(documento.getQuienEntrega())
                        && !documento.isEntregado()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Stub: verificar documentación completa. */
    public static boolean iscompletaDocumentacion(com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null) {
            return false;
        }
        for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
            for (DtoDocumentoPresentado documento : tramite.getListaDocumentosGestion()) {
                if (!documento.isEntregado()) {
                    return false;
                }
            }
        }
        DtoEstadoDeGestion nuevoEstado = new DtoEstadoDeGestion();
        nuevoEstado.setNombre(ConstantesNegocio.DOCUMENTACION_COMPLETA);
        DtoEstadoDeGestion estado = getInstancia().obtenerDtoEstadoDeGestion(nuevoEstado);
        dto.setEstado(estado);
        getInstancia().modificarEstadoDeGestionDeEscritura(dto);
        getInstancia().registrarMovimientoHistorial(dto);
        return true;
    }

    /** Stub: modificar documentación entidades externas. */
    public com.licensis.notaire.dto.DtoFlag modificarDocumentacionEntidadesExternas(
            java.util.ArrayList<com.licensis.notaire.dto.DtoDocumentoPresentado> lista,
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        return modificarDocumentacion(lista, dto);
    }

    /** Stub: buscar tipos de tramite habilitados. */
    public List<com.licensis.notaire.dto.DtoTipoDeTramite> buscarTiposDeTramiteHabilitados() {
        List<DtoTipoDeTramite> result = new ArrayList<>();
        try {
            for (GenericDto tipo : tipoTramiteClient.findAll()) {
                DtoTipoDeTramite mapped = toDtoTipoTramite(tipo);
                if (Boolean.TRUE.equals(mapped.getHabilitado())) {
                    result.add(mapped);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando tipos de tramite", e);
        }
        return result;
    }

    /** Stub: obtener plantillas de tramite. */
    public List<com.licensis.notaire.dto.DtoPlantillaTramite> obtenerPlantillasTramite(
            com.licensis.notaire.dto.DtoTipoDeTramite dto) {
        return new ArrayList<>();
    }

    /** Stub: verificar documentación completa cliente. */
    public boolean documentacionCompletaCliente(com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null) {
            return false;
        }
        for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
            for (DtoDocumentoPresentado documento : tramite.getListaDocumentosGestion()) {
                if (ConstantesNegocio.DOCUMENTACION_CLIENTE.equals(documento.getQuienEntrega())
                        && !documento.isEntregado()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Stub: modificar cliente. */
    public com.licensis.notaire.dto.DtoPersona modificarCliente(com.licensis.notaire.dto.DtoPersona dto)
            throws Exception {
        modificarPersona(dto);
        return dto;
    }

    /** Stub: buscar persona por nombre y apellido. */
    public static ArrayList<com.licensis.notaire.dto.DtoPersona> buscarPersonaNombreApellido(
            com.licensis.notaire.dto.DtoPersona dto) {
        return getInstancia().buscarPersonaNombreApellidoConFiltros(dto, false);
    }

    /** Stub: obtener historial gestión. */
    public ArrayList<com.licensis.notaire.dto.DtoHistorial> obtenerHistorialGestion(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        ArrayList<DtoHistorial> result = new ArrayList<>();
        if (dto == null || dto.getIdGestion() == null) {
            return result;
        }
        try {
            for (GenericDto historialRaw : historialClient.findAll()) {
                GenericDto gestionRaw = asGenericDto(historialRaw.get("fkIdGestion"));
                if (gestionRaw == null || !Objects.equals(gestionRaw.getInt("idGestion"), dto.getIdGestion())) {
                    continue;
                }
                DtoHistorial historial = new DtoHistorial();
                historial.setIdHistorial(historialRaw.getInt("idHistorial"));
                Object fecha = historialRaw.get("fecha");
                if (fecha instanceof Date d) {
                    historial.setFecha(d);
                }
                historial.setObservaciones(historialRaw.getString("observaciones"));
                GenericDto estadoRaw = asGenericDto(historialRaw.get("fkIdEstadoGestion"));
                if (estadoRaw != null) {
                    historial.setEstadosDeGestion(toDtoEstado(estadoRaw));
                }
                historial.setGestionesDeEscrituras(dto);
                result.add(historial);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo historial de gestion", e);
        }
        return result;
    }

    /** Stub: buscar personas clientes. */
    public static ArrayList<com.licensis.notaire.dto.DtoPersona> buscarPersonasClientes() {
        ArrayList<DtoPersona> result = new ArrayList<>();
        try {
            List<GenericDto> personas = getInstancia().personaClient.findAll();
            for (GenericDto persona : personas) {
                DtoPersona mapped = toDtoPersona(persona);
                if (mapped.getEsCliente()) {
                    result.add(mapped);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando personas clientes", e);
        }
        return result;
    }

    /** Stub: modificar documentación reingreso. */
    public com.licensis.notaire.dto.DtoFlag modificarDocumentacionReingreso(
            java.util.ArrayList<com.licensis.notaire.dto.DtoDocumentoPresentado> lista,
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        return modificarDocumentacion(lista, dto);
    }

    /** Stub: obtener lista escribanos disponibles. */
    public ArrayList<com.licensis.notaire.dto.DtoPersona> obtenerListaEscribanosDisponibles() {
        ArrayList<DtoPersona> result = new ArrayList<>();
        try {
            List<GenericDto> personas = personaClient.findAll();
            for (GenericDto persona : personas) {
                DtoPersona dtoPersona = toDtoPersona(persona);
                if (dtoPersona.getRegistroEscribano() != null) {
                    result.add(dtoPersona);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo lista de escribanos", e);
        }
        return result;
    }

    /** Stub: buscar escrituras por registro. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscriturasPorRegistro(
            com.licensis.notaire.dto.DtoPersona dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        if (dto == null || dto.getRegistroEscribano() == null) {
            return result;
        }
        try {
            for (GenericDto tramite : tramiteClient.findAll()) {
                GenericDto gestion = asGenericDto(tramite.get("fkIdGestion"));
                if (gestion == null) {
                    continue;
                }
                GenericDto escribano = asGenericDto(gestion.get("fkIdPersonaEscribano"));
                if (escribano == null || !Objects.equals(escribano.getInt("registroEscribano"), dto.getRegistroEscribano())) {
                    continue;
                }
                GenericDto escritura = asGenericDto(tramite.get("fkIdEscritura"));
                if (escritura == null) {
                    continue;
                }
                DtoEscritura mapped = toDtoEscritura(escritura);
                boolean exists = false;
                for (DtoEscritura current : result) {
                    if (Objects.equals(current.getIdEscritura(), mapped.getIdEscritura())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    result.add(mapped);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando escrituras por registro", e);
        }
        return result;
    }

    /** Stub: buscar escritura por número. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscrituraPorNumero(
            com.licensis.notaire.dto.DtoEscritura dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        try {
            for (GenericDto escritura : escrituraClient.findAll()) {
                DtoEscritura mapped = toDtoEscritura(escritura);
                if (dto != null && dto.getNumero() > 0 && dto.getNumero() != mapped.getNumero()) {
                    continue;
                }
                result.add(mapped);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando escritura por numero", e);
        }
        return result;
    }

    /** Stub: buscar escrituras por registro firmadas. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscriturasPorRegistroFirmadas(
            com.licensis.notaire.dto.DtoPersona dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        for (DtoEscritura escritura : buscarEscriturasPorRegistro(dto)) {
            if (ConstantesNegocio.ESCRITURA_FIRMADA.equalsIgnoreCase(escritura.getEstado())) {
                result.add(escritura);
            }
        }
        return result;
    }

    /** Stub: buscar escritura por número firmada. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscrituraPorNumeroFirmada(
            com.licensis.notaire.dto.DtoEscritura dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        for (DtoEscritura escritura : buscarEscrituraPorNumero(dto)) {
            if (ConstantesNegocio.ESCRITURA_FIRMADA.equalsIgnoreCase(escritura.getEstado())) {
                result.add(escritura);
            }
        }
        return result;
    }

    /** Stub: buscar escrituras por registro firmadas sin archivo. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscriturasPorRegistroFirmadasSinArchivo(
            com.licensis.notaire.dto.DtoPersona dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        for (DtoEscritura escritura : buscarEscriturasPorRegistroFirmadas(dto)) {
            if (escritura.getMatriculaInscripcion() == null || escritura.getMatriculaInscripcion().isBlank()) {
                result.add(escritura);
            }
        }
        return result;
    }

    /** Stub: verificar si se inscribe escritura. */
    public boolean verificarSeInscribeEscritura(com.licensis.notaire.dto.DtoEscritura dto) {
        if (dto == null || dto.getIdEscritura() == null) {
            return false;
        }
        try {
            for (GenericDto tramite : tramiteClient.findAll()) {
                GenericDto escritura = asGenericDto(tramite.get("fkIdEscritura"));
                if (escritura == null || !Objects.equals(escritura.getInt("idEscritura"), dto.getIdEscritura())) {
                    continue;
                }
                GenericDto tipoTramite = asGenericDto(tramite.get("fkIdTipoTramite"));
                if (tipoTramite != null && Boolean.TRUE.equals(tipoTramite.getBoolean("seInscribe"))) {
                    return true;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error verificando si la escritura se inscribe", e);
        }
        return false;
    }

    /** Stub: buscar escritura por número firmada sin archivo. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscrituraPorNumeroFirmadaSinArchivo(
            com.licensis.notaire.dto.DtoEscritura dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        for (DtoEscritura escritura : buscarEscrituraPorNumeroFirmada(dto)) {
            if (escritura.getMatriculaInscripcion() == null || escritura.getMatriculaInscripcion().isBlank()) {
                result.add(escritura);
            }
        }
        return result;
    }

    /** Stub: buscar escrituras por registro firmadas inscriptas. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscriturasPorRegistroFirmadasInscriptas(
            com.licensis.notaire.dto.DtoPersona dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        for (DtoEscritura escritura : buscarEscriturasPorRegistro(dto)) {
            if (ConstantesNegocio.ESCRITURA_INSCRIPTA.equalsIgnoreCase(escritura.getEstado())) {
                result.add(escritura);
            }
        }
        return result;
    }

    /** Stub: buscar escritura por número firmada inscripta. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscrituraPorNumeroFirmadaInscripta(
            com.licensis.notaire.dto.DtoEscritura dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        for (DtoEscritura escritura : buscarEscrituraPorNumero(dto)) {
            if (ConstantesNegocio.ESCRITURA_INSCRIPTA.equalsIgnoreCase(escritura.getEstado())) {
                result.add(escritura);
            }
        }
        return result;
    }

    /** Stub: modificar movimiento testimonio inscripción. */
    public Boolean modificarMovimientoTestimonioInscripcion(
            com.licensis.notaire.dto.DtoMovimientoTestimonio dtoMT, com.licensis.notaire.dto.DtoEscritura dtoE) {
        if (dtoMT == null || dtoMT.getIdMovimientoTestimonio() == null || dtoE == null || dtoE.getIdEscritura() == null) {
            return false;
        }
        try {
            GenericDto movimientoPayload = new GenericDto();
            movimientoPayload.put("idMovimientoTestimonio", dtoMT.getIdMovimientoTestimonio());
            movimientoPayload.put("fechaIngreso", dtoMT.getFechaIngreso());
            movimientoPayload.put("fechaSalida", dtoMT.getFechaSalida());
            movimientoPayload.put("fechaInscripcion", dtoMT.getFechaInscripcion());
            movimientoPayload.put("inscripta", dtoMT.isInscripta());
            movimientoPayload.put("numeroCarton", dtoMT.getNumeroCarton());
            movimientoPayload.put("observaciones", dtoMT.getObservaciones());
            if (dtoMT.getTestimonio() != null && dtoMT.getTestimonio().getIdTestimonio() != null) {
                GenericDto testimonio = new GenericDto();
                testimonio.put("idTestimonio", dtoMT.getTestimonio().getIdTestimonio());
                movimientoPayload.put("testimonio", testimonio);
            }
            movimientoTestimonioClient.edit(movimientoPayload);

            GenericDto escrituraPayload = new GenericDto();
            escrituraPayload.put("idEscritura", dtoE.getIdEscritura());
            escrituraPayload.put("numero", dtoE.getNumero());
            escrituraPayload.put("fechaEscrituracion", dtoE.getFechaEscrituracion());
            escrituraPayload.put("cuerpo", dtoE.getCuerpo());
            escrituraPayload.put("estado", dtoE.getEstado());
            escrituraPayload.put("matriculaInscripcion", dtoE.getMatriculaInscripcion());
            escrituraPayload.put("fechaInscripcion", dtoE.getFechaInscripcion());
            escrituraPayload.put("observaciones", dtoE.getObservaciones());
            escrituraClient.edit(escrituraPayload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error modificando movimiento/testimonio de inscripcion", e);
            return false;
        }
    }

    /** Stub: buscar escritura. */
    public com.licensis.notaire.dto.DtoEscritura buscarEscritura(com.licensis.notaire.dto.DtoEscritura dto) {
        if (dto == null) {
            return null;
        }
        try {
            if (dto.getIdEscritura() != null) {
                return toDtoEscritura(escrituraClient.find(dto.getIdEscritura()));
            }
            ArrayList<DtoEscritura> matches = buscarEscrituraPorNumero(dto);
            return matches.isEmpty() ? dto : matches.get(0);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando escritura", e);
            return dto;
        }
    }

    /** Stub: obtener testimonios escritura. */
    public ArrayList<com.licensis.notaire.dto.DtoTestimonio> obtenerTestimoniosEscritura(
            com.licensis.notaire.dto.DtoEscritura dto) {
        ArrayList<DtoTestimonio> result = new ArrayList<>();
        if (dto == null || dto.getIdEscritura() == null) {
            return result;
        }
        try {
            for (GenericDto testimonio : testimonioClient.findAll()) {
                GenericDto escritura = (GenericDto) testimonio.get("fkIdEscritura");
                if (escritura != null && Objects.equals(escritura.getInt("idEscritura"), dto.getIdEscritura())) {
                    result.add(toDtoTestimonio(testimonio));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo testimonios de escritura", e);
        }
        return result;
    }

    /** Stub: obtener movimientos testimonio. */
    public ArrayList<com.licensis.notaire.dto.DtoMovimientoTestimonio> obtenerMovimientosTestimonio(
            com.licensis.notaire.dto.DtoTestimonio dto) {
        ArrayList<DtoMovimientoTestimonio> result = new ArrayList<>();
        if (dto == null || dto.getIdTestimonio() == null) {
            return result;
        }
        try {
            for (GenericDto movimiento : movimientoTestimonioClient.findAll()) {
                GenericDto testimonio = (GenericDto) movimiento.get("testimonio");
                if (testimonio != null && Objects.equals(testimonio.getInt("idTestimonio"), dto.getIdTestimonio())) {
                    result.add(toDtoMovimientoTestimonio(movimiento));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo movimientos de testimonio", e);
        }
        return result;
    }

    /** Stub: buscar movimiento testimonio. */
    public com.licensis.notaire.dto.DtoMovimientoTestimonio buscarMovimientoTestimonio(
            com.licensis.notaire.dto.DtoMovimientoTestimonio dto) {
        return dto;
    }

    /** Stub: modificar movimiento testimonio. */
    public Boolean modificarMovimientoTestimonio(
            com.licensis.notaire.dto.DtoMovimientoTestimonio dto) {
        if (dto == null || dto.getIdMovimientoTestimonio() == null) {
            return false;
        }
        try {
            GenericDto payload = MAPPER.convertValue(dto, GenericDto.class);
            movimientoTestimonioClient.edit(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error modificando movimiento testimonio", e);
            return false;
        }
    }

    /** Stub: crear movimiento testimonio. */
    public Boolean crearMovimientoTestimonio(
            com.licensis.notaire.dto.DtoMovimientoTestimonio dto) {
        try {
            GenericDto payload = MAPPER.convertValue(dto, GenericDto.class);
            movimientoTestimonioClient.create(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creando movimiento testimonio", e);
            return false;
        }
    }

    /** Stub: crear testimonio. */
    public Boolean crearTestimonio(com.licensis.notaire.dto.DtoTestimonio dto,
            java.util.List<com.licensis.notaire.dto.DtoCopia> list) {
        try {
            GenericDto payload = MAPPER.convertValue(dto, GenericDto.class);
            GenericDto created = testimonioClient.create(payload);
            DtoTestimonio createdDto = toDtoTestimonio(created);
            if (list != null && createdDto.getIdTestimonio() != null) {
                for (DtoCopia copia : list) {
                    GenericDto copyPayload = MAPPER.convertValue(copia, GenericDto.class);
                    GenericDto fk = new GenericDto();
                    fk.put("idTestimonio", createdDto.getIdTestimonio());
                    copyPayload.put("fkIdTestimonio", fk);
                    copiaClient.create(copyPayload);
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creando testimonio", e);
            return false;
        }
    }

    /** Stub: obtener copias testimonio. */
    public ArrayList<com.licensis.notaire.dto.DtoCopia> obtenerCopiasTestimonio(
            com.licensis.notaire.dto.DtoTestimonio dto) {
        ArrayList<DtoCopia> result = new ArrayList<>();
        if (dto == null || dto.getIdTestimonio() == null) {
            return result;
        }
        try {
            for (GenericDto copia : copiaClient.findAll()) {
                GenericDto testimonio = (GenericDto) copia.get("fkIdTestimonio");
                if (testimonio != null && Objects.equals(testimonio.getInt("idTestimonio"), dto.getIdTestimonio())) {
                    result.add(toDtoCopia(copia));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo copias de testimonio", e);
        }
        return result;
    }

    /** Stub: obtener escribano escritura. */
    public com.licensis.notaire.dto.DtoPersona obtenerEscribanoEscritura(com.licensis.notaire.dto.DtoEscritura dto) {
        if (dto == null || dto.getIdEscritura() == null) {
            return new DtoPersona();
        }
        try {
            for (GenericDto folio : folioClient.findAll()) {
                Object escrituraObj = folio.get("fkIdEscritura");
                if (escrituraObj instanceof GenericDto escritura
                        && Objects.equals(escritura.getInt("idEscritura"), dto.getIdEscritura())) {
                    Object personaObj = folio.get("fkIdPersona");
                    if (personaObj instanceof GenericDto persona) {
                        return toDtoPersona(persona);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo escribano de escritura", e);
        }
        return new DtoPersona();
    }

    /** Stub: modificar copias testimonio. */
    public Boolean modificarCopiasTestimonio(
            java.util.List<com.licensis.notaire.dto.DtoCopia> list, com.licensis.notaire.dto.DtoTestimonio dto) {
        return true;
    }

    /** Stub: buscar pagos presupuesto. */
    public ArrayList<com.licensis.notaire.dto.DtoPago> buscarPagosPresupuesto(
            com.licensis.notaire.dto.DtoPresupuesto dto) {
        ArrayList<DtoPago> result = new ArrayList<>();
        if (dto == null || dto.getIdPresupuesto() == null) {
            return result;
        }
        try {
            for (GenericDto pago : pagoClient.findAllByPath("presupuesto/" + dto.getIdPresupuesto())) {
                result.add(toDtoPago(pago));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando pagos de presupuesto", e);
        }
        return result;
    }

    /** Stub: dar alta pago. */
    public Boolean darAltaPago(com.licensis.notaire.dto.DtoPago dto) {
        try {
            GenericDto payload = MAPPER.convertValue(dto, GenericDto.class);
            pagoClient.create(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error dando alta pago", e);
            return false;
        }
    }

    /** Stub: modificar presupuesto. */
    public Boolean modificarPresupuesto(com.licensis.notaire.dto.DtoPresupuesto dto,
            java.util.ArrayList<com.licensis.notaire.dto.DtoItem> list1,
            java.util.ArrayList<com.licensis.notaire.dto.DtoItem> list2) {
        return true;
    }

    /** Stub: buscar inmueble. */
    public com.licensis.notaire.dto.DtoInmueble buscarInmueble(com.licensis.notaire.dto.DtoTramite dto) {
        DtoInmueble result = new DtoInmueble();
        if (dto == null || dto.getIdTramite() == null) {
            return result;
        }
        try {
            GenericDto tramite = tramiteClient.find(dto.getIdTramite());
            Object inmueble = tramite.get("fkIdInmueble");
            if (inmueble instanceof GenericDto gd) {
                result.setIdInmueble(gd.getInt("idInmueble"));
                result.setNomenclaturaCatastral(gd.getString("nomenclaturaCatastral"));
                result.setValuacionFiscal(gd.getString("valuacionFiscal"));
                result.setDomicilio(gd.getString("domicilio"));
                result.setTipoInmueble(gd.getString("tipoInmueble"));
                result.setObservaciones(gd.getString("observaciones"));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando inmueble por tramite", e);
        }
        return result;
    }

    /** Stub: buscar inmueble alias. */
    public com.licensis.notaire.dto.DtoInmueble buscarInmueble(com.licensis.notaire.dto.DtoInmueble dto) {
        return dto;
    }

    /** Stub: existe plantilla presupuesto. */
    public boolean existePlantillaPresupuesto(com.licensis.notaire.dto.DtoTipoDeTramite dto) {
        return false;
    }

    /** Stub: crear presupuesto con inmueble. */
    public int crearPresupuesto(com.licensis.notaire.dto.DtoPersona dtoP,
            com.licensis.notaire.dto.DtoPresupuesto dtoPr, com.licensis.notaire.dto.DtoTramite dtoT,
            com.licensis.notaire.dto.DtoInmueble dtoI, java.util.ArrayList<com.licensis.notaire.dto.DtoItem> list) {
        if (dtoI == null) {
            return crearPresupuesto(dtoP, dtoPr, dtoT, list);
        }
        try {
            GenericDto inmueblePayload = new GenericDto();
            inmueblePayload.put("nomenclaturaCatastral", dtoI.getNomenclaturaCatastral());
            inmueblePayload.put("valuacionFiscal", dtoI.getValuacionFiscal());
            inmueblePayload.put("domicilio", dtoI.getDomicilio());
            inmueblePayload.put("tipoInmueble", dtoI.getTipoInmueble());
            inmueblePayload.put("observaciones", dtoI.getObservaciones());
            inmuebleClient.create(inmueblePayload);

            Integer inmuebleId = null;
            for (GenericDto inmueble : inmuebleClient.findAll()) {
                if (Objects.equals(inmueble.getString("nomenclaturaCatastral"), dtoI.getNomenclaturaCatastral())
                        && Objects.equals(inmueble.getString("domicilio"), dtoI.getDomicilio())) {
                    inmuebleId = inmueble.getInt("idInmueble");
                }
            }
            if (inmuebleId == null) {
                return -1;
            }
            dtoI.setIdInmueble(inmuebleId);
            dtoT.setInmueble(dtoI);
            return crearPresupuesto(dtoP, dtoPr, dtoT, list);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creando presupuesto con inmueble", e);
            return -1;
        }
    }

    /** Stub: crear presupuesto sin inmueble. */
    public int crearPresupuesto(com.licensis.notaire.dto.DtoPersona dtoP,
            com.licensis.notaire.dto.DtoPresupuesto dtoPr, com.licensis.notaire.dto.DtoTramite dtoT,
            java.util.ArrayList<com.licensis.notaire.dto.DtoItem> list) {
        if (dtoP == null || dtoP.getIdPersona() == null || dtoT == null || dtoPr == null) {
            return -1;
        }
        try {
            GenericDto tramitePayload = new GenericDto();
            tramitePayload.put("observaciones", dtoT.getObservaciones());
            if (dtoT.getTipoDeTramite() != null && dtoT.getTipoDeTramite().getIdTipoTramite() != null) {
                GenericDto tipoRef = new GenericDto();
                tipoRef.put("idTipoTramite", dtoT.getTipoDeTramite().getIdTipoTramite());
                tramitePayload.put("fkIdTipoTramite", tipoRef);
            }
            if (dtoT.getInmueble() != null && dtoT.getInmueble().getIdInmueble() != null) {
                GenericDto inmuebleRef = new GenericDto();
                inmuebleRef.put("idInmueble", dtoT.getInmueble().getIdInmueble());
                tramitePayload.put("fkIdInmueble", inmuebleRef);
            }
            tramiteClient.create(tramitePayload);

            Integer tramiteId = null;
            for (GenericDto tramite : tramiteClient.findAll()) {
                GenericDto tipo = asGenericDto(tramite.get("fkIdTipoTramite"));
                if (tipo != null && dtoT.getTipoDeTramite() != null
                        && Objects.equals(tipo.getInt("idTipoTramite"), dtoT.getTipoDeTramite().getIdTipoTramite())) {
                    tramiteId = tramite.getInt("idTramite");
                }
            }
            if (tramiteId == null) {
                return -1;
            }
            dtoT.setIdTramite(tramiteId);

            GenericDto presupuestoPayload = new GenericDto();
            presupuestoPayload.put("fecha", new Date());
            presupuestoPayload.put("total", dtoPr.getTotal());
            presupuestoPayload.put("saldo", dtoPr.getSaldo());
            presupuestoPayload.put("observaciones", dtoPr.getObservaciones());
            presupuestoPayload.put("fkIdPersona", toGenericPersonaRef(dtoP.getIdPersona()));
            presupuestoPayload.put("fkIdTramite", toGenericTramiteRef(tramiteId));
            presupuestoClient.create(presupuestoPayload);

            Integer presupuestoId = null;
            for (GenericDto presupuesto : presupuestoClient.findAll()) {
                GenericDto persona = asGenericDto(presupuesto.get("fkIdPersona"));
                GenericDto tramite = asGenericDto(presupuesto.get("fkIdTramite"));
                if (persona != null && tramite != null
                        && Objects.equals(persona.getInt("idPersona"), dtoP.getIdPersona())
                        && Objects.equals(tramite.getInt("idTramite"), tramiteId)
                        && Objects.equals(presupuesto.getFloat("total"), dtoPr.getTotal())) {
                    presupuestoId = presupuesto.getInt("idPresupuesto");
                }
            }
            if (presupuestoId == null) {
                return -1;
            }

            GenericDto tramiteUpdate = new GenericDto();
            tramiteUpdate.put("idTramite", tramiteId);
            tramiteUpdate.put("observaciones", dtoT.getObservaciones());
            if (dtoT.getTipoDeTramite() != null && dtoT.getTipoDeTramite().getIdTipoTramite() != null) {
                GenericDto tipoRef = new GenericDto();
                tipoRef.put("idTipoTramite", dtoT.getTipoDeTramite().getIdTipoTramite());
                tramiteUpdate.put("fkIdTipoTramite", tipoRef);
            }
            tramiteUpdate.put("fkIdPresupuesto", new GenericDto());
            ((GenericDto) tramiteUpdate.get("fkIdPresupuesto")).put("idPresupuesto", presupuestoId);
            if (dtoT.getInmueble() != null && dtoT.getInmueble().getIdInmueble() != null) {
                GenericDto inmuebleRef = new GenericDto();
                inmuebleRef.put("idInmueble", dtoT.getInmueble().getIdInmueble());
                tramiteUpdate.put("fkIdInmueble", inmuebleRef);
            }
            tramiteClient.edit(tramiteUpdate);

            if (list != null) {
                for (DtoItem item : list) {
                    GenericDto itemPayload = new GenericDto();
                    itemPayload.put("nombre", item.getNombre());
                    itemPayload.put("valor", item.getValor());
                    itemPayload.put("porcentaje", item.getPorcentaje());
                    itemPayload.put("observaciones", item.getObservaciones());
                    itemPayload.put("conceptoFijo", item.isFijo());
                    GenericDto presupuestoRef = new GenericDto();
                    presupuestoRef.put("idPresupuesto", presupuestoId);
                    itemPayload.put("fkIdPresupuesto", presupuestoRef);
                    itemClient.create(itemPayload);
                }
            }
            return presupuestoId;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creando presupuesto", e);
            return -1;
        }
    }

    /** Stub: buscar presupuesto por número. */
    public com.licensis.notaire.dto.DtoPresupuesto buscarPresupuestoPorNumero(
            com.licensis.notaire.dto.DtoPresupuesto dto) {
        if (dto == null || dto.getIdPresupuesto() == null) {
            return dto;
        }
        try {
            GenericDto presupuesto = presupuestoClient.find(dto.getIdPresupuesto());
            return toDtoPresupuesto(presupuesto);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando presupuesto por numero", e);
            return dto;
        }
    }

    /** Stub: buscar presupuestos persona. */
    public ArrayList<com.licensis.notaire.dto.DtoPresupuesto> buscarPresupuestosPersona(
            com.licensis.notaire.dto.DtoPersona dto) {
        ArrayList<DtoPresupuesto> result = new ArrayList<>();
        if (dto == null || dto.getIdPersona() == null) {
            return result;
        }
        try {
            for (GenericDto tramite : tramiteClient.findAll()) {
                Object personasObj = tramite.get("personaList");
                if (!(personasObj instanceof List<?> personaList)) {
                    continue;
                }
                boolean belongsToPersona = false;
                for (Object personaObj : personaList) {
                    if (personaObj instanceof java.util.Map<?, ?> map) {
                        Object idPersona = map.get("idPersona");
                        if (idPersona instanceof Number num && Objects.equals(num.intValue(), dto.getIdPersona())) {
                            belongsToPersona = true;
                            break;
                        }
                    }
                }
                if (!belongsToPersona) {
                    continue;
                }
                Object presupuestoObj = tramite.get("fkIdPresupuesto");
                if (presupuestoObj instanceof GenericDto pres) {
                    result.add(toDtoPresupuesto(pres));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando presupuestos por persona", e);
        }
        return result;
    }

    /** Stub: verificar existencia folios. */
    public boolean verificarExistenciaFolios(com.licensis.notaire.dto.DtoFolio dto1,
            com.licensis.notaire.dto.DtoFolio dto2) {
        if (dto1 == null || dto2 == null || dto1.getPersonaEscribano() == null || dto2.getPersonaEscribano() == null) {
            return false;
        }
        try {
            for (GenericDto folio : folioClient.findAll()) {
                Object personaObj = folio.get("fkIdPersona");
                if (!(personaObj instanceof GenericDto persona)) {
                    continue;
                }
                Integer idEscribano = persona.getInt("idPersona");
                Integer numero = folio.getInt("numero");
                if (idEscribano != null
                        && (Objects.equals(idEscribano, dto1.getPersonaEscribano().getIdPersona())
                        || Objects.equals(idEscribano, dto2.getPersonaEscribano().getIdPersona()))
                        && numero != null
                        && numero >= dto1.getNumero()
                        && numero <= dto2.getNumero()) {
                    return true;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error verificando existencia de folios", e);
        }
        return false;
    }

    /** Stub: buscar tipo de folio. */
    public com.licensis.notaire.dto.DtoTipoDeFolio buscarTipoDeFolio(com.licensis.notaire.dto.DtoTipoDeFolio dto) {
        if (dto == null) {
            return null;
        }
        try {
            for (GenericDto tipo : tipoFolioClient.findAll()) {
                if (dto.getNombre() != null && dto.getNombre().equalsIgnoreCase(tipo.getString("nombre"))) {
                    DtoTipoDeFolio mapped = new DtoTipoDeFolio();
                    mapped.setIdTipoFolio(tipo.getInt("idTipoFolio"));
                    mapped.setNombre(tipo.getString("nombre"));
                    mapped.setObservaciones(tipo.getString("observaciones"));
                    mapped.setHabilitado(Boolean.TRUE.equals(tipo.getBoolean("habilitado")));
                    return mapped;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando tipo de folio", e);
        }
        return dto;
    }

    /** Stub: registrar ingreso nuevos folios. */
    public Boolean registrarIngresoNuevosFolios(com.licensis.notaire.dto.DtoFolio dto1,
            com.licensis.notaire.dto.DtoFolio dto2) {
        if (dto1 == null || dto2 == null || dto1.getPersonaEscribano() == null || dto1.getTiposDeFolio() == null) {
            return false;
        }
        try {
            for (int i = dto1.getNumero(); i <= dto2.getNumero(); i++) {
                GenericDto payload = new GenericDto();
                payload.put("numero", i);
                payload.put("anio", dto1.getAnio());
                payload.put("estado", dto1.getEstado());
                payload.put("observaciones", dto1.getObservaciones());
                GenericDto persona = new GenericDto();
                persona.put("idPersona", dto1.getPersonaEscribano().getIdPersona());
                payload.put("fkIdPersona", persona);
                GenericDto tipo = new GenericDto();
                tipo.put("idTipoFolio", dto1.getTiposDeFolio().getIdTipoFolio());
                payload.put("fkIdTipoFolio", tipo);
                folioClient.create(payload);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error registrando ingreso de folios", e);
            return false;
        }
    }

    /** Stub: modificar folio. */
    public boolean modificarFolio(com.licensis.notaire.dto.DtoFolio dto) {
        if (dto == null || dto.getIdFolio() == null) {
            return false;
        }
        try {
            GenericDto payload = MAPPER.convertValue(dto, GenericDto.class);
            folioClient.edit(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error modificando folio", e);
            return false;
        }
    }

    /** Stub: obtener lista folios. */
    public ArrayList<com.licensis.notaire.dto.DtoFolio> obtenerListaFolios(com.licensis.notaire.dto.DtoFolio dto) {
        ArrayList<DtoFolio> result = new ArrayList<>();
        try {
            for (GenericDto folio : folioClient.findAll()) {
                DtoFolio mapped = new DtoFolio();
                mapped.setIdFolio(folio.getInt("idFolio"));
                Integer numero = folio.getInt("numero");
                if (numero != null) {
                    mapped.setNumero(numero);
                }
                Integer anio = folio.getInt("anio");
                if (anio != null) {
                    mapped.setAnio(anio);
                }
                mapped.setEstado(folio.getString("estado"));
                mapped.setObservaciones(folio.getString("observaciones"));
                result.add(mapped);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo lista de folios", e);
        }
        return result;
    }

    /** Stub: archivar gestión. */
    public com.licensis.notaire.dto.DtoFlag archivarGestion(
            java.util.List<com.licensis.notaire.dto.DtoGestionDeEscritura> list) {
        DtoFlag result = new DtoFlag();
        result.setFlag(true);
        return result;
    }

    /** Stub: buscar persona nombre apellido con gestión. */
    public static ArrayList<com.licensis.notaire.dto.DtoPersona> buscarPersonaNombreApellidoConGestion(
            com.licensis.notaire.dto.DtoPersona dto) {
        return getInstancia().buscarPersonaNombreApellidoConFiltros(dto, true);
    }

    /** Stub: buscar persona tipo número identificación con gestión. */
    public static com.licensis.notaire.dto.DtoPersona buscarPersonaTipoNumeroIdentificacionConGestion(
            com.licensis.notaire.dto.DtoPersona dto) {
        DtoPersona persona = getInstancia().buscarPersonaTipoNumeroIdentificacion(dto);
        if (persona == null || persona.getIdPersona() == null || persona.getIdPersona().equals(DtoPersona.ID_DTO_INICIALIZADO)) {
            return persona;
        }
        try {
            List<GenericDto> tramites = getInstancia().tramiteClient.findAll();
            for (GenericDto tramite : tramites) {
                Object personasObj = tramite.get("personaList");
                if (personasObj instanceof List<?> personaList) {
                    for (Object p : personaList) {
                        if (p instanceof java.util.Map<?, ?> map && Objects.equals(((Number) map.get("idPersona")).intValue(), persona.getIdPersona())) {
                            return persona;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validando persona con gestion", e);
        }
        DtoPersona invalida = new DtoPersona();
        invalida.setIdPersona(DtoPersona.ID_DTO_INICIALIZADO);
        return invalida;
    }

    /** Stub: obtener cliente referencia gestión. */
    public com.licensis.notaire.dto.DtoPersona obtenerClienteReferenciaGestion(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        return new com.licensis.notaire.dto.DtoPersona();
    }

    /** Stub: buscar escrituras gestión. */
    public ArrayList<com.licensis.notaire.dto.DtoEscritura> buscarEscriturasGestion(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        ArrayList<DtoEscritura> result = new ArrayList<>();
        if (dto == null) {
            return result;
        }
        try {
            Integer idGestion = dto.getIdGestion();
            if (idGestion == null || idGestion <= 0) {
                DtoGestionDeEscritura gestion = buscarDtoGestion(dto);
                idGestion = gestion != null ? gestion.getIdGestion() : null;
            }
            if (idGestion == null) {
                return result;
            }
            for (GenericDto tramite : tramiteClient.findAll()) {
                GenericDto gestion = asGenericDto(tramite.get("fkIdGestion"));
                if (gestion == null || !Objects.equals(gestion.getInt("idGestion"), idGestion)) {
                    continue;
                }
                GenericDto escritura = asGenericDto(tramite.get("fkIdEscritura"));
                if (escritura == null) {
                    continue;
                }
                DtoEscritura mapped = toDtoEscritura(escritura);
                boolean exists = false;
                for (DtoEscritura actual : result) {
                    if (Objects.equals(actual.getIdEscritura(), mapped.getIdEscritura())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    result.add(mapped);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando escrituras de gestion", e);
        }
        return result;
    }

    /** Stub: modificar documentación reingreso. */
    public Boolean modificarDocumentacionReingreso(com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        return true;
    }

    /** Stub: iniciar gestión de escritura. */
    public static com.licensis.notaire.dto.DtoGestionDeEscritura iniciarGestionDeEscritura(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null) {
            return null;
        }
        ControllerNegocio controller = getInstancia();
        try {
            for (GenericDto gestionExistente : controller.gestionClient.findAll()) {
                if (Objects.equals(gestionExistente.getInt("numero"), dto.getNumero())) {
                    dto.setIdGestion(DtoPersona.VERSION_INICIAL);
                    return dto;
                }
            }
            GenericDto payload = new GenericDto();
            payload.put("numero", dto.getNumero());
            payload.put("fechaInicio", dto.getFechaInicio());
            payload.put("encabezado", dto.getEncabezado());
            payload.put("observaciones", dto.getObservaciones());
            payload.put("numeroArchivo", dto.getNumeroArchivo());
            payload.put("numeroBibliorato", dto.getNumeroBibliorato());
            if (dto.getEstado() != null && dto.getEstado().getIdEstadoGestion() != null) {
                payload.put("fkIdEstadoDeGestion", toGenericEstadoRef(dto.getEstado().getIdEstadoGestion()));
            }
            if (dto.getPersonaEscribano() != null && dto.getPersonaEscribano().getIdPersona() != null) {
                payload.put("fkIdPersonaEscribano", toGenericPersonaRef(dto.getPersonaEscribano().getIdPersona()));
            }
            controller.gestionClient.create(payload);

            DtoGestionDeEscritura creada = null;
            for (GenericDto gestionRaw : controller.gestionClient.findAll()) {
                if (Objects.equals(gestionRaw.getInt("numero"), dto.getNumero())) {
                    creada = toDtoGestion(gestionRaw);
                }
            }
            if (creada == null || creada.getIdGestion() == null) {
                dto.setIdGestion(DtoPersona.ID_DTO_INICIALIZADO);
                return dto;
            }

            for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
                if (tramite.getIdTramite() == null) {
                    continue;
                }
                GenericDto tramiteRaw = controller.tramiteClient.find(tramite.getIdTramite());
                GenericDto tramitePayload = new GenericDto();
                tramitePayload.put("idTramite", tramite.getIdTramite());
                tramitePayload.put("observaciones", tramiteRaw.getString("observaciones"));
                GenericDto tipo = asGenericDto(tramiteRaw.get("fkIdTipoTramite"));
                if (tipo != null) {
                    tramitePayload.put("fkIdTipoTramite", tipo);
                }
                GenericDto presupuesto = asGenericDto(tramiteRaw.get("fkIdPresupuesto"));
                if (presupuesto != null) {
                    tramitePayload.put("fkIdPresupuesto", presupuesto);
                }
                GenericDto inmueble = asGenericDto(tramiteRaw.get("fkIdInmueble"));
                if (inmueble != null) {
                    tramitePayload.put("fkIdInmueble", inmueble);
                }
                tramitePayload.put("fkIdGestion", new GenericDto());
                ((GenericDto) tramitePayload.get("fkIdGestion")).put("idGestion", creada.getIdGestion());
                controller.tramiteClient.edit(tramitePayload);
            }
            dto.setIdGestion(creada.getIdGestion());
            controller.registrarMovimientoHistorial(dto);
            return dto;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error iniciando gestion de escritura", e);
            dto.setIdGestion(DtoPersona.ID_DTO_INICIALIZADO);
            return dto;
        }
    }

    /** Stub: obtener proxima gestión de escritura. */
    public com.licensis.notaire.dto.DtoGestionDeEscritura obtenerProximaGestionDeEscritura() {
        DtoGestionDeEscritura result = new DtoGestionDeEscritura();
        int maxNumero = 0;
        try {
            for (GenericDto gestion : gestionClient.findAll()) {
                Integer numero = gestion.getInt("numero");
                if (numero != null && numero > maxNumero) {
                    maxNumero = numero;
                }
            }
            result.setNumero(maxNumero + 1);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo proxima gestion", e);
        }
        return result;
    }

    /** Stub: obtener lista estados de gestión disponibles. */
    public ArrayList<com.licensis.notaire.dto.DtoEstadoDeGestion> obtenerListaEstadosDeGestionDisponibles() {
        ArrayList<DtoEstadoDeGestion> result = new ArrayList<>();
        try {
            for (GenericDto estado : estadoGestionClient.findAll()) {
                result.add(toDtoEstado(estado));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo estados de gestion", e);
        }
        return result;
    }

    /** Stub: obtener estado actual de gestión. */
    public static com.licensis.notaire.dto.DtoEstadoDeGestion obtenerEstadoActualDeGestion(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto != null && dto.getEstado() != null) {
            return dto.getEstado();
        }
        return new DtoEstadoDeGestion();
    }

    /** Stub: buscar trámite. */
    public com.licensis.notaire.dto.DtoTramite buscarTramite(com.licensis.notaire.dto.DtoTramite dto) {
        if (dto == null || dto.getIdTramite() == null) {
            return dto;
        }
        try {
            GenericDto tramite = tramiteClient.find(dto.getIdTramite());
            return toDtoTramite(tramite);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando tramite", e);
            return dto;
        }
    }

    /** Stub: modificar gestión de escritura. */
    public com.licensis.notaire.dto.DtoGestionDeEscritura modificarGestionDeEscritura(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto,
            java.util.List<com.licensis.notaire.dto.DtoPersona> list1,
            java.util.List<com.licensis.notaire.dto.DtoPersona> list2) {
        if (dto == null || dto.getIdGestion() == null) {
            return dto;
        }
        try {
            GenericDto payload = new GenericDto();
            payload.put("idGestion", dto.getIdGestion());
            payload.put("numero", dto.getNumero());
            payload.put("fechaInicio", dto.getFechaInicio());
            payload.put("encabezado", dto.getEncabezado());
            payload.put("observaciones", dto.getObservaciones());
            payload.put("numeroArchivo", dto.getNumeroArchivo());
            payload.put("numeroBibliorato", dto.getNumeroBibliorato());
            if (dto.getEstado() != null && dto.getEstado().getIdEstadoGestion() != null) {
                payload.put("fkIdEstadoDeGestion", toGenericEstadoRef(dto.getEstado().getIdEstadoGestion()));
            }
            if (dto.getPersonaEscribano() != null && dto.getPersonaEscribano().getIdPersona() != null) {
                payload.put("fkIdPersonaEscribano", toGenericPersonaRef(dto.getPersonaEscribano().getIdPersona()));
            }
            gestionClient.edit(payload);
            registrarMovimientoHistorial(dto);
            return dto;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error modificando gestion de escritura", e);
            dto.setIdGestion(ConstantesNegocio.ID_OBJETO_NO_VALIDO);
            return dto;
        }
    }

    /** Stub: obtener conceptos trámite. */
    public ArrayList<com.licensis.notaire.dto.DtoConcepto> obtenerConceptosTramite(
            com.licensis.notaire.dto.DtoTipoDeTramite dto) {
        ArrayList<DtoConcepto> result = new ArrayList<>();
        try {
            for (GenericDto concepto : conceptoClient.findAll()) {
                DtoConcepto mapped = toDtoConcepto(concepto);
                if (Boolean.TRUE.equals(mapped.getHabilitado())) {
                    result.add(mapped);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo conceptos de tramite", e);
        }
        return result;
    }

    /** Stub: buscar items presupuesto. */
    public ArrayList<com.licensis.notaire.dto.DtoItem> buscarItemsPresupuesto(
            com.licensis.notaire.dto.DtoPresupuesto dto) {
        ArrayList<DtoItem> result = new ArrayList<>();
        if (dto == null || dto.getIdPresupuesto() == null) {
            return result;
        }
        try {
            for (GenericDto item : itemClient.findAllByPath("presupuesto/" + dto.getIdPresupuesto())) {
                result.add(toDtoItem(item));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando items presupuesto", e);
        }
        return result;
    }

    /** Stub: verificar testimonio ingresado para inscribir. */
    public Boolean verificarTestimonioIngresadoParaInscribir(
            com.licensis.notaire.dto.DtoEscritura dto) {
        if (dto == null || dto.getIdEscritura() == null) {
            return false;
        }
        List<DtoTestimonio> testimonios = obtenerTestimoniosEscritura(dto);
        if (testimonios.isEmpty()) {
            return false;
        }
        DtoTestimonio ultimo = testimonios.get(testimonios.size() - 1);
        return !obtenerMovimientosTestimonio(ultimo).isEmpty();
    }

    /** Stub: buscar folios disponibles. */
    public ArrayList<com.licensis.notaire.dto.DtoFolio> buscarFoliosDisponibles(java.lang.Integer id) {
        ArrayList<DtoFolio> result = new ArrayList<>();
        if (id == null) {
            return result;
        }
        try {
            for (GenericDto folio : folioClient.findAll()) {
                GenericDto escribano = asGenericDto(folio.get("fkIdPersonaEscribano"));
                if (escribano == null || !Objects.equals(escribano.getInt("registroEscribano"), id)) {
                    continue;
                }
                if (!ConstantesNegocio.ESTADO_FOLIO_NUEVOS.equalsIgnoreCase(folio.getString("estado"))) {
                    continue;
                }
                DtoFolio dtoFolio = new DtoFolio();
                dtoFolio.setIdFolio(folio.getInt("idFolio"));
                Integer numero = folio.getInt("numero");
                if (numero != null) {
                    dtoFolio.setNumero(numero);
                }
                Integer anio = folio.getInt("anio");
                if (anio != null) {
                    dtoFolio.setAnio(anio);
                }
                dtoFolio.setEstado(folio.getString("estado"));
                dtoFolio.setObservaciones(folio.getString("observaciones"));
                result.add(dtoFolio);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error buscando folios disponibles", e);
        }
        return result;
    }

    /** Stub: obtener dto estado de gestión. */
    public com.licensis.notaire.dto.DtoEstadoDeGestion obtenerDtoEstadoDeGestion(
            com.licensis.notaire.dto.DtoEstadoDeGestion dto) {
        if (dto == null || dto.getNombre() == null) {
            return dto;
        }
        for (DtoEstadoDeGestion estado : obtenerListaEstadosDeGestionDisponibles()) {
            if (dto.getNombre().equals(estado.getNombre())) {
                return estado;
            }
        }
        return new DtoEstadoDeGestion("Estado no valido");
    }

    /** Stub: crear escritura. */
    public Boolean crearEscritura(com.licensis.notaire.dto.DtoEscritura dto) {
        if (dto == null) {
            return false;
        }
        try {
            GenericDto payload = new GenericDto();
            payload.put("numero", dto.getNumero());
            payload.put("fechaEscrituracion", dto.getFechaEscrituracion());
            payload.put("cuerpo", dto.getCuerpo());
            payload.put("estado", dto.getEstado());
            payload.put("matriculaInscripcion", dto.getMatriculaInscripcion());
            payload.put("fechaInscripcion", dto.getFechaInscripcion());
            payload.put("observaciones", dto.getObservaciones());
            escrituraClient.create(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creando escritura", e);
            return false;
        }
    }

    /** Stub: modificar estado de gestion de escritura. */
    public Boolean modificarEstadoDeGestionDeEscritura(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null || dto.getIdGestion() == null || dto.getEstado() == null || dto.getEstado().getIdEstadoGestion() == null) {
            return false;
        }
        try {
            GenericDto gestion = gestionClient.find(dto.getIdGestion());
            GenericDto payload = new GenericDto();
            payload.put("idGestion", dto.getIdGestion());
            payload.put("numero", gestion.getInt("numero"));
            payload.put("fechaInicio", gestion.get("fechaInicio"));
            payload.put("encabezado", gestion.getString("encabezado"));
            payload.put("observaciones", gestion.getString("observaciones"));
            payload.put("numeroArchivo", gestion.getInt("numeroArchivo"));
            payload.put("numeroBibliorato", gestion.getInt("numeroBibliorato"));
            GenericDto escribano = asGenericDto(gestion.get("fkIdPersonaEscribano"));
            if (escribano != null) {
                payload.put("fkIdPersonaEscribano", escribano);
            }
            payload.put("fkIdEstadoDeGestion", toGenericEstadoRef(dto.getEstado().getIdEstadoGestion()));
            gestionClient.edit(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error modificando estado de gestion", e);
            return false;
        }
    }

    /** Stub: registrar movimiento historial. */
    public Boolean registrarMovimientoHistorial(
            com.licensis.notaire.dto.DtoGestionDeEscritura dto) {
        if (dto == null || dto.getIdGestion() == null || dto.getEstado() == null || dto.getEstado().getIdEstadoGestion() == null) {
            return false;
        }
        try {
            GenericDto payload = new GenericDto();
            payload.put("fecha", new Date());
            payload.put("observaciones", "Gestion: " + dto.getNumero() + ", Estado: " + dto.getEstado().getNombre());
            GenericDto gestion = new GenericDto();
            gestion.put("idGestion", dto.getIdGestion());
            payload.put("fkIdGestion", gestion);
            GenericDto estado = new GenericDto();
            estado.put("idEstadoGestion", dto.getEstado().getIdEstadoGestion());
            payload.put("fkIdEstadoGestion", estado);
            historialClient.create(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error registrando movimiento historial", e);
            return false;
        }
    }

    /** Stub: modificar escritura. */
    public Boolean modificarEscritura(com.licensis.notaire.dto.DtoEscritura dto) {
        if (dto == null || dto.getIdEscritura() == null) {
            return false;
        }
        try {
            GenericDto payload = new GenericDto();
            payload.put("idEscritura", dto.getIdEscritura());
            payload.put("numero", dto.getNumero());
            payload.put("fechaEscrituracion", dto.getFechaEscrituracion());
            payload.put("cuerpo", dto.getCuerpo());
            payload.put("estado", dto.getEstado());
            payload.put("matriculaInscripcion", dto.getMatriculaInscripcion());
            payload.put("fechaInscripcion", dto.getFechaInscripcion());
            payload.put("observaciones", dto.getObservaciones());
            escrituraClient.edit(payload);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error modificando escritura", e);
            return false;
        }
    }

    /** Stub: obtener gestiones en tramite. */
    public ArrayList<com.licensis.notaire.dto.DtoGestionDeEscritura> obtenerGestionesEnTramite() {
        ArrayList<DtoGestionDeEscritura> result = new ArrayList<>();
        try {
            List<GenericDto> gestiones = gestionClient.findAll();
            for (GenericDto gestion : gestiones) {
                DtoGestionDeEscritura dto = toDtoGestion(gestion);
                if (dto.getEstado() == null || dto.getEstado().getNombre() == null
                        || !ConstantesNegocio.GESTION_ARCHIVADA.equalsIgnoreCase(dto.getEstado().getNombre())) {
                    result.add(dto);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo gestiones en tramite", e);
        }
        return result;
    }
}
