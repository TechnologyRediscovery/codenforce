/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;

/**
 *
 * corresponding database table DDL
 * 
 * 
   INSERT INTO public.ceevent(
            eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, 
            eventtimestamp, eventdescription, login_userid, disclosetomunicipality, 
            disclosetopublic, activeevent, requiresviewconfirmation, viewconfirmed, 
            hidden, notes)
    VALUES (?, ?, ?, ?, 
            ?, ?, ?, ?, 
            ?, ?, ?, ?, 
            ?, ?);

* 
 * @author cedba
 */
public class EventCase extends Event implements Serializable{
    
    
    private int caseID;
    private ArrayList<Person> eventPersons;
    
   
    /**
     * @return the caseID
     */
    public int getCaseID() {
        return caseID;
    }

    /**
     * @param caseID the caseID to set
     */
    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

   
    /**
     * @return the eventPersons
     */
    public ArrayList<Person> getEventPersons() {
        return eventPersons;
    }

    /**
     * @param eventPersons the eventPersons to set
     */
    public void setEventPersons(ArrayList<Person> eventPersons) {
        this.eventPersons = eventPersons;
    }

   

    
    
    
}
