/*
 * Copyright (C) 2017 cedba
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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.CodeCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.faces.application.Application;
import java.sql.Connection;
import com.tcvcog.tcvce.integration.PostgresConnectionFactory;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.coordinators.ViolationCoordinator;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;

// occupancy integrators
import com.tcvcog.tcvce.occupancy.integration.ChecklistIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyPermitIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;

// system integrators
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.LogIntegrator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import javax.el.ValueExpression;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;


/**
 *
 * @author cedba
 */

public class BackingBeanUtils implements Serializable{
    
//    @ManagedProperty(value="sessionBean")
    private SessionBean sessionBean;
    
    private UserCoordinator userCoordinator;
    private UserIntegrator userIntegrator;
    
    private MunicipalityIntegrator municipalityIntegrator;
    
    private CaseCoordinator caseCoordinator;
    private CaseIntegrator caseIntegrator;
    
    private EventCoordinator eventCoordinator;
    private EventIntegrator eventIntegrator;
    
    private CodeViolationIntegrator codeViolationIntegrator;
    private ViolationCoordinator violationCoordinator;
    private CitationIntegrator citationIntegrator;
    private CourtEntityIntegrator courtEntityIntegrator;
    
    private PropertyIntegrator propertyIntegrator;
    private CEActionRequestIntegrator cEActionRequestIntegrator;
    
    private ChecklistIntegrator checklistIntegrator;
    private OccupancyInspectionIntegrator occupancyInspectionIntegrator;
    private OccupancyPermitIntegrator occupancyPermitIntegrator;
    private PaymentIntegrator paymentIntegrator;
    
    // system integrators
    private SystemIntegrator systemIntegrator;
    private LogIntegrator logIntegrator;
    
    private User facesUser;
    
    /**
     * Creates a new instance of BackingBeanUtils
     */
    public BackingBeanUtils() {
        
        // this creation of the usercorodinator should be managed by the 
        // MBCF but doesn't seem to be--this is not a solid object-oriented
        // design concept that works well with the bean model
        // it should be made by the MBCF
        //System.out.println("Constructing BackingBean Utils");
        //userCoordinator = new UserCoordinator();
        
       
        
    }
    
    public static java.sql.Timestamp getCurrentTimeStamp(){
        java.util.Date date = new java.util.Date();
        return new java.sql.Timestamp(date.getTime());
        
    }
    
    public FacesContext getFacesContext(){
        return FacesContext.getCurrentInstance();
    }
    
    public Application getApplication(){
        return getFacesContext().getApplication();
    }
    
    public ResourceBundle getResourceBundle(String bundleName){
        FacesContext context = getFacesContext();
        Application app = getApplication();
        ResourceBundle bundle = app.getResourceBundle(context, bundleName );
        return bundle;
    }
    
    
    
    public void setUserCoordinator(UserCoordinator userCoordinator){
        this.userCoordinator = userCoordinator;
    }

    /**
     * creates a PostgresConnectionFactory factory and calls getCon to get a handle on the
 database connection
     * @return the postgresCon
     */
    public Connection getPostgresCon() {

        // We definitely do not want to be creating a connection 
        // factory in this location-- go get a bean!
       // PostgresConnectionFactory factory = new PostgresConnectionFactory();
       // return factory.getCon();
       
             
         //System.out.println("BackingBeanUtils.getPostgresCon- Getting con through backing bean");
         FacesContext context = getFacesContext();
         PostgresConnectionFactory dbCon = context.getApplication()
                 .evaluateExpressionGet(
                         context, 
                         "#{dBConnection}", 
                         PostgresConnectionFactory.class);
         return dbCon.getCon();
         

    }

    // deleted setter
    
    public int getControlCodeFromTime(){
         long dateInMs = new Date().getTime();
         
         String numAsString = String.valueOf(dateInMs);
         String reducedNum = numAsString.substring(7);
         int controlCode = Integer.parseInt(reducedNum);
         
            
         return controlCode;
    }
    
    public CodeCoordinator getCodeCoordinator() {
        FacesContext context = getFacesContext();
        CodeCoordinator coord = context.getApplication()
                .evaluateExpressionGet(
                        context, 
                        "#{codeCoordinator}", 
                        CodeCoordinator.class);
    return coord;
        
    }
    
    public CodeIntegrator getCodeIntegrator(){
        FacesContext context = getFacesContext();
        CodeIntegrator codeInt = context.getApplication()
                .evaluateExpressionGet(
                        context, 
                        "#{codeIntegrator}", 
                        CodeIntegrator.class);
        
        return codeInt;
        
    }
    
    public PersonIntegrator getPersonIntegrator(){
        FacesContext context = getFacesContext();
        PersonIntegrator personIntegrator = context.getApplication()
                .evaluateExpressionGet(
                        context, 
                        "#{personIntegrator}", 
                        PersonIntegrator.class);
        
        return personIntegrator;
        
    }
    
     
    
    public MunicipalityIntegrator getMunicipalityIntegrator(){
        FacesContext context = getFacesContext();
        municipalityIntegrator = context. getApplication()
                .evaluateExpressionGet(
                        context, 
                        "#{municipalityIntegrator}", 
                        MunicipalityIntegrator.class);
        return municipalityIntegrator;
    }
    
    

    /**
     * @return the userCoordinator
     */
    public UserCoordinator getUserCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(),
                "#{userCoordinator}", UserCoordinator.class);
       userCoordinator = (UserCoordinator) ve.getValue(context.getELContext());
        return userCoordinator;
    }



    /**
     * @param muniIntegrator the muniIntegrator to set
     */
    public void setMunicipalityIntegrator(MunicipalityIntegrator muniIntegrator) {
        this.municipalityIntegrator = muniIntegrator;
    }

    /**
     * @return the propertyIntegrator
     */
    public PropertyIntegrator getPropertyIntegrator() {
        
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), 
                        "#{propertyIntegrator}", PropertyIntegrator.class);
        propertyIntegrator = (PropertyIntegrator) ve.getValue(context.getELContext());
   
        return propertyIntegrator;
    }

    /**
     * @param propertyIntegrator the propertyIntegrator to set
     */
    public void setPropertyIntegrator(PropertyIntegrator propertyIntegrator) {
        this.propertyIntegrator = propertyIntegrator;
    }

    /**
     * @return the cEActionRequestIntegrator
     */
    public CEActionRequestIntegrator getcEActionRequestIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), 
                        "#{cEActionRequestIntegrator}", CEActionRequestIntegrator.class);
        cEActionRequestIntegrator = (CEActionRequestIntegrator) ve.getValue(context.getELContext());
        return cEActionRequestIntegrator;
    }

    /**
     * @param cEActionRequestIntegrator the cEActionRequestIntegrator to set
     */
    public void setcEActionRequestIntegrator(CEActionRequestIntegrator cEActionRequestIntegrator) {
        this.cEActionRequestIntegrator = cEActionRequestIntegrator;
    }

    /**
     * @return the userIntegrator
     */
    public UserIntegrator getUserIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{userIntegrator}", UserIntegrator.class);
        userIntegrator = (UserIntegrator) ve.getValue(context.getELContext());
        
        
        return userIntegrator;
    }

    /**
     * @param userIntegrator the userIntegrator to set
     */
    public void setUserIntegrator(UserIntegrator userIntegrator) {
        this.userIntegrator = userIntegrator;
    }

    /**
     * @return the caseCoordinator
     */
    public CaseCoordinator getCaseCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{caseCoordinator}", CaseCoordinator.class);
        caseCoordinator = (CaseCoordinator) ve.getValue(context.getELContext());
        return caseCoordinator;
    }

    /**
     * @param caseCoordinator the caseCoordinator to set
     */
    public void setCaseCoordinator(CaseCoordinator caseCoordinator) {
        this.caseCoordinator = caseCoordinator;
    }

    /**
     * @return the eventCoordinator
     */
    public EventCoordinator getEventCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{eventCoordinator}", EventCoordinator.class);
        eventCoordinator = (EventCoordinator) ve.getValue(context.getELContext());
        return eventCoordinator;
    }

    /**
     * @param eventCoordinator the eventCoordinator to set
     */
    public void setEventCoordinator(EventCoordinator eventCoordinator) {
        this.eventCoordinator = eventCoordinator;
    }

    /**
     * @return the eventIntegrator
     */
    public EventIntegrator getEventIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{eventIntegrator}", EventIntegrator.class);
        eventIntegrator = (EventIntegrator) ve.getValue(context.getELContext());
        
        return eventIntegrator;
    }

    /**
     * @param eventIntegrator the eventIntegrator to set
     */
    public void setEventIntegrator(EventIntegrator eventIntegrator) {
        this.eventIntegrator = eventIntegrator;
    }

    /**
     * @return the caseIntegrator
     */
    public CaseIntegrator getCaseIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{caseIntegrator}", CaseIntegrator.class);
        caseIntegrator = (CaseIntegrator) ve.getValue(context.getELContext());
        
        return caseIntegrator;
        
        
        
        
    }

    /**
     * @param caseIntegrator the caseIntegrator to set
     */
    public void setCaseIntegrator(CaseIntegrator caseIntegrator) {
        this.caseIntegrator = caseIntegrator;
    }
    
    /**
     * Calculates the number of days since a given date and the current system 
     * date.
     * @param pastDate
     * @return the number of calendar days since a given date 
     */
    public long getDaysSince(LocalDateTime pastDate){
        LocalDateTime currentDateTime = LocalDateTime.now();
        Long daysBetween = pastDate.atZone(ZoneId.systemDefault())
                .until(currentDateTime.atZone(ZoneId.systemDefault()), java.time.temporal.ChronoUnit.DAYS);
        return daysBetween;
        
        
    }
    
    public String getPrettyDate(LocalDateTime d){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");
        String formattedDateTime = d.format(formatter); 
        return formattedDateTime;
    }

    /**
     * @return the codeViolationIntegrator
     */
    public CodeViolationIntegrator getCodeViolationIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{codeViolationIntegrator}", CodeViolationIntegrator.class);
        codeViolationIntegrator = (CodeViolationIntegrator) ve.getValue(context.getELContext());
        
        return codeViolationIntegrator;
    }

    /**
     * @param codeViolationIntegrator the codeViolationIntegrator to set
     */
    public void setCodeViolationIntegrator(CodeViolationIntegrator codeViolationIntegrator) {
        this.codeViolationIntegrator = codeViolationIntegrator;
    }

    /**
     * @return the violationCoordinator
     */
    public ViolationCoordinator getViolationCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{violationCoordinator}", ViolationCoordinator.class);
        violationCoordinator = (ViolationCoordinator) ve.getValue(context.getELContext());
        return violationCoordinator;
    }

    /**
     * @param violationCoordinator the violationCoordinator to set
     */
    public void setViolationCoordinator(ViolationCoordinator violationCoordinator) {
        this.violationCoordinator = violationCoordinator;
    }
    
   
   
    /**
     * @return the occupancyInspectionIntegrator
     */
    public OccupancyInspectionIntegrator getOccupancyInspectionIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{occupancyInspectionIntegrator}", OccupancyInspectionIntegrator.class);
        occupancyInspectionIntegrator = (OccupancyInspectionIntegrator) ve.getValue(context.getELContext());
        return occupancyInspectionIntegrator;
    }

    /**
     * @param occupancyInspectionIntegrator the occupancyInspectionIntegrator to set
     */
    public void setOccupancyInspectionIntegrator(OccupancyInspectionIntegrator occupancyInspectionIntegrator) {
        this.occupancyInspectionIntegrator = occupancyInspectionIntegrator;
    }

    
   
    /**
     * @return the courtEntityIntegrator
     */
    public CourtEntityIntegrator getCourtEntityIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{courtEntityIntegrator}", CourtEntityIntegrator.class);
        courtEntityIntegrator = (CourtEntityIntegrator) ve.getValue(context.getELContext());
        return courtEntityIntegrator;
    }

    
    /**
     * @return the paymentIntegrator
     */
    public PaymentIntegrator getPaymentIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{paymentIntegrator}", PaymentIntegrator.class);
        paymentIntegrator = (PaymentIntegrator) ve.getValue(context.getELContext());
        return paymentIntegrator;
    }

    /**
     * @param courtEntityIntegrator the courtEntityIntegrator to set
     */
    public void setCourtEntityIntegrator(CourtEntityIntegrator courtEntityIntegrator) {
        this.courtEntityIntegrator = courtEntityIntegrator;
    }

    

    /**
     * @param paymentIntegrator the paymentIntegrator to set
     */
    public void setPaymentIntegrator(PaymentIntegrator paymentIntegrator) {
        this.paymentIntegrator = paymentIntegrator;
    }

    

    /**
     * @return the citationIntegrator
     */
    public CitationIntegrator getCitationIntegrator() {
        
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{citationIntegrator}", CitationIntegrator.class);
        citationIntegrator = (CitationIntegrator) ve.getValue(context.getELContext());
        
        return citationIntegrator;
    }

    /**
     * @param citationIntegrator the citationIntegrator to set
     */
    public void setCitationIntegrator(CitationIntegrator citationIntegrator) {
        this.citationIntegrator = citationIntegrator;
    }

    /**
     * @return the checklistIntegrator
     */
    public ChecklistIntegrator getChecklistIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{checklistIntegrator}", ChecklistIntegrator.class);
        checklistIntegrator = (ChecklistIntegrator) ve.getValue(context.getELContext());
        
        return checklistIntegrator;
    }

    /**
     * @param checklistIntegrator the checklistIntegrator to set
     */
    public void setChecklistIntegrator(ChecklistIntegrator checklistIntegrator) {
        this.checklistIntegrator = checklistIntegrator;
    }

    /**
     * @return the occupancyPermitIntegrator
     */
    public OccupancyPermitIntegrator getOccupancyPermitIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{occupancyPermitIntegrator}", OccupancyPermitIntegrator.class);
        occupancyPermitIntegrator = (OccupancyPermitIntegrator) ve.getValue(context.getELContext());
        
        return occupancyPermitIntegrator;
    }

    /**
     * @param occupancyPermitIntegrator the occupancyPermitIntegrator to set
     */
    public void setOccupancyPermitIntegrator(OccupancyPermitIntegrator occupancyPermitIntegrator) {
        this.occupancyPermitIntegrator = occupancyPermitIntegrator;
    }

    /**
     * @return the systemIntegrator
     */
    public SystemIntegrator getSystemIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{systemIntegrator}", SystemIntegrator.class);
        systemIntegrator = (SystemIntegrator) ve.getValue(context.getELContext());
        
        return systemIntegrator;
    }

    /**
     * @return the logIntegrator
     */
    public LogIntegrator getLogIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{logIntegrator}", LogIntegrator.class);
        logIntegrator = (LogIntegrator) ve.getValue(context.getELContext());
        
        return logIntegrator;
    }

    /**
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{sessionBean}", SessionBean.class);
        sessionBean = (SessionBean) ve.getValue(context.getELContext());
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the facesUser
     */
    public User getFacesUser() {
        ExternalContext ec = getFacesContext().getExternalContext();
        facesUser = (User) ec.getSessionMap().get("facesUser");
        return facesUser;
    }

    /**
     * @param facesUser the facesUser to set
     */
    public void setFacesUser(User facesUser) {
        this.facesUser = facesUser;
    }

}