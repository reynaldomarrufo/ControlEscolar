/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

/**
 *
 * @author oscar
 */
public class materia {
    private String nombre; 
    private int id;
 
    public materia(String nombre, int capacidad){ 
        this.nombre=nombre;
        this.id=id;
    }
 
    public int getId(){ 
        return id;
    }
 
    @Override
    public String toString(){ 
        return nombre;
    }
}
