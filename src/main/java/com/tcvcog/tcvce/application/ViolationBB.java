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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationPropCECaseHeavy;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.search.QueryCodeViolation;
import com.tcvcog.tcvce.entities.search.QueryCodeViolationEnum;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.DateTimeUtil;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class ViolationBB extends BackingBeanUtils implements Serializable {

    private CodeViolation currentViolation;
    private CECaseDataHeavy currentCase;
    
    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private List<IntensityClass> severityList;
    
    private String formNoteText;
    private List<EnforcableCodeElement> filteredElementList;
    private CodeSet currentCodeSet;

    private boolean extendStipCompUsingDate;
    private java.util.Date extendedStipCompDate;
    private int extendedStipCompDaysFromToday;
    
    // METRICS
    private BarChartModel barViolationsTimeSeries;
    private HorizontalBarChartModel barViolationsPast30;
    
    private List<CodeViolationPropCECaseHeavy> violationsLoggedNoNOVPast30;
    private List<CodeViolationPropCECaseHeavy> violationsLoggedNOVPast30;
    private List<CodeViolationPropCECaseHeavy> violationsLoggedCompliancePast30;
    private List<CodeViolationPropCECaseHeavy> violationsCitedPast30;
    

    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationBB() {

    }

    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getSessCECase(), getSessionBean().getSessUser());
            
            currentViolation = getSessionBean().getSessCodeViolation();
            if (currentViolation == null) {
                if (currentCase != null && !currentCase.getViolationList().isEmpty()) {
                    currentViolation = currentCase.getViolationList().get(0);
                }
            }

            severityList = sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_violationseverity"))
                    .getClassList();
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));

        }

        filteredElementList = null;
        extendStipCompUsingDate = true;
        currentCodeSet = getSessionBean().getSessCodeSet();

        viewOptionList = Arrays.asList(ViewOptionsActiveListsEnum.values());
        selectedViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        
        System.out.println("ViolationBB.initBean()");
        
        initBarViolationsTimeSeries();
        initBarViolationsPastMonth();

    }
    
    
    private void gatherViolationData(){
        
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        try {
            QueryCodeViolation ccv = sc.initQuery(QueryCodeViolationEnum.LOGGED_NO_NOV_PAST30, getSessionBean().getSessUser().getKeyCard());
            violationsLoggedNoNOVPast30 = sc.runQuery(ccv).getResults();
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryTitle());
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryLog());
            
            ccv = sc.initQuery(QueryCodeViolationEnum.LOGGED_PAST30_NOV_CITMAYBE, getSessionBean().getSessUser().getKeyCard());
            violationsLoggedNOVPast30 = sc.runQuery(ccv).getResults();
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryTitle());
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryLog());
            
            ccv = sc.initQuery(QueryCodeViolationEnum.CITED_PAST30, getSessionBean().getSessUser().getKeyCard());
            violationsCitedPast30 = sc.runQuery(ccv).getResults();
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryTitle());
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryLog());
            
            ccv = sc.initQuery(QueryCodeViolationEnum.COMP_PAST30, getSessionBean().getSessUser().getKeyCard());
            violationsLoggedCompliancePast30 = sc.runQuery(ccv).getResults();
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryTitle());
            System.out.println("ViolationBB.gatherViolationData: " + ccv.getQueryLog());
            
            
            
        } catch (SearchException ex) {
            System.out.println(ex);
        }
    }

      private void initBarViolationsPastMonth(){
          
          gatherViolationData();
          
          barViolationsPast30 = new HorizontalBarChartModel();
          ChartData barData = new ChartData();
          
          BarChartDataSet dsNewViols = new BarChartDataSet();
          dsNewViols.setLabel("New Violations");
          dsNewViols.setBackgroundColor("rgb(255,9,122)");
          List<Number> dsNewViolsVals = new ArrayList<>();
          dsNewViolsVals.add(12);
          dsNewViolsVals.add(18);
          dsNewViolsVals.add(23);
          dsNewViolsVals.add(20);
          dsNewViolsVals.add(15);
          dsNewViolsVals.add(12);
          dsNewViols.setData(dsNewViolsVals);
          
          BarChartDataSet dsCompliance = new BarChartDataSet();
          dsCompliance.setLabel("Compliance");
          dsCompliance.setBackgroundColor("rgb(60,9,122)");
          List<Number> dsComplianceVals = new ArrayList<>();
          dsComplianceVals.add(12);
          dsComplianceVals.add(33);
          dsComplianceVals.add(36);
          dsComplianceVals.add(40);
          dsComplianceVals.add(50);
          dsComplianceVals.add(53);
          dsCompliance.setData(dsComplianceVals);
          
          BarChartDataSet dsCited = new BarChartDataSet();
          dsCited.setLabel("Citation");
          dsCited.setBackgroundColor("rgb(255,9,3)");
          List<Number> dsCitedVals = new ArrayList<>();
          dsCitedVals.add(3);
          dsCitedVals.add(4);
          dsCitedVals.add(8);
          dsCitedVals.add(12);
          dsCitedVals.add(8);
          dsCitedVals.add(12);
          dsCited.setData(dsCitedVals);
          
          BarChartDataSet dsNull = new BarChartDataSet();
          dsNull.setLabel("Nullified");
          dsNull.setBackgroundColor("rgb(4,9,34)");
          List<Number> dsNullifiedVals = new ArrayList<>();
          dsNullifiedVals.add(3);
          dsNullifiedVals.add(4);
          dsNullifiedVals.add(5);
          dsNullifiedVals.add(3);
          dsNullifiedVals.add(4);
          dsNullifiedVals.add(3);
          dsNull.setData(dsNullifiedVals);
          
          
          barData.addChartDataSet(dsNewViols);
          barData.addChartDataSet(dsCompliance);
          barData.addChartDataSet(dsCited);
          barData.addChartDataSet(dsNull);
          
          List<String> labels = new ArrayList<>();
          labels.add("Nov 2020");
          labels.add("Dec 2020");
          labels.add("Jan 2021");
          labels.add("Feb 2021");
          labels.add("Mar 2021");
          labels.add("Apr 2021");
          barData.setLabels(labels);
          
          getBarViolationsTimeSeries().setData(barData);
          
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);    
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Violations: Past 30 days");
        options.setTitle(title);
        
        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);  
        
         barViolationsPast30.setOptions(options);
          
          
          
      }

    
      private void initBarViolationsTimeSeries(){
          
          barViolationsTimeSeries = new BarChartModel();
          ChartData barData = new ChartData();
          
          BarChartDataSet dsNewViols = new BarChartDataSet();
          dsNewViols.setLabel("New Violations");
          dsNewViols.setBackgroundColor("rgb(255,9,122)");
          List<Number> dsNewViolsVals = new ArrayList<>();
          dsNewViolsVals.add(12);
          dsNewViolsVals.add(18);
          dsNewViolsVals.add(23);
          dsNewViolsVals.add(20);
          dsNewViolsVals.add(15);
          dsNewViolsVals.add(12);
          dsNewViols.setData(dsNewViolsVals);
          
          BarChartDataSet dsCompliance = new BarChartDataSet();
          dsCompliance.setLabel("Compliance");
          dsCompliance.setBackgroundColor("rgb(60,9,122)");
          List<Number> dsComplianceVals = new ArrayList<>();
          dsComplianceVals.add(12);
          dsComplianceVals.add(33);
          dsComplianceVals.add(36);
          dsComplianceVals.add(40);
          dsComplianceVals.add(50);
          dsComplianceVals.add(53);
          dsCompliance.setData(dsComplianceVals);
          
          BarChartDataSet dsCited = new BarChartDataSet();
          dsCited.setLabel("Citation");
          dsCited.setBackgroundColor("rgb(255,9,3)");
          List<Number> dsCitedVals = new ArrayList<>();
          dsCitedVals.add(3);
          dsCitedVals.add(4);
          dsCitedVals.add(8);
          dsCitedVals.add(12);
          dsCitedVals.add(8);
          dsCitedVals.add(12);
          dsCited.setData(dsCitedVals);
          
          BarChartDataSet dsNull = new BarChartDataSet();
          dsNull.setLabel("Nullified");
          dsNull.setBackgroundColor("rgb(4,9,34)");
          List<Number> dsNullifiedVals = new ArrayList<>();
          dsNullifiedVals.add(3);
          dsNullifiedVals.add(4);
          dsNullifiedVals.add(5);
          dsNullifiedVals.add(3);
          dsNullifiedVals.add(4);
          dsNullifiedVals.add(3);
          dsNull.setData(dsNullifiedVals);
          
          
          barData.addChartDataSet(dsNewViols);
          barData.addChartDataSet(dsCompliance);
          barData.addChartDataSet(dsCited);
          barData.addChartDataSet(dsNull);
          
          List<String> labels = new ArrayList<>();
          labels.add("Nov 2020");
          labels.add("Dec 2020");
          labels.add("Jan 2021");
          labels.add("Feb 2021");
          labels.add("Mar 2021");
          labels.add("Apr 2021");
          barData.setLabels(labels);
          
          getBarViolationsTimeSeries().setData(barData);
          
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);    
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Violations month over month");
        options.setTitle(title);
        
        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);  
        
        getBarViolationsTimeSeries().setOptions(options);
          
          
          
      }
    
   

    /**
     * Primary listener method which copies a reference to the selected user
     * from the list and sets it on the selected user perch
     *
     * @param viol
     */
    public void onObjectViewButtonChange(CodeViolation viol) {

        if (viol != null) {
            getSessionBean().setSessCodeViolation(viol);
            currentViolation = viol;
        }
        System.out.println("ViolationBB.onObjectViewButtonChange: currentViolation is now " + currentViolation.getViolationID());

    }


    /**
     * Internal logic container for beginning the user creation change process
     * Delegated from the mode button router
     */
    public void onModeInsertInit() {
        CaseCoordinator cc = getCaseCoordinator();
        System.out.println("violationBB.OnModeInsertInit");

        try {
            setCurrentViolation(cc.violation_getCodeViolationSkeleton(currentCase));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }

    }

    /**
     * Listener for beginning of update process
     */
    public void onModeUpdateInit() {
        // nothing to do here yet since the user is selected
    }

    /**
     * Listener for the start of the case removal process
     */
    public void onModeRemoveInit() {

    }

    /**
     * Listener for user selection of a violation from the code set violation
     * table
     *
     * @param ece
     */
    public void onViolationSelectElementButtonChange(EnforcableCodeElement ece) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentViolation = cc.violation_injectOrdinance(currentViolation, ece);
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }

    /**
     * Listener for the start of the violation choosing process
     *
     * @param ev
     */
    public void onViolationSelectElementInitButtonChange(ActionEvent ev) {
        // do nothing yet
    }

    /**
     * Listener for commencement of extending stip comp date
     *
     * @param ev
     */
    public void onViolationExtendStipCompDateInitButtonChange(ActionEvent ev) {
        extendedStipCompDaysFromToday = CaseCoordinator.DEFAULT_EXTENSIONDAYS;
    }

    /**
     * Listener for requests to commit extension of stip comp date
     *
     * @return
     */
    public String onViolationExtendStipCompDateCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        long secBetween;
        try {
            if (extendStipCompUsingDate && extendedStipCompDate != null) {
                LocalDateTime freshDate = extendedStipCompDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                if (freshDate.isBefore(LocalDateTime.now())) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Stipulated compliance dates must be in the future!", ""));
                } else {
                    secBetween = freshDate.toEpochSecond(ZoneOffset.of("-4")) - LocalDateTime.now().toEpochSecond(ZoneOffset.of("-4"));
                    // divide by num seconds in a day
                    long daysBetween = secBetween / (24 * 60 * 60);
                    cc.violation_extendStipulatedComplianceDate(currentViolation, daysBetween, currentCase, getSessionBean().getSessUser());
                }
            } else {
                cc.violation_extendStipulatedComplianceDate(currentViolation, extendedStipCompDaysFromToday, currentCase, getSessionBean().getSessUser());
            }
        } catch (BObStatusException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
        } 
        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Stipulated compliance dates is now: " + DateTimeUtil.getPrettyDate(currentViolation.getStipulatedComplianceDate()), ""));
        return "ceCaseViolations";

    }

    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdateCommitButtonChange() throws IntegrationException, BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        EventCoordinator eventCoordinator = getEventCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        EventCategory ec = eventCoordinator.initEventCategory(
                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));

        try {

            cc.violation_updateCodeViolation(currentCase, currentViolation, getSessionBean().getSessUser());

            // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation in the database",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Please revise the stipulated compliance date"));

        }

        return "ceCaseViolations";
    }

    /**
     * Listener for user requests to commit a violation compliance event
     *
     * @return 
     */
    public String onViolationRecordComplianceCommitButtonChange() {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        
            // build event details package
            EventCnF e = null;
            try {
                
//                cc.violation_recordCompliance(currentViolation, getSessionBean().getSessUser());
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compliance recorded", ""));
                
                // ************ TODO: Finish me with events ******************//
                // ************ TODO: Finish me with events ******************//
                e = ec.generateViolationComplianceEvent(currentViolation);
                e.setUserCreator(getSessionBean().getSessUser());
                e.setTimeStart(LocalDateTime.now());
                
                // ************ TODO: Finish me with events ******************//
                // ************ TODO: Finish me with events ******************//
                
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compliance event attached to case", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
                   return "";
            }

        return "ceCaseViolations";
        

            
        // the user is then shown the add event dialog, and when the
        // event is added to the case, the CaseCoordinator will
        // set the date of record on the violation to match that chosen
        // for the event
//        selectedEvent = e;
    }

    /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
        setFormNoteText(null);

    }

    /**
     * Listener for user requests to commit new note content to the current
     * object
     *
     * @return
     */
    public String onNoteCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentViolation.getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Violation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.violation_updateNotes(mbp, currentViolation);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));
            return "";
        }
        return "ceCaseViolations";
    }

    /**
     * Listener for user requests to abort their insert/update operation
     *
     * @return
     */
    public String onInsertUpdateAbortButtonChange() {
        getSessionBean().setSessCitation(null);
        return "ceCaseViolations";

    }

    
    /**
     * Responds to user requests to commit a new code violation to the CECase
     *
     * @return
     */
    public String onViolationAddCommitButtonChange() {

        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.violation_attachViolationToCase(currentViolation, currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation attached to case.", ""));
            getSessionBean().getSessionBean().setSessCodeViolation(currentViolation);
            System.out.println("ViolationBB.onViolationAddCommmitButtonChange | completed violation process");
        } catch (IntegrationException | SearchException | BObStatusException | EventException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            return "";
        }
        return "ceCaseViolations";

    }

    /**
     * Listener for user requests to remove a violation from a case
     *
     * @return
     */
    public String onViolationRemoveCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_deactivateCodeViolation(currentViolation, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            return "";

        }
        return "ceCaseViolations";

    }
    
    public String onViolationNullifyCommitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
         try {
            cc.violation_deactivateCodeViolation(currentViolation, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            return "";

        }
        return "ceCaseViolations";
        
    }

  
    
    /**
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {

        return currentViolation;
    }

    /**
     * Sets the current violation, then loads its blobs
     * @param currentViolation
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
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

   
    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @return the filteredElementList
     */
    public List<EnforcableCodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    /**
     * @param filteredElementList the filteredElementList to set
     */
    public void setFilteredElementList(List<EnforcableCodeElement> filteredElementList) {
        this.filteredElementList = filteredElementList;
    }

    /**
     * @return the currentCodeSet
     */
    public CodeSet getCurrentCodeSet() {
        return currentCodeSet;
    }

    /**
     * @param currentCodeSet the currentCodeSet to set
     */
    public void setCurrentCodeSet(CodeSet currentCodeSet) {
        this.currentCodeSet = currentCodeSet;
    }

    /**
     * @return the extendedStipCompDaysFromToday
     */
    public int getExtendedStipCompDaysFromToday() {
        return extendedStipCompDaysFromToday;
    }

    /**
     * @param extendedStipCompDaysFromToday the extendedStipCompDaysFromToday to
     * set
     */
    public void setExtendedStipCompDaysFromToday(int extendedStipCompDaysFromToday) {
        this.extendedStipCompDaysFromToday = extendedStipCompDaysFromToday;
    }

    /**
     * @return the extendedStipCompDate
     */
    public java.util.Date getExtendedStipCompDate() {
        return extendedStipCompDate;
    }

    /**
     * @param extendedStipCompDate the extendedStipCompDate to set
     */
    public void setExtendedStipCompDate(java.util.Date extendedStipCompDate) {
        this.extendedStipCompDate = extendedStipCompDate;
    }

    /**
     * @return the extendStipCompUsingDate
     */
    public boolean isExtendStipCompUsingDate() {
        return extendStipCompUsingDate;
    }

    /**
     * @param extendStipCompUsingDate the extendStipCompUsingDate to set
     */
    public void setExtendStipCompUsingDate(boolean extendStipCompUsingDate) {
        this.extendStipCompUsingDate = extendStipCompUsingDate;
    }

    /**
     * @return the severityList
     */
    public List<IntensityClass> getSeverityList() {
        return severityList;
    }

    /**
     * @param severityList the severityList to set
     */
    public void setSeverityList(List<IntensityClass> severityList) {
        this.severityList = severityList;
    }

    /**
     * @return the selectedViewOption
     */
    public ViewOptionsActiveListsEnum getSelectedViewOption() {
        return selectedViewOption;
    }

    /**
     * @param selectedViewOption the selectedViewOption to set
     */
    public void setSelectedViewOption(ViewOptionsActiveListsEnum selectedViewOption) {
        this.selectedViewOption = selectedViewOption;
    }

    /**
     * @return the viewOptionList
     */
    public List<ViewOptionsActiveListsEnum> getViewOptionList() {
        return viewOptionList;
    }

    /**
     * @param viewOptionList the viewOptionList to set
     */
    public void setViewOptionList(List<ViewOptionsActiveListsEnum> viewOptionList) {
        this.viewOptionList = viewOptionList;
    }

    /**
     * @return the barViolationsTimeSeries
     */
    public BarChartModel getBarViolationsTimeSeries() {
        return barViolationsTimeSeries;
    }

    /**
     * @return the barViolationsPast30
     */
    public BarChartModel getBarViolationsPast30() {
        return barViolationsPast30;
    }

    /**
     * @param barViolationsPast30 the barViolationsPast30 to set
     */
    public void setBarViolationsPast30(HorizontalBarChartModel barViolationsPast30) {
        this.barViolationsPast30 = barViolationsPast30;
    }

    /**
     * @return the violationsLoggedNoNOVPast30
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsLoggedNoNOVPast30() {
        return violationsLoggedNoNOVPast30;
    }

    /**
     * @return the violationsLoggedCompliancePast30
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsLoggedCompliancePast30() {
        return violationsLoggedCompliancePast30;
    }

    /**
     * @return the violationsCitedPast30
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsCitedPast30() {
        return violationsCitedPast30;
    }

    /**
     * @param violationsLoggedNoNOVPast30 the violationsLoggedNoNOVPast30 to set
     */
    public void setViolationsLoggedNoNOVPast30(List<CodeViolationPropCECaseHeavy> violationsLoggedNoNOVPast30) {
        this.violationsLoggedNoNOVPast30 = violationsLoggedNoNOVPast30;
    }

    /**
     * @param violationsLoggedCompliancePast30 the violationsLoggedCompliancePast30 to set
     */
    public void setViolationsLoggedCompliancePast30(List<CodeViolationPropCECaseHeavy> violationsLoggedCompliancePast30) {
        this.violationsLoggedCompliancePast30 = violationsLoggedCompliancePast30;
    }

    /**
     * @param violationsCitedPast30 the violationsCitedPast30 to set
     */
    public void setViolationsCitedPast30(List<CodeViolationPropCECaseHeavy> violationsCitedPast30) {
        this.violationsCitedPast30 = violationsCitedPast30;
    }

    /**
     * @return the violationsLoggedNOVPast30
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsLoggedNOVPast30() {
        return violationsLoggedNOVPast30;
    }

    /**
     * @param violationsLoggedNOVPast30 the violationsLoggedNOVPast30 to set
     */
    public void setViolationsLoggedNOVPast30(List<CodeViolationPropCECaseHeavy> violationsLoggedNOVPast30) {
        this.violationsLoggedNOVPast30 = violationsLoggedNOVPast30;
    }

}
