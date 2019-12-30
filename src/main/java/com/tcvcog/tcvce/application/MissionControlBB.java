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
import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

import org.primefaces.model.DashboardModel;
import org.primefaces.component.dashboard.Dashboard;
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
    
    private List<UserAuthorized> userList;
    private User selectedUser;
    
    private DashboardModel mainDash;
    
    private List<EventCECaseCasePropBundle> timelineEventList;
    private List<EventCECaseCasePropBundle> filteredEventWithCasePropList;
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
        currentUser = getSessionBean().getSessionUser();
        generateMainDash();
    }
    
    private void generateMainDash(){
        setMainDash(new DefaultDashboardModel());
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        DashboardColumn column3 = new DefaultDashboardColumn();
        column1.addWidget("dashpanel-ce-cears");
        column1.addWidget("dashpanel-ce-cecases");
        column1.addWidget("dashpanel-ce-todo");
        
        column2.addWidget("dashpanel-occ-periods");
        column2.addWidget("dashpanel-occ-inspections");
        column2.addWidget("dashpanel-persons");
        column2.addWidget("dashpanel-properties");
        
        column3.addWidget("dashpanel-sys-events");
        column3.addWidget("dashpanel-sys-switchmuni");
//        if(     currentUser != null 
//            &&  currentUser.getMyCredential() != null
//            && currentUser.getMyCredential().getGoverningAuthPeriod().getRole() == RoleType.Developer){
        column3.addWidget("dashpanel-sys-switchuser");
        

        mainDash.addColumn(column1);
        mainDash.addColumn(column2);
        mainDash.addColumn(column3);
        
    }
//    
//    
//    
//    public void testPDF(ActionEvent ev){
//        String DEST = "/home/sylvia/GlassFish_Server/glassfish/domains/domain1/applications/helloPDF.pdf";
//        File file = new File(DEST);
//        System.out.println("MissionControlBB.testPDF | can write to loc: " + file.canWrite());
//        file.getParentFile().mkdirs();
//        //Initialize PDF writer
//        Document document;
//        PdfWriter writer;
//        try {
//            writer = new PdfWriter(file);
//        //Initialize PDF document
//        PdfDocument pdf = new PdfDocument(writer);
// 
//        // Initialize document
//        document = new Document(pdf);
// 
//        //Add paragraph to the document
//        document.add(new Paragraph("Hello World!"));
// 
//        //Close document
//        document.close();
//            System.out.println("wrote pdf!");
//        
//        } catch (FileNotFoundException ex) {
//            System.out.println("MissionControlBB.testPDF");
//            System.out.println(ex);
//        }
//    }
//    
//   
//    
    
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
            getSessionBean().setSessionMuni(muniComp);
            getSessionBean().setSessionCodeSet(ci.getCodeSetBySetID(muniComp.getCodeSet().getCodeSetID()));
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
     * @return the user
     */
    public User getCurrentUser() {
        currentUser = getSessionBean().getSessionUser();
        if(currentUser != null){
            System.out.println("MissionControlBB.getUser | facesUser: " + currentUser.getPerson().getFirstName());
        }
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
        currentMuni = getSessionBean().getSessionMuni();
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
     * @return the timelineEventList
     */
    public List<EventCECaseCasePropBundle> getTimelineEventList() {
        EventIntegrator ei = getEventIntegrator();
        try {
            timelineEventList = 
                    (ArrayList<EventCECaseCasePropBundle>) ei.getUpcomingTimelineEvents(getSessionBean().getSessionMuni(), 
                            LocalDateTime.now(), LocalDateTime.now().plusDays(365));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return timelineEventList;
    }

    /**
     * @param timelineEventList the timelineEventList to set
     */
    public void setTimelineEventList(ArrayList<EventCECaseCasePropBundle> timelineEventList) {
        this.timelineEventList = timelineEventList;
    }

    /**
     * @return the filteredEventWithCasePropList
     */
    public List<EventCECaseCasePropBundle> getFilteredEventWithCasePropList() {
        return filteredEventWithCasePropList;
    }

    /**
     * @param filteredEventWithCasePropList the filteredEventWithCasePropList to set
     */
    public void setFilteredEventWithCasePropList(ArrayList<EventCECaseCasePropBundle> filteredEventWithCasePropList) {
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
    public List<UserAuthorized> getUserList() {
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
    public void setUserList(List<UserAuthorized> userList) {
        this.userList = userList;
    }

    /**
     * @param selectedUser the selectedUser to set
     */
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    

   
    

   
}
