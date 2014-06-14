/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.licensis.notaire.servicios;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ConexionTest
{
    
    public ConexionTest()
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
        try
        {
            Conexion nuevaConexion = Conexion.getInstancia();
        }
        catch (IOException ex)
        {
            Logger.getLogger(ConexionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstancia method, of class Conexion.
     */
    @Test
    public void testGetInstancia() throws IOException
    {
        
        Conexion expResult = null;
        Conexion result = Conexion.getInstancia();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDatosConexion method, of class Conexion.
     */
    @Test
    public void testSetDatosConexion()
    {
        System.out.println("setDatosConexion");
        String nuevaConexion = "";
        Conexion instance = null;
        instance.setDatosConexion(nuevaConexion);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConexion method, of class Conexion.
     */
    @Test
    public void testGetConexion() throws IOException
    {
        System.out.println("getConexion");
        Conexion instance = null;
        Connection expResult = null;
        Connection result = instance.getConexion();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of desconectar method, of class Conexion.
     */
    @Test
    public void testDesconectar()
    {
        System.out.println("desconectar");
        Conexion instance = null;
        instance.desconectar();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
