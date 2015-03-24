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
class ControlGui extends JFrame{
    private ControlAgente miControl;
    
    private JTextField capacidad;
    private JComboBox asignatura;
    private JButton asignar,ampliar, cancelar;
    
    
    ControlGui(ControlAgente e){
        
        
        miControl = e;
        
        JPanel a= new JPanel();
        a.setLayout(new GridLayout(4,4));
        a.add(new Label("Universidad Autónoma de Yucatán. Fcultad de Matemáticas"));
        a.add(new Label("Control Escolar"));
        JComboBox i= new JComboBox();
        i.setLayout(new GridLayout(7, 7));
        i.add(new Label("Asignatura: "));
        JTextField s = new JTextField();
       s.setLayout(new GridLayout(5,5));
       s.add(new Label("Capacidad: "));
       JButton as = new JButton();
       as.setLayout(new GridLayout(4, 4));
       as.add(new JButton("Asignar cupo"));
       JButton am = new JButton();
        am.setLayout(new GridLayout(4, 4));
       am.add(new JButton("Ampliar cupo"));
       JButton can = new JButton();
        can.setLayout(new GridLayout(4, 4));
       can.add(new JButton("Cancelar"));
       
        
    }
}
