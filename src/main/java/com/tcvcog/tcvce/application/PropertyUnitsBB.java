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
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();

        boolean missingUnitNum = false;

        boolean duplicateUnitNum = false;//a flag to see if there is more than 1 of  Unit Number.

        boolean badUnitNum = false; //an unusable unit number was entered

        int duplicateNums = 0;//The int to the left stores how many of a given number the loop below finds.

        for (PropertyUnit firstUnit : unitDisplayList) {
            duplicateNums = 0;

            // remove any use of the word "unit" in a unit identifier
            firstUnit.setUnitNumber(firstUnit.getUnitNumber().replaceAll("(?i)unit", ""));

            if (firstUnit.getUnitNumber().compareTo("") == 0) {
                missingUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }

            for (PropertyUnit secondUnit : unitDisplayList) {
                if (firstUnit.getUnitNumber().compareTo(secondUnit.getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                duplicateUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }

            if (firstUnit.getUnitNumber().compareTo("-1") == 0) {
                if (firstUnit.getNotes() == null || firstUnit.getNotes().compareTo("robot-generated unit representing the primary habitable dwelling on a property") != 0) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The unit number -1 is used for default property units. Please use another number or \'-[space]1\'.", ""));
                    badUnitNum = true;
                    break;
                }
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Please change the robot-generated unit to something more meaningful.", ""));
                badUnitNum = true;
                break;

            }

        }

        if (unitDisplayList.isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please add at least one unit.", ""));

        } else if (missingUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "All units must have a Unit Number", ""));

        } else if (duplicateUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Some Units have the same Number", ""));

        } else if (!badUnitNum) {

            getCurrProp().setUnitList(unitDisplayList);

            Iterator<PropertyUnit> iter = getCurrProp().getUnitList().iterator();

            while (iter.hasNext()) {
                PropertyUnit unit = iter.next();

                // decide if we're updating a unit or inserting it based on initial value
                // newly created units don't have an ID, just a default unit number
                unit.setPropertyID(getCurrProp().getPropertyID());

                if (unit.getUnitID() == 0) {
                    try {
                        pi.insertPropertyUnit(unit);

                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! Inserted property unit: " + unit.getUnitNumber(), ""));
                    } catch (IntegrationException ex) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not insert unit with number: " + unit.getUnitNumber(), ""));
                    }
                } else {
                    try {
                        pi.updatePropertyUnit(unit);
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! Updated property unit: " + unit.getUnitNumber(), ""));
                    } catch (IntegrationException ex) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not update unit with number: " + unit.getUnitNumber(), ""));
                    }
                }
            }
        }

        // mark parent property as updated now
        try {
            pc.editProperty(currProp, getSessionBean().getSessUser());
            currProp = pc.assemblePropertyDataHeavy(currProp, getSessionBean().getSessUser());
        } catch (BObStatusException | SearchException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update associated property: ", ""));
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
