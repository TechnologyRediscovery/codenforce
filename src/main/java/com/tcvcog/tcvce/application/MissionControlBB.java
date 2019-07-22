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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;

import org.primefaces.model.DashboardModel;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;

/**
 *
 * @author Eric C. Darsow
 */
public class MissionControlBB extends BackingBeanUtils implements Serializable {
    
    private User user;
    private Municipality currentMuni;
    private ArrayList<Municipality> muniList;
    private Municipality selectedMuni;
    
    private DashboardModel mainDash;
    
    private ArrayList<EventCECaseCasePropBundle> timelineEventList;
    private ArrayList<EventCECaseCasePropBundle> filteredEventWithCasePropList;
    private int timelineEventViewDateRange;
    
 
    
    
    
    /**
     * Creates a new instance of InitiateSessionBB
     */
    public MissionControlBB() {
    }
    
    @PostConstruct
    public void initBean(){
        generateMainDash();
        
    }
    
    private void generateMainDash(){
        setMainDash(new DefaultDashboardModel());
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        DashboardColumn column3 = new DefaultDashboardColumn();
        column1.addWidget("cears");
        column1.addWidget("cecases");
        column2.addWidget("occ");
        column2.addWidget("cetodo");
        column3.addWidget("occtodo");
        column3.addWidget("admintodo");
        getMainDash().addColumn(column1);
        getMainDash().addColumn(column2);
        getMainDash().addColumn(column3);
        
    }
    
    
    
    public void testPDF(ActionEvent ev){
        String DEST = "/home/sylvia/GlassFish_Server/glassfish/domains/domain1/applications/helloPDF.pdf";
        File file = new File(DEST);
        System.out.println("MissionControlBB.testPDF | can write to loc: " + file.canWrite());
        file.getParentFile().mkdirs();
        //Initialize PDF writer
        Document document;
        PdfWriter writer;
        try {
            writer = new PdfWriter(file);
        //Initialize PDF document
        PdfDocument pdf = new PdfDocument(writer);
 
        // Initialize document
        document = new Document(pdf);
 
        //Add paragraph to the document
        document.add(new Paragraph("Hello World!"));
 
        //Close document
        document.close();
            System.out.println("wrote pdf!");
        
        } catch (FileNotFoundException ex) {
            System.out.println("MissionControlBB.testPDF");
            System.out.println(ex);
        }
 
        
    }
    
    
    public String switchMuni(){
        CodeIntegrator ci = getCodeIntegrator();
        getSessionBean().setSessionMuni(selectedMuni);
        try {
            getSessionBean().setActiveCodeSet(ci.getCodeSetBySetID(selectedMuni.getCodeSet().getCodeSetID()));
        } catch (IntegrationException ex) {
            FacesContext facesContext = getFacesContext();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
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
    
    public String loginToMissionControl(){
        System.out.println("MissionControlBB.loginToMissionControl");
        
        return "startInitiationProcess";
    }
    
    public String logout(){
        FacesContext context = getFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        if (session != null) {

//            session.removeAttribute("dBConnection");
//            session.removeAttribute("codeCoordinator");
//            session.removeAttribute("codeIntegrator");
//            session.removeAttribute("municipalitygrator");
//            session.removeAttribute("personIntegrator");
//            session.removeAttribute("propertyIntegrator");
//            session.removeAttribute("cEActionRequestIntegrator");
//            session.removeAttribute("userIntegrator");
            session.invalidate();

            FacesContext facesContext = getFacesContext();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Logout Successful", ""));
            System.out.println("MissionControlBB.logout | Session invalidated");

        } else {
            FacesContext facesContext = getFacesContext();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "ERROR: Unable to invalidate session.", "Your system administrator has been notified."));
        }
        return "logoutSequenceComplete";
    }

    

    /**
     * @return the user
     */
    public User getUser() {
        user = getSessionUser();
        if(user != null){
            System.out.println("MissionControlBB.getUser | facesUser: " + user.getPerson().getFirstName());
        }
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
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
     * @param muniList the muniList to set
     */
    public void setMuniList(ArrayList<Municipality> muniList) {
        this.muniList = muniList;
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
    public ArrayList<EventCECaseCasePropBundle> getTimelineEventList() {
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

   

   
    

   
}
