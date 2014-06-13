/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.servicios;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase concreta Conexion, que implementa la interfaz Serializable y el patron Singleton. Tiene
 * como finalidad definir y establecer una conexion de red con el servidor de base de datos.
 *
 * @author matias
 */
public class Conexion implements Serializable
{

    /**
     * Atributo privado y estatico que representa la instancia de la clase conecion (Singleton)
     */
    private static Conexion instancia = null;
    /**
     * Atributo que representa la instancia de una conexion.
     */
    private Connection miConexion = null;    // variable de conexion
    /**
     * Atributo tipo String que contiene los datos de conexion. <ol> <li> Path de la base de datos
     * <li> Usuario de la base de datos. <li> Contraseña de la base de datos. </ol<
     */
    private String datosConexion;

    /**
     * Constructor privado y sin argumentos para la clase conexion.
     */
    private Conexion()
    {
    }

    /**
     * Metodo publico y estatico para obtener la instancia de la clase conexion (Singleton)
     *
     * @return Una instancia de Conexion.
     */
    public static Conexion getInstancia()
    {
        if (instancia == null)
        {
            instancia = new Conexion();
        }
        return instancia;
    }

    /**
     * Permite establecer los datos de la conexion: <ol> <li> Path de la base de datos <li> Usuario
     * de la base de datos. <li> Contraseña de la base de datos. </ol<
     *
     * @param nuevaConexion tipo String.
     */
    public void setDatosConexion(String nuevaConexion)
    {
        datosConexion = nuevaConexion;
    }

    /**
     * Crea una nueva conexion con el servidor de base de datos.
     *
     * @return Un Objeto tipo Connection si se pudo establecer una conexion o nulo en caso
     * contrario.
     */
    public Connection getConexion()
    {
        try
        {
            //TODO: usar un archivo de properties para este tipo de configuraciones.
            miConexion = DriverManager.getConnection("jdbc:mysql://localhost/notaire", "root", "");

        }
        catch (SQLException ex)
        {

            System.out.println("Error de conexion. No se pudo establecer una conexion con el servido de base de datos!: " + ex.getMessage());
        }
        return miConexion;
    }

    /**
     * Desconecta la conexion (cierra).
     */
    public void desconectar()
    {
        try
        {
            if (miConexion != null)
            {
                miConexion.close();
            }
        }
        catch (SQLException ex)
        {
            System.out.println("No se ha podido cerrar la conexion");
        }

    }
}
