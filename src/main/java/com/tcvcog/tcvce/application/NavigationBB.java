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

import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.NavigationItem;
import com.tcvcog.tcvce.entities.NavigationSubItem;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Eric C. Darsow
 */
public class NavigationBB extends BackingBeanUtils implements Serializable {

    private boolean noActiveUser;
    private boolean noActiveCase;
    private boolean noActiveProperty;
    private boolean noActiveInspection;
    private boolean noActiveSource;
    private boolean noActivePerson;

    /**
     * Creates a new instance of NavigationBB
     */
    public NavigationBB() {
    }

    //Xiaohong
    @PostConstruct
    public void initBean() {
        // Load Navigation lists from SystemCoordinator and place
        // in member variables here
        SystemCoordinator ssc = getSystemCoordinator();
        NavList = ssc.navList();
        sideBarNavList = ssc.sideBarNavList();
        currentPageNavItemValue = ssc.getCurrentPageNavItemValue();
        System.out.println("NavigationBB.initBean");

    }

    private List<NavigationItem> NavList;

    private List<NavigationItem> sideBarNavList;

    private String currentPageNavItemValue;

    public String getCurrentPageNavItemValue() {
        return currentPageNavItemValue;
    }

    public void setCurrentPageNavItemValue(String currentPageNavItemValue) {
        this.currentPageNavItemValue = currentPageNavItemValue;
    }

    public List<NavigationItem> getNavList() {
        return NavList;
    }

    public void setNavList(List<NavigationItem> NavList) {
        this.NavList = NavList;
    }

    public List<NavigationItem> getSideBarNavList() {
        return sideBarNavList;
    }

    public void setSideBarNavList(List<NavigationItem> sideBarNavList) {
        this.sideBarNavList = sideBarNavList;
    }

    //Eric
    public String gotoPropertyProfile() {
        if (getSessionBean().getSessionProperty() != null) {
            return "propertyProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No active property to profile! Please search for and "
                            + "select a property and re-attempt navigation",
                            ""));
            return "propertySearch";
        }
    }

    public String gotoCaseProfile() {
        if (getSessionBean().getSessionCECase() != null) {
            return "caseProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No active case! Please select a case from the list below and re-attempt navigation",
                            ""));
            return "ceCases";
        }

    }

    public String gotoPersonProfile() {
        if (getSessionBean().getSessionPerson() != null) {
            return "personProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No active case! Please select a case from the list below and re-attempt navigation",
                            ""));
            return "personSearch";
        }
    }

    /**
     * @return the noActiveCase
     */
    public boolean isNoActiveCase() {
        CECase c = getSessionBean().getSessionCECase();
        noActiveCase = (c == null);
        return noActiveCase;
    }

    /**
     * @return the noActiveProperty
     */
    public boolean isNoActiveProperty() {
        Property p = getSessionBean().getSessionProperty();
        noActiveProperty = (p == null);
        return noActiveProperty;
    }

    /**
     * @return the noActiveInspection
     */
    public boolean isNoActiveInspection() {
        return noActiveInspection;
    }

    /**
     * @param noActiveCase the noActiveCase to set
     */
    public void setNoActiveCase(boolean noActiveCase) {
        this.noActiveCase = noActiveCase;
    }

    /**
     * @param noActiveProperty the noActiveProperty to set
     */
    public void setNoActiveProperty(boolean noActiveProperty) {
        this.noActiveProperty = noActiveProperty;
    }

    /**
     * @param noActiveInspection the noActiveInspection to set
     */
    public void setNoActiveInspection(boolean noActiveInspection) {
        this.noActiveInspection = noActiveInspection;
    }

    /**
     * @return the noActiveSource
     */
    public boolean isNoActiveSource() {
        CodeSource cs = getSessionBean().getActiveCodeSource();
        noActiveSource = (cs == null);
        return noActiveSource;
    }

    /**
     * @param noActiveSource the noActiveSource to set
     */
    public void setNoActiveSource(boolean noActiveSource) {
        this.noActiveSource = noActiveSource;
    }

    /**
     * @return the noActivePerson
     */
    public boolean isNoActivePerson() {
        Person p = getSessionBean().getSessionPerson();
        noActivePerson = (p == null);
        return noActivePerson;
    }

    /**
     * @param noActivePerson the noActivePerson to set
     */
    public void setNoActivePerson(boolean noActivePerson) {
        this.noActivePerson = noActivePerson;
    }

    /**
     * @return the noActiveUser
     */
    public boolean isNoActiveUser() {
        noActiveUser = (getSessionBean().getSessionUser() == null);
        return noActiveUser;
    }

    /**
     * @param noActiveUser the noActiveUser to set
     */
    public void setNoActiveUser(boolean noActiveUser) {
        this.noActiveUser = noActiveUser;
    }

}
