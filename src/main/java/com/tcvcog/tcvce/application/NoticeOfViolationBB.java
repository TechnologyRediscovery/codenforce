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
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolationBB extends BackingBeanUtils implements Serializable {
    
    private String formLetterText;
    private Date formDateOfRecord;
    private NoticeOfViolation currentNotice;
    private ArrayList<CodeViolation> activeVList;
    private ArrayList<TextBlock> blockListByMuni;
    
    private Person selectedRecipient;
    private ArrayList<Person> personCandidateAL;
    
    private boolean addPersonByID;
    private int recipientPersonID;
    
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
    
    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {
        useTb1 = false;
        useTb2 = false;
        useTb3 = false;
        useTb4 = false;
    }
    
    public String assembleNotice(){
        
        if((addPersonByID == false && selectedRecipient == null) && (addPersonByID == true && recipientPersonID == 0)){
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "A notice needs a recipient! Please either select a person from the table or add a person by ID",""));
            return "";
        }
        
        currentNotice = new NoticeOfViolation();
        StringBuilder sb = new StringBuilder();
        
        PersonIntegrator pi = getPersonIntegrator();
        
        if(isAddPersonByID()){
            try {
                System.out.println("NoticeOfViolationBB.assembleNotice | entered person ID " + recipientPersonID );
                currentNotice.setRecipient(pi.getPerson(recipientPersonID));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to load code violation list", 
                    "This is a system-level error that must be corrected by Eric"));
                
            }
        } else {
            currentNotice.setRecipient(selectedRecipient);
        }
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append("<br/><br/>");
        appendRecipientAddrBlock(sb, currentNotice.getRecipient());
        appendTextBlockAsPara(greetingBlock, sb);
        appendTextBlockAsPara(introBlock, sb);
        if(useTb1){
            appendTextBlockAsPara(tb1, sb);
        }
        if(useTb2){
            appendTextBlockAsPara(tb2, sb);
        }
        appendViolationList(activeVList, sb);
        appendTextBlockAsPara(complianceBlock, sb);
        appendTextBlockAsPara(penaltyBlock, sb);
        if(useTb3){
            appendTextBlockAsPara(tb3, sb);
        }
        if(useTb4){
            appendTextBlockAsPara(tb4, sb);
        }
        appendSignatureBlock(sb);
        
        // finally, extract the String from the StringBuilder and add to our
        // current notice, which we'll make the active notice for editing
        currentNotice.setNoticeText(sb.toString());
        getSessionBean().setActiveNotice(currentNotice);
        
        return "noticeOfViolationEditor";
    }

    
    private StringBuilder appendViolationList(ArrayList<CodeViolation> vlist, StringBuilder sb){
        
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
        sb.append(u.getFName());
        sb.append(" ");
        sb.append(u.getLName());
        sb.append("<br>");
        sb.append(u.getWorkTitle());
        sb.append("<br>");
        sb.append(u.getMuni().getMuniName());
        sb.append("<br>");
        sb.append(u.getPhoneWork());
        sb.append("<br>");
        sb.append(u.getEmail());
        sb.append("</p>");
        return sb;
        
    }
    
    private StringBuilder appendRecipientAddrBlock(StringBuilder sb, Person p){
        sb.append(p.getFirstName());
        sb.append(" ");
        sb.append(p.getLastName());
        sb.append("<br>");
        sb.append(p.getAddress_street());
        sb.append("<br>");
        sb.append(p.getAddress_city());
        sb.append(", ");
        sb.append(p.getAddress_state());
        sb.append(" ");
        sb.append(p.getAddress_zip());
        sb.append("<br>");
        
        return sb;
        
    }
    
    public String queueNotice(){
        System.out.println("NoticeOfViolationBB.QueueNotice");
        CaseCoordinator caseCoord = getCaseCoordinator();
        
        CECase ceCase = getSessionBean().getActiveCase();

        NoticeOfViolation notice = getSessionBean().getActiveNotice();
//        NoticeOfViolation notice = caseCoord.generateNoticeSkeleton(ceCase);
        
        notice.setNoticeText(formLetterText);
        notice.setDateOfRecord(formDateOfRecord.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());

        try {
            
            caseCoord.queueNoticeOfViolation(ceCase, currentNotice);
            
        } catch (CaseLifecyleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to deploy notice due to a business process corruption hazard. "
                                + "Please make a notice event to discuss with Eric and Team", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update case phase due to a database connectivity error",
                        "this issue must be corrected by a system administrator, sorry"));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, 
                        "The automatic event generation associated with this action has thrown an error. "
                                + "Please create an event manually which logs this letter being queued for mailing", ""));
            
        }
        
        return "caseProfile";
        
    }
    
    public String saveNoticeDraft(){
        
        CECase c = getSessionBean().getActiveCase();
        NoticeOfViolation notice = getSessionBean().getActiveNotice();
        
        CodeViolationIntegrator ci = getCodeViolationIntegrator();
        
        notice.setNoticeText(formLetterText);
        notice.setDateOfRecord(formDateOfRecord.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        try {
        
            if(currentNotice.getInsertionTimeStamp() == null){
                ci.insertViolationLetter(c, currentNotice);
                
            } else {
                ci.updateViolationLetter(currentNotice);
            }
            
        } catch (IntegrationException ex) {
            System.out.println("NoticeOfViolationBB.saveNoticeDraft");
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to saveDraft of notice letter due to a database error. "
                                + "This must be corrected by Eric.", ""));
            return "";
        }
        return "caseViolations";
    } // close method
    
    /**
     * @return the formLetterText
     */
    public String getFormLetterText() {
        formLetterText = currentNotice.getNoticeText();
        
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
    public ArrayList<TextBlock> getTextBlockListByMuni() {
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        Municipality m = getSessionBean().getActiveCodeSet().getMuni();
        try {
            blockListByMuni = cvi.getTextBlocks(m);
        } catch (IntegrationException ex) {
            System.out.println(ex);
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
    public ArrayList<CodeViolation> getActiveVList() {
        
        activeVList = getSessionBean().getActiveViolationList();
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
    public ArrayList<Person> getPersonCandidateAL() {
        PersonIntegrator pi = getPersonIntegrator();
        
        Property prop = getSessionBean().getActiveProp();
        try {
            personCandidateAL = pi.getPersonList(prop);
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
    
}
