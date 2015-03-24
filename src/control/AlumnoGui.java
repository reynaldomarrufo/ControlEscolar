/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import jade.core.AID;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;


/**
 *
 * @author Emmanuel
 */
 class AlumnoGui extends JFrame {
    private AlumnoAgente miAgente;
    
    private JTextField matricula, nombre;
    private JButton aceptar, cancelar;
    private JComboBox asignatura;
    private JRadioButton Alta, Baja;

     AlumnoGui(AlumnoAgente a){
//        super(a);
        
        miAgente = a;
        
        JPanel p= new JPanel();
        p.setLayout(new GridLayout(4,4));
        p.add(new Label("Matricula:"));
        matricula = new JTextField(15);
        JPanel q= new JPanel();
        q.setLayout(new GridLayout(4,4));
        q.add(new Label("Nombre:"));
        nombre = new JTextField(15);
        JRadioButton t = new JRadioButton();
        t.setLayout(new GridLayout(2, 2));
        t.add(new Label("Alta"));
        JRadioButton s = new JRadioButton();
        s.setLayout(new GridLayout(2, 2));
        s.add(new Label("Baja"));
        JComboBox e = new JComboBox();
        e.setLayout(new GridLayout(7,7));
        e.add(new Label("Asignatura:"));
        JButton c = new JButton();
        c.setLayout(new GridLayout(4,4));
        c.add(new JButton("aceptar"));
        JButton d = new JButton();
        d.setLayout(new GridLayout(4,4));
        d.add(new JButton("cancelar"));
        
        
        
       
        
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
       
    
    
}
 }
