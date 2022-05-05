/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itson.test.pruebas;

import com.itson.acceso.servicio.OfertaFacadeREST;
import com.itson.acceso.servicio.UsuarioFacadeREST;
import com.itson.accesojpa.entidades.Usuario;
import com.itson.rabbitmq.rpc.NotificacionRPC;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Juan Pablo
 */
public class pruebas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        //enviarNotificacion("OXXO", "Descuento en jabones", 20, 4);
        String mercado = "Oxxo";
        String descripcion = "Descuento en jabones";
        float descuento = 20;
        int duracion = 5;
        NotificacionRPC notificacionRPC = new NotificacionRPC();
        UsuarioFacadeREST usREST = new UsuarioFacadeREST();
        System.out.println("Facade REST Creada");
        String destinatario = "";
        String mensaje = "";
        String asunto = "Nueva oferta ha sido publicada por: " + mercado;
        for (int i = 0; i < usREST.findAll().size(); i++) {
            System.out.println("Inicio el fot");
            if (usREST.findAll().get(i).getTipoUsuario().equals("Consumidor")) {
                System.out.println("Entró en el if");
                destinatario = usREST.findAll().get(i).getEmail();
                mensaje = "Hola " + usREST.findAll().get(i).getNombre() + "! \n"
                        + "El mercado " + mercado + " ha publicado una oferta!\n"
                        + "Detalles de la oferta: \n"
                        + "Descripcion: " + descripcion + ". \n"
                        + "Descuento: " + descuento + "% \n"
                        + "Duración: " + duracion + " días.\n"
                        + "Saludos Cordiales \n \n"
                        + "Procuraduría Federal del consumidor.";

                notificacionRPC.notificacion(destinatario, asunto, mensaje);
                System.out.println("se envio un correo");
            }
        }
        notificacionRPC.notificacion("juantamazula60@gmail.com", "prueba", "Hola mundo!");

    }
}
