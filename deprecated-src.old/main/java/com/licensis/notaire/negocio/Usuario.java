/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoUsuario;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "usuarios")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM Usuario u"),
            @NamedQuery(name = "Usuario.findByIdUsuario", query = "SELECT u FROM Usuario u WHERE u.idUsuario = :idUsuario"),
            @NamedQuery(name = "Usuario.findByEstado", query = "SELECT u FROM Usuario u WHERE u.estado = :estado")
        })
public class Usuario implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_usuario")
    private Integer idUsuario;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @Lob
    @Column(name = "contrasenia")
    private String contrasenia;
    @Basic(optional = false)
    @Column(name = "estado")
    private boolean estado;
    @Basic(optional = false)
    @Lob
    @Column(name = "tipo")
    private String tipo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdUsuario", fetch = FetchType.LAZY)
    private List<RegistroAuditoria> registroAuditoriaList;
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id_persona")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Persona fkIdPersona;

    public Usuario()
    {
    }

    public Usuario(Integer idUsuario)
    {
        this.idUsuario = idUsuario;
    }

    public Usuario(Integer idUsuario, String nombre, String contrasenia, boolean estado, String tipo)
    {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.contrasenia = contrasenia;
        this.estado = estado;
        this.tipo = tipo;
    }

    public Integer getIdUsuario()
    {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario)
    {
        this.idUsuario = idUsuario;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getContrasenia()
    {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia)
    {
        this.contrasenia = contrasenia;
    }

    public boolean getEstado()
    {
        return estado;
    }

    public void setEstado(boolean estado)
    {
        this.estado = estado;
    }

    public String getTipo()
    {
        return tipo;
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

    @XmlTransient
    public List<RegistroAuditoria> getRegistroAuditoriaList()
    {
        return registroAuditoriaList;
    }

    public void setRegistroAuditoriaList(List<RegistroAuditoria> registroAuditoriaList)
    {
        this.registroAuditoriaList = registroAuditoriaList;
    }

    public Persona getFkIdPersona()
    {
        return fkIdPersona;
    }

    public void setFkIdPersona(Persona fkIdPersona)
    {
        this.fkIdPersona = fkIdPersona;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idUsuario != null ? idUsuario.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usuario))
        {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.idUsuario == null && other.idUsuario != null) || (this.idUsuario != null && !this.idUsuario.equals(other.idUsuario)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Usuarios[ version=" + version + " ]"
                + "[ idUsuario=" + idUsuario + " ]"
                + "[ nombre=" + nombre + " ]"
                + "[ tipo=" + tipo + " ] "
                + "[ estado=" + estado + " ]";
    }

    public void setAtributos(DtoUsuario dtoUsuario)
    {

        //Ref persona de Usuario
        Persona miPersona = new Persona();
        miPersona.setAtributos(dtoUsuario.getPersonas());
        setFkIdPersona(miPersona);

        //Atributos usuario
        setContrasenia(dtoUsuario.getContrasenia());;
        setEstado(dtoUsuario.isEstado());
        setIdUsuario(dtoUsuario.getIdUsuario());
        setNombre(dtoUsuario.getNombre());
        setTipo(dtoUsuario.getTipo());

        if (dtoUsuario.getVersion() != null)
        {
            //Controlo Version Objeto
            setVersion(dtoUsuario.getVersion());
        }

    }

    public DtoUsuario getDto()
    {
        DtoUsuario miDto = new DtoUsuario();
        try
        {
            miDto.setContrasenia(contrasenia);
            miDto.setEstado(estado);
            miDto.setIdUsuario(idUsuario);
            miDto.setNombre(nombre);

            DtoPersona miDtoPersona = new DtoPersona();
            miDtoPersona = this.getFkIdPersona().getDto();

            miDto.setPersonas(miDtoPersona);
            miDto.setTipo(tipo);

            //Controlo la version del objeto
            miDto.setVersion(version);

        }
        catch (NullPointerException e)
        {
            System.out.println("Error Metodo : getDtoUsuario");
        }
        return miDto;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
