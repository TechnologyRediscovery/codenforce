/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class CeActionRequestsBB extends BackingBeanUtils implements Serializable {

    private ArrayList<CEActionRequest> requestList;
    private CEActionRequest selectedRequest;
    /**
     * Creates a new instance of CeActionRequestsBB
     */
    public CeActionRequestsBB() {
    }
    
     /**
     * @return the requestList
     */
    public ArrayList<CEActionRequest> getRequestList() {
        CEActionRequestIntegrator ari = getcEActionRequestIntegrator();
        if(requestList == null){
            try {
                requestList = ari.getCEActionRequestList(getSessionBean().getActiveMuni().getMuniCode());
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "Unable to load action requests due to an error in the Integration Module", ""));
            }
        }
        return requestList;
    }

     public String viewSelectedActionRequest(){
        System.out.println("CeActionRequestsBB.viewSelectedActionRequest");
        getSessionBean().setActionRequest(selectedRequest);
        return "actionRequestManage";
    }

    /**
     * @param requestList the requestList to set
     */
    public void setRequestList(ArrayList<CEActionRequest> requestList) {
        this.requestList = requestList;
    }

    /**
     * @return the selectedRequest
     */
    public CEActionRequest getSelectedRequest() {
        return selectedRequest;
    }

    /**
     * @param selectedRequest the selectedRequest to set
     */
    public void setSelectedRequest(CEActionRequest selectedRequest) {
        this.selectedRequest = selectedRequest;
    }
    
}
