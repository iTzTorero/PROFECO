/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itson.acceso.servicio;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.itson.accesojpa.entidades.Usuario;
import com.itson.rabbitmq.rpc.NotificacionRPC;
import com.mysql.jdbc.Connection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Juan Pablo
 */
@Stateless
@Path("/usuario")
public class UsuarioFacadeREST extends AbstractFacade<Usuario> {

    private NotificacionRPC notificacionRPC;

    @PersistenceContext(unitName = "AccesoDatosJPAPU")
    private EntityManager em;

    public UsuarioFacadeREST() {
        super(Usuario.class);
        this.em = Persistence.createEntityManagerFactory("AccesoDatosJPAPU").createEntityManager();
        try {
            notificacionRPC = new NotificacionRPC();
        } catch (IOException ex) {
            Logger.getLogger(UsuarioFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(UsuarioFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_JSON})
    public void create(Usuario entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Integer id, Usuario entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("/{id}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Usuario find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_JSON})
    public List<Usuario> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Usuario> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("/count/")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public boolean login(@FormParam("email") String email, @FormParam("psw") String psw) throws IOException, InterruptedException {
        String token = null;
        if (comprobarLogin(email, psw)) {
            return true;
//            try {
//                Algorithm alg = Algorithm.HMAC256("tiburoncin");
//                token = JWT.create().withIssuer("auth0").withClaim("usr", usr).sign(alg);
//            } catch (JWTCreationException e) {
//
//            }
//            return Response.ok().entity(token).build();
        }
        return false;
    }

    @POST
    @Path("/reportar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void login(@FormParam("idConsumidor") int idConsumidor, @FormParam("idProducto") int idProducto, @FormParam("descripcion") String descripcion) throws IOException, InterruptedException {
        enviarReporte(idConsumidor, idProducto, descripcion);

    }

    private void enviarReporte(int idConsumidor, int idProducto, String descripcion) {

        ProductoFacadeREST prods = new ProductoFacadeREST();
        int idMercado = prods.find(idProducto).getIdMercado().getIdUsuario();
        String destinatario = "profeco.gob.mexico@gmail.com";
        String mensaje = "";
        String asunto = "Reporte generado hacia: " + super.find(idMercado).getNombre();

        mensaje = "Hola PROFECO! \n"
                + "El mercado " + super.find(idMercado).getNombre() + " ha sido reportado!\n"
                + "Detalles del reporte: \n"
                + "Consumidor: " + super.find(idConsumidor).getNombre() + ". \n"
                + "Descripción: " + descripcion + " \n"
                + "Saludos Cordiales \n \n"
                + "Queda en espera la sanción pertinente.";
        try {
            notificacionRPC.notificacion(destinatario, asunto, mensaje);
        } catch (IOException ex) {
            Logger.getLogger(OfertaFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OfertaFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        asunto = "Haz recibido un reporte de parte de "+ super.find(idConsumidor).getNombre();
        mensaje = "Hola " + super.find(idMercado).getNombre() + "! \n"
                + "Haz recibido un reporte de parte de "+ super.find(idConsumidor).getNombre() + " \n"
                + "Detalles del reporte: \n"
                + "Descripcion: " + descripcion + ". \n"
                + "Puedes ser acreedor de una sanción"
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

    private boolean comprobarLogin(String email, String psw) {

        for (int i = 0; i < super.findAll().size(); i++) {
            if (super.findAll().get(i).getEmail().equalsIgnoreCase(email) && super.findAll().get(i).getContrasenia().equals(psw)) {
                return true;
            }
        }
        return false;

    }

}
