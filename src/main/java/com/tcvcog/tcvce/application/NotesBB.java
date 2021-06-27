package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.IFace_NoteHolder;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.MessageBuilderParams;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import java.io.Serializable;

/**
 * The premier backing bean for universal notes panel workflow.
 *
 * @author jurplel
 */
public class NotesBB extends BackingBeanUtils implements Serializable {

    private String formNoteText;

    private EventDomainEnum pageEventDomain;

    private IFace_NoteHolder currentNoteHolder;

    public NotesBB() {}

    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        EventCoordinator ec = getEventCoordinator();

        // Find event holder and setup event list
        updateNoteHolder();
    }

    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentEventHolder object with something we can grab events from!
     */
    public void updateNoteHolder() {
        SessionBean sb = getSessionBean();

        pageEventDomain = sb.getSessEventsPageEventDomainRequest();
        switch (pageEventDomain) {
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
     * This method goes down the coordinator stack to add a cached note
     * (formNoteText) to the end of the occperiod's note field in the database
     * in a structured and consistent way, while also requiring different permissions.
     *
     */
    public void appendNotes() {
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentNoteHolder.getNotes());
        mbp.setNewMessageContent(formNoteText);
        mbp.setHeader("Occupancy Period Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {
            switch (pageEventDomain) {
                case CODE_ENFORCEMENT:
                    CaseCoordinator cc = getCaseCoordinator();
                    CECase ceCase = (CECase) currentNoteHolder;
                    cc.cecase_updateCECaseNotes(mbp, ceCase);
                    break;
                case OCCUPANCY:
                    OccupancyCoordinator oc = getOccupancyCoordinator();
                    OccPeriod occPeriod = (OccPeriod) currentNoteHolder;
                    oc.attachNoteToOccPeriod(mbp, occPeriod);
                    break;
                case UNIVERSAL:
                    System.out.println("NotesBB reached universal case in appendNotes()--do something about this maybe?");
                    return;
            }
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

    public IFace_NoteHolder getCurrentNoteHolder() {
        return currentNoteHolder;
    }

    public void setCurrentNoteHolder(IFace_NoteHolder currentNoteHolder) {
        this.currentNoteHolder = currentNoteHolder;
    }

}
