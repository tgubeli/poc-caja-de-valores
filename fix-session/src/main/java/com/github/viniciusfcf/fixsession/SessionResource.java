package com.github.viniciusfcf.fixsession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import quickfix.Session;

@Path("/sessions")
public class SessionResource {


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "Sessions size: "+Session.numSessions();
    }
}