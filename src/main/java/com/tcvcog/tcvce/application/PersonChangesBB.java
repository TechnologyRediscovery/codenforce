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

import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonChangeOrder;
import com.tcvcog.tcvce.entities.PersonWithChanges;
import com.tcvcog.tcvce.integration.PersonIntegrator;
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
public class PersonChangesBB
        extends BackingBeanUtils {

    private List<PersonWithChanges> currPersonList;
    private PersonWithChanges currPerson;
    private PersonChangeOrder currChangeOrder;

    private List<PersonWithChanges> displayList;

    private List<ViewOptionsActiveListsEnum> allViewOptions;
    private ViewOptionsActiveListsEnum currentViewOption;

    public PersonChangesBB() {
    }

    @PostConstruct
    public void initBean() {

        allViewOptions = Arrays.asList(ViewOptionsActiveListsEnum.values());

        if (currentViewOption == null) {

            setCurrentViewOption(ViewOptionsActiveListsEnum.VIEW_ACTIVE);
        }
    }

    public void refreshCurrentObjects() {

        PersonCoordinator pc = getPersonCoordinator();

        //let's grab the latest copy of the prop from the database and set it on the session bean
        try {

            if (getSessionBean().getSessPersonList() != null) {

                currPersonList = pc.getPersonWithChangesList(getSessionBean().getSessPersonList());

            } else {

                currPersonList = new ArrayList<>();

                currPersonList.add(pc.getPersonWithChanges(getSessionBean().getSessPerson().getPersonID()));
            }

        } catch (IntegrationException ex) {
            System.out.println("PersonChangesBB.refreshCurrentObjects() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to load information from the database.", ""));
        }

        currPerson = new PersonWithChanges();

        currChangeOrder = new PersonChangeOrder();

    }

    /**
     * Just a little method used by the UI to display whether a public user
     * wanted to change a boolean value, and if so what they want to change it
     * to.
     *
     * @param input
     * @return
     */
    public String checkForChangeBoolean(String input) {

        if (input == null) {
            return "No change";
        } else if (Boolean.valueOf(input)) {
            return "Yes";
        } else {
            return "No";
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
            System.out.println("PersonChangesBB.goBack() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "An error occurred while trying to redirect you back to the previous page!", ""));
                    context.getExternalContext().getFlash().setKeepMessages(true);
            return "missionControl";
        }
    }

    public String goToPerson(PersonWithChanges person) {
        PersonCoordinator pc = getPersonCoordinator();

        getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(person, getSessionBean().getSessUser().getMyCredential()));

        getSessionBean().getNavStack().pushCurrentPage();

        return "personInfo";

    }

    public void initializeChangeComparison(PersonWithChanges person, PersonChangeOrder change) {
        currPerson = person;
        currChangeOrder = change;
    }

    public String approvedByWho(PersonChangeOrder change) {

        if (change.getApprovedBy() != null) {
            return "Approved by: " + change.getApprovedBy().getPerson().getFirstName()
                    + " "
                    + change.getApprovedBy().getPerson().getLastName()
                    + " (ID# "
                    + change.getApprovedBy().getPersonID()
                    + ")";
        } else if (change.isActive()) {
            return "No action taken yet";
        } else {
            return "Rejected";
        }

    }

    public void rejectChangeOrder() {
        PersonIntegrator pi = getPersonIntegrator();

        currChangeOrder.setActive(false);

        try {
            pi.updatePersonChangeOrder(currChangeOrder);
        } catch (IntegrationException ex) {
            System.out.println("PersonChangesBB.rejectChangeOrder() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the database.", ""));
        }

        setCurrentViewOption(currentViewOption);

    }

    public void applyChangeOrder() {
        PersonCoordinator pc = getPersonCoordinator();

        try {
            pc.implementPersonChangeOrder(currChangeOrder);
        } catch (IntegrationException ex) {
            System.out.println("PersonChangesBB.applyChangeOrder() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the database.", ""));
        }

        setCurrentViewOption(currentViewOption);

    }

    public List<PersonWithChanges> getCurrPersonList() {
        return currPersonList;
    }

    public void setCurrPersonList(List<PersonWithChanges> currPersonList) {
        this.currPersonList = currPersonList;
    }

    public PersonWithChanges getCurrPerson() {
        return currPerson;
    }

    public void setCurrPerson(PersonWithChanges currPerson) {
        this.currPerson = currPerson;
    }

    public PersonChangeOrder getCurrChangeOrder() {
        return currChangeOrder;
    }

    public void setCurrChangeOrder(PersonChangeOrder currChangeOrder) {
        this.currChangeOrder = currChangeOrder;
    }

    public List<PersonWithChanges> getDisplayList() {
        return displayList;
    }

    public void setDisplayList(List<PersonWithChanges> displayList) {
        this.displayList = displayList;
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
        currentViewOption = input;

        refreshCurrentObjects();

        currentViewOption = input;

        displayList = new ArrayList<>();

        if (currentViewOption == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to set the current view option. Returning to default.", ""));
            currentViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        } else {

            switch (currentViewOption) {
                case VIEW_ALL:

                    for (PersonWithChanges person : currPersonList) {

                        if (!person.getChangeOrderList().isEmpty()) {
                            displayList.add(person);
                        }
                    }

                    break;

                case VIEW_ACTIVE:

                    for (PersonWithChanges person : currPersonList) {

                        List<PersonChangeOrder> activeChanges = new ArrayList<>();

                        for (PersonChangeOrder change : person.getChangeOrderList()) {
                            if (change.isActive()) {
                                activeChanges.add(change);
                            }
                        }

                        if (!activeChanges.isEmpty()) {
                            person.setChangeOrderList(activeChanges);
                            displayList.add(person);
                        }
                    }

                    break;

                case VIEW_INACTIVE:

                    for (PersonWithChanges person : currPersonList) {

                        List<PersonChangeOrder> inactiveChanges = new ArrayList<>();

                        for (PersonChangeOrder change : person.getChangeOrderList()) {
                            if (!change.isActive()) {
                                inactiveChanges.add(change);
                            }
                        }

                        if (!inactiveChanges.isEmpty()) {
                            person.setChangeOrderList(inactiveChanges);
                            displayList.add(person);
                        }
                    }

                    break;
            }

        }
    }

}
