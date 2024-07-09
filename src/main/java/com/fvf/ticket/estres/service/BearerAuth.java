/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fvf.ticket.estres.service;

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
public class BearerAuth {
    
   //private WebResource webResource;
    private Client client;
    private WebTarget webTarget;
    private WebTarget bearerPath; 
    
    public void initConnection() {

        this.client = ClientBuilder.newClient();
        this.webTarget = client.target("https://get.ticketplate.com/auth-api");
        this.bearerPath = this.webTarget.path("login/");
        
    }
    
    public String getBearerToken(String username, String pass) throws Exception{
    
        Invocation.Builder invocationBuilder = this.bearerPath.request(MediaType.APPLICATION_JSON);
        
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", pass);
        
        Response response = invocationBuilder.post(Entity.json(body.toString()));
        
        
        return response.readEntity(String.class);
    
    }

}
