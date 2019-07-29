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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.ChoiceCoordinator;
import com.tcvcog.tcvce.coordinators.CodeCoordinator;
import com.tcvcog.tcvce.coordinators.DataCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.PublicInfoCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.faces.application.Application;
import java.sql.Connection;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.ChoiceIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.ViolationIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;

// occupancy integrators
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;

// system integrators
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.LogIntegrator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.el.ValueExpression;
import javax.faces.context.ExternalContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;


/**
 * Collection of convenience methods for accessing application-level objects
 * that beans of various scopes use, most notably Coordinators and Integrators
 * 
 * @author Xander Darsow
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
    
    private ViolationIntegrator codeViolationIntegrator;
    private CitationIntegrator citationIntegrator;
    private CourtEntityIntegrator courtEntityIntegrator;
    
    private PropertyIntegrator propertyIntegrator;
    private CEActionRequestIntegrator cEActionRequestIntegrator;
    private PublicInfoCoordinator publicInfoCoordinator;
    private PersonCoordinator personCoordinator;
    private PropertyCoordinator propertyCoordinator;
    
    private OccInspectionIntegrator occInspectionIntegrator;
    private OccupancyIntegrator occupancyIntegrator;
    private PaymentIntegrator paymentIntegrator;
    private OccupancyCoordinator occupancyCoordinator;
    private DataCoordinator dataCoordinator;
    
    private BlobCoordinator blobCoordinator;
    private BlobIntegrator blobIntegrator;
    
    // system 
    private SystemIntegrator systemIntegrator;
    private SystemCoordinator systemCoordinator;
    private LogIntegrator logIntegrator;
    
    private SearchCoordinator searchCoordinator;
    
    private User facesUser;
    
    private DataSource dataSource;
    private Connection connx;
    
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
    
        
    public boolean isMunicodeInMuniList(int muniCode, List<Municipality> muniList){
        Municipality m;
        boolean isInList = false;
        Iterator<Municipality> iter = muniList.iterator();
        while(iter.hasNext()){
            m = iter.next();
            if(m.getMuniCode() == muniCode){
                isInList = true;
            }
        }
        return isInList;
    }
    
    
    public void setUserCoordinator(UserCoordinator userCoordinator){
        this.userCoordinator = userCoordinator;
    }

    /**
     * creates a PostgresConnectionFactory factory and calls getCon to get a handle on the
     * database connection
     * @return the postgresCon
     */
    public Connection getPostgresCon() {
        String jndi_name = getResourceBundle(Constants.DB_CONNECTION_PARAMS).getString("jndi_name");
        
        Context initContext = null;
        try {
            initContext = new InitialContext();
            Context envCtx = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envCtx.lookup(jndi_name);
            connx = dataSource.getConnection();
        } catch (NamingException | SQLException ex) {
            System.out.println(ex);
        }
        finally {
//             removed to avoid a "connection closed error" when migrating to glassfish managed connection pool
//            if (connx != null) { try { connx.close();} catch (SQLException e) { /* ignored */}}
        } 
        return connx;
      
    }
    
    public String appendNoteBlock(MessageBuilderParams mcc){
        StringBuilder sb = new StringBuilder();
        sb.append(mcc.existingContent);
        sb.append("<br />******************** NOTE ********************<br />");
        sb.append(mcc.header);
        sb.append("<br />");
        if(mcc.explanation != null){
            sb.append(mcc.explanation);
            sb.append("<br />");
        }
        sb.append("creatd by: ");
        sb.append(mcc.user.getPerson().getFirstName());
        sb.append(" ");
        sb.append(mcc.user.getPerson().getLastName());
        sb.append(" (username:  ");
        sb.append(mcc.user.getUsername());
        sb.append(", id#: ");
        sb.append(mcc.user.getUserID());
        sb.append(")");
        sb.append("<br />");
        sb.append(" at ");
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append("<br />----------------note-text-----------------<br />");
        sb.append(mcc.newMessageContent);
        sb.append("<br />**************** END NOTE *****************<br />");
        return sb.toString();
    }
    

    /**
     * Chops up the current time to get seven random digits
     * @return 
     */
    public int generateControlCodeFromTime(){
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
        if(d != null){
            String formattedDateTime = d.format(formatter); 
            return formattedDateTime;
        } else {
            return "";
        }
    }

    /**
     * @return the codeViolationIntegrator
     */
    public ViolationIntegrator getCodeViolationIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{codeViolationIntegrator}", ViolationIntegrator.class);
        codeViolationIntegrator = (ViolationIntegrator) ve.getValue(context.getELContext());
        
        return codeViolationIntegrator;
    }

    /**
     * @param codeViolationIntegrator the codeViolationIntegrator to set
     */
    public void setCodeViolationIntegrator(ViolationIntegrator codeViolationIntegrator) {
        this.codeViolationIntegrator = codeViolationIntegrator;
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
     * @return the occInspectionIntegrator
     */
    public OccInspectionIntegrator getOccInspectionIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{occInspectionIntegrator}", OccInspectionIntegrator.class);
        occInspectionIntegrator = (OccInspectionIntegrator) ve.getValue(context.getELContext());
        
        return occInspectionIntegrator;
    }

    /**
     * @param occInspectionIntegrator the occInspectionIntegrator to set
     */
    public void setOccInspectionIntegrator(OccInspectionIntegrator occInspectionIntegrator) {
        this.occInspectionIntegrator = occInspectionIntegrator;
    }

    /**
     * @return the occupancyIntegrator
     */
    public OccupancyIntegrator getOccupancyIntegrator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{occupancyIntegrator}", OccupancyIntegrator.class);
        occupancyIntegrator = (OccupancyIntegrator) ve.getValue(context.getELContext());
        
        return occupancyIntegrator;
    }

    /**
     * @param occupancyIntegrator the occupancyIntegrator to set
     */
    public void setOccupancyIntegrator(OccupancyIntegrator occupancyIntegrator) {
        this.occupancyIntegrator = occupancyIntegrator;
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
    public SearchCoordinator getSearchCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{searchCoordinator}", SearchCoordinator.class);
        searchCoordinator = (SearchCoordinator) ve.getValue(context.getELContext());
        return searchCoordinator;
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
    public User getJBOSSUser() {
        ExternalContext ec = getFacesContext().getExternalContext();
        facesUser = (User) ec.getSessionMap().get("facesUser");
        return facesUser;
    }

    /**
     * @param facesUser the facesUser to set
     */
    public void setSessionUser(User facesUser) {
        this.facesUser = facesUser;
    }
    
    public String getSessionID(){
        
        FacesContext fc = getFacesContext();
        HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
        // prints out the current session attributes to standard out
//        Enumeration e = session.getAttributeNames();
//        System.out.println("SessionInitailzier.getSessionID | Dumping lots of attrs");
//        while (e.hasMoreElements())
//        {
//          String attr = (String)e.nextElement();
//          System.out.println("      attr  = "+ attr);
//          Object value = session.getValue(attr);
//          System.out.println("      value = "+ value);
//        }
        String sessionID = session.getId();
        return sessionID;
    }

    /**
     * @return the publicInfoCoordinator
     */
    public PublicInfoCoordinator getPublicInfoCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{publicInfoCoordinator}", PublicInfoCoordinator.class);
        publicInfoCoordinator = (PublicInfoCoordinator) ve.getValue(context.getELContext());
        
        return publicInfoCoordinator;
    }

    /**
     * @param publicInfoCoordinator the publicInfoCoordinator to set
     */
    public void setPublicInfoCoordinator(PublicInfoCoordinator publicInfoCoordinator) {
        this.publicInfoCoordinator = publicInfoCoordinator;
    }

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return the personCoordinator
     */
    public PersonCoordinator getPersonCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{personCoordinator}", PersonCoordinator.class);
        personCoordinator = (PersonCoordinator) ve.getValue(context.getELContext());
        
        return personCoordinator;
    }

    /**
     * @param personCoordinator the personCoordinator to set
     */
    public void setPersonCoordinator(PersonCoordinator personCoordinator) {
        this.personCoordinator = personCoordinator;
    }

    /**
     * @return the propertyCoordinator
     */
    public PropertyCoordinator getPropertyCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{propertyCoordinator}", PropertyCoordinator.class);
        propertyCoordinator = (PropertyCoordinator) ve.getValue(context.getELContext());
        
        return propertyCoordinator;
    }

    /**
     * @param propertyCoordinator the propertyCoordinator to set
     */
    public void setPropertyCoordinator(PropertyCoordinator propertyCoordinator) {
        this.propertyCoordinator = propertyCoordinator;
    }

    /**
     * @return the occupancyCoordiator
     */
    public OccupancyCoordinator getOccupancyCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{occupancyCoordinator}", OccupancyCoordinator.class);
        occupancyCoordinator = (OccupancyCoordinator) ve.getValue(context.getELContext());
        return occupancyCoordinator;
    }

    /**
     * @param occupancyCoordiator the occupancyCoordiator to set
     */
    public void setOccupancyCoordinator(OccupancyCoordinator occupancyCoordiator) {
        this.occupancyCoordinator = occupancyCoordiator;
    }
/**
     * @return the systemCoordinator
     */
    public SystemCoordinator getSystemCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{systemCoordinator}", SystemCoordinator.class);
        systemCoordinator = (SystemCoordinator) ve.getValue(context.getELContext());
        return systemCoordinator;
    }
/**
     * @return the systemCoordinator
     */
    public DataCoordinator getDataCoordinator() {
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{dataCoordinator}", DataCoordinator.class);
        dataCoordinator = (DataCoordinator) ve.getValue(context.getELContext());
        return dataCoordinator;
    }

    /**
     * @param systemCoordinator the systemCoordinator to set
     */
    public void setSystemCoordinator(SystemCoordinator systemCoordinator) {
        this.systemCoordinator = systemCoordinator;
    }

    
    public ChoiceCoordinator getChoiceCoordinator(){
        ChoiceCoordinator cc;;
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{ChoiceCoordinator}", ChoiceCoordinator.class);
        cc = (ChoiceCoordinator) ve.getValue(context.getELContext());
        return cc;
    }
    
    public ChoiceIntegrator getChoiceIntegrator(){
        ChoiceIntegrator ci;
        FacesContext context = getFacesContext();
        ValueExpression ve = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{ChoiceIntegrator}", ChoiceIntegrator.class);
        ci = (ChoiceIntegrator) ve.getValue(context.getELContext());
        return ci;
    }
}