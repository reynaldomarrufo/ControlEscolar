/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author oscar
 */
public class Conexion {

    private static String host="localhost";
    private static String bd= "control";
    private static String login="postgres";
    private static String password="root";
 
            
    public static Connection getConexion(String host, String bd, String login, String password){
    Connection conexion=null;
    String urlConexion="jdbc:postgresql://"+ host+":5433/"+bd;
    try{
        conexion=DriverManager.getConnection(urlConexion, login,password);
    }
    catch(SQLException ex){
        JOptionPane.showMessageDialog(null, "Error apertura: " + ex.getMessage());
    }
    return conexion;
    }
    
    public static Connection getConexion(){
        return getConexion(host,bd,login,password);
    }
    
    public static void cerrarConexion(Connection conexion){
        if(conexion !=null){
            try {
                if(conexion.isClosed())
                    conexion.close();
            } catch(SQLException ex){
        JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
    }
        }
    }
    
    
}
