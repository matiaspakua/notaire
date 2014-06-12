/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.dto.DtoCopia;
import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoHistorial;
import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoPago;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPlantillaPresupuesto;
import com.licensis.notaire.dto.DtoPlantillaTramite;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.dto.DtoSuplencia;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.servicios.AdministradorJpa;
import java.util.ArrayList;
import java.util.List;
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
public class ControllerNegocioTest
{
    
    public ControllerNegocioTest()
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
     * Test of getInstancia method, of class ControllerNegocio.
     */
    @Test
    public void testGetInstancia()
    {
        System.out.println("getInstancia");
        ControllerNegocio expResult = null;
        ControllerNegocio result = ControllerNegocio.getInstancia();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMiAdministradorJpa method, of class ControllerNegocio.
     */
    @Test
    public void testGetMiAdministradorJpa()
    {
        System.out.println("getMiAdministradorJpa");
        ControllerNegocio instance = null;
        AdministradorJpa expResult = null;
        AdministradorJpa result = instance.getMiAdministradorJpa();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMiAdministradorJpa method, of class ControllerNegocio.
     */
    @Test
    public void testSetMiAdministradorJpa()
    {
        System.out.println("setMiAdministradorJpa");
        AdministradorJpa miAdministradorJpa = null;
        ControllerNegocio instance = null;
        instance.setMiAdministradorJpa(miAdministradorJpa);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaPersona method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaPersona() throws Exception
    {
        System.out.println("darAltaPersona");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.darAltaPersona(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of listarTiposIdentificacion method, of class ControllerNegocio.
     */
    @Test
    public void testListarTiposIdentificacion()
    {
        System.out.println("listarTiposIdentificacion");
        ControllerNegocio instance = null;
        ArrayList<DtoTipoIdentificacion> expResult = null;
        ArrayList<DtoTipoIdentificacion> result = instance.listarTiposIdentificacion();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of siExistePersona method, of class ControllerNegocio.
     */
    @Test
    public void testSiExistePersona()
    {
        System.out.println("siExistePersona");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.siExistePersona(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPersonaTipoNumeroIdentificacion method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPersonaTipoNumeroIdentificacion()
    {
        System.out.println("buscarPersonaTipoNumeroIdentificacion");
        DtoPersona miDtoPersona = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.buscarPersonaTipoNumeroIdentificacion(miDtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPersonaTipoNumeroIdentificacionConGestion method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPersonaTipoNumeroIdentificacionConGestion()
    {
        System.out.println("buscarPersonaTipoNumeroIdentificacionConGestion");
        DtoPersona miDtoPersona = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.buscarPersonaTipoNumeroIdentificacionConGestion(miDtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPersonaNombreApellido method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPersonaNombreApellido()
    {
        System.out.println("buscarPersonaNombreApellido");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        ArrayList<DtoPersona> expResult = null;
        ArrayList<DtoPersona> result = instance.buscarPersonaNombreApellido(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPersonaNombreApellidoConGestion method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPersonaNombreApellidoConGestion()
    {
        System.out.println("buscarPersonaNombreApellidoConGestion");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        ArrayList<DtoPersona> expResult = null;
        ArrayList<DtoPersona> result = instance.buscarPersonaNombreApellidoConGestion(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPersonasClientes method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPersonasClientes() throws Exception
    {
        System.out.println("buscarPersonasClientes");
        ControllerNegocio instance = null;
        ArrayList<DtoPersona> expResult = null;
        ArrayList<DtoPersona> result = instance.buscarPersonasClientes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarPersona method, of class ControllerNegocio.
     */
    @Test
    public void testModificarPersona() throws Exception
    {
        System.out.println("modificarPersona");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.modificarPersona(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaCliente method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaCliente() throws Exception
    {
        System.out.println("darAltaCliente");
        DtoPersona dtoCliente = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.darAltaCliente(dtoCliente);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarCliente method, of class ControllerNegocio.
     */
    @Test
    public void testModificarCliente() throws Exception
    {
        System.out.println("modificarCliente");
        DtoPersona dtoCliente = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.modificarCliente(dtoCliente);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of asociarFkTipoIdentificacion method, of class ControllerNegocio.
     */
    @Test
    public void testAsociarFkTipoIdentificacion()
    {
        System.out.println("asociarFkTipoIdentificacion");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        int expResult = 0;
        int result = instance.asociarFkTipoIdentificacion(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of asociarNombreTipoIdentificacion method, of class ControllerNegocio.
     */
    @Test
    public void testAsociarNombreTipoIdentificacion()
    {
        System.out.println("asociarNombreTipoIdentificacion");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        String expResult = "";
        String result = instance.asociarNombreTipoIdentificacion(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of controlModificacionPersona method, of class ControllerNegocio.
     */
    @Test
    public void testControlModificacionPersona()
    {
        System.out.println("controlModificacionPersona");
        DtoPersona dtoPersonaOrginal = null;
        DtoPersona dtoPersonaModificada = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.controlModificacionPersona(dtoPersonaOrginal, dtoPersonaModificada);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarInmueble method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarInmueble_DtoInmueble()
    {
        System.out.println("buscarInmueble");
        DtoInmueble miDtoInmueble = null;
        ControllerNegocio instance = null;
        DtoInmueble expResult = null;
        DtoInmueble result = instance.buscarInmueble(miDtoInmueble);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarInmueble method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarInmueble_DtoTramite()
    {
        System.out.println("buscarInmueble");
        DtoTramite miDtoTramite = null;
        ControllerNegocio instance = null;
        DtoInmueble expResult = null;
        DtoInmueble result = instance.buscarInmueble(miDtoTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerConceptosTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerConceptosTramite()
    {
        System.out.println("obtenerConceptosTramite");
        DtoTipoDeTramite dtoTipoTramite = null;
        ControllerNegocio instance = null;
        ArrayList<DtoConcepto> expResult = null;
        ArrayList<DtoConcepto> result = instance.obtenerConceptosTramite(dtoTipoTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crearPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testCrearPresupuesto_5args()
    {
        System.out.println("crearPresupuesto");
        DtoPersona dtoPersona = null;
        DtoPresupuesto dtoPresupuesto = null;
        DtoTramite dtoTramite = null;
        DtoInmueble dtoInmueble = null;
        ArrayList<DtoItem> dtosItems = null;
        ControllerNegocio instance = null;
        int expResult = 0;
        int result = instance.crearPresupuesto(dtoPersona, dtoPresupuesto, dtoTramite, dtoInmueble, dtosItems);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crearPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testCrearPresupuesto_4args()
    {
        System.out.println("crearPresupuesto");
        DtoPersona dtoPersona = null;
        DtoPresupuesto dtoPresupuesto = null;
        DtoTramite dtoTramite = null;
        ArrayList<DtoItem> dtosItems = null;
        ControllerNegocio instance = null;
        int expResult = 0;
        int result = instance.crearPresupuesto(dtoPersona, dtoPresupuesto, dtoTramite, dtosItems);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPresupuestosPersona method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPresupuestosPersona() throws Exception
    {
        System.out.println("buscarPresupuestosPersona");
        DtoPersona dtoPersona = null;
        ControllerNegocio instance = null;
        ArrayList<DtoPresupuesto> expResult = null;
        ArrayList<DtoPresupuesto> result = instance.buscarPresupuestosPersona(dtoPersona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerRedObjetosPersona method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerRedObjetosPersona() throws Exception
    {
        System.out.println("obtenerRedObjetosPersona");
        Persona persona = null;
        ControllerNegocio instance = null;
        Persona expResult = null;
        Persona result = instance.obtenerRedObjetosPersona(persona);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarItemsPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarItemsPresupuesto()
    {
        System.out.println("buscarItemsPresupuesto");
        DtoPresupuesto dtoPresupuesto = null;
        ControllerNegocio instance = null;
        ArrayList<DtoItem> expResult = null;
        ArrayList<DtoItem> result = instance.buscarItemsPresupuesto(dtoPresupuesto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPresupuestoPorNumero method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPresupuestoPorNumero()
    {
        System.out.println("buscarPresupuestoPorNumero");
        DtoPresupuesto miDtoPresupuesto = null;
        ControllerNegocio instance = null;
        DtoPresupuesto expResult = null;
        DtoPresupuesto result = instance.buscarPresupuestoPorNumero(miDtoPresupuesto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testModificarPresupuesto() throws Exception
    {
        System.out.println("modificarPresupuesto");
        DtoPresupuesto miDtoPresupuesto = null;
        ArrayList<DtoItem> dtosItems = null;
        ArrayList<DtoItem> dtosItemsNuevos = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarPresupuesto(miDtoPresupuesto, dtosItems, dtosItemsNuevos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPresupuesto() throws Exception
    {
        System.out.println("buscarPresupuesto");
        DtoPresupuesto miDtoPresupuesto = null;
        ControllerNegocio instance = null;
        DtoPresupuesto expResult = null;
        DtoPresupuesto result = instance.buscarPresupuesto(miDtoPresupuesto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerProximaGestionDeEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerProximaGestionDeEscritura()
    {
        System.out.println("obtenerProximaGestionDeEscritura");
        ControllerNegocio instance = null;
        DtoGestionDeEscritura expResult = null;
        DtoGestionDeEscritura result = instance.obtenerProximaGestionDeEscritura();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarGestion method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarGestion()
    {
        System.out.println("buscarGestion");
        DtoGestionDeEscritura gestionBuscar = null;
        ControllerNegocio instance = null;
        GestionDeEscritura expResult = null;
        GestionDeEscritura result = instance.buscarGestion(gestionBuscar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of iniciarGestionDeEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testIniciarGestionDeEscritura()
    {
        System.out.println("iniciarGestionDeEscritura");
        DtoGestionDeEscritura dtoNuevaGestion = null;
        ControllerNegocio instance = null;
        DtoGestionDeEscritura expResult = null;
        DtoGestionDeEscritura result = instance.iniciarGestionDeEscritura(dtoNuevaGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarGestionDeEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testModificarGestionDeEscritura() throws Exception
    {
        System.out.println("modificarGestionDeEscritura");
        DtoGestionDeEscritura dtoGestionModificar = null;
        List<DtoPersona> listaDtoClientesAgregados = null;
        List<DtoPersona> listaDtoClientesEliminados = null;
        ControllerNegocio instance = null;
        DtoGestionDeEscritura expResult = null;
        DtoGestionDeEscritura result = instance.modificarGestionDeEscritura(dtoGestionModificar, listaDtoClientesAgregados, listaDtoClientesEliminados);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerEstadoDeGestion method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerEstadoDeGestion()
    {
        System.out.println("obtenerEstadoDeGestion");
        String nombreEstadoGestion = "";
        ControllerNegocio instance = null;
        EstadoDeGestion expResult = null;
        EstadoDeGestion result = instance.obtenerEstadoDeGestion(nombreEstadoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerDtoEstadoDeGestion method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerDtoEstadoDeGestion()
    {
        System.out.println("obtenerDtoEstadoDeGestion");
        DtoEstadoDeGestion nombreEstadoGestion = null;
        ControllerNegocio instance = null;
        DtoEstadoDeGestion expResult = null;
        DtoEstadoDeGestion result = instance.obtenerDtoEstadoDeGestion(nombreEstadoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarEstadoDeGestionDeEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testModificarEstadoDeGestionDeEscritura() throws Exception
    {
        System.out.println("modificarEstadoDeGestionDeEscritura");
        DtoGestionDeEscritura dtoGestion = null;
        ControllerNegocio instance = null;
        DtoGestionDeEscritura expResult = null;
        DtoGestionDeEscritura result = instance.modificarEstadoDeGestionDeEscritura(dtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerEstadoActualDeGestion method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerEstadoActualDeGestion()
    {
        System.out.println("obtenerEstadoActualDeGestion");
        DtoGestionDeEscritura dtoGestion = null;
        ControllerNegocio instance = null;
        DtoEstadoDeGestion expResult = null;
        DtoEstadoDeGestion result = instance.obtenerEstadoActualDeGestion(dtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerGestionesEnTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerGestionesEnTramite() throws Exception
    {
        System.out.println("obtenerGestionesEnTramite");
        ControllerNegocio instance = null;
        List<DtoGestionDeEscritura> expResult = null;
        List<DtoGestionDeEscritura> result = instance.obtenerGestionesEnTramite();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of archivarGestion method, of class ControllerNegocio.
     */
    @Test
    public void testArchivarGestion() throws Exception
    {
        System.out.println("archivarGestion");
        List<DtoGestionDeEscritura> listaDtoGestionesDeEscritura = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.archivarGestion(listaDtoGestionesDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarDtoGestion method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarDtoGestion()
    {
        System.out.println("buscarDtoGestion");
        DtoGestionDeEscritura gestionBuscar = null;
        ControllerNegocio instance = null;
        DtoGestionDeEscritura expResult = null;
        DtoGestionDeEscritura result = instance.buscarDtoGestion(gestionBuscar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerHistorialGestion method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerHistorialGestion()
    {
        System.out.println("obtenerHistorialGestion");
        DtoGestionDeEscritura dtoGestion = null;
        ControllerNegocio instance = null;
        List<DtoHistorial> expResult = null;
        List<DtoHistorial> result = instance.obtenerHistorialGestion(dtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registrarMovimientoHistorial method, of class ControllerNegocio.
     */
    @Test
    public void testRegistrarMovimientoHistorial()
    {
        System.out.println("registrarMovimientoHistorial");
        DtoGestionDeEscritura dtoGestion = null;
        ControllerNegocio instance = null;
        DtoHistorial expResult = null;
        DtoHistorial result = instance.registrarMovimientoHistorial(dtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerClienteReferenciaGestion method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerClienteReferenciaGestion() throws Exception
    {
        System.out.println("obtenerClienteReferenciaGestion");
        DtoGestionDeEscritura dtoGestion = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.obtenerClienteReferenciaGestion(dtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerDocumentosNecesarioTipoTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerDocumentosNecesarioTipoTramite()
    {
        System.out.println("obtenerDocumentosNecesarioTipoTramite");
        DtoTramite dtoTramite = null;
        ControllerNegocio instance = null;
        ArrayList<DtoTipoDeDocumento> expResult = null;
        ArrayList<DtoTipoDeDocumento> result = instance.obtenerDocumentosNecesarioTipoTramite(dtoTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerDocumentosNecesariosPorTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerDocumentosNecesariosPorTramite()
    {
        System.out.println("obtenerDocumentosNecesariosPorTramite");
        ArrayList<DtoTramite> listaDtoTramitesDeGestion = null;
        ControllerNegocio instance = null;
        ArrayList<DtoTramite> expResult = null;
        ArrayList<DtoTramite> result = instance.obtenerDocumentosNecesariosPorTramite(listaDtoTramitesDeGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerDocumentosPresentadosPorTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerDocumentosPresentadosPorTramite()
    {
        System.out.println("obtenerDocumentosPresentadosPorTramite");
        ArrayList<DtoTramite> listaDtoTramitesDeGestion = null;
        ControllerNegocio instance = null;
        ArrayList<DtoTramite> expResult = null;
        ArrayList<DtoTramite> result = instance.obtenerDocumentosPresentadosPorTramite(listaDtoTramitesDeGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerDocumentosNoPresentadosPorTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerDocumentosNoPresentadosPorTramite()
    {
        System.out.println("obtenerDocumentosNoPresentadosPorTramite");
        ArrayList<DtoTramite> listaDtoTramitesDeGestion = null;
        ControllerNegocio instance = null;
        ArrayList<DtoTramite> expResult = null;
        ArrayList<DtoTramite> result = instance.obtenerDocumentosNoPresentadosPorTramite(listaDtoTramitesDeGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of documentacionCompletaCliente method, of class ControllerNegocio.
     */
    @Test
    public void testDocumentacionCompletaCliente()
    {
        System.out.println("documentacionCompletaCliente");
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        boolean expResult = false;
        boolean result = instance.documentacionCompletaCliente(dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of documentacionCompletaExterna method, of class ControllerNegocio.
     */
    @Test
    public void testDocumentacionCompletaExterna()
    {
        System.out.println("documentacionCompletaExterna");
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        boolean expResult = false;
        boolean result = instance.documentacionCompletaExterna(dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of iscompletaDocumentacion method, of class ControllerNegocio.
     */
    @Test
    public void testIscompletaDocumentacion()
    {
        System.out.println("iscompletaDocumentacion");
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        boolean expResult = false;
        boolean result = instance.iscompletaDocumentacion(dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerDocNecesarioEntregadosNoEntregadosDeGestion method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerDocNecesarioEntregadosNoEntregadosDeGestion()
    {
        System.out.println("obtenerDocNecesarioEntregadosNoEntregadosDeGestion");
        DtoGestionDeEscritura dtoGestion = null;
        ControllerNegocio instance = null;
        DtoGestionDeEscritura expResult = null;
        DtoGestionDeEscritura result = instance.obtenerDocNecesarioEntregadosNoEntregadosDeGestion(dtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of ingresarDocumentos method, of class ControllerNegocio.
     */
    @Test
    public void testIngresarDocumentos() throws Exception
    {
        System.out.println("ingresarDocumentos");
        ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados = null;
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.ingresarDocumentos(listaDtoDocumentoPresentados, dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarDocumentacion method, of class ControllerNegocio.
     */
    @Test
    public void testModificarDocumentacion() throws Exception
    {
        System.out.println("modificarDocumentacion");
        ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados = null;
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.modificarDocumentacion(listaDtoDocumentoPresentados, dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarDocumentacionEntidadesExternas method, of class ControllerNegocio.
     */
    @Test
    public void testModificarDocumentacionEntidadesExternas() throws Exception
    {
        System.out.println("modificarDocumentacionEntidadesExternas");
        ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados = null;
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.modificarDocumentacionEntidadesExternas(listaDtoDocumentoPresentados, dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarDocumentacionReingreso method, of class ControllerNegocio.
     */
    @Test
    public void testModificarDocumentacionReingreso() throws Exception
    {
        System.out.println("modificarDocumentacionReingreso");
        ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados = null;
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.modificarDocumentacionReingreso(listaDtoDocumentoPresentados, dtoGestionDeEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of consultarDocumentosProximosVencer method, of class ControllerNegocio.
     */
    @Test
    public void testConsultarDocumentosProximosVencer()
    {
        System.out.println("consultarDocumentosProximosVencer");
        ControllerNegocio instance = null;
        List<DtoDocumentoPresentado> expResult = null;
        List<DtoDocumentoPresentado> result = instance.consultarDocumentosProximosVencer();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of ingresarDocumentacion method, of class ControllerNegocio.
     */
    @Test
    public void testIngresarDocumentacion() throws Exception
    {
        System.out.println("ingresarDocumentacion");
        DtoGestionDeEscritura dtoGestionDeEscritura = null;
        ControllerNegocio instance = null;
        instance.ingresarDocumentacion(dtoGestionDeEscritura);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crearEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testCrearEscritura() throws Exception
    {
        System.out.println("crearEscritura");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.crearEscritura(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscriturasPorRegistro method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscriturasPorRegistro()
    {
        System.out.println("buscarEscriturasPorRegistro");
        DtoPersona miEscribano = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscriturasPorRegistro(miEscribano);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscriturasPorRegistroFirmadas method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscriturasPorRegistroFirmadas()
    {
        System.out.println("buscarEscriturasPorRegistroFirmadas");
        DtoPersona miEscribano = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscriturasPorRegistroFirmadas(miEscribano);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscriturasPorRegistroFirmadasSinArchivo method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscriturasPorRegistroFirmadasSinArchivo()
    {
        System.out.println("buscarEscriturasPorRegistroFirmadasSinArchivo");
        DtoPersona miEscribano = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscriturasPorRegistroFirmadasSinArchivo(miEscribano);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscriturasPorRegistroFirmadasInscriptas method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscriturasPorRegistroFirmadasInscriptas()
    {
        System.out.println("buscarEscriturasPorRegistroFirmadasInscriptas");
        DtoPersona miEscribano = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscriturasPorRegistroFirmadasInscriptas(miEscribano);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscrituraPorNumero method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscrituraPorNumero()
    {
        System.out.println("buscarEscrituraPorNumero");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscrituraPorNumero(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscrituraPorNumeroFirmada method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscrituraPorNumeroFirmada()
    {
        System.out.println("buscarEscrituraPorNumeroFirmada");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscrituraPorNumeroFirmada(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscrituraPorNumeroFirmadaSinArchivo method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscrituraPorNumeroFirmadaSinArchivo()
    {
        System.out.println("buscarEscrituraPorNumeroFirmadaSinArchivo");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscrituraPorNumeroFirmadaSinArchivo(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscrituraPorNumeroFirmadaInscripta method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscrituraPorNumeroFirmadaInscripta()
    {
        System.out.println("buscarEscrituraPorNumeroFirmadaInscripta");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscrituraPorNumeroFirmadaInscripta(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscritura()
    {
        System.out.println("buscarEscritura");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        DtoEscritura expResult = null;
        DtoEscritura result = instance.buscarEscritura(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testModificarEscritura() throws Exception
    {
        System.out.println("modificarEscritura");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarEscritura(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarTramite method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarTramite()
    {
        System.out.println("buscarTramite");
        DtoTramite miDtoTramite = null;
        ControllerNegocio instance = null;
        DtoTramite expResult = null;
        DtoTramite result = instance.buscarTramite(miDtoTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerEscribanoEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerEscribanoEscritura()
    {
        System.out.println("obtenerEscribanoEscritura");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        DtoPersona expResult = null;
        DtoPersona result = instance.obtenerEscribanoEscritura(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarEscriturasGestion method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarEscriturasGestion()
    {
        System.out.println("buscarEscriturasGestion");
        DtoGestionDeEscritura miDtoGestion = null;
        ControllerNegocio instance = null;
        List<DtoEscritura> expResult = null;
        List<DtoEscritura> result = instance.buscarEscriturasGestion(miDtoGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of verificarSeInscribeEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testVerificarSeInscribeEscritura()
    {
        System.out.println("verificarSeInscribeEscritura");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.verificarSeInscribeEscritura(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crearTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testCrearTestimonio()
    {
        System.out.println("crearTestimonio");
        DtoTestimonio miDtoTestimonio = null;
        List<DtoCopia> listaDtoCopias = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.crearTestimonio(miDtoTestimonio, listaDtoCopias);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerTestimoniosEscritura method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerTestimoniosEscritura()
    {
        System.out.println("obtenerTestimoniosEscritura");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        List<DtoTestimonio> expResult = null;
        List<DtoTestimonio> result = instance.obtenerTestimoniosEscritura(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerMovimientosTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerMovimientosTestimonio()
    {
        System.out.println("obtenerMovimientosTestimonio");
        DtoTestimonio miDtoTestimonio = null;
        ControllerNegocio instance = null;
        List<DtoMovimientoTestimonio> expResult = null;
        List<DtoMovimientoTestimonio> result = instance.obtenerMovimientosTestimonio(miDtoTestimonio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerCopiasTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerCopiasTestimonio()
    {
        System.out.println("obtenerCopiasTestimonio");
        DtoTestimonio miDtoTestimonio = null;
        ControllerNegocio instance = null;
        List<DtoCopia> expResult = null;
        List<DtoCopia> result = instance.obtenerCopiasTestimonio(miDtoTestimonio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarCopiasTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testModificarCopiasTestimonio() throws Exception
    {
        System.out.println("modificarCopiasTestimonio");
        List<DtoCopia> listaDtoCopias = null;
        DtoTestimonio miDtoTestimonio = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarCopiasTestimonio(listaDtoCopias, miDtoTestimonio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of verificarTestimonioIngresadoParaInscribir method, of class ControllerNegocio.
     */
    @Test
    public void testVerificarTestimonioIngresadoParaInscribir()
    {
        System.out.println("verificarTestimonioIngresadoParaInscribir");
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.verificarTestimonioIngresadoParaInscribir(miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crearMovimientoTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testCrearMovimientoTestimonio() throws Exception
    {
        System.out.println("crearMovimientoTestimonio");
        DtoMovimientoTestimonio miDtoMovimientoTestimonio = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.crearMovimientoTestimonio(miDtoMovimientoTestimonio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarMovimientoTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarMovimientoTestimonio()
    {
        System.out.println("buscarMovimientoTestimonio");
        DtoMovimientoTestimonio miDtoMovimientoTestimonio = null;
        ControllerNegocio instance = null;
        DtoMovimientoTestimonio expResult = null;
        DtoMovimientoTestimonio result = instance.buscarMovimientoTestimonio(miDtoMovimientoTestimonio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarMovimientoTestimonio method, of class ControllerNegocio.
     */
    @Test
    public void testModificarMovimientoTestimonio() throws Exception
    {
        System.out.println("modificarMovimientoTestimonio");
        DtoMovimientoTestimonio miDtoMovimientoTestimonio = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarMovimientoTestimonio(miDtoMovimientoTestimonio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarMovimientoTestimonioInscripcion method, of class ControllerNegocio.
     */
    @Test
    public void testModificarMovimientoTestimonioInscripcion() throws Exception
    {
        System.out.println("modificarMovimientoTestimonioInscripcion");
        DtoMovimientoTestimonio miDtoMovimientoTestimonio = null;
        DtoEscritura miDtoEscritura = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarMovimientoTestimonioInscripcion(miDtoMovimientoTestimonio, miDtoEscritura);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarTipoDeFolio method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarTipoDeFolio()
    {
        System.out.println("buscarTipoDeFolio");
        DtoTipoDeFolio dtoTipoDeFolio = null;
        ControllerNegocio instance = null;
        DtoTipoDeFolio expResult = null;
        DtoTipoDeFolio result = instance.buscarTipoDeFolio(dtoTipoDeFolio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of verificarExistenciaFolios method, of class ControllerNegocio.
     */
    @Test
    public void testVerificarExistenciaFolios()
    {
        System.out.println("verificarExistenciaFolios");
        DtoFolio desde = null;
        DtoFolio hasta = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.verificarExistenciaFolios(desde, hasta);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registrarIngresoNuevosFolios method, of class ControllerNegocio.
     */
    @Test
    public void testRegistrarIngresoNuevosFolios()
    {
        System.out.println("registrarIngresoNuevosFolios");
        DtoFolio desde = null;
        DtoFolio hasta = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.registrarIngresoNuevosFolios(desde, hasta);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerListaFolios method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerListaFolios()
    {
        System.out.println("obtenerListaFolios");
        DtoFolio dtoDatosRegistroAnio = null;
        ControllerNegocio instance = null;
        List<DtoFolio> expResult = null;
        List<DtoFolio> result = instance.obtenerListaFolios(dtoDatosRegistroAnio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarFolio method, of class ControllerNegocio.
     */
    @Test
    public void testModificarFolio() throws Exception
    {
        System.out.println("modificarFolio");
        DtoFolio dtoFolioModificado = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarFolio(dtoFolioModificado);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarFoliosDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarFoliosDisponibles()
    {
        System.out.println("buscarFoliosDisponibles");
        Integer numeroRegistro = null;
        ControllerNegocio instance = null;
        List<DtoFolio> expResult = null;
        List<DtoFolio> result = instance.buscarFoliosDisponibles(numeroRegistro);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaPago method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaPago()
    {
        System.out.println("darAltaPago");
        DtoPago miDtoPago = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darAltaPago(miDtoPago);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarPagosPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarPagosPresupuesto()
    {
        System.out.println("buscarPagosPresupuesto");
        DtoPresupuesto miDtoPresupuesto = null;
        ControllerNegocio instance = null;
        List<DtoPago> expResult = null;
        List<DtoPago> result = instance.buscarPagosPresupuesto(miDtoPresupuesto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaUsuario method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaUsuario()
    {
        System.out.println("darAltaUsuario");
        DtoUsuario dtoUsuario = null;
        ControllerNegocio instance = null;
        DtoUsuario expResult = null;
        DtoUsuario result = instance.darAltaUsuario(dtoUsuario);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encriptaEnMD5 method, of class ControllerNegocio.
     */
    @Test
    public void testEncriptaEnMD5()
    {
        System.out.println("encriptaEnMD5");
        String stringAEncriptar = "";
        ControllerNegocio instance = null;
        String expResult = "";
        String result = instance.encriptaEnMD5(stringAEncriptar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarUsuario method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarUsuario() throws Exception
    {
        System.out.println("buscarUsuario");
        DtoUsuario dtoUsuario = null;
        ControllerNegocio instance = null;
        DtoUsuario expResult = null;
        DtoUsuario result = instance.buscarUsuario(dtoUsuario);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarUsuariosDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarUsuariosDisponibles() throws Exception
    {
        System.out.println("buscarUsuariosDisponibles");
        ControllerNegocio instance = null;
        ArrayList<DtoUsuario> expResult = null;
        ArrayList<DtoUsuario> result = instance.buscarUsuariosDisponibles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarUsuario method, of class ControllerNegocio.
     */
    @Test
    public void testModificarUsuario() throws Exception
    {
        System.out.println("modificarUsuario");
        DtoUsuario miDtoUsuario = null;
        ControllerNegocio instance = null;
        DtoUsuario expResult = null;
        DtoUsuario result = instance.modificarUsuario(miDtoUsuario);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isPasswordCorrect method, of class ControllerNegocio.
     */
    @Test
    public void testIsPasswordCorrect()
    {
        System.out.println("isPasswordCorrect");
        char[] j1 = null;
        char[] j2 = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.isPasswordCorrect(j1, j2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarRegistrosAuditoria method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarRegistrosAuditoria() throws Exception
    {
        System.out.println("buscarRegistrosAuditoria");
        DtoUsuario miDtoUsuario = null;
        ControllerNegocio instance = null;
        ArrayList<DtoRegistroAuditoria> expResult = null;
        ArrayList<DtoRegistroAuditoria> result = instance.buscarRegistrosAuditoria(miDtoUsuario);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registrarAuditoria method, of class ControllerNegocio.
     */
    @Test
    public void testRegistrarAuditoria()
    {
        System.out.println("registrarAuditoria");
        Object miObjeto = null;
        String modulo = "";
        ControllerNegocio instance = null;
        boolean expResult = false;
        boolean result = instance.registrarAuditoria(miObjeto, modulo);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaEscribano method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaEscribano() throws Exception
    {
        System.out.println("darAltaEscribano");
        DtoPersona dtoNuevoEscribano = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darAltaEscribano(dtoNuevoEscribano);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerListaEscribanosDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerListaEscribanosDisponibles()
    {
        System.out.println("obtenerListaEscribanosDisponibles");
        ControllerNegocio instance = null;
        List<DtoPersona> expResult = null;
        List<DtoPersona> result = instance.obtenerListaEscribanosDisponibles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registrarSuplenciaEscribano method, of class ControllerNegocio.
     */
    @Test
    public void testRegistrarSuplenciaEscribano()
    {
        System.out.println("registrarSuplenciaEscribano");
        DtoSuplencia detalleSuplencia = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.registrarSuplenciaEscribano(detalleSuplencia);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of consultarSuplencias method, of class ControllerNegocio.
     */
    @Test
    public void testConsultarSuplencias()
    {
        System.out.println("consultarSuplencias");
        DtoSuplencia dtoSuplenciasDesde = null;
        ControllerNegocio instance = null;
        List<DtoSuplencia> expResult = null;
        List<DtoSuplencia> result = instance.consultarSuplencias(dtoSuplenciasDesde);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaTipoDeTramite method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaTipoDeTramite() throws Exception
    {
        System.out.println("darAltaTipoDeTramite");
        DtoTipoDeTramite miDtoTipoDeTramite = null;
        ArrayList<DtoTipoDeDocumento> listaDtoTipoDeDocumentosDocumentos = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darAltaTipoDeTramite(miDtoTipoDeTramite, listaDtoTipoDeDocumentosDocumentos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarTiposDeTramiteHabilitados method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarTiposDeTramiteHabilitados()
    {
        System.out.println("buscarTiposDeTramiteHabilitados");
        ControllerNegocio instance = null;
        ArrayList<DtoTipoDeTramite> expResult = null;
        ArrayList<DtoTipoDeTramite> result = instance.buscarTiposDeTramiteHabilitados();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerPlantillasTramite method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerPlantillasTramite()
    {
        System.out.println("obtenerPlantillasTramite");
        DtoTipoDeTramite miDtoTipoDeTramite = null;
        ControllerNegocio instance = null;
        ArrayList<DtoPlantillaTramite> expResult = null;
        ArrayList<DtoPlantillaTramite> result = instance.obtenerPlantillasTramite(miDtoTipoDeTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarTipoDeTramite method, of class ControllerNegocio.
     */
    @Test
    public void testModificarTipoDeTramite() throws Exception
    {
        System.out.println("modificarTipoDeTramite");
        DtoTipoDeTramite miDtoTipoDeTramite = null;
        ArrayList<DtoTipoDeDocumento> listaDtoDocumentosAsociados = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarTipoDeTramite(miDtoTipoDeTramite, listaDtoDocumentosAsociados);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of eliminarTipoDeTramite method, of class ControllerNegocio.
     */
    @Test
    public void testEliminarTipoDeTramite() throws Exception
    {
        System.out.println("eliminarTipoDeTramite");
        DtoTipoDeTramite miDtoTipoTramite = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.eliminarTipoDeTramite(miDtoTipoTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaDocumento method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaDocumento() throws Exception
    {
        System.out.println("darAltaDocumento");
        DtoTipoDeDocumento miDtoTipoDocumento = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darAltaDocumento(miDtoTipoDocumento);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarTiposDeDocumentoDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarTiposDeDocumentoDisponibles()
    {
        System.out.println("buscarTiposDeDocumentoDisponibles");
        ControllerNegocio instance = null;
        ArrayList<DtoTipoDeDocumento> expResult = null;
        ArrayList<DtoTipoDeDocumento> result = instance.buscarTiposDeDocumentoDisponibles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buscarTipoDeDocumento method, of class ControllerNegocio.
     */
    @Test
    public void testBuscarTipoDeDocumento()
    {
        System.out.println("buscarTipoDeDocumento");
        String nombre = "";
        ControllerNegocio instance = null;
        DtoTipoDeDocumento expResult = null;
        DtoTipoDeDocumento result = instance.buscarTipoDeDocumento(nombre);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarTipoDeDocumento method, of class ControllerNegocio.
     */
    @Test
    public void testModificarTipoDeDocumento() throws Exception
    {
        System.out.println("modificarTipoDeDocumento");
        DtoTipoDeDocumento miDto = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.modificarTipoDeDocumento(miDto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of eliminarTipoDeDocumento method, of class ControllerNegocio.
     */
    @Test
    public void testEliminarTipoDeDocumento() throws Exception
    {
        System.out.println("eliminarTipoDeDocumento");
        DtoTipoDeDocumento miDto = null;
        ControllerNegocio instance = null;
        DtoFlag expResult = null;
        DtoFlag result = instance.eliminarTipoDeDocumento(miDto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaConcepto method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaConcepto()
    {
        System.out.println("darAltaConcepto");
        DtoConcepto miDto = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darAltaConcepto(miDto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerListaConceptosDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerListaConceptosDisponibles()
    {
        System.out.println("obtenerListaConceptosDisponibles");
        ControllerNegocio instance = null;
        List<DtoConcepto> expResult = null;
        List<DtoConcepto> result = instance.obtenerListaConceptosDisponibles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarConcepto method, of class ControllerNegocio.
     */
    @Test
    public void testModificarConcepto() throws Exception
    {
        System.out.println("modificarConcepto");
        DtoConcepto conceptoParaModificar = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarConcepto(conceptoParaModificar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of eliminarConcepto method, of class ControllerNegocio.
     */
    @Test
    public void testEliminarConcepto() throws Exception
    {
        System.out.println("eliminarConcepto");
        DtoConcepto conceptoParaEliminar = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.eliminarConcepto(conceptoParaEliminar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of existeConcepto method, of class ControllerNegocio.
     */
    @Test
    public void testExisteConcepto()
    {
        System.out.println("existeConcepto");
        String nombreConcepto = "";
        ControllerNegocio instance = null;
        boolean expResult = false;
        boolean result = instance.existeConcepto(nombreConcepto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darAltaEstadoDeGestion method, of class ControllerNegocio.
     */
    @Test
    public void testDarAltaEstadoDeGestion() throws Exception
    {
        System.out.println("darAltaEstadoDeGestion");
        DtoEstadoDeGestion miDto = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darAltaEstadoDeGestion(miDto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerListaEstadosDeGestionDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerListaEstadosDeGestionDisponibles()
    {
        System.out.println("obtenerListaEstadosDeGestionDisponibles");
        ControllerNegocio instance = null;
        List<DtoEstadoDeGestion> expResult = null;
        List<DtoEstadoDeGestion> result = instance.obtenerListaEstadosDeGestionDisponibles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarEstadoDeGestion method, of class ControllerNegocio.
     */
    @Test
    public void testModificarEstadoDeGestion() throws Exception
    {
        System.out.println("modificarEstadoDeGestion");
        DtoEstadoDeGestion dtoEstadoDeGestion = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarEstadoDeGestion(dtoEstadoDeGestion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of darDeAltaTipoDeFolio method, of class ControllerNegocio.
     */
    @Test
    public void testDarDeAltaTipoDeFolio() throws Exception
    {
        System.out.println("darDeAltaTipoDeFolio");
        DtoTipoDeFolio nuevoDtoTipoDeFolio = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.darDeAltaTipoDeFolio(nuevoDtoTipoDeFolio);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerListaTiposDeFoliosDisponibles method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerListaTiposDeFoliosDisponibles()
    {
        System.out.println("obtenerListaTiposDeFoliosDisponibles");
        ControllerNegocio instance = null;
        List<DtoTipoDeFolio> expResult = null;
        List<DtoTipoDeFolio> result = instance.obtenerListaTiposDeFoliosDisponibles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarTipoDeFolios method, of class ControllerNegocio.
     */
    @Test
    public void testModificarTipoDeFolios() throws Exception
    {
        System.out.println("modificarTipoDeFolios");
        DtoTipoDeFolio dtoTipoDeFolioModificar = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarTipoDeFolios(dtoTipoDeFolioModificar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of eliminarTipoDeFolio method, of class ControllerNegocio.
     */
    @Test
    public void testEliminarTipoDeFolio() throws Exception
    {
        System.out.println("eliminarTipoDeFolio");
        DtoTipoDeFolio dtoTipoDeFolioEliminar = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.eliminarTipoDeFolio(dtoTipoDeFolioEliminar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of existePlantillaPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testExistePlantillaPresupuesto()
    {
        System.out.println("existePlantillaPresupuesto");
        DtoTipoDeTramite dtoTipoDeTramite = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.existePlantillaPresupuesto(dtoTipoDeTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crearPlantillaPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testCrearPlantillaPresupuesto() throws Exception
    {
        System.out.println("crearPlantillaPresupuesto");
        DtoTipoDeTramite miDtoTipoDeTramite = null;
        ArrayList<DtoConcepto> misDtoConceptos = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.crearPlantillaPresupuesto(miDtoTipoDeTramite, misDtoConceptos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtenerPlantillasPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testObtenerPlantillasPresupuesto()
    {
        System.out.println("obtenerPlantillasPresupuesto");
        DtoTipoDeTramite miDtoTipoDeTramite = null;
        ControllerNegocio instance = null;
        ArrayList<DtoPlantillaPresupuesto> expResult = null;
        ArrayList<DtoPlantillaPresupuesto> result = instance.obtenerPlantillasPresupuesto(miDtoTipoDeTramite);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modificarPlantillaPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testModificarPlantillaPresupuesto() throws Exception
    {
        System.out.println("modificarPlantillaPresupuesto");
        DtoTipoDeTramite miDtoTipoDeTramite = null;
        ArrayList<DtoConcepto> misDtoConceptos = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.modificarPlantillaPresupuesto(miDtoTipoDeTramite, misDtoConceptos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of eliminarPlantillaPresupuesto method, of class ControllerNegocio.
     */
    @Test
    public void testEliminarPlantillaPresupuesto() throws Exception
    {
        System.out.println("eliminarPlantillaPresupuesto");
        ArrayList<DtoPlantillaPresupuesto> dtosPlantillasPresupuesto = null;
        ControllerNegocio instance = null;
        Boolean expResult = null;
        Boolean result = instance.eliminarPlantillaPresupuesto(dtosPlantillasPresupuesto);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
