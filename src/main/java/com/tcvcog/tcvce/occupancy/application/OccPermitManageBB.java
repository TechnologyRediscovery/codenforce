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
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.occupancy.OccApplicationStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Nathan Dietz
 */
public class OccPermitManageBB extends BackingBeanUtils implements Serializable {
    
    private String currentMode;
    private boolean currentApplicationSelected;
    
    private OccPermitApplication searchParams;
    private List<OccApplicationStatusEnum> statusList;
    private LocalDateTime queryBegin;
    private LocalDateTime queryEnd;
    private boolean reason_ctl;
    private boolean status_ctl;
    private boolean connectedOccPeriod_ctl;
    private boolean connectedOccPeriod_val;
    
    private OccPermitApplication selectedApplication;
//    private QueryOccPermitApplication selectedQueryOccApplicaton;
    private List<OccPermitApplication> applicationList;

    public OccPermitManageBB() {
    }

    @PostConstruct
    public void initBean() {

        //initialize default current mode : Lookup
        currentMode = "Search";
        
        statusList = new ArrayList<>();
        
        for (OccApplicationStatusEnum status : OccApplicationStatusEnum.values()){
            
            statusList.add(status);
            
        }
        
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
        
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        currentApplicationSelected = false;
        
        try{
        applicationList = oi.getOccPermitApplicationList();
        } catch (BObStatusException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error while retrieving Occupancy Permit Applications: " + ex, ""));
        } catch(AuthorizationException | EventException | IntegrationException | ViolationException ex){
            System.out.println("OccPermitManageBB.defaultSetting() | ERROR: "+ ex);
        }
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
    
    public void executeCustomQuery(){
        
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        currentApplicationSelected = false;
        
        if(queryBegin == null || queryEnd ==null){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please specify a start and end date when executing a custom query", ""));
            return;
        }
        
        try{
        List<OccPermitApplication> results = oi.getOccPermitApplicationList();
        
        Iterator itr = results.iterator();
        
        //A variable that represents the result of a question we ask about an application.
        //If the answer is different as the given search parameter, remove the application
        boolean testCondition = false;
        
        while(itr.hasNext()){
            
            OccPermitApplication temp = (OccPermitApplication) itr;
            
            if(temp.getSubmissionDate().compareTo(queryBegin) < 0 || temp.getSubmissionDate().compareTo(queryEnd) > 0){
                itr.remove();
            }
            
            if(reason_ctl){
                
                if(temp.getReason() != searchParams.getReason()){
                    itr.remove();
                }
                
            }
            
            if(status_ctl){
               if(temp.getStatus() != searchParams.getStatus()){
                    itr.remove();
                } 
            }
            
            if(connectedOccPeriod_ctl){
                
                testCondition = temp.getConnectedPeriod() != null && temp.getConnectedPeriod().getPeriodID() != 0; //test if an occperiod is connected
                
                if(testCondition != connectedOccPeriod_val) {
                    //if the test does not evaluate to our desired state, remove it
                    itr.remove();
                }
                
            }
            
        }
        
                
        } catch (BObStatusException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error while retrieving Occupancy Permit Applications: " + ex, ""));
        } catch(AuthorizationException | EventException | IntegrationException | ViolationException ex){
            System.out.println("OccPermitManageBB.defaultSetting() | ERROR: "+ ex);
        }
        
    }
    
    public void onApplicationSelection(OccPermitApplication application){
        
        if(currentApplicationSelected){
        selectedApplication = application;
        
        applicationList = new ArrayList<>();
        
        applicationList.add(application);
        
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Currently Selected Application: " + application.getReason().getTitle() + "ID:(" + application.getId() + ")", ""));
        } else{
            defaultSetting();
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Application: " + application.getReason().getTitle() + "ID:(" + application.getId() + ")", ""));
            
        }
        
    }

    public boolean isCurrentApplicationSelected() {
        return currentApplicationSelected;
    }

    public void setCurrentApplicationSelected(boolean currentApplicationSelected) {
        this.currentApplicationSelected = currentApplicationSelected;
    }

    public List<OccPermitApplication> getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(List<OccPermitApplication> applicationList) {
        this.applicationList = applicationList;
    }

    public OccPermitApplication getSelectedApplication() {
        return selectedApplication;
    }

    public void setSelectedApplication(OccPermitApplication selectedApplication) {
        this.selectedApplication = selectedApplication;
    }

    public OccPermitApplication getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(OccPermitApplication searchParams) {
        this.searchParams = searchParams;
    }

    public boolean isReason_ctl() {
        return reason_ctl;
    }

    public void setReason_ctl(boolean reason_ctl) {
        this.reason_ctl = reason_ctl;
    }

    public boolean isStatus_ctl() {
        return status_ctl;
    }

    public void setStatus_ctl(boolean status_ctl) {
        this.status_ctl = status_ctl;
    }
    
    public boolean isConnectedOccPeriod_ctl() {
        return connectedOccPeriod_ctl;
    }

    public void setConnectedOccPeriod_ctl(boolean connectedOccPeriod_ctl) {
        this.connectedOccPeriod_ctl = connectedOccPeriod_ctl;
    }

    public boolean isConnectedOccPeriod_val() {
        return connectedOccPeriod_val;
    }

    public void setConnectedOccPeriod_val(boolean connectedOccPeriod_val) {
        this.connectedOccPeriod_val = connectedOccPeriod_val;
    }

    public LocalDateTime getQueryBegin() {
        return queryBegin;
    }

    public void setQueryBegin(LocalDateTime queryBegin) {
        this.queryBegin = queryBegin;
    }
    
    public Date getQueryBegin_Util() {
        
        Date utilDate = null;
        if(queryBegin != null){
           utilDate = Date.from(queryBegin.atZone(ZoneId.systemDefault()).toInstant());
        }        
        return utilDate;
    }

    public void setQueryBegin_Util(Date queryBeginUtil) {
        if(queryBeginUtil != null){
        queryBegin = queryBeginUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    public LocalDateTime getQueryEnd() {
        return queryEnd;
    }

    public void setQueryEnd(LocalDateTime queryEnd) {
        this.queryEnd = queryEnd;
    }
    
    public Date getQueryEnd_Util() {
        Date utilDate = null;
        if(queryEnd != null){
           utilDate = Date.from(queryEnd.atZone(ZoneId.systemDefault()).toInstant());
        }        
        return utilDate;
    }

    public void setQueryEnd_Util(Date queryEndUtil) {
        if(queryEndUtil != null){
        queryEnd = queryEndUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    public List<OccApplicationStatusEnum> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<OccApplicationStatusEnum> statusList) {
        this.statusList = statusList;
    }
    
}
