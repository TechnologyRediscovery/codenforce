/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class CECaseNoticesBB 
        extends     BackingBeanUtils
        implements  Serializable {

    private CECaseDataHeavy currentCase;
    
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessionCECase();
       
    }

    /**
     * Creates a new instance of CECaseNotices
     */
    public CECaseNoticesBB() {
    }
    
    
    public String createNewNotice() throws SQLException {
        NoticeOfViolation nov;
        PropertyCoordinator pc = getPropertyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
            if (!currentCase.getViolationListUnresolved().isEmpty()) {
                try {
                    getSessionBean().getSessionPropertyList().add(0, currentCase.getProperty());
                    getSessionBean().setSessionProperty(pc.assemblePropertyDataHeavy(currentCase.getProperty(), getSessionBean().getSessionUser().getMyCredential()));
//                    positionCurrentCaseAtHeadOfQueue();
                    nov = cc.novGetNewNOVSkeleton(currentCase, getSessionBean().getSessionMuni());
                    nov.setCreationBy(getSessionBean().getSessionUser());
                    getSessionBean().setSessionNotice(nov);
                    return "noticeOfViolationBuilder";
                } catch (AuthorizationException | IntegrationException | BObStatusException | SearchException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot build new notice", ""));
                }
            } else {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No unresolved violations exist for building a letter", ""));
            }
        return "";
    }

    public void resetNotice(NoticeOfViolation nov) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.novResetMailing(nov, getSessionBean().getSessionUser());
//            refreshCurrentCase();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice mailing status has been reset", ""));
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }

    

    public String printNotice(NoticeOfViolation nov) {
        getSessionBean().setSessionNotice(nov);
//        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationPrint";
    }

    public String editNoticeOfViolation(NoticeOfViolation nov) {
        getSessionBean().setSessionNotice(nov);
//        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationEditor";
    }

    public void lockNoticeAndQueueForMailing(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        
        try {
            caseCoord.novLockAndQueue(currentCase, nov, getSessionBean().getSessionUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "The automatic event generation associated with this action has thrown an error. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));

        } catch (ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Unable to queue notice of violatio. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));
        }
    }

    public void deleteSelectedEvent() {

    }

    public void deleteNoticeOfViolation(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.novDelete(nov);
            currentCase = caseCoord.assembleCECaseDataHeavy(currentCase, getSessionBean().getSessionUser().getMyCredential());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + nov.getNoticeID() + " has been nuked forever", ""));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete this notice of violation, "
                            + "probably because it has been sent already", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));

        }
    }

    public void markNoticeOfViolationAsSent(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.novMarkAsSent(currentCase, nov, getSessionBean().getSessionUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Marked notice as sent and added event to case",
                            ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),""));
        }catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to generate case event to log phase change",
                            "Note that because this message is being displayed, the phase change"
                            + "has probably succeeded"));
        }
    }

    public void markNoticeOfViolationAsReturned(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.novMarkAsReturned(currentCase, nov, getSessionBean().getSessionUser());
            currentCase = caseCoord.assembleCECaseDataHeavy(currentCase, getSessionBean().getSessionUser().getMyCredential());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + nov.getNoticeID()
                            + " has been marked as returned on today's date", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
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
    
}
