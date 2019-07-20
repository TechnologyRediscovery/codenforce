/*
 * Copyright (C) 2018 Adam Gutonski 
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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;

import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;

import java.io.Serializable;
//imported when adding  and @ViewScoped

import java.util.*;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Adam Gutonski
 */

@ViewScoped
public class SpaceTypeBB extends BackingBeanUtils implements Serializable {

    private ArrayList<OccSpaceType> spaceTypeList;
    private OccSpaceType selectedSpaceType;
    private int formSpaceTypeID;
    private String formSpaceTypeTitle;
    private String formSpaceTypeDescription;
    
    
    
    /**
     * Creates a new instance of SpaceTypeBB
     */
    public SpaceTypeBB() {
    }

    /**
     * @return the spaceTypeList
     */
    public List<OccSpaceType> getSpaceTypeList() {
        OccInspectionIntegrator si = getOccInspectionIntegrator();
        
//        try {
////            spaceTypeList = si.getSpa();
//        } catch (IntegrationException ex) {
//            getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//                        "Unable to load space type list", 
//                        "This must be corrected by the System Administrator"));
//        }
//         if(spaceTypeList != null){
//            return spaceTypeList;
//            
//        } else {
//            spaceTypeList = new ArrayList();
//            return spaceTypeList;
//        }

        return new ArrayList<>();
    }

    /**
     * @param spaceTypeList the spaceTypeList to set
     */
    public void setSpaceTypeList(ArrayList<OccSpaceType> spaceTypeList) {
        this.spaceTypeList = spaceTypeList;
    }

    /**
     * @return the selectedSpaceType
     */
    public OccSpaceType getSelectedSpaceType() {
        return selectedSpaceType;
    }

    /**
     * @param selectedSpaceType the selectedSpaceType to set
     */
    public void setSelectedSpaceType(OccSpaceType selectedSpaceType) {
        this.selectedSpaceType = selectedSpaceType;
    }

    /**
     * @return the formSpaceTypeID
     */
    public int getFormSpaceTypeID() {
        return formSpaceTypeID;
    }

    /**
     * @param formSpaceTypeID the formSpaceTypeID to set
     */
    public void setFormSpaceTypeID(int formSpaceTypeID) {
        this.formSpaceTypeID = formSpaceTypeID;
    }

    /**
     * @return the formSpaceTypeTitle
     */
    public String getFormSpaceTypeTitle() {
        return formSpaceTypeTitle;
    }

    /**
     * @param formSpaceTypeTitle the formSpaceTypeTitle to set
     */
    public void setFormSpaceTypeTitle(String formSpaceTypeTitle) {
        this.formSpaceTypeTitle = formSpaceTypeTitle;
    }

    /**
     * @return the formSpaceTypeDescription
     */
    public String getFormSpaceTypeDescription() {
        return formSpaceTypeDescription;
    }

    /**
     * @param formSpaceTypeDescription the formSpaceTypeDescription to set
     */
    public void setFormSpaceTypeDescription(String formSpaceTypeDescription) {
        this.formSpaceTypeDescription = formSpaceTypeDescription;
    }
    
}
