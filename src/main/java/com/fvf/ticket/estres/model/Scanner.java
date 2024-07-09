/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fvf.ticket.estres.model;

import com.fvf.ticket.estres.controller.ExcelResults;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author franciscogomezlopez
 */
public class Scanner implements Runnable {
    
    private int id;
    private HashMap<String, JSONObject> tickets = new LinkedHashMap<String, JSONObject>();
    private long finishsec = 0;
    private String bearerToken ;
    private Client client;
    private WebTarget webTarget;
    private WebTarget ticketPath; 
    
    public void checkTicket(String ticket, long sleepmilis) {
        
        if(!this.tickets.containsKey(ticket)){
            return;
        }
        
        
        JSONObject actualticket = this.tickets.get(ticket);
        
        try {
            
            if(sleepmilis > 0){
                Thread.sleep(sleepmilis);
            }
            
            long startProcess = Calendar.getInstance().getTimeInMillis();
            
            this.ticketPath = this.webTarget.path("tickets/" + ticket + "/consume/");
            
            Invocation.Builder invocationBuilder = this.ticketPath.request(MediaType.APPLICATION_JSON);
        
            JSONObject body = new JSONObject();
            
            Response response = invocationBuilder
                    .header("Authorization", "Bearer " + this.getBearerToken())
                    .post(Entity.json(body.toString()));
            
            long longTimeFinish = Calendar.getInstance().getTimeInMillis() - startProcess;
            
            actualticket.put("time_send", startProcess);
            actualticket.put("time_response", longTimeFinish);
            actualticket.put("status_response", response.getStatus());
            actualticket.put("time_sleep", sleepmilis);
            actualticket.put("device", this.id);
            
            // update response
            this.tickets.put(ticket, actualticket);
           
        } catch (Exception e) {
            System.out.println("error sending ticket : " + ticket);
            e.printStackTrace();
            
        }
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
    
    public void initConnection() {

        this.client = ClientBuilder.newClient();
        this.webTarget = client.target("https://get.ticketplate.com/scanner-api");
        
    } 
    
    public void addTicket(String ticket) {
    
        if(this.tickets.containsKey(ticket)) {
            return ;
        }
        try {
            JSONObject value = new JSONObject();
            value.put("ticket", ticket);
            value.put("time_created", Calendar.getInstance().getTimeInMillis());
            this.tickets.put(ticket, value);
        } catch (Exception e) {
            System.out.println("error adding ticket at id: " + this.getId() );
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        
        if(this.tickets.size() <= 0 ) {
            System.out.println("Scanner id: " + this.id + " was empty");
            return;
        } 
        
        long startProcess = Calendar.getInstance().getTimeInMillis();
        long sleepmilis = 0l;
        
        for (String key: this.tickets.keySet()) {
        
            this.checkTicket(key, sleepmilis);
            sleepmilis = (long) (Math.random()*(12000-1000))+1000;
            
        }
        
        long longTimeFinish = Calendar.getInstance().getTimeInMillis() - startProcess;
        this.finishsec = longTimeFinish / 1000;
        
        System.out.println("Scanner id: " + this.getId() + " finish in : " + this.finishsec + " secs");
        
        JSONObject scannerinfo = new JSONObject();
        try {
            scannerinfo.put("device", this.id);
            scannerinfo.put("time_completed", this.finishsec);
            scannerinfo.put("total_tickets", this.tickets.size());
        } catch (Exception e) {
            System.out.println("error loading scannerinfo");
            e.printStackTrace();
        }
        
        ExcelResults.getInstance().resumeExcel(this.tickets, scannerinfo);
        
    }

}
