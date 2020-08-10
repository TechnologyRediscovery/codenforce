/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PropertyUnitsBB
        extends BackingBeanUtils {

    private PropertyDataHeavy currProp;
    private PropertyUnit currPropUnit;
    private ArrayList<PropertyUnit> unitDisplayList;
    private ArrayList<PropertyUnitDataHeavy> heavyDisplayList;

    private PropertyUnitDataHeavy currPropUnitWithLists;

    private List<ViewOptionsActiveListsEnum> allViewOptions;
    private ViewOptionsActiveListsEnum currentViewOption;

    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyUnitsBB() {
    }

    @PostConstruct
    public void initBean() {
        currProp = getSessionBean().getSessProperty();

        allViewOptions = Arrays.asList(ViewOptionsActiveListsEnum.values());

        if (currentViewOption == null) {

            setCurrentViewOption(ViewOptionsActiveListsEnum.VIEW_ALL);

        }

    }

    public String goToChanges() {

        return "unitchanges";
    }

    public String goToUnitList() {
        return "propertyUnits";
    }

    public String manageOccPeriod(OccPeriod op) {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        try {
            getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, getSessionBean().getSessUser().getMyCredential()));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not load occupancy period with data" + ex.getMessage(), ""));

        }
        return "occPeriodWorkflow";

    }

    /**
     * Logic container for steps needed to be taken before a unit list is edited
     *
     * @param ev
     */
    public void beginPropertyUnitUpdates(ActionEvent ev) {
    }

    /**
     * Adds a blank unit to propUnitsToAdd list. This newly-created unit can
     * then be selected and edited by the user.
     */
    public void addUnitToNewPropUnits() {
        PropertyUnit unitToAdd;
        PropertyCoordinator pc = getPropertyCoordinator();
        unitToAdd = pc.initPropertyUnit(currProp);
        unitDisplayList.add(unitToAdd);

//        clearAddUnitFormValues();
    }

    public void removePropertyUnitFromEditTable(PropertyUnit pu) {
        getCurrProp().getUnitList().remove(pu);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Zap!", ""));

    }

    /**
     * Finalizes the unit list the user has created so that it can be compared
     * to the existing one in the database.
     *
     */
    public void finalizeUnitList() {
        PropertyCoordinator pc = getPropertyCoordinator();

        try {
            pc.applyUnitList(unitDisplayList, currProp);
        } catch(IntegrationException ex) {
            System.out.println("PropertyUnitsBB.finalizeUnitList() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "An error occurred while trying to save your changes to the database", "")); 
        }catch (BObStatusException ex){
           getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    ex.toString(), "")); 
        }

        refreshCurrPropWithLists();

        setCurrentViewOption(ViewOptionsActiveListsEnum.VIEW_ACTIVE);
    } // close method

    private void refreshCurrPropWithLists() {
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currProp = pc.getPropertyDataHeavy(currProp.getPropertyID(), getSessionBean().getSessUser());
            getSessionBean().setSessProperty(currProp);
        } catch (IntegrationException | BObStatusException | SearchException | AuthorizationException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update current property with lists | Exception details: " + ex.getMessage(), ""));
        }

    }

    /**
     * @return the currProp
     */
    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }

    /**
     * @param currProp the currProp to set
     */
    public void setCurrProp(PropertyDataHeavy currProp) {
        this.currProp = currProp;
    }

    /**
     * @return the currPropUnit
     */
    public PropertyUnit getCurrPropUnit() {
        return currPropUnit;
    }

    /**
     * @param currPropUnit the currPropUnit to set
     */
    public void setCurrPropUnit(PropertyUnit currPropUnit) {
        this.currPropUnit = currPropUnit;
    }

    /**
     * @return the currPropUnitWithLists
     */
    public PropertyUnitDataHeavy getCurrPropUnitWithLists() {
        return currPropUnitWithLists;
    }

    /**
     * @param currPropUnitWithLists the currPropUnitWithLists to set
     */
    public void setCurrPropUnitWithLists(PropertyUnitDataHeavy currPropUnitWithLists) {
        this.currPropUnitWithLists = currPropUnitWithLists;
    }

    public ArrayList<PropertyUnit> getUnitDisplayList() {
        return unitDisplayList;
    }

    public void setUnitDisplayList(ArrayList<PropertyUnit> unitDisplayList) {
        this.unitDisplayList = unitDisplayList;
    }

    public ViewOptionsActiveListsEnum getCurrentViewOption() {
        return currentViewOption;
    }

    public void setCurrentViewOption(ViewOptionsActiveListsEnum input) {

        currentViewOption = input;

        unitDisplayList = new ArrayList<>();

        heavyDisplayList = new ArrayList<>();

        if (null == currentViewOption) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to set the current view option. Returning to default.", ""));
            currentViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        } else {
            switch (currentViewOption) {
                case VIEW_ALL:
                    unitDisplayList.addAll(currProp.getUnitList());
                    heavyDisplayList.addAll(currProp.getUnitWithListsList());
                    break;

                case VIEW_ACTIVE:
                    for (PropertyUnit unit : currProp.getUnitList()) {
                        if (unit.isActive()) {
                            unitDisplayList.add(unit);
                        }
                    }

                    for (PropertyUnitDataHeavy unit : currProp.getUnitWithListsList()) {
                        if (unit.isActive()) {
                            heavyDisplayList.add(unit);
                        }
                    }

                    break;

                case VIEW_INACTIVE:
                    for (PropertyUnit unit : currProp.getUnitList()) {
                        if (!unit.isActive()) {
                            unitDisplayList.add(unit);
                        }
                    }

                    for (PropertyUnitDataHeavy unit : currProp.getUnitWithListsList()) {
                        if (unit.isActive()) {
                            heavyDisplayList.add(unit);
                        }
                    }

                    break;
            }

            Collections.sort(heavyDisplayList, new Comparator<PropertyUnitDataHeavy>() {
                @Override
                public int compare(PropertyUnitDataHeavy unit1, PropertyUnitDataHeavy unit2) {

                    return Boolean.compare(unit2.isActive(), unit1.isActive());
                }

            });
        }
        
        Collections.sort(unitDisplayList, new Comparator<PropertyUnit>() {
                @Override
                public int compare(PropertyUnit unit1, PropertyUnit unit2) {

                    return Boolean.compare(unit2.isActive(), unit1.isActive());
                }

            });

    }

public List<ViewOptionsActiveListsEnum> getAllViewOptions() {
        return allViewOptions;
    }

    public void setAllViewOptions(List<ViewOptionsActiveListsEnum> allViewOptions) {
        this.allViewOptions = allViewOptions;
    }

    public ArrayList<PropertyUnitDataHeavy> getHeavyDisplayList() {
        return heavyDisplayList;
    }

    public void setHeavyDisplayList(ArrayList<PropertyUnitDataHeavy> heavyDisplayList) {
        this.heavyDisplayList = heavyDisplayList;
    }

}
