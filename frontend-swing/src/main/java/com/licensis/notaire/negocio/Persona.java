package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoPersona;

/**
 * Clase Persona simplificada para el frontend.
 * Versión sin dependencias JPA, solo para compatibilidad con código existente.
 *
 * @author juanca
 */
public class Persona {

    private Integer idPersona;
    private String nombre;
    private String apellido;
    private String nacionalidad;
    private String cuit;
    private String sexo;
    private java.util.Date fechaNacimiento;
    private String estadoCivil;
    private Integer numeroNupcias;
    private String ocupacion;
    private String domicilio;
    private String telefono;
    private Integer registroEscribano;
    private boolean esCliente;
    private String numeroidentificacion;
    private String eMail;
    private int version;

    public Persona() {
    }

    public Integer getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public java.util.Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(java.util.Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public Integer getNumeroNupcias() {
        return numeroNupcias;
    }

    public void setNumeroNupcias(Integer numeroNupcias) {
        this.numeroNupcias = numeroNupcias;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getRegistroEscribano() {
        return registroEscribano;
    }

    public void setRegistroEscribano(Integer registroEscribano) {
        this.registroEscribano = registroEscribano;
    }

    public boolean isEsCliente() {
        return esCliente;
    }

    public void setEsCliente(boolean esCliente) {
        this.esCliente = esCliente;
    }

    public String getNumeroidentificacion() {
        return numeroidentificacion;
    }

    public void setNumeroidentificacion(String numeroidentificacion) {
        this.numeroidentificacion = numeroidentificacion;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Establece los atributos de la persona desde un DTO
     */
    public void setAtributos(DtoPersona dtoPersona) {
        if (dtoPersona == null) {
            return;
        }
        
        setIdPersona(dtoPersona.getIdPersona());
        setNombre(dtoPersona.getNombre());
        setApellido(dtoPersona.getApellido());
        setNacionalidad(dtoPersona.getNacionalidad());
        setCuit(dtoPersona.getCuit());
        setSexo(dtoPersona.getSexo());
        setFechaNacimiento(dtoPersona.getFechaNacimiento());
        setEstadoCivil(dtoPersona.getEstadoCivil());
        setNumeroNupcias(dtoPersona.getNumeroNupcias());
        setOcupacion(dtoPersona.getOcupacion());
        setDomicilio(dtoPersona.getDomicilio());
        setTelefono(dtoPersona.getTelefono());
        setRegistroEscribano(dtoPersona.getRegistroEscribano());
        setEsCliente(dtoPersona.getEsCliente());
        setNumeroidentificacion(dtoPersona.getNumeroIdentificacion());
        seteMail(dtoPersona.getEmail());
        
        if (dtoPersona.getVersion() != null) {
            setVersion(dtoPersona.getVersion());
        }
    }

    /**
     * Convierte la persona a DTO
     */
    public DtoPersona getDto() {
        DtoPersona miDto = new DtoPersona();
        miDto.setIdPersona(idPersona);
        miDto.setNombre(nombre);
        miDto.setApellido(apellido);
        miDto.setNacionalidad(nacionalidad);
        miDto.setCuit(cuit);
        miDto.setSexo(sexo);
        miDto.setFechaNacimiento(fechaNacimiento);
        miDto.setEstadoCivil(estadoCivil);
        miDto.setNumeroNupcias(numeroNupcias);
        miDto.setOcupacion(ocupacion);
        miDto.setDomicilio(domicilio);
        miDto.setTelefono(telefono);
        miDto.setRegistroEscribano(registroEscribano);
        miDto.setEsCliente(esCliente);
        miDto.setNumeroIdentificacion(numeroidentificacion);
        miDto.setEmail(eMail);
        miDto.setVersion(version);
        return miDto;
    }
}
