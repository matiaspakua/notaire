package com.licensis.notaire.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a user account.
 */
public class DtoUser
{

    private Integer idUser;
    private DtoPerson persons;
    private String name;
    private String password;
    private boolean status;
    private String type;
    private String auditRecord;
    private Integer version;
    private boolean valid = false;
    private Integer roleId;
    private String roleName;
    private String token;

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public DtoUser()
    {
    }

    public Integer getIdUser()
    {
        return this.idUser;
    }

    public void setIdUser(Integer idUser)
    {
        this.idUser = idUser;
    }

    public DtoPerson getPersons()
    {
        return this.persons;
    }

    public void setPersons(DtoPerson persons)
    {
        this.persons = persons;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isStatus()
    {
        return this.status;
    }

    public boolean getStatus()
    {
        return this.status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @JsonIgnore
    public String getAuditLabel()
    {
        String auditRecord;

        auditRecord = this.getName() + " - " + this.getType();

        return auditRecord;
    }

    // Object version
    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Integer getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Integer roleId)
    {
        this.roleId = roleId;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    /**
     * JWT issued by POST /users/login; absent outside the login response.
     */
    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
}
