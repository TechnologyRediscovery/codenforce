/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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

import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChangeOrder;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Nathan Dietz
 */
public class PropertyUnitChangesBB
        extends BackingBeanUtils {

    private PropertyDataHeavy currProp;
    private PropertyUnit currPropUnit;
    private PropertyUnitChangeOrder currChangeOrder;

    private List<PropertyUnitDataHeavy> heavyDisplayList;

    private List<ViewOptionsActiveListsEnum> allViewOptions;
    private ViewOptionsActiveListsEnum currentViewOption;

    public PropertyUnitChangesBB() {
    }

    @PostConstruct
    public void initBean() {
        currProp = getSessionBean().getSessProperty();

        allViewOptions = Arrays.asList(ViewOptionsActiveListsEnum.values());

        if (currentViewOption == null) {

            setCurrentViewOption(ViewOptionsActiveListsEnum.VIEW_ACTIVE);
        }
    }

    public void refreshCurrentObjects() {
        PropertyCoordinator pc = getPropertyCoordinator();

        //let's grab the latest copy of the prop from the database and set it on the session bean
        try {

            Property dbProp = pc.getProperty(currProp.getPropertyID());

            currProp = pc.assemblePropertyDataHeavy(dbProp, getSessionBean().getSessUser());

            getSessionBean().setSessProperty(currProp);
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
            currProp = getSessionBean().getSessProperty();
        } catch (IntegrationException | SearchException ex) {
            System.out.println("PropertyUnitChangesBB.refreshCurrentObjects() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to load information from the database.", ""));
            currProp = getSessionBean().getSessProperty();
        }

        currPropUnit = new PropertyUnit();

        currChangeOrder = new PropertyUnitChangeOrder();

    }

    public String approvedByWho(PropertyUnitChangeOrder change) {

        if (change.getApprovedBy() != null) {
            return "Approved by: " + change.getApprovedBy().getPerson().getFirstName()
                    + " "
                    + change.getApprovedBy().getPerson().getLastName()
                    + " (ID# "
                    + change.getApprovedBy().getPersonID()
                    + ")";
        } else if(change.isActive()) {
            return "No action taken yet";
        } else {
            return "Rejected";
        }

    }
    
    /**
     * were we redirected to this page from another?
     *
     * @return
     */
    public boolean wasRedirected() {
        return getSessionBean().getNavStack().peekLastPage() != null;
    }

    public String goBack() {
        try {
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("PropertyUnitChangesBB.goBack() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to redirect you back to the previous page!", ""));
                    context.getExternalContext().getFlash().setKeepMessages(true);
            return "missionControl";
        }
    }

    public void initializeChangeComparison(PropertyUnit unit, PropertyUnitChangeOrder change) {
        currPropUnit = unit;
        currChangeOrder = change;
    }

    public void rejectChangeOrder() {
        PropertyIntegrator pi = getPropertyIntegrator();

        currChangeOrder.setActive(false);

        try {
            pi.updatePropertyUnitChange(currChangeOrder);
        } catch (IntegrationException ex) {
            System.out.println("PropertyUnitChangesBB.rejectChangeOrder() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the database.", ""));
        }

        setCurrentViewOption(currentViewOption);
    }

    public void applyChangeOrder() {
        PropertyCoordinator pc = getPropertyCoordinator();
        
        try {
            pc.implementPropertyUnitChangeOrder(currChangeOrder);
        } catch (IntegrationException ex) {
            System.out.println("PropertyUnitChangesBB.applyChangeOrder() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the database.", ""));
        }

        setCurrentViewOption(currentViewOption);
    }
    
    public String goToPropertyUnits() {
        return "propertyUnits";
    }

    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }

    public void setCurrProp(PropertyDataHeavy currProp) {
        this.currProp = currProp;
    }

    public PropertyUnit getCurrPropUnit() {
        return currPropUnit;
    }

    public void setCurrPropUnit(PropertyUnit currPropUnit) {
        this.currPropUnit = currPropUnit;
    }

    public List<PropertyUnitDataHeavy> getHeavyDisplayList() {
        return heavyDisplayList;
    }

    public void setHeavyDisplayList(List<PropertyUnitDataHeavy> heavyDisplayList) {
        this.heavyDisplayList = heavyDisplayList;
    }

    public List<ViewOptionsActiveListsEnum> getAllViewOptions() {
        return allViewOptions;
    }

    public void setAllViewOptions(List<ViewOptionsActiveListsEnum> allViewOptions) {
        this.allViewOptions = allViewOptions;
    }

    public ViewOptionsActiveListsEnum getCurrentViewOption() {
        return currentViewOption;
    }

    public void setCurrentViewOption(ViewOptionsActiveListsEnum input) {

        refreshCurrentObjects();

        currentViewOption = input;

        heavyDisplayList = new ArrayList<>();

        if (currentViewOption == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to set the current view option. Returning to default.", ""));
            currentViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        } else {

            switch (currentViewOption) {
                case VIEW_ALL:

                    for (PropertyUnitDataHeavy unit : currProp.getUnitWithListsList()) {

                        if (!unit.getChangeOrderList().isEmpty()) {
                            heavyDisplayList.add(unit);
                        }
                    }

                    break;

                case VIEW_ACTIVE:

                    for (PropertyUnitDataHeavy unit : currProp.getUnitWithListsList()) {

                        List<PropertyUnitChangeOrder> activeChanges = new ArrayList<>();

                        for (PropertyUnitChangeOrder change : unit.getChangeOrderList()) {
                            if (change.isActive()) {
                                activeChanges.add(change);
                            }
                        }

                        if (!activeChanges.isEmpty()) {
                            unit.setChangeOrderList(activeChanges);
                            heavyDisplayList.add(unit);
                        }
                    }

                    break;

                case VIEW_INACTIVE:

                    for (PropertyUnitDataHeavy unit : currProp.getUnitWithListsList()) {

                        List<PropertyUnitChangeOrder> inactiveChanges = new ArrayList<>();

                        for (PropertyUnitChangeOrder change : unit.getChangeOrderList()) {
                            if (!change.isActive()) {
                                inactiveChanges.add(change);
                            }
                        }

                        if (!inactiveChanges.isEmpty()) {
                            unit.setChangeOrderList(inactiveChanges);
                            heavyDisplayList.add(unit);
                        }
                    }

                    break;
            }

        }

    }

    public PropertyUnitChangeOrder getCurrChangeOrder() {
        return currChangeOrder;
    }

    public void setCurrChangeOrder(PropertyUnitChangeOrder currChangeOrder) {
        this.currChangeOrder = currChangeOrder;
    }

}
