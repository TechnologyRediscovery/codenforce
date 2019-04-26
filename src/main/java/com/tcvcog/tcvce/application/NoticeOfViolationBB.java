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

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolationBB extends BackingBeanUtils implements Serializable {
    
    private CECase currentCase;

    private NoticeOfViolation currentNotice;
    private List<CodeViolation> activeVList;
    
    private List<TextBlock> blockListByMuni;
    
    private List<Person> personCandidateList;
    
    private List<TextBlock> blockListBeforeViolations;
    private List<TextBlock> blockListAfterViolations;
    
    
    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {
        
    }
    
    @PostConstruct
    public void initBean(){
        PersonIntegrator pi = getPersonIntegrator();
        currentNotice = getSessionBean().getActiveNotice();
        currentCase = getSessionBean().getcECaseQueue().get(0);
        blockListBeforeViolations = new ArrayList<>();
        blockListAfterViolations = new ArrayList<>();
        try {
            personCandidateList = pi.getPersonList(currentCase.getProperty());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }
    
    public void addBlockBeforeViolations(TextBlock tb){
        blockListByMuni.remove(tb);
        blockListBeforeViolations.add(tb);
    }
    
    public void removeBlockBeforeViolations(TextBlock tb){
        blockListBeforeViolations.remove(tb);
        blockListByMuni.add(tb);
        
    }
    
    public void removeBlockAfterViolations(TextBlock tb){
        blockListAfterViolations.remove(tb);
        blockListByMuni.add(tb);
        
    }
    
    
    public void addBlockAfterViolations(TextBlock tb){
        blockListByMuni.remove(tb);
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
    
    
    public String assembleNotice(){
        CaseCoordinator cc = getCaseCoordinator();
        
        StringBuilder sb = new StringBuilder();
        Iterator<TextBlock> it = blockListBeforeViolations.iterator();
        while(it.hasNext()){
            appendTextBlockAsPara(it.next(), sb);
        }
        currentNotice.setNoticeTextBeforeViolations(sb.toString());
        
        it = blockListAfterViolations.iterator();
        while(it.hasNext()){
            appendTextBlockAsPara(it.next(), sb);
        }
        
        currentNotice.setNoticeTextAfterViolations(sb.toString());
        
        getSessionBean().setActiveNotice(currentNotice);
        try {
            cc.novInsertNotice(currentNotice, currentCase, getSessionBean().getFacesUser());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            System.out.println(ex);
        }
        return "noticeOfViolationEditor";
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
    
}
