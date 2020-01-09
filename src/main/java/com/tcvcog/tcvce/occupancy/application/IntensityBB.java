/*
 * Copyright (C) 2019 Nathan Dietz
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.IntensitySchema;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Nathan Dietz
 */
public class IntensityBB extends BackingBeanUtils implements Serializable {

    private IntensityClass workingIntensityClass;
    private IntensityClass selectedIntensityClass;
    private ArrayList<IntensityClass> existingIntensityList;
    private IntensitySchema selectedSchema;
    private IntensitySchema workingSchema;
    private ArrayList<IntensitySchema> schemaList;
    private ArrayList<Icon> iconList;

    private boolean editing;

    public IntensityBB() {

    }

    @PostConstruct
    public void initBean() {

        if (workingIntensityClass == null) {

            workingIntensityClass = new IntensityClass();

            workingSchema = new IntensitySchema();

        }

        SystemIntegrator si = getSystemIntegrator();
        try {
            iconList = (ArrayList<Icon>) si.getIconList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

    }

    public void editIntensity(ActionEvent e) {
        if (selectedIntensityClass != null) {
            workingIntensityClass.setClassID(selectedIntensityClass.getClassID());
            workingIntensityClass.setTitle(selectedIntensityClass.getTitle());
            workingIntensityClass.setMuni(selectedIntensityClass.getMuni());
            workingIntensityClass.setNumericRating(selectedIntensityClass.getNumericRating());
            workingIntensityClass.setSchema(selectedIntensityClass.getSchema());
            workingIntensityClass.setActive(selectedIntensityClass.isActive());
            workingIntensityClass.setIcon(selectedIntensityClass.getIcon());

            editing = true;
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an Intensity Class to edit", ""));
        }
    }

    public void commitEdits() {

        SystemIntegrator si = new SystemIntegrator();

        try {
            si.updateIntensityClass(workingIntensityClass);

            workingIntensityClass = new IntensityClass();

            queryIntensityClasses();

            editing = false;
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update Intensity Classes.",
                            "This must be corrected by the System Administrator"));
        }

    }

    public void cancelEdits() {

        workingIntensityClass = new IntensityClass();

        queryIntensityClasses();

        editing = false;

    }

    public void createIntensityClass() {

        SystemIntegrator si = new SystemIntegrator();

        if (selectedSchema != null) {

            workingIntensityClass.setSchema(selectedSchema);

            try {
                si.insertIntensityClass(workingIntensityClass);

                workingIntensityClass = new IntensityClass();

                queryIntensityClasses();

            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to insert Intensity Class.",
                                "This must be corrected by the System Administrator"));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a schema before creating a class.",
                            ""));
        }

    }

    public void queryIntensityClasses() {

        System.out.println("Intensity Classes Queried!");

        if (selectedSchema != null) {

            SystemIntegrator si = new SystemIntegrator();

            try {
                existingIntensityList = (ArrayList<IntensityClass>) si.getIntensityClassList(selectedSchema);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to get Intensity Classes.",
                                "This must be corrected by the System Administrator"));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an Intensity Schema", ""));
        }

    }

    public void addIntensitySchema() {

        if (workingSchema.getLabel() != null
                && !workingSchema.getLabel().isEmpty()
                && workingSchema.getLabel().matches(".*\\w.*")) {

            schemaList.add(workingSchema);

            workingSchema = new IntensitySchema();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Schema added to list!",
                            ""));

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter in a label for your new schema.",
                            ""));
        }

        System.out.println("IntensityBB.addIntensitySchema | Completed method ");
    }

    public IntensityClass getWorkingIntensityClass() {
        return workingIntensityClass;
    }

    public void setWorkingIntensityClass(IntensityClass workingIntensityClass) {
        this.workingIntensityClass = workingIntensityClass;
    }

    public ArrayList<IntensityClass> getExistingIntensityList() {
        return existingIntensityList;
    }

    public void setExistingIntensityList(ArrayList<IntensityClass> existingIntensityList) {
        this.existingIntensityList = existingIntensityList;
    }

    public IntensitySchema getSelectedSchema() {
        return selectedSchema;
    }

    public void setSelectedSchema(IntensitySchema selectedSchema) {
        this.selectedSchema = selectedSchema;
    }

    public ArrayList<IntensitySchema> getSchemaList() {

        if (schemaList != null) {
            return schemaList;
        } else {

            SystemIntegrator si = new SystemIntegrator();

            try {
                schemaList = (ArrayList<IntensitySchema>) si.getIntensitySchemaList();
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to load schema list",
                                "This must be corrected by the system administrator"));
            }
            return schemaList;

        }

    }

    public void setSchemaList(ArrayList<IntensitySchema> schemaList) {
        this.schemaList = schemaList;
    }

    public IntensityClass getSelectedIntensityClass() {
        return selectedIntensityClass;
    }

    public void setSelectedIntensityClass(IntensityClass selectedIntensityClass) {
        this.selectedIntensityClass = selectedIntensityClass;
    }

    public IntensitySchema getWorkingSchema() {
        return workingSchema;
    }

    public void setWorkingSchema(IntensitySchema workingSchema) {
        this.workingSchema = workingSchema;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public ArrayList<Icon> getIconList() {
        return iconList;
    }

    public void setIconList(ArrayList<Icon> iconList) {
        this.iconList = iconList;
    }

}
