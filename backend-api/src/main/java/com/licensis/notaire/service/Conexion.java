package com.licensis.notaire.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Conexion implements Serializable
{

    protected static Log log = LogFactory.getLog(Conexion.class);
    private static Conexion instancia = null;
    private Connection miConexion = null;
    private String datosConexion;

    private Properties notaireProperties;

    private Conexion(int nothig) throws FileNotFoundException, IOException
    {
        log.debug("test conexion 1");
        try (InputStream archivoPropiedades = new FileInputStream("config.properties"))
        {
            log.debug("cargando el archivo de conexion");
            notaireProperties = new Properties();
            notaireProperties.load(archivoPropiedades);
        }
    }

    private Conexion()
    {

    }

    public static Conexion getInstancia() throws IOException
    {
        if (instancia == null)
        {
            instancia = new Conexion(0);
        }
        return instancia;
    }

    public void setDatosConexion(String nuevaConexion)
    {
        datosConexion = nuevaConexion;
    }

    public Connection getConexion() throws FileNotFoundException, IOException
    {
        log.debug("test conexion 1");
        System.out.println("Test conexion");
        try (InputStream archivoPropiedades = new FileInputStream("config.properties"))
        {
            log.debug("cargando el archivo de conexion");
            notaireProperties = new Properties();
            notaireProperties.load(archivoPropiedades);
        }
        try
        {
            miConexion = DriverManager.getConnection("jdbc:mysql://localhost", "matias", "");
        }
        catch (SQLException ex)
        {

            System.out.println("Error de conexion. No se pudo establecer una conexion con el servido de base de datos!: " + ex.getMessage());
        }
        return miConexion;
    }

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

    public Properties getNotaireProperties()
    {
        return notaireProperties;
    }

    public void setNotaireProperties(Properties notaireProperties)
    {
        this.notaireProperties = notaireProperties;
    }
}
