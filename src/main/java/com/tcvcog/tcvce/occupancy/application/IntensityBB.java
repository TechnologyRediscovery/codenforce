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
import com.tcvcog.tcvce.entities.Intensity;
import com.tcvcog.tcvce.entities.IntensityCategory;
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
    
    private Intensity workingIntensityClass;
    private Intensity selectedIntensityClass;
    private ArrayList<Intensity> existingIntensityList;
    private IntensityCategory selectedCategory;
    private ArrayList<IntensityCategory> categoryList;
    
    public IntensityBB() {
        
    }
    
    @PostConstruct
    public void initBean() {
        
    }

    public void editIntensity(ActionEvent e){
        if(selectedIntensityClass != null){
            workingIntensityClass.setClassID(selectedIntensityClass.getClassID());
            workingIntensityClass.setTitle(selectedIntensityClass.getTitle());
            workingIntensityClass.setMuni(selectedIntensityClass.getMuni());
            workingIntensityClass.setNumericRating(selectedIntensityClass.getNumericRating());
            workingIntensityClass.setSchemaName(selectedIntensityClass.getSchemaName());
            workingIntensityClass.setActive(selectedIntensityClass.isActive());
            workingIntensityClass.setIcon(selectedIntensityClass.getIcon());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an Intensity Class to edit", ""));
        }
    }
    
    public Intensity getWorkingIntensityClass() {
        return workingIntensityClass;
    }

    public void setWorkingIntensityClass(Intensity workingIntensityClass) {
        this.workingIntensityClass = workingIntensityClass;
    }

    public ArrayList<Intensity> getExistingIntensityList() {
        return existingIntensityList;
    }

    public void setExistingIntensityList(ArrayList<Intensity> existingIntensityList) {
        this.existingIntensityList = existingIntensityList;
    }

    public IntensityCategory getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(IntensityCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public ArrayList<IntensityCategory> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(ArrayList<IntensityCategory> categoryList) {
        this.categoryList = categoryList;
    }

    public Intensity getSelectedIntensityClass() {
        return selectedIntensityClass;
    }

    public void setSelectedIntensityClass(Intensity selectedIntensityClass) {
        this.selectedIntensityClass = selectedIntensityClass;
    }
            
    
    
}
