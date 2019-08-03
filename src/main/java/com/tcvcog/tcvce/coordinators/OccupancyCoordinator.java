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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityComplete;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccAppPersonRequirement;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Eric C. Darsow
 */
public class OccupancyCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of OccupancyCoordinator
     */
    public OccupancyCoordinator() {
    }
    
    public OccPeriod configureOccPeriod(OccPeriod period){
        
        return period;
        
    }
    
    public OccInspection configureOccInspection(OccInspection inspection){
        
        
        return inspection;
    }
    
    public OccInspection getOccInspectionSkeleton(){
        return new OccInspection();
    }
    
    public OccPeriod initializeNewOccPeriod(Property p, PropertyUnit pu, User u, MunicipalityComplete muni) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        OccPeriod op = new OccPeriod();
        
        op.setPropertyUnitID(pu.getUnitID());
        op.setType(muni.getProfile().getOccPeriodTypeList().get(0));
        op.setManager(u);
        op.setCreatedBy(u);
        op.setCreatedTS(LocalDateTime.now());
        
        op.setStartDate(LocalDateTime.now());
        op.setStartDateCertifiedBy(u);
        op.setStartDateCertifiedTS(LocalDateTime.now());
        
        op.setEndDate(op.getStartDate().plusDays(op.getType().getDefaultValidityPeriodDays()));
        op.setEndDateCertifiedBy(u);
        op.setEndDateCertifiedTS(LocalDateTime.now());
        
        op.setSource(si.getBOBSource(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("occPeriodNewInternalBOBSourceID"))));
        
        return op;
    }
    
    public int insertNewOccPeriod(OccPeriod op, User u) throws IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        return oi.insertOccPeriod(op);
        
    }
    
    /**
     * Supervises the creation of a new Occupancy Inspection object in the database.
     * The designed flow would be the backing bean calls getOccInspectionSkeleton() and 
     * sets member variables on there and then passes it into this method.
     * 
     * @param in A skeleton of an OccInspection without an ID number
     * @param period the OccPeriod to which the OccInspection should be linked
     * @param templ The template from which the Inspection will draw its SpaceTypes
     * @param user The current user who will become the Inspector
     * @param muni The current Muni
     * @return An OccInspection object with the ID given in the DB and a configured Template inside
     * @throws InspectionException
     * @throws IntegrationException 
     */
    public OccInspection commenceOccupancyInspection(   OccInspection in,
                                                        OccPeriod period,
                                                        OccChecklistTemplate templ, 
                                                        User user,
                                                        Municipality muni) throws InspectionException, IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccInspection inspec = null;
        
        if(period.getType().isActive()
                &&
            period.getType().isInspectable()){
            
            if(in != null){
                inspec = in;
            } else {
                inspec = new OccInspection();
            }
            inspec.setOccPeriodID(period.getPeriodID());
            inspec.setChecklistTemplate(templ);
            inspec.setInspector(user);
            inspec.setPacc(generateControlCodeFromTime());
//            if(muni.isEnablePublicOccInspectionTODOs()){
//                inspec.setEnablePacc(true);
//            } else {
//                inspec.setEnablePacc(false);
//            }
            inspec = oii.insertOccInspection(in);
        } else {
            throw new InspectionException("Occ period either inactive or uninspectable");
        }
        return inspec;
    }
    
    
    /**
     * Called by the backing bean when the user selects a space to start inspecting.
     * 
     * @param inspection The current inspection
     * @param u The current user--not necessarily the official Inspector of the OccInspection
     * @param spc The OccSpace pulled from the OccInspectionTemplate list
     * @param loc A populated location descriptor for this Space. Can be an existing location or an new one
     * @return Containing a List of InspectedCodeElement objects ready to be evaluated
     * @throws IntegrationException 
     */
    public OccInspection inspectionAction_commenceSpaceInspection(  OccInspection inspection, 
                                                                    User u, 
                                                                    OccSpace spc, 
                                                                    OccLocationDescriptor loc) 
                                                                throws IntegrationException{
        OccInspectionIntegrator inspecInt = getOccInspectionIntegrator();
        
        // Feed the given OccSpace to the constructor of the InspectedSpace
        OccInspectedSpace inspSpace = new OccInspectedSpace(spc);
        // then configure the OccInspectedSpace for first insertion
        inspSpace.setLocation(loc);
        inspSpace.setLastInspectedBy(u);
        inspSpace.setLastInspectedTS(LocalDateTime.now());
        // We are inspecting all the code elements associated with this space in the checklist template
        ListIterator<OccSpaceElement> elementIterator = spc.getSpaceElementList().listIterator();
        OccInspectedSpaceElement inspEle; // Holds our new objects as we add them to the list
        List<OccInspectedSpaceElement> inElementList = new ArrayList<>();
        
        // wrap each CodeElement in this space in a InspectedCodeElement blanket to keep it warm
        while(elementIterator.hasNext()){
            OccSpaceElement ele = elementIterator.next();
            // Create an OccInspectedElement by by passing in a CodeElement using the special constructor
            inspEle = new OccInspectedSpaceElement(ele, ele.getSpaceElementID());
            inspEle.setLastInspectedBy(u);
            inElementList.add(inspEle);
            // each element in this space gets a reference to the same OccLocationDescriptor object
            if(loc == null){
                inspSpace.setLocation(inspecInt.getLocationDescriptor(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("locationdescriptor_implyfromspacename"))));
            } else {
                inspSpace.setLocation(loc);
            }
        }
        
        // Critical moment of injecting an new (i.e. ID-less) InspectedElement into its OccInspectedSpace
        inspSpace.setInspectedElementList(inElementList);
        
        // With a fully built inspected space, we can record our start of inspection in the DB
        inspSpace = inspecInt.recordCommencementOfSpaceInspection(inspSpace, inspection);
        System.out.println("OccucpancyCoordinator.inpectionAction_commenceSpaceInspection | commenced inspecting of space");
        
        // now use our convenience method to record Inspection of the space's individual elements
        inspecInt.recordInspectionOfSpaceElements(inspSpace, inspection);
        
        inspSpace = inspecInt.getInspectedSpace(inspSpace.getSpaceID());
        System.out.println("OccucpancyCoordinator.inpectionAction_commenceSpaceInspection | retrievedInspectedSpaceid="+inspSpace);
        
        return inspection;
    }    
    
    
    public OccInspection inspectionAction_updateSpaceElementData(OccInspection inspection) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.updateOccInspection(inspection);
        oii.getOccInspection(inspection.getInspectionID());
        return inspection;
    }
    
    public OccPermitApplication getNewOccPermitApplication(){
        OccPermitApplication occpermitapp = new OccPermitApplication();        
        occpermitapp.setSubmissionDate(LocalDateTime.now());        
        return occpermitapp;       
    }
    
    /**
     * Sets boolean requirementSatisfied on an OccPermitApplication based on the application reason,
     * the person requirement for that reason, and the PersonTypes of the Persons attached to the 
     * application.
     * @param opa 
     */
    public void verifyOccPermitPersonsRequirement (OccPermitApplication opa){
        boolean isRequirementSatisfied = true;
        OccAppPersonRequirement pr = opa.getReason().getPersonsRequirement();        
        List<PersonType> requiredPersonTypes = pr.getRequiredPersonTypes();
        List<Person> applicationPersons = opa.getAttachedPersons();
        List<PersonType> applicationPersonTypes = new ArrayList<>();
        for (Person applicationPerson:applicationPersons){
            applicationPersonTypes.add(applicationPerson.getPersonType());
        }
        for (PersonType personType:requiredPersonTypes) {
            if (!applicationPersonTypes.contains(personType)){
                isRequirementSatisfied = false;               
            }
        }
        pr.setRequirementSatisfied(isRequirementSatisfied);
    }
}
