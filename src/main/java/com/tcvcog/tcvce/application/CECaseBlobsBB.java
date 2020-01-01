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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
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
        currentCase = sb.getSessionCECase();
        
    
    }
    
    

    public void deletePhoto(int photoID) {
        // TODO: remove entry from linker table for deleted photos
//        for(Integer pid : this.selectedViolation.getBlobIDList()){
//            if(pid.compareTo(photoID) == 0){
//                this.selectedViolation.getBlobIDList().remove(pid);
//                break;
//            }
//        }
        BlobCoordinator blobc = getBlobCoordinator();
        try {
            blobc.deleteBlob(photoID);
        } catch (IntegrationException ex) {
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
