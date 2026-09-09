package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoSubmittedDocument;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.jpa.UserJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.business.BusinessController;
import com.licensis.notaire.business.User;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdministradorValidaciones
{

    private static AdministradorValidaciones instancia = null;

    private AdministradorValidaciones()
    {
    }

    public static AdministradorValidaciones getInstancia()
    {
        if (instancia == null)
        {
            instancia = new AdministradorValidaciones();
        }
        return instancia;
    }

    public Boolean validarCampoVacio(String campoParaValidar)
    {
        return campoParaValidar.isEmpty();
    }

    public Boolean validarCampoSoloTexto(String campoParaValidar)
    {
        Boolean resultado = Boolean.TRUE;
        char[] caractes = campoParaValidar.toCharArray();
        for (char c : caractes)
        {
            if (!((Character.isLetter(c)) || (Character.isSpaceChar(c))))
            {
                resultado = Boolean.FALSE;
            }
        }
        return resultado;
    }

    public Boolean validarCampoSoloNumerosEnteros(String campoParaValidar)
    {
        Boolean resultado = Boolean.TRUE;
        char[] caractes = campoParaValidar.toCharArray();
        for (char c : caractes)
        {
            if (!Character.isDigit(c))
            {
                resultado = Boolean.FALSE;
            }
        }
        return resultado;
    }

    public Boolean validarCampoLetrasYNumeros(String campoParaValidar)
    {
        Boolean resultado = Boolean.TRUE;
        try
        {
            if (campoParaValidar.isEmpty())
            {
                return false;
            }
            char[] caractes = campoParaValidar.toCharArray();
            for (char c : caractes)
            {
                if (!Character.isLetterOrDigit(c))
                {
                    if (!Character.isWhitespace(c))
                    {
                        resultado = Boolean.FALSE;
                    }
                }
            }

        }
        catch (NullPointerException e)
        {
            return false;
        }
        return resultado;
    }

    public Boolean validarCharacters(String pCampo)
    {
        Boolean flag = false;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if ((!Character.isLetter(c)
                    || Character.isSpaceChar(c)
                    || c == '-' || c == '_' || c == '(' || c == '.' || c == ','
                    || c == '@' || c == '<' || c == '>' || c == '/' || c == '?'
                    || c == '¿') || c == '=' || c == '!' || c == '"' || c == '#'
                    || c == '&' || c == '¡' || c == '+' || c == '*' || c == ' ')
            {
                return flag = true;

            }
        }
        return flag;
    }

    public Boolean validarSoloLetrasNumeros(String pCampo)
    {
        Boolean flag = true;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if (c == '-' || c == '_' || c == '(' || c == '.' || c == ','
                    || c == '@' || c == '<' || c == '>' || c == '/' || c == '?'
                    || c == '¿' || c == '=' || c == '!' || c == '"' || c == '#'
                    || c == '&' || c == '¡' || c == '+' || c == '*')
            {
                return flag = false;

            }
        }
        return flag;

    }

    public Boolean validarLetrasGuiones(String pCampo)
    {
        Boolean flag = true;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if (!Character.isLetterOrDigit(c))
            {
                if (c != '-')
                {
                    return flag = false;
                }
            }
        }
        return flag;
    }

    public Boolean validarCantidadCharacters(int pCantCharter, String pCampo)
    {
        Boolean flag = false;
        int count = 0;

        for (int i = 0; i < pCampo.length(); i++)
        {
            count++;
        }

        if (count > pCantCharter)
        {
            flag = true;
        }

        return flag;
    }

    public Boolean validarNumber(int numberParaValidar)
    {
        Boolean resultado = Boolean.TRUE;

        if (numberParaValidar < 0)
        {
            return false;
        }

        return resultado;
    }

    public Boolean validarCampoEspacios(String pCampo)
    {
        Boolean flag = false;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if ((Character.isSpaceChar(c)))
            {
                return flag = true;
            }

        }
        return flag;
    }

    public Boolean validarNumberFloat(String number)
    {
        Boolean valido = true;

        try
        {
            Float numberFloat = Float.parseFloat(number);
        }
        catch (NumberFormatException e)
        {
            valido = false;
        }
        finally
        {
            return valido;
        }
    }

    public Boolean validarDatePosteriorHoy(Date dateParaValidar)
    {
        boolean resultado = true;

        if (dateParaValidar.before(Calendar.getInstance().getTime()))
        {
            return false;
        }

        return resultado;
    }

    public Boolean validarFechasPosterioresHoy(Date dateDesde, Date dateHasta)
    {
        try
        {
            if (dateHasta.after(dateDesde))
            {
                return true;
            } else
            {
                return false;
            }

        }
        catch (NullPointerException ex)
        {
            return false;
        }
    }

    public Boolean validarDateYearLimite(Date year)
    {
        boolean resultado = false;

        Date actual = Calendar.getInstance().getTime();

        if (actual.before(year))
        {
            resultado = true;
        }
        if (year.getYear() == actual.getYear())
        {
            resultado = true;
        }

        return resultado;
    }
}
