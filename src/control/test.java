/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import static control.consultas.cerrarConexion;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author oscar
 */
public class test {
    //
            
   // consultas con = new consultas();
   public static void main(String[] args) {
       Connection activo;
       int numFilas;
       consultas con= new consultas();
       activo=con.getConexion();
        String order= "insert into alumno (nombre,matricula) values "+ "('Oscar','01')";
       Statement sentencia = null;
        try {
            sentencia= activo.createStatement();
            numFilas= sentencia.executeUpdate(order);
            System.out.println("Query Correcto numero de filas afectadas, " +numFilas);
        } catch(SQLException ex){
        JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
    }
        
       order= "select * from alumno";
       ResultSet rs= null;
        try {
            rs= sentencia.executeQuery(order);
            while(rs.next()){
            System.out.println(rs.getInt("id"));
                System.out.println(" - "+ rs.getString("nombre"));
            System.out.println(" - " + rs.getString("matricula"));
        }
        } catch(SQLException ex){
        JOptionPane.showMessageDialog(null, "Error en la consulta Select: " + ex.getMessage());
    }
        con.cerrarConexion(activo);
   }
}
