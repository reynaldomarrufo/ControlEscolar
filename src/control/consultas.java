/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author Emmanuel
 */
public class consultas {
    
    private static String host ="localhost";
    private static String bd = "control";
    private static String login = "postgres";
    private static String password = "reynaldo";
    
    public static Connection getConexion(String host,String bd,String login,String password){
       Connection conexion =null;
       String urlConexion = "jdbc:postgresql://" + host + "/" + bd;
        try {
            conexion = DriverManager.getConnection(urlConexion, login, password);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error apertura: " + ex.getMessage());
        }
       return conexion;
       
    }
    
    public static Connection getConexion(){
        return getConexion(host,bd,login,password);
    }
    
    public static void cerrarConexion(Connection con){
        if(con != null){
            try {
                if(!con.isClosed())
                    con.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error cierre: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
       int numFilas=0;
       Connection con = getConexion();
       String orden = "insert into alumno (nombre, matricula) values ('Reynaldo Marrufo','11216289')";
       Statement sentencia = null;
        try {
            sentencia = con.createStatement();
            for(int i=0;i<1000;i++){
               //numFilas = sentencia.executeUpdate(orden);
            }
            
            System.out.println("YUPI!!, orden ejecutada, se afectaron: "+ numFilas+" filas");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error statement: " + e.getMessage());
        }
        
        orden = "select * from alumno";
        ResultSet rs = null;
        try {
            rs = sentencia.executeQuery(orden);
            while(rs.next()){
                System.out.println(rs.getInt("id"));
                System.out.println(" - " + rs.getString("nombre"));
                System.out.println(" - " + rs.getString("matricula"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error en la consulta: " + e.getMessage());
        }
        
        cerrarConexion(con);
    }  
}
