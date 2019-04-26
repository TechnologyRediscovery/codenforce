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


import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.ViolationIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolationBB extends BackingBeanUtils implements Serializable {
    
    private String formLetterText;
    private java.util.Date formDateOfRecord;
    private NoticeOfViolation currentNotice;
    private List<CodeViolation> activeVList;
    private List<TextBlock> blockListByMuni;
    
    private Person selectedRecipient;
    private List<Person> personCandidateAL;
    private List<TextBlock> selectedBlockList;
    
    
    private boolean addPersonByID;
    private int recipientPersonID;
    
    private Person retrievedManualLookupPerson;
    private List<Person> manualRetrievedPersonList;
    
    private TextBlock greetingBlock;
    private TextBlock introBlock;
    private boolean useTb1;
    private TextBlock tb1;
    private boolean useTb2;
    private TextBlock tb2;
    private TextBlock complianceBlock;
    private TextBlock penaltyBlock;
    private boolean useTb3;
    private TextBlock tb3;
    private boolean useTb4;
    private TextBlock tb4;
    private TextBlock closing;
    
    private TextBlock chosenBlock;
    private List<TextBlock> blockListBeforeViolations;
    private List<TextBlock> blockListAfterViolations;
    
    
    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {
        useTb1 = false;
        useTb2 = false;
        useTb3 = false;
        useTb4 = false;
        formDateOfRecord = java.sql.Date.valueOf(LocalDate.now());
    }
    
    @PostConstruct
    public void initBean(){
        blockListBeforeViolations = new ArrayList<>();
        blockListAfterViolations = new ArrayList<>();
        manualRetrievedPersonList = new ArrayList<>();
    }
    
    public void addBlockBeforeViolations(TextBlock tb){
        blockListByMuni.remove(tb);
        blockListBeforeViolations.add(tb);
    }
    
    
    public void addBlockAfterViolations(TextBlock tb){
        blockListByMuni.remove(tb);
        blockListAfterViolations.add(tb);
    }
    
    
    
    public void addBlockToList(ActionEvent ae){
        
        System.out.println("NoticeOfViolationBB.addBlockToList");
        selectedBlockList.add(greetingBlock);
    }
    
    public void removeViolationFromList(CodeViolation viol){
        activeVList.remove(viol);
        getSessionBean().setViolationQueue(activeVList);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Done: violation ID " + viol.getViolationID() + "will not be included in letter.",""));
    }
    
    public String setupNewNotice(){
        CaseCoordinator cc = getCaseCoordinator();
        if(activeVList.isEmpty()){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "You must keep at least one violation to include in your letter!",""));
            
            return "";
        }
        currentNotice = cc.novGetNewNOVSkeleton();
        getSessionBean().setActiveNotice(currentNotice);
        return "noticeOfViolationBuilderPersons";
    }
    
    public void checkNOVRecipient(ActionEvent ev){
        PersonIntegrator pi = getPersonIntegrator();
        try {
            retrievedManualLookupPerson = pi.getPerson(recipientPersonID);
            System.out.println("NoticeOfViolationBB.checkNOVRecipient | looked up person: " + retrievedManualLookupPerson);
            if(retrievedManualLookupPerson != null){
                manualRetrievedPersonList.add(retrievedManualLookupPerson);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucess! Found person " + retrievedManualLookupPerson.getPersonID()  + "( " + retrievedManualLookupPerson.getLastName()+ ")",""));
                
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Person not found; try again!",""));
                
            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                recipientPersonID + "not mapped to a known person", "Please try again or visit our person search page."));
        }
    }
    
    
    public String connectPersonAndStoreRecipient(Person pers){
        PersonIntegrator pi = getPersonIntegrator();
        try {
                pi.connectPersonToProperty(retrievedManualLookupPerson, getSessionBean().getPropertyQueue().get(0));
            } catch (IntegrationException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error connecting this person to the current property. Please tell Eric about this.",""));
            }
        
        return storeRecipient(pers);
    }
    
    public String storeRecipient(Person pers){
        currentNotice = getSessionBean().getActiveNotice();
        currentNotice.setRecipient(pers);
           
        return "noticeOfViolationBuilderText";
    }
    
    
    public String assembleNotice(){
        currentNotice = getSessionBean().getActiveNotice();
        activeVList = getSessionBean().getViolationQueue();
        
        StringBuilder sb = new StringBuilder();
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append("<br/><br/>");
        appendRecipientAddrBlock(sb, currentNotice.getRecipient());
        Iterator<TextBlock> it = blockListBeforeViolations.iterator();
        while(it.hasNext()){
            appendTextBlockAsPara(it.next(), sb);
        }
        
        appendViolationList(activeVList, sb);
        
        it = blockListAfterViolations.iterator();
        while(it.hasNext()){
            appendTextBlockAsPara(it.next(), sb);
        }
        
        
        appendSignatureBlock(sb);
        
        // finally, extract the String from the StringBuilder and add to our
        // current notice, which we'll make the active notice for editing
        currentNotice.setNoticeTextBeforeViolations(sb.toString());
        getSessionBean().setActiveNotice(currentNotice);
        
        return "noticeOfViolationEditor";
    }

    
    private StringBuilder appendViolationList(List<CodeViolation> vlist, StringBuilder sb){
        
        Iterator<CodeViolation> iter = vlist.iterator();
        
        while(iter.hasNext()){
            CodeViolation cv = (CodeViolation) iter.next();
            
            sb.append(cv.getViolatedEnfElement().getCodeElement().getOrdchapterNo());
            sb.append(" : ");
            sb.append(cv.getViolatedEnfElement().getCodeElement().getOrdSecNum());
            sb.append(" : ");
            sb.append(cv.getViolatedEnfElement().getCodeElement().getOrdSubSecNum());
            sb.append(" - ");
            sb.append(cv.getViolatedEnfElement().getCodeElement().getOrdSubSecTitle());
            sb.append("<br><br>");
            sb.append(cv.getViolatedEnfElement().getCodeElement().getOrdTechnicalText());
            sb.append("<br>");
        } //close while
        
        System.out.println("NoticeOfViolationBB.buildStringFromViolationList | notice text: " + sb.toString());
        return sb;
    }
    
    private StringBuilder appendTextBlockAsPara(TextBlock tb, StringBuilder sb){
        sb.append("<p>");
        sb.append(tb.getTextBlockText());
        sb.append("</p>");
        return sb;
    }
    
    private StringBuilder appendSignatureBlock(StringBuilder sb){
        
        User u = getFacesUser();
        sb.append("<p>");
        sb.append(u.getPerson().getFirstName());
        sb.append(" ");
        sb.append(u.getPerson().getLastName());
        sb.append("<br>");
        sb.append(u.getPerson().getJobTitle());
        sb.append("<br>");
        sb.append(getSessionBean().getActiveMuni().getMuniName());
        sb.append("<br>");
        sb.append(u.getPerson().getPhoneWork());
        sb.append("<br>");
        sb.append(u.getPerson().getEmail());
        sb.append("</p>");
        return sb;
        
    }
    
    private StringBuilder appendRecipientAddrBlock(StringBuilder sb, Person p){
        sb.append(p.getFirstName());
        sb.append(" ");
        sb.append(p.getLastName());
        sb.append("<br>");
        sb.append(p.getAddressStreet());
        sb.append("<br>");
        sb.append(p.getAddressCity());
        sb.append(", ");
        sb.append(p.getAddressState());
        sb.append(" ");
        sb.append(p.getAddressZip());
        sb.append("<br>");
        
        return sb;
        
    }
    
    public String saveNoticeDraft(){
        
        CECase c = getSessionBean().getcECase();
        NoticeOfViolation notice = getSessionBean().getActiveNotice();
        CaseIntegrator csi = getCaseIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        ViolationIntegrator ci = getCodeViolationIntegrator();
        
        notice.setNoticeTextBeforeViolations(formLetterText);
        notice.setDateOfRecord(formDateOfRecord.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        try {
        
            // new notices won't have insertion time stamps
            if(currentNotice.getCreationTS() == null){
                cc.(currentNotice, currentNotice);
                
            } else {
                ci.novUpdate(currentNotice);
            }
            // refresh case
            getSessionBean().setcECase(
                    csi.getCECase(getSessionBean().getcECase().getCaseID()));
            
        } catch (IntegrationException ex) {
            System.out.println("NoticeOfViolationBB.saveNoticeDraft");
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to saveDraft of notice letter due to a database error. "
                                + "This must be corrected by Eric.", ""));
            return "";
        } catch (CaseLifecyleException ex) {
            Logger.getLogger(NoticeOfViolationBB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ceCases";
    } // close method
    
    /**
     * @return the formLetterText
     */
    public String getFormLetterText() {
        formLetterText = currentNotice.getNoticeTextBeforeViolations();
        
        return formLetterText;
    }

    /**
     * @param formLetterText the formLetterText to set
     */
    public void setFormLetterText(String formLetterText) {
        this.formLetterText = formLetterText;
    }

    /**
     * @return the formDateOfRecord
     */
    public Date getFormDateOfRecord() {
        if(currentNotice.getDateOfRecord() != null){
            formDateOfRecord = java.util.Date.from(currentNotice.getDateOfRecord().toInstant(ZoneOffset.UTC));
            
        } else {
            formDateOfRecord = null;
        }
            return formDateOfRecord;
    }

    /**
     * @param formDateOfRecord the formDateOfRecord to set
     */
    public void setFormDateOfRecord(Date formDateOfRecord) {
        this.formDateOfRecord = formDateOfRecord;
    }

    /**
     * @return the currentNotice
     */
    public NoticeOfViolation getCurrentNotice() {
        
        currentNotice = getSessionBean().getActiveNotice();
        return currentNotice;
    }

    /**
     * @param currentNotice the currentNotice to set
     */
    public void setCurrentNotice(NoticeOfViolation currentNotice) {
        this.currentNotice = currentNotice;
    }

    /**
     * @return the textBlockListByMuni
     */
    public List<TextBlock> getBlockListByMuni() {
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        Municipality m = getSessionBean().getActiveMuni();
        if(blockListByMuni == null){
            try {
                blockListByMuni = cvi.getTextBlocks(m);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return blockListByMuni;
    }

    /**
     * @param textBlockListByMuni the textBlockListByMuni to set
     */
    public void setTextBlockListByMuni(ArrayList<TextBlock> textBlockListByMuni) {
        this.blockListByMuni = textBlockListByMuni;
    }

    /**
     * @return the activeVList
     */
    public List<CodeViolation> getActiveVList() {
        if(activeVList == null){
            activeVList = getSessionBean().getViolationQueue();
        }
        return activeVList;
    }

    /**
     * @param activeVList the activeVList to set
     */
    public void setActiveVList(ArrayList<CodeViolation> activeVList) {
        this.activeVList = activeVList;
    }

    /**
     * @return the greetingBlock
     */
    public TextBlock getGreetingBlock() {
        return greetingBlock;
    }

    /**
     * @return the introBlock
     */
    public TextBlock getIntroBlock() {
        return introBlock;
    }

    /**
     * @return the tb1
     */
    public TextBlock getTb1() {
        return tb1;
    }

    /**
     * @return the tb2
     */
    public TextBlock getTb2() {
        return tb2;
    }

    /**
     * @return the complianceBlock
     */
    public TextBlock getComplianceBlock() {
        return complianceBlock;
    }

    /**
     * @return the penaltyBlock
     */
    public TextBlock getPenaltyBlock() {
        return penaltyBlock;
    }

    /**
     * @return the tb3
     */
    public TextBlock getTb3() {
        return tb3;
    }

    /**
     * @return the tb4
     */
    public TextBlock getTb4() {
        return tb4;
    }

    /**
     * @return the closing
     */
    public TextBlock getClosing() {
        return closing;
    }

    /**
     * @param greetingBlock the greetingBlock to set
     */
    public void setGreetingBlock(TextBlock greetingBlock) {
        this.greetingBlock = greetingBlock;
    }

    /**
     * @param introBlock the introBlock to set
     */
    public void setIntroBlock(TextBlock introBlock) {
        this.introBlock = introBlock;
    }

    /**
     * @param tb1 the tb1 to set
     */
    public void setTb1(TextBlock tb1) {
        this.tb1 = tb1;
    }

    /**
     * @param tb2 the tb2 to set
     */
    public void setTb2(TextBlock tb2) {
        this.tb2 = tb2;
    }

    /**
     * @param complianceBlock the complianceBlock to set
     */
    public void setComplianceBlock(TextBlock complianceBlock) {
        this.complianceBlock = complianceBlock;
    }

    /**
     * @param penaltyBlock the penaltyBlock to set
     */
    public void setPenaltyBlock(TextBlock penaltyBlock) {
        this.penaltyBlock = penaltyBlock;
    }

    /**
     * @param tb3 the tb3 to set
     */
    public void setTb3(TextBlock tb3) {
        this.tb3 = tb3;
    }

    /**
     * @param tb4 the tb4 to set
     */
    public void setTb4(TextBlock tb4) {
        this.tb4 = tb4;
    }

    /**
     * @param closing the closing to set
     */
    public void setClosing(TextBlock closing) {
        this.closing = closing;
    }

    /**
     * @return the selectedRecipient
     */
    public Person getSelectedRecipient() {
        return selectedRecipient;
    }

    /**
     * @param selectedRecipient the selectedRecipient to set
     */
    public void setSelectedRecipient(Person selectedRecipient) {
        this.selectedRecipient = selectedRecipient;
    }

    /**
     * @return the personCandidateAL
     */
    public List<Person> getPersonCandidateAL() {
        PersonIntegrator pi = getPersonIntegrator();
        
        Property prop = getSessionBean().getActiveProp();
        try {
            personCandidateAL = pi.getPersonList(prop);
            if(personCandidateAL == null){
                personCandidateAL = new ArrayList<>();
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return personCandidateAL;
    }

    /**
     * @param personCandidateAL the personCandidateAL to set
     */
    public void setPersonCandidateAL(ArrayList<Person> personCandidateAL) {
        this.personCandidateAL = personCandidateAL;
    }

    /**
     * @return the recipientPersonID
     */
    public int getRecipientPersonID() {
        return recipientPersonID;
    }

    /**
     * @param recipientPersonID the recipientPersonID to set
     */
    public void setRecipientPersonID(int recipientPersonID) {
        this.recipientPersonID = recipientPersonID;
    }

    /**
     * @return the addPersonByID
     */
    public boolean isAddPersonByID() {
        return addPersonByID;
    }

    /**
     * @param addPersonByID the addPersonByID to set
     */
    public void setAddPersonByID(boolean addPersonByID) {
        this.addPersonByID = addPersonByID;
    }

    /**
     * @return the useTb1
     */
    public boolean isUseTb1() {
        return useTb1;
    }

    /**
     * @return the useTb2
     */
    public boolean isUseTb2() {
        return useTb2;
    }

    /**
     * @return the useTb3
     */
    public boolean isUseTb3() {
        return useTb3;
    }

    /**
     * @return the useTb4
     */
    public boolean isUseTb4() {
        return useTb4;
    }

    /**
     * @param useTb1 the useTb1 to set
     */
    public void setUseTb1(boolean useTb1) {
        this.useTb1 = useTb1;
    }

    /**
     * @param useTb2 the useTb2 to set
     */
    public void setUseTb2(boolean useTb2) {
        this.useTb2 = useTb2;
    }

    /**
     * @param useTb3 the useTb3 to set
     */
    public void setUseTb3(boolean useTb3) {
        this.useTb3 = useTb3;
    }

    /**
     * @param useTb4 the useTb4 to set
     */
    public void setUseTb4(boolean useTb4) {
        this.useTb4 = useTb4;
    }

    /**
     * @return the retrievedManualLookupPerson
     */
    public Person getRetrievedManualLookupPerson() {
        return retrievedManualLookupPerson;
    }

    /**
     * @param retrievedManualLookupPerson the retrievedManualLookupPerson to set
     */
    public void setRetrievedManualLookupPerson(Person retrievedManualLookupPerson) {
        this.retrievedManualLookupPerson = retrievedManualLookupPerson;
    }

    /**
     * @return the selectedBlockList
     */
    public List<TextBlock> getSelectedBlockList() {
        return selectedBlockList;
    }

    /**
     * @param selectedBlockList the selectedBlockList to set
     */
    public void setSelectedBlockList(List<TextBlock> selectedBlockList) {
        this.selectedBlockList = selectedBlockList;
    }

    /**
     * @return the chosenBlock
     */
    public TextBlock getChosenBlock() {
        return chosenBlock;
    }

    /**
     * @param chosenBlock the chosenBlock to set
     */
    public void setChosenBlock(TextBlock chosenBlock) {
        this.chosenBlock = chosenBlock;
    }

    /**
     * @return the blockListBeforeViolations
     */
    public List<TextBlock> getBlockListBeforeViolations() {
        return blockListBeforeViolations;
    }

    /**
     * @param blockListBeforeViolations the blockListBeforeViolations to set
     */
    public void setBlockListBeforeViolations(List<TextBlock> blockListBeforeViolations) {
        this.blockListBeforeViolations = blockListBeforeViolations;
    }

    /**
     * @return the blockListAfterViolations
     */
    public List<TextBlock> getBlockListAfterViolations() {
        return blockListAfterViolations;
    }

    /**
     * @param blockListAfterViolations the blockListAfterViolations to set
     */
    public void setBlockListAfterViolations(List<TextBlock> blockListAfterViolations) {
        this.blockListAfterViolations = blockListAfterViolations;
    }

    /**
     * @return the manualRetrievedPersonList
     */
    public List<Person> getManualRetrievedPersonList() {
        return manualRetrievedPersonList;
    }

    /**
     * @param manualRetrievedPersonList the manualRetrievedPersonList to set
     */
    public void setManualRetrievedPersonList(List<Person> manualRetrievedPersonList) {
        this.manualRetrievedPersonList = manualRetrievedPersonList;
    }
    
}
