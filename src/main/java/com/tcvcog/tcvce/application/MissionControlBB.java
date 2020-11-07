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
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class MissionControlBB extends BackingBeanUtils implements Serializable {
    
    private User currentUser;
    private Municipality currentMuni;
    private Municipality selectedMuni;
    
    private List<User> userList;
    private User selectedUser;
    
    private DashboardModel mainDash;
    
    private List<EventCnFPropUnitCasePeriodHeavy> filteredEventWithCasePropList;
    private int timelineEventViewDateRange;
    
    private List<ProposalCECase> ceProposalList;
    private List<ProposalOccPeriod> occProposalList;
    
    /**
     * Creates a new instance of InitiateSessionBB
     */
    public MissionControlBB() {
    }
    
    @PostConstruct
    public void initBean() {
        UserCoordinator uc = getUserCoordinator();
        currentUser = getSessionBean().getSessUser();
        userList = uc.user_assembleUserListForSearch(getSessionBean().getSessUser());
        
        generateMainDash();
    }
    
    private void generateMainDash(){
        setMainDash(new DefaultDashboardModel());
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        DashboardColumn column3 = new DefaultDashboardColumn();
        column1.addWidget("dashpanel-ce-cears");
        column1.addWidget("dashpanel-properties");
        column1.addWidget("dashpanel-property-profile");
//        column1.addWidget("dashpanel-ce-todo");
        
        column2.addWidget("dashpanel-cecase");
        column2.addWidget("dashpanel-cecases");
        
        column2.addWidget("dashpanel-events-recent");
        column2.addWidget("dashpanel-events-upcoming");
        column2.addWidget("dashpanel-todo");
//        column2.addWidget("dashpanel-occ-periods");
//        column2.addWidget("dashpanel-occ-inspections");
//        column2.addWidget("dashpanel-persons");
        
//        column3.addWidget("dashpanel-sys-events");
        column3.addWidget("dashpanel-sys-switchmuni");
        column3.addWidget("dashpanel-sys-switchuser");
        

        mainDash.addColumn(column1);
        mainDash.addColumn(column2);
        mainDash.addColumn(column3);
        
    }
    
    /**
     * TODO: Push this logic into the coordinators and session folks!!
     * @return
     * @throws IntegrationException
     * @throws SQLException 
     */
    public String switchMuni() throws IntegrationException, SQLException{
        CodeIntegrator ci = getCodeIntegrator();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        MunicipalityDataHeavy muniComp;
        try {
            muniComp = mi.getMunDataHeavy(selectedMuni.getMuniCode());
            getSessionBean().setSessMuni(muniComp);
            getSessionBean().setSessCodeSet(ci.getCodeSetBySetID(muniComp.getCodeSet().getCodeSetID()));
        } catch (IntegrationException ex) {
            FacesContext facesContext = getFacesContext();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
        } catch (AuthorizationException ex) {
            System.out.println(ex);
        } 
        System.out.println("MissionControlBB.switchMuni | selected muni: " + selectedMuni.getMuniName());
        FacesContext facesContext = getFacesContext();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Successfully switch your current municipality to: " + selectedMuni.getMuniName(), ""));
            
        return "missionControl";
    }
    
    public String jumpToPublicPortal(){
        return "publicPortal";
    }
    
    /**
     * Listener for user requests to view at CEAR
     * @param req
     * @return 
     */
    public String onViewCEARButtonChange(CEActionRequest req){
        if(req != null){
            getSessionBean().setSessCEAR(req);
        }
        return "cEActionRequests";
    }
    
    /**
     * Listener for user requests to view a CECase
     * @param cse
     * @return 
     */
    public String onViewCECaseButtonChange(CECase cse){
        if(cse != null){
            System.out.println("view case " + cse.getCaseID());
            getSessionBean().setSessCECase(cse);
        }
        return "ceCaseSearchProfile";
    }
    
    public String onViewOccPeriodButtonChange(OccPeriod per){
        if(per != null){
            getSessionBean().setSessOccPeriod(per);
        }
        
        return "occPeriodWorkflow";
    }
    
    
    public String switchToUser(){
        System.out.println("MissionControlBB.switchToUser");
        if(selectedUser != null){
            getSessionBean().setUserForReInit(selectedUser);
        } else {
            getSessionBean().setUserForReInit(getSessionBean().getSessUser());
        }
        return "sessionReinit";
    }
    
    public String reauthenticate(){
        System.out.println("MissionControlBB.reauthenticate");
        return "sessionReinit";
    }
    
   
    
    /**
     * @return the user
     */
    public User getCurrentUser() {
        
        return currentUser;
    }

    /**
     * @param currentUser the user to set
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * @return the currentMuni
     */
    public Municipality getCurrentMuni() {
        currentMuni = getSessionBean().getSessMuni();
        return currentMuni;
    }

    /**
     * @param currentMuni the currentMuni to set
     */
    public void setCurrentMuni(Municipality currentMuni) {
        this.currentMuni = currentMuni;
    }

    

    

    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }

    
    /**
     * @return the filteredEventWithCasePropList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getFilteredEventWithCasePropList() {
        return filteredEventWithCasePropList;
    }

    /**
     * @param filteredEventWithCasePropList the filteredEventWithCasePropList to set
     */
    public void setFilteredEventWithCasePropList(List<EventCnFPropUnitCasePeriodHeavy> filteredEventWithCasePropList) {
        this.filteredEventWithCasePropList = filteredEventWithCasePropList;
    }

    /**
     * @return the timelineEventViewDateRange
     */
    public int getTimelineEventViewDateRange() {
        return timelineEventViewDateRange;
    }

    /**
     * @param timelineEventViewDateRange the timelineEventViewDateRange to set
     */
    public void setTimelineEventViewDateRange(int timelineEventViewDateRange) {
        this.timelineEventViewDateRange = timelineEventViewDateRange;
    }

    /**
     * @return the mainDash
     */
    public DashboardModel getMainDash() {
        return mainDash;
    }

    /**
     * @param mainDash the mainDash to set
     */
    public void setMainDash(DashboardModel mainDash) {
        this.mainDash = mainDash;
    }


    /**
     * @return the occProposalList
     */
    public List<ProposalOccPeriod> getOccProposalList() {
        return occProposalList;
    }

    /**
     * @param occProposalList the occProposalList to set
     */
    public void setOccProposalList(List<ProposalOccPeriod> occProposalList) {
        this.occProposalList = occProposalList;
    }

    /**
     * @return the ceProposalList
     */
    public List<ProposalCECase> getCeProposalList() {
        return ceProposalList;
    }

    /**
     * @param ceProposalList the ceProposalList to set
     */
    public void setCeProposalList(List<ProposalCECase> ceProposalList) {
        this.ceProposalList = ceProposalList;
    }

    /**
     * @return the userList
     */
    public List<User> getUserList() {
        return userList;
    }

    /**
     * @return the selectedUser
     */
    public User getSelectedUser() {
        return selectedUser;
    }

    /**
     * @param userList the userList to set
     */
    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    /**
     * @param selectedUser the selectedUser to set
     */
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    

   
    

   
}
