/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fvf.ticket.estres;

import com.fvf.ticket.estres.controller.EstresController;
import java.util.Calendar;

/**
 *
 * @author franciscogomezlopez
 */
public class app {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        String pathTicketsFile = "/Users/franciscogomezlopez/Downloads/Tickets-prueba.xlsx";
        if (args.length > 0) {
            pathTicketsFile = args[0];
        }
        
        String pathResultFile = "/Users/franciscogomezlopez/Downloads/ticket-resultados.xlsx";
        if (args.length > 1) {
            pathTicketsFile = args[1];
        }
        
        String deviceQuantity = "2";
        if (args.length > 2) {
            deviceQuantity = args[2];
        }
        
        String bearerUsername = "pruebasfvf@ticketplate.com";
        if (args.length > 3) {
            bearerUsername = args[3];
        }
        
        String bearerPassword = "fvf123456";
        if (args.length > 4) {
            bearerPassword = args[4];
        }
        
        /*
        String bearerTarget = "https://get.ticketplate.com/auth-api";
        if (args.length > 5) {
            bearerTarget = args[5];
        }
        
        String ticketScannTarget = "https://get.ticketplate.com/scanner-api";
        if (args.length > 6) {
            ticketScannTarget = args[6];
        }
        */
        
        EstresController control = new EstresController();

        control.initAuth(bearerUsername, bearerPassword);
        control.createDevices(Integer.parseInt(deviceQuantity));

        try {
            control.loadDataInScanner(pathTicketsFile);
            control.startTicketScanningProcess(pathResultFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
