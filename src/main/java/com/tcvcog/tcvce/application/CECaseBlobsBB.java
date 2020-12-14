/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import java.io.IOException;
import java.io.Serializable;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public  class       CECaseBlobsBB 
        extends     BackingBeanUtils
        implements  Serializable {

    
    
    private CECaseDataHeavy currentCase;
    
    /**
     * Creates a new instance of CECaseBlobs
     */
    public CECaseBlobsBB() {
    }
    
      @PostConstruct
    public void initBean() {
        CaseCoordinator caseCoord = getCaseCoordinator();
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessCECase();
        
    
    }
    
    

    public void deletePhoto(int photoID) {
        // TODO: A user should not delete a blob before they have manually removed
        //all links to the blob. You could do it automatically, but we don't
        //want to remove a photo without knowing what you're removing it drom.

        BlobCoordinator blobc = getBlobCoordinator();
        try {
            BlobLight blob = blobc.getPhotoBlob(photoID);
            blobc.deletePhotoBlob(blob);
        } catch (IntegrationException
                | AuthorizationException
                | BObStatusException
                | BlobException
                | ClassNotFoundException
                | EventException
                | IOException
                | ViolationException ex) {
            System.out.println(ex);
        }
    }
    

    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }
    
}
