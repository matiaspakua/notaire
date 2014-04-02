/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.dto;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author matias
 */
public class DtoConceptoTest
{

    private DtoConcepto miConcepto;

    public DtoConceptoTest()
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
        miConcepto = new DtoConcepto();
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void setNombreTest()
    {
        miConcepto.setNombre("test");

        org.junit.Assert.assertEquals("test", miConcepto.getNombre());
    }
}
