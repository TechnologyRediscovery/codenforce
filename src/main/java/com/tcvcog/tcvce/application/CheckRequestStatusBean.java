/*
 * Copyright (C) 2017 ellen bascomb of apt 31y
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.entities.CEActionRequest;
import java.io.Serializable;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CheckRequestStatusBean extends BackingBeanUtils implements Serializable {

    private int lookupControlCode;
    private CEActionRequest retrievedRequest;
    private String noteToAdd;
    

    
    /**
     * Creates a new instance of checkRequestStatusBean
     */
    public CheckRequestStatusBean() {
    }
    
    
    public String lookupRequest(){
//        CEActionRequestIntegratorPublic ceari = getcEActionRequestIntegratorPublic();
//            retrievedRequest = ceari.getActionRequestByControlCode(lookupControlCode);
            // now that we've got a request, store it in our session's active action request
            
            getSessionBean().setSessCEAR(retrievedRequest);
            getFacesContext().addMessage(null, new FacesMessage 
                    (FacesMessage.SEVERITY_INFO, "Success! Code Enforcement Action Request Lookup returned the following information", ""));
            return "success";
//        
//        getFacesContext().addMessage(null, new FacesMessage 
//                (FacesMessage.SEVERITY_ERROR, "Sorry, no action requests exist in the system with that control code. "
//                        + "Please check the code you entered and search again.", ""));
//        return "";  // reload page
    }

    /**
     * @return the lookupControlCode
     */
    public int getLookupControlCode() {
        return lookupControlCode;
    }

    /**
     * @param lookupControlCode the lookupControlCode to set
     */
    public void setLookupControlCode(int lookupControlCode) {
        this.lookupControlCode = lookupControlCode;
    }

    /**
     * @return the retrievedRequest
     */
    public CEActionRequest getRetrievedRequest() {
        return retrievedRequest;
    }

    /**
     * @param retrievedRequest the retrievedRequest to set
     */
    public void setRetrievedRequest(CEActionRequest retrievedRequest) {
        this.retrievedRequest = retrievedRequest;
    }

    /**
     * @return the noteToAdd
     */
    public String getNoteToAdd() {
        return noteToAdd;
    }

    /**
     * @param noteToAdd the noteToAdd to set
     */
    public void setNoteToAdd(String noteToAdd) {
        this.noteToAdd = noteToAdd;
    }
    
}
