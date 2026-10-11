package com.licensis.notaire.service;

import java.util.Calendar;
import java.util.Date;

public class ValidationAdministrator
{

    private static ValidationAdministrator instance = null;

    private ValidationAdministrator()
    {
    }

    public static ValidationAdministrator getInstance()
    {
        if (instance == null)
        {
            instance = new ValidationAdministrator();
        }
        return instance;
    }

    public Boolean validateEmptyField(String fieldToValidate)
    {
        return fieldToValidate.isEmpty();
    }

    public Boolean validateTextOnlyField(String fieldToValidate)
    {
        Boolean result = Boolean.TRUE;
        char[] characters = fieldToValidate.toCharArray();
        for (char c : characters)
        {
            if (!((Character.isLetter(c)) || (Character.isSpaceChar(c))))
            {
                result = Boolean.FALSE;
            }
        }
        return result;
    }

    public Boolean validateIntegerOnlyField(String fieldToValidate)
    {
        Boolean result = Boolean.TRUE;
        char[] characters = fieldToValidate.toCharArray();
        for (char c : characters)
        {
            if (!Character.isDigit(c))
            {
                result = Boolean.FALSE;
            }
        }
        return result;
    }

    public Boolean validateLettersAndNumbersField(String fieldToValidate)
    {
        Boolean result = Boolean.TRUE;
        try
        {
            if (fieldToValidate.isEmpty())
            {
                return false;
            }
            char[] characters = fieldToValidate.toCharArray();
            for (char c : characters)
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

    public Boolean validateCharacters(String field)
    {
        Boolean flag = false;

        for (int i = 0; i < field.length(); i++)
        {
            char c = field.charAt(i);

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

    public Boolean validateLettersAndNumbersOnly(String field)
    {
        Boolean flag = true;

        for (int i = 0; i < field.length(); i++)
        {
            char c = field.charAt(i);

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

    public Boolean validateLettersAndHyphens(String field)
    {
        Boolean flag = true;

        for (int i = 0; i < field.length(); i++)
        {
            char c = field.charAt(i);

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

    public Boolean validateCharacterCount(int expectedCount, String field)
    {
        Boolean flag = false;
        int count = 0;

        for (int i = 0; i < field.length(); i++)
        {
            count++;
        }

        if (count > expectedCount)
        {
            flag = true;
        }

        return flag;
    }

    public Boolean validateNumber(int numberToValidate)
    {
        Boolean result = Boolean.TRUE;

        if (numberToValidate < 0)
        {
            return false;
        }

        return result;
    }

    public Boolean validateFieldSpaces(String field)
    {
        Boolean flag = false;

        for (int i = 0; i < field.length(); i++)
        {
            char c = field.charAt(i);

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

    public Boolean validateDateAfterToday(Date dateToValidate)
    {
        boolean result = true;

        if (dateToValidate.before(Calendar.getInstance().getTime()))
        {
            return false;
        }

        return result;
    }

    public Boolean validateDatesAfterToday(Date startDate, Date endDate)
    {
        try
        {
            if (endDate.after(startDate))
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

        Date today = Calendar.getInstance().getTime();

        if (today.before(year))
        {
            result = true;
        }
        if (year.getYear() == today.getYear())
        {
            result = true;
        }

        return result;
    }
}
