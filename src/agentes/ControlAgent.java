package agentes;

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent
 * systems in compliance with the FIPA specifications. Copyright (C) 2000 CSELT
 * S.p.A. * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 2.1 of the License. * This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * ***************************************************************
 */
import modelo.Conexion;
import jade.core.AID;
import vistas.Inicio;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import vistas.Control;

public class ControlAgent extends Agent {

    // The catalogue of books for sale (maps the title of a book to its price)
    
    // The GUI by means of which the user can add books in the catalogue
    private Control myGui;
    private String targetBookTitle;
    // Put agent initializations here
    private Hashtable catalogue;
    private AID[] AlumnoAgent;

    protected void setup() {
        // Create the catalogue
        catalogue = new Hashtable();

        // Create and show the GUI 
        myGui = new Control(this);
        myGui.setVisible(true);

        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                System.out.println("Intentar el insert");
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("book-selling");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    AlumnoAgent = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        AlumnoAgent[i] = result[i].getName();
                       
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                myAgent.addBehaviour(new ControlAgent.RequestPerformer());
            }
        });

    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        myGui.dispose();
        // Printout a dismissal message
        System.out.println("agent " + getAID().getName() + " terminating.");
    }

    /**
     * This is invoked by the GUI when the user adds a new book for sale
     */
    public void updateCatalogue(final String nombre, final int capacidad) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                Connection activo;
                targetBookTitle = nombre;
                 
                int numFilas;
                Conexion con = new Conexion();
                activo = con.getConexion();
                String order = "UPDATE materia SET capacidad= ? where nombre = ?";
                try {
                    PreparedStatement preparedStmt = activo.prepareStatement(order);
                    preparedStmt.setInt(1, capacidad);
                    preparedStmt.setString(2, nombre);
                    numFilas = preparedStmt.executeUpdate();
                    // sentencia = activo.createStatement();
                    // numFilas = sentencia.executeUpdate(order);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
                }
                con.cerrarConexion(activo);
                System.out.println("Se actualizo correctamente la capacidad para la materia: " + nombre);
            }
        });
    }
    
    public int getCapacidadActual(Connection activo, final String materia) {
        int capacidadMateria = 0;
        ResultSet resultadocapacidadMateria;
        String queryCapacidadMateria = "select capacidad from materia where nombre = ? limit 1";
        try {
            PreparedStatement preparedStmtMateria = activo.prepareStatement(queryCapacidadMateria);
            preparedStmtMateria.setString(1, materia);
            resultadocapacidadMateria = preparedStmtMateria.executeQuery();
            while (resultadocapacidadMateria.next()) {
               capacidadMateria = resultadocapacidadMateria.getInt("capacidad");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error en consulta de de capacidad de la materia: " + ex.getMessage());
        }
        return capacidadMateria;
    }

    /**
     * Inner class RequestPerformer. This is the behaviour used by Book-buyer
     * agents to request seller agents the target book.
     */
    private class RequestPerformer extends Behaviour {

        private AID bestSeller; // The agent who provides the best offer 
        private int bestPrice;  // The best offered price
        private String matricula;
        private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < AlumnoAgent.length; ++i) {
                        cfp.addReceiver(AlumnoAgent[i]);
                    }
                    if(targetBookTitle!=null){
                    cfp.setContent(targetBookTitle);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    }
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                      
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            if (bestSeller == null) {
                                // This is the best offer at present 
                                bestSeller = reply.getSender();
                                matricula = reply.getContent();
                            } else   {
                            }
                        }
                        repliesCnt++;
                        step = 2;
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId("book-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                             //myAgent.doDelete();
                        } else {
                            System.out.println("Esperando nuevos Lugares");
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }
        

        public boolean done() {
            if (step == 2 && bestSeller == null) {
                System.out.println("Esperando por Nuevos luagares");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }

}
