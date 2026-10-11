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
        Boolean result = Boolean.TRUE;
        char[] caractes = campoParaValidar.toCharArray();
        for (char c : caractes)
        {
            if (!((Character.isLetter(c)) || (Character.isSpaceChar(c))))
            {
                result = Boolean.FALSE;
            }
        }
        return result;
    }

    public Boolean validateIntegerOnlyField(String campoParaValidar)
    {
        Boolean result = Boolean.TRUE;
        char[] caractes = campoParaValidar.toCharArray();
        for (char c : caractes)
        {
            if (!Character.isDigit(c))
            {
                result = Boolean.FALSE;
            }
        }
        return result;
    }

    public Boolean validateLettersAndNumbersField(String campoParaValidar)
    {
        Boolean result = Boolean.TRUE;
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
                        result = Boolean.FALSE;
                    }
                }
            }

        }
        catch (NullPointerException e)
        {
            return false;
        }
        return result;
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
        Boolean result = Boolean.TRUE;

        if (numberParaValidar < 0)
        {
            return false;
        }

        return result;
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
        boolean result = true;

        if (dateParaValidar.before(Calendar.getInstance().getTime()))
        {
            return false;
        }

        return result;
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
        boolean result = false;

        Date actual = Calendar.getInstance().getTime();

        if (actual.before(year))
        {
            result = true;
        }
        if (year.getYear() == actual.getYear())
        {
            result = true;
        }

        return result;
    }
}
