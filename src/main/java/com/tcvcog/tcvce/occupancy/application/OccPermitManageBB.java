/*
 * Copyright (C) 2020 Technology Rediscovery
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Nathan Dietz
 */
public class OccPermitManageBB extends BackingBeanUtils implements Serializable {
    
    private String currentMode;
    private boolean currentPermitSelected;
    
    private OccPermitApplication selectedPermit;
    
    private List<OccPermitApplication> permitList;

    public OccPermitManageBB() {
    }

    @PostConstruct
    public void initBean() {

        //initialize default current mode : Lookup
        currentMode = "Search";
        //initialize default setting 
        defaultSetting();
    }
    
    /**
     * Determines whether or not a user should currently be able to select a CEAR.
     * Users should only select CEARs if they're in search mode.
     * @return 
     */
    public boolean getSelectedButtonActive(){
        return !"Search".equals(currentMode);
    }
    
    public boolean getActiveSearchMode(){
        return "Search".equals(currentMode);
    }
    
    public boolean getActiveActionsMode(){
        return "Actions".equals(currentMode);
    }
    
    public boolean getActiveObjectsMode(){
        return "Objects".equals(currentMode);
    }
    
    public boolean getActiveNotesMode(){
        return "Notes".equals(currentMode);
    }

    public String getCurrentMode() {
        return currentMode;
    }
    
    public void defaultSetting(){
        
        SearchCoordinator sc = getSearchCoordinator();
        
        currentPermitSelected = false;
        
        //permitList use search coordinator 
        
    }

    /**
     *
     * @param currentMode Search, Actions, Object, Notes
     */
    public void setCurrentMode(String currentMode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        defaultSetting();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));

    }
    
    public void onPermitSelection(OccPermitApplication permit){
        
        if(currentPermitSelected){
        selectedPermit = permit;
        
        permitList = new ArrayList<>();
        
        permitList.add(permit);
        
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Currently Selected Application: " + permit.getReason().getTitle() + "ID:(" + permit.getId() + ")", ""));
        } else{
            defaultSetting();
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Application: " + permit.getReason().getTitle() + "ID:(" + permit.getId() + ")", ""));
            
        }
        
    }

    public boolean isCurrentPermitSelected() {
        return currentPermitSelected;
    }

    public void setCurrentPermitSelected(boolean currentPermitSelected) {
        this.currentPermitSelected = currentPermitSelected;
    }

    public List<OccPermitApplication> getPermitList() {
        return permitList;
    }

    public void setPermitList(List<OccPermitApplication> permitList) {
        this.permitList = permitList;
    }

    public OccPermitApplication getSelectedPermit() {
        return selectedPermit;
    }

    public void setSelectedPermit(OccPermitApplication selectedPermit) {
        this.selectedPermit = selectedPermit;
    }
    
}
