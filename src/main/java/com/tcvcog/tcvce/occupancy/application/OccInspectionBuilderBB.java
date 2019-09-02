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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Xiaohong
 */
public class OccInspectionBuilderBB extends BackingBeanUtils implements Serializable {

    private List<OccChecklistTemplate> occChecklistTemplateList;
    private List<OccSpaceTypeInspectionDirective> selectedSpacesInTypeList;
    private List<OccSpace> occSpaceList;

    private List<OccSpaceElement> occSpaceElementList;

    private OccChecklistTemplate selectedOccChecklistTemplate;
    private OccSpaceTypeInspectionDirective selectedSpaceType;
    private OccSpace selectedSpace;
    private OccSpaceElement selectedElement;

    /**
     * Creates a new instance of OccInspectionBuilderBB
     */
    public OccInspectionBuilderBB() {
    }

    @PostConstruct
    public void initBean() {
        // populate any permanent drop down lists or objects the pages
        // need for first load

        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        try {
            occChecklistTemplateList = oii.getOccChecklistTemplatelist();

        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

    }

    public void onSelectedOccChecklistTemplateChange() {

        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        try {

            selectedSpacesInTypeList = oii.getOccInspecTemplateSpaceTypeList(selectedOccChecklistTemplate.getInspectionChecklistID());

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded check list title!", ""));

        } catch (IntegrationException ex) {

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }

    }

    public void onSelectedSpaceTypeChange(OccSpaceTypeInspectionDirective spaceType) {

        if (spaceType.isOverrideSpaceTypeRequired() != false) {
            selectedSpaceType = spaceType;
            selectedSpacesInTypeList = new ArrayList<>();
            selectedSpacesInTypeList.add(spaceType);
            occSpaceList = spaceType.getSpaceList();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded space types!", ""));

        } else {
            onSelectedOccChecklistTemplateChange();
            occSpaceList = new ArrayList<>();
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Unload space types!", ""));
        }
    }

    public void onSelectedSpaceChange(OccSpace space) {

        if (space.isRequired() != false) {
            selectedSpace = space;
            occSpaceList = new ArrayList<>();
            occSpaceList.add(space);
            occSpaceElementList = space.getSpaceElementList();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded space!", ""));
        } else {

            occSpaceList = selectedSpaceType.getSpaceList();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Unload space!", ""));

        }
    }

    public void onSelectedElementChange(OccSpaceElement element) {
        if (element.isIsSelected() != false) {
            occSpaceElementList = new ArrayList<>();
            occSpaceElementList.add(element);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Loaded elements!", ""));

        } else {
            occSpaceElementList = selectedSpace.getSpaceElementList();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Unload elements!", ""));
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

    public List<OccSpaceTypeInspectionDirective> getSpacesInTypeList() {
        return selectedSpacesInTypeList;
    }

    public void setSpacesInTypeList(List<OccSpaceTypeInspectionDirective> spacesInTypeList) {
        this.selectedSpacesInTypeList = spacesInTypeList;
    }

    public List<OccSpace> getOccSpaceList() {
        return occSpaceList;
    }

    public void setOccSpaceList(List<OccSpace> occSpaceList) {
        this.occSpaceList = occSpaceList;
    }

    public List<OccSpaceElement> getOccSpaceElementList() {
        return occSpaceElementList;
    }

    public void setOccSpaceElementList(List<OccSpaceElement> occSpaceElementList) {
        this.occSpaceElementList = occSpaceElementList;
    }

    public List<OccSpaceTypeInspectionDirective> getSelectedSpacesInTypeList() {
        return selectedSpacesInTypeList;
    }

    public void setSelectedSpacesInTypeList(List<OccSpaceTypeInspectionDirective> selectedSpacesInTypeList) {
        this.selectedSpacesInTypeList = selectedSpacesInTypeList;
    }
}
