package com.licensis.notaire.service;

import java.util.Calendar;
import java.util.Date;

public class ValidationAdministrator
{

    private static ValidationAdministrator instancia = null;

    private ValidationAdministrator()
    {
    }

    public static ValidationAdministrator getInstancia()
    {
        if (instancia == null)
        {
            instancia = new ValidationAdministrator();
        }
        return instancia;
    }

    public Boolean validateEmptyField(String campoParaValidar)
    {
        return campoParaValidar.isEmpty();
    }

    public Boolean validateTextOnlyField(String campoParaValidar)
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

    public Boolean validateIntegerOnlyField(String campoParaValidar)
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

    public Boolean validateLettersAndNumbersField(String campoParaValidar)
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

    public Boolean validateCharacters(String pCampo)
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

    public Boolean validateLettersAndNumbersOnly(String pCampo)
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

    public Boolean validateLettersAndHyphens(String pCampo)
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

    public Boolean validateCharacterCount(int pCantCharter, String pCampo)
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

    public Boolean validateNumber(int numberParaValidar)
    {
        Boolean resultado = Boolean.TRUE;

        if (numberParaValidar < 0)
        {
            return false;
        }

        return resultado;
    }

    public Boolean validateFieldSpaces(String pCampo)
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

    public Boolean validateFloatNumber(String number)
    {
        Boolean valid = true;

        try
        {
            Float numberFloat = Float.parseFloat(number);
        }
        catch (NumberFormatException e)
        {
            valid = false;
        }
        finally
        {
            return valid;
        }
    }

    public Boolean validateDateAfterToday(Date dateParaValidar)
    {
        boolean resultado = true;

        if (dateParaValidar.before(Calendar.getInstance().getTime()))
        {
            return false;
        }

        return resultado;
    }

    public Boolean validateDatesAfterToday(Date dateDesde, Date dateHasta)
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

    public Boolean validateYearLimit(Date year)
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
