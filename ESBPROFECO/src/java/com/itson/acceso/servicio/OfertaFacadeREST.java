/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itson.acceso.servicio;

import com.itson.accesojpa.entidades.Oferta;
import com.itson.rabbitmq.rpc.NotificacionRPC;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Juan Pablo
 */
@Stateless
@Path("/oferta")
public class OfertaFacadeREST extends AbstractFacade<Oferta> {

    @PersistenceContext(unitName = "AccesoDatosJPAPU")
    private EntityManager em;
    private NotificacionRPC notificacionRPC;

    public OfertaFacadeREST() throws IOException, TimeoutException {
        super(Oferta.class);
        notificacionRPC = new NotificacionRPC();
        this.em = Persistence.createEntityManagerFactory("AccesoDatosJPAPU").createEntityManager();

    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})

    public void create(Oferta entity) {
        super.create(entity);
        UsuarioFacadeREST usr = null;
        usr = new UsuarioFacadeREST();
        enviarNotificacion(usr.find(3).getNombre(), entity.getDescripcion(), entity.getDescuento(), entity.getDuracion());

    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Integer id, Oferta entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Oferta find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_JSON})
    public List<Oferta> findAll() {
       
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Oferta> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    private void enviarNotificacion(String mercado, String descripcion, float descuento, int duracion) {
        UsuarioFacadeREST usREST;
        usREST = new UsuarioFacadeREST();
        String destinatario = "";
        String mensaje = "";
        String asunto = "Nueva oferta ha sido publicada por: " + mercado;
        for (int i = 0; i < usREST.findAll().size(); i++) {
            if (usREST.findAll().get(i).getTipoUsuario().equals("Consumidor")) {

                destinatario = usREST.findAll().get(i).getEmail();
                mensaje = "Hola " + usREST.findAll().get(i).getNombre() + "! \n"
                        + "El mercado " + mercado + " ha publicado una oferta!\n"
                        + "Detalles de la oferta: \n"
                        + "Descripcion: " + descripcion + ". \n"
                        + "Descuento: " + descuento + "% \n"
                        + "Duración: " + duracion + " días.\n"
                        + "Saludos Cordiales \n \n"
                        + "Procuraduría Federal del consumidor.";
                try {
                    notificacionRPC.notificacion(destinatario, asunto, mensaje);
                } catch (IOException ex) {
                    Logger.getLogger(OfertaFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OfertaFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
