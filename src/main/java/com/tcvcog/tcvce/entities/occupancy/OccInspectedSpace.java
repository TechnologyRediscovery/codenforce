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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Subclass of Space: stores inspection-specific data about each space element
 * that is part of the super class. When a space is inspected, the ArrayList of CodeElements
 * in the Space are wrapped in an inspection blanket and added to the 
 * inspectedElementList which captures the compliance status, comp date, and notes.
 * 
 * @author ellen bascomb of apt 31y, Technology Rediscovery LLC 
 */
public class OccInspectedSpace
        implements  Serializable, 
                    Cloneable, 
                    Comparable<OccInspectedSpace> {

    private int inspectedSpaceID;
    
    protected OccSpaceTypeChecklistified type;
    
    private List<OccInspectedSpaceElement> inspectedElementList;
    private List<OccInspectedSpaceElement> inspectedElementListVisible;
    private ViewOptionsOccChecklistItemsEnum viewSetting;
    
    private List<OccInsElementGroup> inspectedElementGroupList;
    private Map<OccInspectionStatusEnum, List<OccInspectedSpaceElement>> elementStatusMap;
    
    private OccLocationDescriptor location;

    private User addedToChecklistBy;
    private LocalDateTime addedToChecklistTS;
    
    private OccInspectableStatus status;
    
    //This field exists just to make it easier to find the inspection associated
    //with an inspected space.
    private int inspectionID;
    

    public OccInspectedSpace() {}

    public OccInspectedSpace(OccInspectedSpace space) {
        this.inspectedSpaceID = space.getInspectedSpaceID();
        this.type = space.getType();

        this.inspectedElementList = space.getInspectedElementList();
        this.inspectedElementListVisible = space.getInspectedElementListVisible();
        this.viewSetting = space.getViewSetting();

        this.inspectedElementGroupList = space.getInspectedElementGroupList();
        this.elementStatusMap = space.getElementStatusMap();
        
        this.location = space.getLocation();

        this.addedToChecklistBy = space.getAddedToChecklistBy();
        this.addedToChecklistTS = space.getAddedToChecklistTS();

        this.status = space.getStatus();

        this.inspectionID = space.getInspectionID();
    }

    public void configureVisibleElementList(ViewOptionsOccChecklistItemsEnum vo) {
        viewSetting = vo;
        inspectedElementListVisible = new ArrayList<>();

        for(OccInspectedSpaceElement oise: inspectedElementList){
            switch(viewSetting){
                case ALL_ITEMS:
                    inspectedElementListVisible.add(oise);
                    break;
                case FAILED_ITEMS_ONLY:
                    // look for failed items
                    if(checkForFailure(oise)){
                        inspectedElementListVisible.add(oise);
                    } 
                    break;
                case FAILED_PASSEDWPHOTOFINDING:
                    if(checkForFailure(oise) || checkForPassPhotoOrFinding(oise)){
                        inspectedElementListVisible.add(oise);
                    }
                    break;
                case PASSED_AND_FAILED: 
                    if(checkForCompliance(oise) || checkForFailure(oise)){
                        inspectedElementListVisible.add(oise);
                    }
                    break;
                case PASSED_ITEMS:
                    if(checkForCompliance(oise)){
                        inspectedElementListVisible.add(oise);
                    }
                    break;
                case UNISPECTED_ITEMS_ONLY:
                    // look for failed items
                    if(checkForNoninspection(oise)){
                        inspectedElementListVisible.add(oise);
                    } 
                    break;
                default:
                    inspectedElementListVisible.add(oise);
            }
        }
    }
    
    /**
     * Internal orgran for inspected space element logic
     * @param oise
     * @return if the element should be included in a visible liste
     */
    private boolean checkForFailure(OccInspectedSpaceElement oise){
        return oise.getComplianceGrantedTS() == null && oise.getLastInspectedTS() != null;
        
    }
    
    /**
     * Internal orgran for inspected space element logic
     * @param oise
     * @return if the element should be included in a visible liste
     */
    private boolean checkForCompliance(OccInspectedSpaceElement oise){
        return oise.getComplianceGrantedTS() != null;
        
    }
    
    /**
     * Internal orgran for inspected space element logic
     * @param oise
     * @return if the element should be included in a visible liste
     */
    private boolean checkForNoninspection(OccInspectedSpaceElement oise){
        return oise.getComplianceGrantedTS() == null && oise.getLastInspectedTS() == null;
    }
    
    /**
     * Internal orgran for inspected space element logic
     * @param oise
     * @return if the element should be included in a visible liste
     */
    private boolean checkForPassPhotoOrFinding(OccInspectedSpaceElement oise){
        return checkForCompliance(oise) 
                && ((oise.getBlobList() != null && !oise.getBlobList().isEmpty())
                        || (oise.getInspectionNotes() != null && !oise.getInspectionNotes().equals("")));
        
    }
    
    public List<OccInspectedSpaceElement> getElementListPass(){
        if(elementStatusMap != null){
            return elementStatusMap.get(OccInspectionStatusEnum.PASS);
        }
        return new ArrayList<>();
    }
    
    public List<OccInspectedSpaceElement> getElementListFail(){
        if(elementStatusMap != null){
            return elementStatusMap.get(OccInspectionStatusEnum.VIOLATION);
        }
        return new ArrayList<>();
        
    }
    public List<OccInspectedSpaceElement> getElementListNotIns(){
        if(elementStatusMap != null){
            return elementStatusMap.get(OccInspectionStatusEnum.NOTINSPECTED);
        }
        return new ArrayList<>();
        
    }


    public List<OccLocationDescriptor> getAllUniqueLocationDescriptors() {
        Set<OccLocationDescriptor> locationDescriptors = new HashSet();

        for (OccInspectedSpaceElement inspectedSpaceElement : inspectedElementList) {
            locationDescriptors.add(inspectedSpaceElement.getLocation());
        }

        List<OccLocationDescriptor> locationDescriptorList = new ArrayList();
        locationDescriptorList.addAll(locationDescriptors);
        return locationDescriptorList;

    }
    
    public List<CodeElement> getInspectedCodeElementsWithoutShell() {
        List<CodeElement> eleList = new ArrayList<>();
        if(inspectedElementList != null){
            Iterator<OccInspectedSpaceElement> iter = inspectedElementList.iterator();
            while(iter.hasNext()){
                eleList.add(iter.next());
            }
        }
        return eleList;
    }
    

    /**
     * @return the inspectedElementList
     */
    public List<OccInspectedSpaceElement> getInspectedElementList() {
        return inspectedElementList;
    }

    /**
     * @param inspectedElementList the inspectedElementList to set
     */
    public void setInspectedElementList(List<OccInspectedSpaceElement> inspectedElementList) {
        this.inspectedElementList = inspectedElementList;
    }

    /**
     * @return the location
     */
    public OccLocationDescriptor getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(OccLocationDescriptor location) {
        this.location = location;
    }

   
    /**
     * @return the spaceType
     */
    public OccSpaceTypeChecklistified getType() {
        return type;
    }

  

    /**
     * @param type the spaceType to set
     */
    public void setType(OccSpaceTypeChecklistified type) {
        this.type = type;
    }


    /**
     * @return the addedToChecklistBy
     */
    public User getAddedToChecklistBy() {
        return addedToChecklistBy;
    }

    /**
     * @return the addedToChecklistTS
     */
    public LocalDateTime getAddedToChecklistTS() {
        return addedToChecklistTS;
    }

    /**
     * @param addedToChecklistBy the addedToChecklistBy to set
     */
    public void setAddedToChecklistBy(User addedToChecklistBy) {
        this.addedToChecklistBy = addedToChecklistBy;
    }

    /**
     * @param addedToChecklistTS the addedToChecklistTS to set
     */
    public void setAddedToChecklistTS(LocalDateTime addedToChecklistTS) {
        this.addedToChecklistTS = addedToChecklistTS;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.inspectedElementList);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.addedToChecklistBy);
        hash = 53 * hash + Objects.hashCode(this.addedToChecklistTS);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OccInspectedSpace other = (OccInspectedSpace) obj;
        if (!Objects.equals(this.inspectedElementList, other.inspectedElementList)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.addedToChecklistBy, other.addedToChecklistBy)) {
            return false;
        }
        if (!Objects.equals(this.addedToChecklistTS, other.addedToChecklistTS)) {
            return false;
        }
        return true;
    }

    /**
     * @return the inspectedSpaceID
     */
    public int getInspectedSpaceID() {
        return inspectedSpaceID;
    }

    /**
     * @param inspectedSpaceID the inspectedSpaceID to set
     */
    public void setInspectedSpaceID(int inspectedSpaceID) {
        this.inspectedSpaceID = inspectedSpaceID;
    }

    

    /**
     * @return the status
     */
    public OccInspectableStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccInspectableStatus status) {
        this.status = status;
    }

    /**
     * @return the inspectedElementListVisible
     */
    public List<OccInspectedSpaceElement> getInspectedElementListVisible() {
        
        return inspectedElementListVisible;
    }

    /**
     * @param inspectedElementListVisible the inspectedElementListVisible to set
     */
    public void setInspectedElementListVisible(List<OccInspectedSpaceElement> inspectedElementListVisible) {
        this.inspectedElementListVisible = inspectedElementListVisible;
    }

    /**
     * @return the viewSetting
     */
    public ViewOptionsOccChecklistItemsEnum getViewSetting() {
        
        return viewSetting;
    }

    /**
     * @param viewSetting the viewSetting to set
     */
    public void setViewSetting(ViewOptionsOccChecklistItemsEnum viewSetting) {
        this.viewSetting = viewSetting;
    }

    @Override
    public int compareTo(OccInspectedSpace o) {
        if(o.addedToChecklistTS == null || this.addedToChecklistTS == null){
            return 0;
        
        }
        return this.addedToChecklistTS.compareTo(o.addedToChecklistTS);
    }

    public int getInspectionID() {
        return inspectionID;
    }

    public void setInspectionID(int inspectionID) {
        this.inspectionID = inspectionID;
    }

    /**
     * @return the inspectedElementGroupList
     */
    public List<OccInsElementGroup> getInspectedElementGroupList() {
        return inspectedElementGroupList;
    }

    /**
     * @param inspectedElementGroupList the inspectedElementGroupList to set
     */
    public void setInspectedElementGroupList(List<OccInsElementGroup> inspectedElementGroupList) {
        this.inspectedElementGroupList = inspectedElementGroupList;
    }

    /**
     * @return the elementStatusMap
     */
    public Map<OccInspectionStatusEnum, List<OccInspectedSpaceElement>> getElementStatusMap() {
        return elementStatusMap;
    }

    /**
     * @param elementStatusMap the elementStatusMap to set
     */
    public void setElementStatusMap(Map<OccInspectionStatusEnum, List<OccInspectedSpaceElement>> elementStatusMap) {
        this.elementStatusMap = elementStatusMap;
    }
    
}