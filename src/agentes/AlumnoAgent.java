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
                Connection activo;

                int idAlumno;
                int idMateria;
                consultas con = new consultas();
                activo = con.getConexion();

                idAlumno = getAlumnoId(activo, matricula, nombre);
                idMateria = getMateriaId(activo, materia);
                if (accion == "alta") {
                    if (agregarCarga(activo, idAlumno, idMateria, materia, matricula)) {
                        JOptionPane.showMessageDialog(null, "Carga academica correcta");
                    }
                } else if (accion == "baja") {
                    eliminarCarga(activo, idAlumno, idMateria);
                    JOptionPane.showMessageDialog(null, "Carga eliminada");
                }
                con.cerrarConexion(activo);
            }

        });
    }

    public int getAlumnoId(Connection activo, final String matricula, final String nombre) {
        int idAlumno = 0;
        ResultSet resultado;
        String queryIdAlumno = "select id from alumno where matricula = ? limit 1";
        try {
            PreparedStatement preparedStmt = activo.prepareStatement(queryIdAlumno);
            preparedStmt.setString(1, matricula);
            resultado = preparedStmt.executeQuery();
            while (resultado.next()) {
                idAlumno = Integer.parseInt(resultado.getString("id"));
            }
            if (resultado != null) {
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
        }
        return idAlumno;
    }

    public int getMateriaId(Connection activo, final String materia) {
        int idMateria = 0;
        ResultSet resultadoMateria;
        String queryIdMateria = "select id from materia where nombre = ? limit 1";
        try {
            PreparedStatement preparedStmtMateria = activo.prepareStatement(queryIdMateria);
            preparedStmtMateria.setString(1, materia);
            resultadoMateria = preparedStmtMateria.executeQuery();
            while (resultadoMateria.next()) {
                idMateria = Integer.parseInt(resultadoMateria.getString("id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
        }
        return idMateria;
    }

    public boolean agregarCarga(Connection activo, final int idAlumno, final int idMateria, final String materia, final String matricula) {
        if (cupoDisponible(activo, idMateria)) {
            try {
                if (true) {
                    String insertGrupo = "Insert into grupo (alumno_id,materia_id) values (?,?)";
                    PreparedStatement preparedStmtGrupo = activo.prepareStatement(insertGrupo);
                    preparedStmtGrupo.setInt(1, idAlumno);
                    preparedStmtGrupo.setInt(2, idMateria);
                    preparedStmtGrupo.execute();
                    return true;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
            }

        } else {
            String matriculas;
            matriculas = (String) catalogue.get(materia);
            System.out.println("matriculas catalogo " + matriculas);
            if (matriculas != null) {
                matriculas = matriculas + "," + matricula;
            } else {
                matriculas = matricula;
            }
            catalogue.put(materia, matriculas);
            System.out.println(materia + " inserted into catalogue. matricula = " + matricula);
            JOptionPane.showMessageDialog(null, "Cupo no disponible, Agregado a lista de espera");
        }
        return false;
    }

    public boolean eliminarCarga(Connection activo, final int idAlumno, final int idMateria) {
        try {
            String insertGrupo = "DELETE from grupo where alumno_id= ? and materia_id= ?";
            PreparedStatement preparedStmtGrupo = activo.prepareStatement(insertGrupo);
            preparedStmtGrupo.setInt(1, idAlumno);
            preparedStmtGrupo.setInt(2, idMateria);
            preparedStmtGrupo.execute();
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cierre: " + ex.getMessage());
        }
        return false;
    }

    public boolean cupoDisponible(Connection activo, final int idMateria) {
        int inscritos = 0;
        int capacidadT = 0;
        ResultSet resultado;
        ResultSet resultadoMateria;
        String order = "select count(*) as total from grupo where materia_id= ?";
        String capacidad = "select capacidad from materia where id= ?";
        try {
            PreparedStatement preparedStmt = activo.prepareStatement(order);
            PreparedStatement preparedStmtC = activo.prepareStatement(capacidad);
            preparedStmtC.setInt(1, idMateria);
            preparedStmt.setInt(1, idMateria);
            resultado = preparedStmt.executeQuery();
            resultadoMateria = preparedStmtC.executeQuery();
            while (resultado.next()) {
                inscritos = Integer.parseInt(resultado.getString("total"));
            }
            while (resultadoMateria.next()) {
                capacidadT = Integer.parseInt(resultadoMateria.getString("capacidad"));
            }
            if (inscritos < capacidadT) {

                return true;
            } else {

                return false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error validacion: " + ex.getMessage());
        }
        return false;
    }

    public boolean cupoDisponible(final String materia) {
        Connection activo;
        consultas con = new consultas();
        activo = con.getConexion();
        int idMateria;
        idMateria = getMateriaId(activo, materia);
        return cupoDisponible(activo, idMateria);
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
                if(matricula!=null){
                String[] parts = matricula.split(",");
                String part1 = parts[0];
                
                //System.out.println("esta es las matriculas " + part1);
                if (part1 != null) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(part1));
                } else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
                }
                
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
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String materia = msg.getContent();
                ACLMessage reply = msg.createReply();
                String aux = null;
                String matricula = (String) catalogue.get(materia);
                if (matricula != null & validacion.cupoDisponible(materia)) {
                    matricula = (String) catalogue.remove(materia);
                    String[] AlumnosLista = matricula.split(",");
                    for(int i=0; i<AlumnosLista.length;i++){
                    if(validacion.cupoDisponible(materia))
                    addAlumno(materia, AlumnosLista[i]);
                    else{
                    if(aux!=null)
                    aux=aux+","+AlumnosLista[i];
                    else
                    aux=AlumnosLista[i];    
                    }
                    }
                    if(aux!=null){
                    System.out.println();
                    catalogue.put(materia, aux);
                    }
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
