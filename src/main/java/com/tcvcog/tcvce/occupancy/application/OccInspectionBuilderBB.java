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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Xiaohong
 */
public class OccInspectionBuilderBB extends BackingBeanUtils implements Serializable {

    private List<OccChecklistTemplate> occChecklistTemplateList;
    private List<OccSpaceTypeInspectionDirective> selectedSpaceTypeList;
    private List<OccSpace> selectedOccSpaceList;
    private List<OccSpaceElement> selectedOccSpaceElementList;

    private OccChecklistTemplate selectedOccChecklistTemplate;
    private OccSpaceTypeInspectionDirective selectedSpaceType;
    private OccSpace selectedSpace;
    private OccSpaceElement selectedElement;

    private OccSpaceTypeInspectionDirective currentSpaceType;
    private OccSpace currentSpace;
    private OccSpaceElement currentElement;

    /**
     * Creates a new instance of OccInspectionBuilderBB
     */
    public OccInspectionBuilderBB() {
    }

    @PostConstruct
    public void initBean() {

        OccupancyCoordinator oc = getOccupancyCoordinator();

        try {
            occChecklistTemplateList = oc.getOccChecklistTemplatelist();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

    }

    public void onSelectedOccChecklistTitleChange() {

        OccupancyCoordinator oc = getOccupancyCoordinator();

        int selectedChecklistID = selectedOccChecklistTemplate.getInspectionChecklistID();

        selectedSpaceTypeList = new ArrayList<>();
        selectedOccSpaceList = new ArrayList<>();
        selectedOccSpaceElementList = new ArrayList<>();

        selectedSpaceType = null;
        selectedSpace = null;
        selectedElement = null;

        try {
            selectedSpaceTypeList = oc.getOccSpaceTypeList(selectedChecklistID);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded Checklist Title", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }

    }

    public void onSelectedSpaceTypeChange(OccSpaceTypeInspectionDirective spaceType) {

        OccupancyCoordinator oc = getOccupancyCoordinator();

        if (spaceType.isSelected() != false) {

            selectedSpaceType = spaceType;

            selectedSpace = null;

            selectedElement = null;

            selectedSpaceTypeList = new ArrayList<>();

            selectedSpaceTypeList.add(spaceType);

            selectedOccSpaceList = new ArrayList<>();

            selectedOccSpaceElementList = new ArrayList<>();

            try {

                selectedOccSpaceList = oc.getSpacelist(selectedSpaceType.getSpaceTypeID());
            } catch (IntegrationException ex) {
                Logger.getLogger(OccInspectionBuilderBB.class.getName()).log(Level.SEVERE, null, ex);
            }

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded Space Type", ""));

        } else {

            onSelectedOccChecklistTitleChange();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Unload Space Type", ""));
        }
    }

    public void editSpaceType(OccSpaceTypeInspectionDirective st) {
        if (selectedSpaceType == null) {
            currentSpaceType = st;
        } else {
            currentSpaceType = selectedSpaceType;
        }
    }

    public void initNewSpaceTypeButton() {

        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentSpaceType = oc.getOccSpaceTypeSkeleton();
    }

    public boolean showAddSpaceTypeButton() {
        return selectedOccChecklistTemplate != null;
    }

    public boolean showDialogSpaceTypeCreateButton() {

        if (currentSpaceType != null) {
            return currentSpaceType.getSpaceTypeID() == 0;
        } else {
            return false;
        }

    }

    public boolean showDialogSpaceTypeUpdateButton() {
        if (currentSpaceType != null) {
            return currentSpaceType.getSpaceTypeID() != 0;
        } else {
            return false;
        }

    }

    public void createNewSpaceTypeButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.addNewChecklistSpacetype(selectedOccChecklistTemplate.getInspectionChecklistID(), currentSpaceType);
            onSelectedOccChecklistTitleChange();
            //onSpaceTypeEditChange();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Created New Space Type", ""));

        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void updateSpaceTypeButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateChecklistSpacetype(currentSpaceType);
            onSelectedOccChecklistTitleChange();
            //onSpaceTypeEditChange();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Updated Space Type", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void deleteSpaceTypeButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.deleteChecklistSpacetype(currentSpaceType);
            onSelectedOccChecklistTitleChange();
            //onSpaceTypeEditChange();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Deleted Space Type", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void onSelectedSpaceChange(OccSpace space) {

        OccupancyCoordinator oc = getOccupancyCoordinator();

        if (space.isSelected() != false) {

            selectedSpace = space;

            selectedElement = null;

            selectedOccSpaceList = new ArrayList<>();

            selectedOccSpaceList.add(space);

            selectedOccSpaceElementList = new ArrayList<>();

            try {
                selectedOccSpaceElementList = oc.getSpace(space.getSpaceID()).getSpaceElementList();
            } catch (IntegrationException ex) {
                Logger.getLogger(OccInspectionBuilderBB.class.getName()).log(Level.SEVERE, null, ex);
            }

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded Space", ""));
        } else {

            onSelectedSpaceTypeChange(selectedSpaceType);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Unload Space", ""));

        }
    }

    public void editSpace(OccSpace os) {
        currentSpace = selectedSpace;
        if (selectedSpace == null) {
            currentSpace = os;
        } else {
            currentSpace = selectedSpace;
        }
    }

    public void initNewSpaceButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentSpace = oc.getOccSpaceSkeleton();
    }

    public boolean showAddSpaceButton() {
        return selectedSpaceType != null;
    }

    public boolean showDialogSpaceCreateButton() {
        if (currentSpace != null) {
            return currentSpace.getSpaceID() == 0;
        } else {
            return false;
        }

    }

    public boolean showDialogSpaceUpdateButton() {
        if (currentSpace != null) {
            return currentSpace.getSpaceID() != 0;
        } else {
            return false;
        }

    }

    public void createNewSpaceButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.addNewChecklistSpace(currentSpace, selectedSpaceType);

            onSelectedSpaceTypeChange(selectedSpaceType);
            //onSpaceEditChange();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Created New Space ", ""));

        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void updateSpaceButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateChecklistSpace(currentSpace);

            onSelectedSpaceTypeChange(selectedSpaceType);
            //onSpaceEditChange();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Updated Space Type", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void deleteSpaceButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.deleteChecklistSpace(currentSpace);

            onSelectedSpaceTypeChange(selectedSpaceType);
            //onSpaceEditChange();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Deleted Space Type", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void editSpaceElement(OccSpaceElement ose) {
        if (selectedElement == null) {
            currentElement = ose;
        } else {
            currentElement = selectedElement;
        }
    }

    public void initNewElementButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentElement = oc.getOccSpaceElementSkeleton();
    }

    public boolean showAddElementButton() {
        return selectedSpace != null;
    }

    public boolean showDialogElementCreateButton() {
        if (currentElement != null) {
            return currentElement.getSpaceElementID() == 0;
        } else {
            return false;
        }

    }

    public boolean showDialogElementDeleteButton() {
        if (currentElement != null) {
            return currentElement.getSpaceElementID() != 0;
        } else {
            return false;
        }

    }

    public void createNewElementButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.createChecklistElement(selectedSpace, currentElement.getElementID());
            onSelectedSpaceChange(selectedSpace);
            //onSpaceElementEditChange();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Create Space Element", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    public void deleteNewElementButton() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.deleteChecklistElement(selectedSpace, currentElement.getElementID());

            onSelectedSpaceChange(selectedSpace);
            //onSpaceElementEditChange();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Deleted Space Element", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }

    //invalid(for updating)
    public void onSpaceTypeEditChange() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        selectedSpaceType = null;
        selectedSpace = null;
        selectedElement = null;

        selectedOccSpaceList = new ArrayList<>();
        selectedOccSpaceElementList = new ArrayList<>();
        selectedSpaceTypeList = new ArrayList<>();

        try {
            int checklistId = selectedOccChecklistTemplate.getInspectionChecklistID();
            selectedOccChecklistTemplate = oc.getChecklistTemplate(checklistId);
            selectedSpaceTypeList = oc.getOccSpaceTypeList(checklistId);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

    //invalid(for updating)
    public void onSpaceEditChange() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        selectedElement = null;
        selectedSpace = null;

        selectedOccSpaceElementList = new ArrayList<>();
        selectedOccSpaceList = new ArrayList<>();
        selectedSpaceTypeList = new ArrayList<>();

        try {
            int checklistId = selectedOccChecklistTemplate.getInspectionChecklistID();
            selectedOccChecklistTemplate = oc.getChecklistTemplate(checklistId);
            selectedSpaceTypeList = oc.getOccSpaceTypeList(checklistId);

            int spacetypeid = selectedSpaceType.getSpaceTypeID();
            selectedOccSpaceList = oc.getSpacelist(spacetypeid);
            selectedSpaceType = oc.getSpaceType(spacetypeid);
            selectedSpaceType.setSelected(true);

        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

    //invalid(for updating)
    public void onSpaceElementEditChange() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        selectedElement = null;

        selectedOccSpaceElementList = new ArrayList<>();
        selectedSpaceTypeList = new ArrayList<>();
        selectedOccSpaceList = new ArrayList<>();

        try {
            int checklistId = selectedOccChecklistTemplate.getInspectionChecklistID();
            selectedOccChecklistTemplate = oc.getChecklistTemplate(checklistId);
            selectedSpaceTypeList = oc.getOccSpaceTypeList(checklistId);

            int spacetypeid = selectedSpaceType.getSpaceTypeID();
            selectedOccSpaceList = oc.getSpacelist(spacetypeid);
            selectedSpaceType = oc.getSpaceType(spacetypeid);
            selectedSpaceType.setSelected(true);

            int spaceid = selectedSpace.getSpaceID();
            selectedSpace = oc.getSpace(spaceid);
            selectedOccSpaceElementList = selectedSpace.getSpaceElementList();
            selectedSpace.setSelected(true);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

    public boolean lockSpaceType() {
        if (selectedSpaceType == null) {
            return false;
        } else {
            return selectedSpaceType.isSelected() != false;
        }
    }

    public boolean lockSpace() {
        if (selectedSpace == null) {
            return false;
        } else {
            return selectedSpace.isSelected() != false;
        }
    }

    public OccChecklistTemplate getSelectedOccChecklistTemplate() {
        return selectedOccChecklistTemplate;
    }

    public void setSelectedOccChecklistTemplate(OccChecklistTemplate selectedOccChecklistTemplate) {
        this.selectedOccChecklistTemplate = selectedOccChecklistTemplate;
    }

    public List<OccChecklistTemplate> getOccChecklistTemplateList() {
        return occChecklistTemplateList;
    }

    public void setOccChecklistTemplateList(List<OccChecklistTemplate> occChecklistTemplateList) {
        this.occChecklistTemplateList = occChecklistTemplateList;
    }

    public List<OccSpaceTypeInspectionDirective> getSelectedSpaceTypeList() {
        return selectedSpaceTypeList;
    }

    public void setSelectedSpaceTypeList(List<OccSpaceTypeInspectionDirective> selectedSpaceTypeList) {
        this.selectedSpaceTypeList = selectedSpaceTypeList;
    }

    public List<OccSpace> getSelectedOccSpaceList() {
        return selectedOccSpaceList;
    }

    public void setSelectedOccSpaceList(List<OccSpace> selectedOccSpaceList) {
        this.selectedOccSpaceList = selectedOccSpaceList;
    }

    public List<OccSpaceElement> getSelectedOccSpaceElementList() {
        return selectedOccSpaceElementList;
    }

    public void setSelectedOccSpaceElementList(List<OccSpaceElement> selectedOccSpaceElementList) {
        this.selectedOccSpaceElementList = selectedOccSpaceElementList;
    }

    public OccSpaceTypeInspectionDirective getSelectedSpaceType() {
        return selectedSpaceType;
    }

    public void setSelectedSpaceType(OccSpaceTypeInspectionDirective selectedSpaceType) {
        this.selectedSpaceType = selectedSpaceType;
    }

    public OccSpace getSelectedSpace() {
        return selectedSpace;
    }

    public void setSelectedSpace(OccSpace selectedSpace) {
        this.selectedSpace = selectedSpace;
    }

    public OccSpaceElement getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(OccSpaceElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    public OccSpaceTypeInspectionDirective getCurrentSpaceType() {
        return currentSpaceType;
    }

    public void setCurrentSpaceType(OccSpaceTypeInspectionDirective currentSpaceType) {
        this.currentSpaceType = currentSpaceType;
    }

    public OccSpace getCurrentSpace() {
        return currentSpace;
    }

    public void setCurrentSpace(OccSpace currentSpace) {
        this.currentSpace = currentSpace;
    }

    public OccSpaceElement getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(OccSpaceElement currentElement) {
        this.currentElement = currentElement;
    }

}
