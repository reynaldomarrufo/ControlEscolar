/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import control.consultas;
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

import java.util.*;
import javax.swing.JOptionPane;
import vistas.Inicio;

/**
 *
 * @author oscar
 */
public class AlumnoAgent extends Agent {

    // The catalogue of books for sale (maps the title of a book to its price)
    private Hashtable catalogue;
    // The GUI by means of which the user can add books in the catalogue
    private Inicio myGui;

    // Put agent initializations here
    protected void setup() {
        // Create the catalogue
        catalogue = new Hashtable();
        // Create and show the GUI 
        myGui = new Inicio(this);
        myGui.setVisible(true);
        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestsServer());
        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());
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
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    /**
     * This is invoked by the GUI when the user adds a new book for sale
     */
    public void updateCatalogue(final String matricula, final String nombre, final String accion, final String materia) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {

                if (cupoDisponible(materia)) {
                    int idAlumno = 0;
                    int idMateria = 0;
                    Connection activo;
                    ResultSet resultado;
                    ResultSet resultadoMateria;
                    consultas con = new consultas();
                    activo = con.getConexion();
                    String queryIdAlumno = "select id from alumno where matricula = ? limit 1";
                    String queryIdMateria = "select id from materia where nombre = ? limit 1";
                    try {
                        PreparedStatement preparedStmt = activo.prepareStatement(queryIdAlumno);
                        PreparedStatement preparedStmtMateria = activo.prepareStatement(queryIdMateria);
                        preparedStmt.setString(1, matricula);
                        preparedStmtMateria.setString(1, materia);
                        resultado = preparedStmt.executeQuery();
                        resultadoMateria = preparedStmtMateria.executeQuery();
                        while (resultado.next()) {
                            idAlumno = Integer.parseInt(resultado.getString("id"));

                        }
                        while (resultadoMateria.next()) {
                            idMateria = Integer.parseInt(resultadoMateria.getString("id"));
                        }
                        if (resultado != null && resultadoMateria != null) {

                            String insertGrupo = "Insert into grupo (alumno_id,materia_id) values (?,?)";
                            PreparedStatement preparedStmtGrupo = activo.prepareStatement(insertGrupo);
                            preparedStmtGrupo.setInt(1, idAlumno);
                            preparedStmtGrupo.setInt(2, idMateria);
                            preparedStmtGrupo.execute();
                        }

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
                    }
                    con.cerrarConexion(activo);

                } else {

                    catalogue.put(materia, matricula);
                    System.out.println(materia + " inserted into catalogue. matricula = " + matricula);
                    JOptionPane.showMessageDialog(null, "Cupo LLENO");
                }
            }

        });
    }

    public boolean cupoDisponible(String materia) {
        Connection activo;
        consultas con = new consultas();
        activo = con.getConexion();
        int inscritos = 0;
        int capacidadT = 0;
        ResultSet resultado;
        ResultSet resultadoMateria;
        String order = "select count(*) as total from grupo where materia_id= ?";
        String capacidad = "select capacidad from materia where nombre= ?";
        try {
            PreparedStatement preparedStmt = activo.prepareStatement(order);
            PreparedStatement preparedStmtC = activo.prepareStatement(capacidad);
            preparedStmtC.setString(1, materia);
            preparedStmt.setInt(1, 1);
            resultado = preparedStmt.executeQuery();
            resultadoMateria = preparedStmtC.executeQuery();
                   // sentencia = activo.createStatement();
            // numFilas = sentencia.executeUpdate(order);
            while (resultado.next()) {
                inscritos = Integer.parseInt(resultado.getString("total"));
            }
            while (resultadoMateria.next()) {
                capacidadT = Integer.parseInt(resultadoMateria.getString("capacidad"));
            }
            if (inscritos < capacidadT) {
                con.cerrarConexion(activo);
                return true;
            } else {
                
                con.cerrarConexion(activo);
                return false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error validacion: " + ex.getMessage());
        }
        con.cerrarConexion(activo);

        return false;
    }

    /**
     * Inner class OfferRequestsServer. This is the behaviour used by
     * Book-seller agents to serve incoming requests for offer from buyer
     * agents. If the requested book is in the local catalogue the seller agent
     * replies with a PROPOSE message specifying the price. Otherwise a REFUSE
     * message is sent back.
     */
    private class OfferRequestsServer extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // CFP Message received. Process it
                String materia = msg.getContent();
                String matricula;
                ACLMessage reply = msg.createReply();
                matricula = (String) catalogue.get(materia);
                if (matricula != null) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(matricula));
                } else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

    /**
     * Inner class PurchaseOrdersServer. This is the behaviour used by
     * Book-seller agents to serve incoming offer acceptances (i.e. purchase
     * orders) from buyer agents. The seller agent removes the purchased book
     * from its catalogue and replies with an INFORM message to notify the buyer
     * that the purchase has been sucesfully completed.
     */
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            AlumnoAgent validacion = new AlumnoAgent();
            if (msg != null ) {
                // ACCEPT_PROPOSAL Message received. Process it
                String materia = msg.getContent();
                ACLMessage reply = msg.createReply();
                String matricula = (String) catalogue.remove(materia);
               
                if (matricula != null & validacion.cupoDisponible(materia)) {
                   
                    
                        addAlumno(materia, matricula);
                    
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(materia + " sold to agent " + msg.getSender().getName());
                } else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }

        private boolean addAlumno(String materia, String matricula) {
            int idAlumno = 0;
            int idMateria = 0;
            Connection activo;
            ResultSet resultado;
            ResultSet resultadoMateria;
            consultas con = new consultas();
            activo = con.getConexion();
            String queryIdAlumno = "select id from alumno where matricula = ? limit 1";
            String queryIdMateria = "select id from materia where nombre = ? limit 1";
            try {
                PreparedStatement preparedStmt = activo.prepareStatement(queryIdAlumno);
                PreparedStatement preparedStmtMateria = activo.prepareStatement(queryIdMateria);
                preparedStmt.setString(1, matricula);
                preparedStmtMateria.setString(1, materia);
                resultado = preparedStmt.executeQuery();
                resultadoMateria = preparedStmtMateria.executeQuery();
                while (resultado.next()) {
                    idAlumno = Integer.parseInt(resultado.getString("id"));

                }
                while (resultadoMateria.next()) {
                    idMateria = Integer.parseInt(resultadoMateria.getString("id"));
                }
                if (resultado != null && resultadoMateria != null) {

                    String insertGrupo = "Insert into grupo (alumno_id,materia_id) values (?,?)";
                    PreparedStatement preparedStmtGrupo = activo.prepareStatement(insertGrupo);
                    preparedStmtGrupo.setInt(1, idAlumno);
                    preparedStmtGrupo.setInt(2, idMateria);
                    preparedStmtGrupo.execute();
                    System.out.println("correcto");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
            }
            con.cerrarConexion(activo);
            return false;
        }
    }  // End of inner class OfferRequestsServer

}
