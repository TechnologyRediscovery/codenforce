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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationDisplayable;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.integration.ViolationIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolationBB extends BackingBeanUtils implements Serializable {
    
    private CECase currentCase;

    private NoticeOfViolation currentNotice;
    private List<CodeViolation> activeVList;
    
    private Person noticePerson;
    
    private List<TextBlock> blockList;
    
    private List<Person> personCandidateList;
    private List<Person> manualRetrievedPersonList;
    
    private List<TextBlock> blockListBeforeViolations;
    private List<TextBlock> blockListAfterViolations;
    
    private Person retrievedManualLookupPerson;
    private int recipientPersonID;
    
    private boolean showTextBlocksAllMuni;
    
    
    
    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {
        
    }
    
    @PostConstruct
    public void initBean(){
        PersonIntegrator pi = getPersonIntegrator();
        currentNotice = getSessionBean().getActiveNotice();
        currentCase = getSessionBean().getSessionCECase();
        blockListBeforeViolations = new ArrayList<>();
        blockListAfterViolations = new ArrayList<>();
        try {
            personCandidateList = pi.getPersonList(currentCase.getProperty());
            if(personCandidateList != null){
                System.out.println("NoticeOfViolationBuilderBB.initbean "
                        + "| person candidate list size: " + personCandidateList.size());
            }
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        manualRetrievedPersonList = new ArrayList<>();
        showTextBlocksAllMuni = false;
        
        
    }
    
    public void loadBlocksAllMunis(){
        blockList = null;
        System.out.println("NOVBB.loadblocksAllMunis");
        
    }
    
    public void addBlockBeforeViolations(TextBlock tb){
        blockList.remove(tb);
        blockListBeforeViolations.add(tb);
    }
    
    public void removeBlockBeforeViolations(TextBlock tb){
        blockListBeforeViolations.remove(tb);
        blockList.add(tb);
        
    }
    
    public void removeBlockAfterViolations(TextBlock tb){
        blockListAfterViolations.remove(tb);
        blockList.add(tb);
        
    }
    
    
    public void addBlockAfterViolations(TextBlock tb){
        blockList.remove(tb);
        blockListAfterViolations.add(tb);
    }
    
    
    public void removeViolationFromList(CodeViolationDisplayable viol){
        currentNotice.getViolationList().remove(viol);
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
            "Done: violation ID " + viol.getViolationID() + "will not be included in letter.",""));
    }
    
   
    
    
    public void storeRecipient(Person pers){
        currentNotice.setRecipient(pers);
    }
    
    public void checkNOVRecipient(ActionEvent ev){
        PersonIntegrator pi = getPersonIntegrator();
        try {
            setRetrievedManualLookupPerson(pi.getPerson(getRecipientPersonID()));
            System.out.println("NoticeOfViolationBB.checkNOVRecipient | looked up person: " + getRetrievedManualLookupPerson());
            if(getRetrievedManualLookupPerson() != null){
                getManualRetrievedPersonList().add(getRetrievedManualLookupPerson());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucess! Found person " + getRetrievedManualLookupPerson().getPersonID()  + "( " + getRetrievedManualLookupPerson().getLastName()+ ")",""));
                
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Person not found; try again!",""));
                
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                getRecipientPersonID() + "not mapped to a known person", "Please try again or visit our person search page."));
        }
    }
    
    public String assembleNotice(){
        if(currentNotice.getRecipient() != null){
            CaseCoordinator cc = getCaseCoordinator();
            int newNoticeId = 0;

            StringBuilder sb = new StringBuilder();
            Iterator<TextBlock> it = blockListBeforeViolations.iterator();
            while(it.hasNext()){
                appendTextBlockAsPara(it.next(), sb);
            }
            currentNotice.setNoticeTextBeforeViolations(sb.toString());

            sb = new StringBuilder();
            it = blockListAfterViolations.iterator();
            while(it.hasNext()){
                appendTextBlockAsPara(it.next(), sb);
            }

            currentNotice.setNoticeTextAfterViolations(sb.toString());

            try {
                newNoticeId = cc.novInsertNotice(currentNotice, currentCase, getSessionBean().getFacesUser());
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                System.out.println(ex);
            }
            currentNotice.setNoticeID(newNoticeId);
            getSessionBean().setActiveNotice(currentNotice);
            return "noticeOfViolationEditor";
        } else {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                getRecipientPersonID() + "Please choose a notice recipient before moving on", ""));
            
            return "";
        }
    }

    
    
    private StringBuilder appendTextBlockAsPara(TextBlock tb, StringBuilder sb){
        sb.append("<p>");
        sb.append(tb.getTextBlockText());
        sb.append("</p>");
        return sb;
    }
    
    
    public String saveNoticeDraft(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.novUpdate(currentNotice);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        return "ceCases";
    } // close method
    


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
    public List<TextBlock> getBlockList() {
        ViolationIntegrator cvi = getCodeViolationIntegrator();
        Municipality m = getSessionBean().getActiveMuni();
        if(blockList == null){
            try {
                if(showTextBlocksAllMuni){
                    blockList = cvi.getAllTextBlocks();
                } else {
                    blockList = cvi.getTextBlocks(m);
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return blockList;
    }

    /**
     * @param textBlockListByMuni the textBlockListByMuni to set
     */
    public void setTextBlockListByMuni(ArrayList<TextBlock> textBlockListByMuni) {
        this.blockList = textBlockListByMuni;
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
     * @return the personCandidateList
     */
    public List<Person> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @param personCandidateAL the personCandidateList to set
     */
    public void setPersonCandidateAL(ArrayList<Person> personCandidateAL) {
        this.personCandidateList = personCandidateAL;
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
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the retrievedManualLookupPerson
     */
    public Person getRetrievedManualLookupPerson() {
        return retrievedManualLookupPerson;
    }

    /**
     * @return the recipientPersonID
     */
    public int getRecipientPersonID() {
        return recipientPersonID;
    }

    /**
     * @param retrievedManualLookupPerson the retrievedManualLookupPerson to set
     */
    public void setRetrievedManualLookupPerson(Person retrievedManualLookupPerson) {
        this.retrievedManualLookupPerson = retrievedManualLookupPerson;
    }

    /**
     * @param recipientPersonID the recipientPersonID to set
     */
    public void setRecipientPersonID(int recipientPersonID) {
        this.recipientPersonID = recipientPersonID;
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

    /**
     * @return the showTextBlocksAllMuni
     */
    public boolean isShowTextBlocksAllMuni() {
        return showTextBlocksAllMuni;
    }

    /**
     * @param showTextBlocksAllMuni the showTextBlocksAllMuni to set
     */
    public void setShowTextBlocksAllMuni(boolean showTextBlocksAllMuni) {
        this.showTextBlocksAllMuni = showTextBlocksAllMuni;
    }

    /**
     * @return the noticePerson
     */
    public Person getNoticePerson() {
        return noticePerson;
    }

    /**
     * @param noticePerson the noticePerson to set
     */
    public void setNoticePerson(Person noticePerson) {
        this.noticePerson = noticePerson;
    }
    
}
