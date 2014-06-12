/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.licensis.notaire.gui;

import com.licensis.notaire.dto.DtoUsuario;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author matias
 */
public class LoginTest
{
    
    public LoginTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of main method, of class Login.
     */
    @Test
    public void testMain()
    {
        System.out.println("main");
        String[] args = null;
        Login.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of validarLoginGui method, of class Login.
     */
    @Test
    public void testValidarLoginGui()
    {
        System.out.println("validarLoginGui");
        DtoUsuario dtoUsuario = null;
        Login instance = new Login();
        DtoUsuario expResult = null;
        DtoUsuario result = instance.validarLoginGui(dtoUsuario);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
