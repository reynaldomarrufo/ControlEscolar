/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.awt.GridLayout;
import javax.swing.*;

/**
 *
 * @author Emmanuel
 */
class PrincipalGui extends JFrame {
    
    private JButton Inscripcion, Crear, Salir;
    
    
    PrincipalGui(){
        
        JButton ins = new JButton();
        ins.setLayout(new GridLayout(4, 4));
        ins.add(new JButton("Inscripción a Asignatura"));
         JButton cre = new JButton();
        cre.setLayout(new GridLayout(4, 4));
        cre.add(new JButton("Crear grupo de Asignatura"));
         JButton sal = new JButton();
        sal.setLayout(new GridLayout(4, 4));
        sal.add(new JButton("salir"));
        
    }
    
}
