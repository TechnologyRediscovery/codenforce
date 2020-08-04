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
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.NavigationItem;
import com.tcvcog.tcvce.entities.NavigationSubItem;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Xiaohong Chen
 * 
 */
public class NavigationBB extends BackingBeanUtils implements Serializable {

    private boolean noActiveUser;
    private boolean noActiveCase;
    private boolean noActiveProperty;
    private boolean noActiveInspection;
    private boolean noActiveSource;
    private boolean noActivePerson;

    private List<NavigationItem> NavList;

    private List<NavigationItem> sideBarNavList;
    
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

        System.out.println("NavigationBB.initBean");

    }

    public String getCurrentDashBoardInfo() {
        SessionBean s = getSessionBean();
        try {
            String info = s.getSessMuni().getMuniName();
            return "Current Municipality: " + info;
        } catch (Exception ex) {
            return "Current Municipality: ";
        }

    }

    public String getCurrentPropertyInfo() {
        SessionBean s = getSessionBean();
        try {
            String propertyAddress = s.getSessProperty().getAddress();
            String propertyId = String.valueOf(s.getSessProperty().getPropertyID());
            return "Current Property: " + propertyAddress + " | ID: " + propertyId;
        } catch (Exception ex) {
            return "Current Property: " + " | ID: ";
        }

    }

    public String getCurrentCEInfo() {
        SessionBean s = getSessionBean();
        try {
            String caseName = s.getSessCECase().getCaseName();
            String caseId = String.valueOf(s.getSessCECase().getCaseID());
            return "Current Case: " + caseName + " | ID: " + caseId;
        } catch (Exception ex) {
            return "Current Case: " + " | ID: ";
        }

    }

    public String getCurrentPersonInfo() {
        SessionBean sb = getSessionBean();
        try {
            String personName = sb.getSessPerson().getFirstName() 
            + " " + sb.getSessPerson().getLastName(); 
            
            String personId = String.valueOf(sb.getSessPerson().getPersonID());
            return "Current Person: " + personName + " | ID: " + personId;
        } catch (Exception ex) {
            return "Current Person: " + " | ID: ";
        }

    }

    public String getCurrentPeriodInfo() {
        SessionBean s = getSessionBean();
        try {
            String periodId = String.valueOf(s.getSessOccPeriod().getPeriodID());
            String periodType = s.getSessOccPeriod().getType().getTitle();
            return "Current Period: " + periodType + " | ID: " + periodId;
        } catch (Exception ex) {
            return "Current Period: " + " | ID: ";
        }

    }

    public Map<String, String> navCurrentInfoMap() {
        HashMap<String, String> navCurrentInfoMap;
        navCurrentInfoMap = new HashMap<>();
        navCurrentInfoMap.put("Dashboard", getCurrentDashBoardInfo());
        navCurrentInfoMap.put("Property", getCurrentPropertyInfo());
        navCurrentInfoMap.put("Code Enf", getCurrentCEInfo());
        navCurrentInfoMap.put("Occupancy", getCurrentPersonInfo());
        navCurrentInfoMap.put("Person", getCurrentPeriodInfo());
        navCurrentInfoMap.put("Code", "Current Code: ");
        return navCurrentInfoMap;
    }

    public Map<String, String> navCategoryMap() {

        HashMap<String, String> categoryMap;
        categoryMap = new HashMap<>();

        if(NavList != null && !NavList.isEmpty()){

            for (int i = 0; i < NavList.size(); i++) {
                NavigationItem navitem = (NavigationItem) NavList.get(i);
                List subnavList = navitem.getSubNavitem();
                String categoryName = navitem.getValue();
                for (int m = 0; m < subnavList.size(); m++) {
                    NavigationSubItem subnavitem = (NavigationSubItem) subnavList.get(m);
                    String pagePath = subnavitem.getPagePath();
                    categoryMap.put(pagePath, categoryName);
                }
            }
        }
        return categoryMap;
    }

    public String getCurrentPageNavItemValue() {
        String currentViewPagePath = getviewPagePath();
        return navCategoryMap().get(currentViewPagePath);
    }

    public String getviewPagePath() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String viewID = fc.getViewRoot().getViewId();
        return viewID;
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
        if (getSessionBean().getSessProperty() != null) {
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
        if (getSessionBean().getSessCECase() != null) {
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
        if (getSessionBean().getSessPerson()!= null) {
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
        CECaseDataHeavy c = getSessionBean().getSessCECase();
        noActiveCase = (c == null);
        return noActiveCase;
    }

    /**
     * @return the noActiveProperty
     */
    public boolean isNoActiveProperty() {
        Property p = getSessionBean().getSessProperty();
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
        CodeSource cs = getSessionBean().getSessCodeSource();
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
        Person p = getSessionBean().getSessPerson();
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
        noActiveUser = (getSessionBean().getSessUser() == null);
        return noActiveUser;
    }

    /**
     * @param noActiveUser the noActiveUser to set
     */
    public void setNoActiveUser(boolean noActiveUser) {
        this.noActiveUser = noActiveUser;
    }

}
