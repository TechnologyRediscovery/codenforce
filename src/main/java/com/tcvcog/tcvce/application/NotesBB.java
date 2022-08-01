/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.session.SessionBean;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.IFace_noteHolder;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.MessageBuilderParams;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The premier backing bean for universal notes panel workflow.
 *
 * @author jurplel
 */
public class NotesBB extends BackingBeanUtils implements Serializable {

    private String formNoteText;

    private DomainEnum pageDomain;

    private IFace_noteHolder currentNoteHolder;

    public NotesBB() {}

    @PostConstruct
    public void initBean() {
        // Find event holder and setup event list
        updateNoteHolder();
    }

    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentNoteHolder object with something we can grab events from!
     */
    public void updateNoteHolder() {
        SessionBean sb = getSessionBean();
        pageDomain = getSessionEventConductor().getSessEventsPageEventDomainRequest();
        switch (pageDomain) {
            case CODE_ENFORCEMENT:
                currentNoteHolder = sb.getSessCECase();
                break;
            case OCCUPANCY:
                currentNoteHolder = sb.getSessOccPeriod();
                break;
            case UNIVERSAL:
                System.out.println("NotesBB reached universal case in updateNoteHolder()--do something about this maybe?");
                break;
        }
    }

    /**
     * This method clears the cache of the last written note.
     *
     */
    public void clearFormNoteText() {
        formNoteText = new String();
    }

    /**
     * Asks the session if notes need to be reloaded
     * @return 
     */
    public String getManagedNoteContent(){
        LocalDateTime trigger = getSessionBean().getNoteholderRefreshTimestampTrigger();
        if(trigger != null){
            updateNoteHolder();
            getSessionBean().setNoteholderRefreshTimestampTrigger(null);
        }
        return currentNoteHolder.getNotes();
    }
    
    
    /**
     * This method goes down the coordinator stack to add a cached note
     * (formNoteText) to the end of a note field in the database in a structured
     * and consistent way, while also requiring different permissions.
     *
     */
    public void appendNotes() {
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentNoteHolder.getNotes());
        mbp.setNewMessageContent(formNoteText);
        mbp.setUser(getSessionBean().getSessUser());

        try {
            switch (pageDomain) {
                case CODE_ENFORCEMENT:
                    mbp.setHeader("Occupancy Period Note");
                    CaseCoordinator cc = getCaseCoordinator();
                    CECase ceCase = (CECase) currentNoteHolder;
                    cc.cecase_updateCECaseNotes(mbp, ceCase);
                    break;
                case OCCUPANCY:
                    mbp.setHeader("CE Case Note");
                    OccupancyCoordinator oc = getOccupancyCoordinator();
                    OccPeriod occPeriod = (OccPeriod) currentNoteHolder;
                    oc.attachNoteToOccPeriod(mbp, occPeriod);
                    break;
                case UNIVERSAL:
                    System.out.println("NotesBB reached universal case in appendNotes()--do something about this maybe?");
                    return;
            }
            updateNoteHolder();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));

        }
    }

    public String getFormNoteText() {
        return formNoteText;
    }

    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    public IFace_noteHolder getCurrentNoteHolder() {
        return currentNoteHolder;
    }

    public void setCurrentNoteHolder(IFace_noteHolder currentNoteHolder) {
        this.currentNoteHolder = currentNoteHolder;
    }

    
}
