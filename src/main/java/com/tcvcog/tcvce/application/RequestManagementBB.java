/*
 * Copyright (C) 2017 Eric C. Darsow
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

import java.util.ArrayList;
import javax.faces.component.html.HtmlDataTable;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import java.io.Serializable;

/**
 *
 * @author Eric C. Darsow
 */
public class RequestManagementBB extends BackingBeanUtils implements Serializable{
    
    
    private CEActionRequest currentRequest;
    private RequestStatus newStatus;
    
    // bean utilities
    private CEActionRequestIntegrator integrator = new CEActionRequestIntegrator();
    
    /**
     * Creates a new instance of RequestManagementBBean
     */
    public RequestManagementBB() {
    }

  
    /**
     * @return the currentRequest
     */
    public CEActionRequest getCurrentRequest() {
        if(currentRequest == null){
            currentRequest = getSessionBean().getActionRequest();
        }
        return currentRequest;
    }

    /**
     * @param currentRequest the currentRequest to set
     */
    public void setCurrentRequest(CEActionRequest currentRequest) {
        this.currentRequest = currentRequest;
    }

    /**
     * @return the newStatus
     */
    public RequestStatus getNewStatus() {
        return newStatus;
    }

    /**
     * @param newStatus the newStatus to set
     */
    public void setNewStatus(RequestStatus newStatus) {
        this.newStatus = newStatus;
    }
    
}
