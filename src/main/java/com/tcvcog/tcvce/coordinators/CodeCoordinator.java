/*
 * Copyright (C) 2017 Turtle Creek Valley
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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The controller class for all things code element (i.e. ordinances)
 * @author ellen bascomb of apt 31y
 */
public class CodeCoordinator extends BackingBeanUtils implements Serializable {

    
    final int ECE_DEFAULT_PENALTY_MAX = 1000;
    final int ECE_DEFAULT_PENALTY_NORM = 50;
    final int ECE_DEFAULT_PENALTY_MIN = 10;
    final String ECE_DEFAULT_PENALTY_NOTES = "Default values";
    final int ECE_DEFAULT_DAYS_TO_COMPLY = 30;
    final String ECE_DEFAULT_DAYSTOCOMPLY_NOTES = "Default values";
    
    final static String FMT_SECTION_ENTITY = "&#167;";
    
    final static String FMT_CH = "ch.";
    final static String FMT_SPACE = " ";
    final static String FMT_PAREN_L = "(";
    final static String FMT_PAREN_R = ")";
    final static String FMT_DASH = "-";
    final static String FMT_COLON = ":";
    
    
    
    /**
     * Creates a new instance of CodeCoordinator
     */
    public CodeCoordinator() {
    }
    
    // *************************************************************
    // *********************CODE SOURCES****************************
    // *************************************************************
    
        
    /**
     * Factory for CodeSource objects
     * @return 
     */
    public CodeSource getCodeSourceSkeleton(){
        
        return new CodeSource();
        
    }
    
    /**
     * Retrieval method for code sources
     * @param sourceID
     * @return
     * @throws IntegrationException 
     */
    public CodeSource getCodeSource(int sourceID) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeSource(sourceID);
        
    }
    /**
     * Main generator for a fully baked code source and all its elements
     * @param sourceID
     * @return
     * @throws IntegrationException 
     */
    public CodeSource retrieveCodeSourceByID(int sourceID) throws IntegrationException{
        if(sourceID == 0) return null;
        CodeIntegrator integrator = getCodeIntegrator();
        CodeSource s = integrator.getCodeSource(sourceID);
       
        return s;
    }
    
     /**
     * Primary getter for lists of CodeSource objects
     * @return
     * @throws IntegrationException 
     */
    public List<CodeSource> getCodeSourceList() throws IntegrationException{
        CodeIntegrator integrator = getCodeIntegrator();
        List<CodeSource> sources = integrator.getCompleteCodeSourceList();
        return sources;
    }
    
    /**
     * Logic pass through for insertion of CodeSource objects
     * @param source
     * @throws IntegrationException 
     */
    public void addNewCodeSource(CodeSource source) throws IntegrationException{
        getCodeIntegrator().insertCodeSource(source);
    }
    
    /**
     * Logic pass through for updates to code sources
     * @param source
     * @throws IntegrationException 
     */
    public void updateCodeSource(CodeSource source) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        
        
        ci.updateCodeSource(source);
        
    }
    
    /**
     * Logic pass through for deactivation of a code source
     * @param source
     * @throws IntegrationException 
     */
    public void deactivateCodeSource(CodeSource source) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        
        ci.deactivateCodeSource(source);
        
        
        
    }

    
    
    
    // *************************************************************
    // **************CODE ELEMENTS (ORDINANCES)*********************
    // *************************************************************
   
    
    /**
     * Primary factory method for CodeElement objects
     * @return 
     */
    public CodeElement getCodeElementSkeleton(){
        return new CodeElement();
    }
    
      
    /**
     * Primary getter for all code elements system-wide
     * @param eleid
     * @return
     * @throws IntegrationException 
     */
    public CodeElement getCodeElement(int eleid) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        try {
            return configureCodeElement(ci.getCodeElement(eleid));
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }

    /**
     * Logic bundle for code elements; 
     * @param ce
     * @return 
     */
    private CodeElement configureCodeElement(CodeElement ce){
       ce.setHeaderString(buildOrdinanceHeaderString(ce));
        
        return ce;
    }
    
    /**
     * Implements logic to create a title for a code element; Designed to be injected
     * into the code elements ordiance
     * 
     * TODO: Finish my guts
     * @param ce
     * @return 
     */
    private String buildOrdinanceHeaderString(CodeElement ce){
        StringBuilder sb = new StringBuilder();
         if(ce != null){
            // Prefix with source and year
            if(ce.getSource() != null){
                sb.append(ce.getSource().getSourceName());
                if(ce.getSource().getSourceYear() != 0){
                    sb.append(FMT_PAREN_L);
                    sb.append(ce.getSource().getSourceYear());
                    sb.append(FMT_PAREN_R);
                }
                sb.append(FMT_SPACE);
            }
            
            // If we have a standard IPMC listing, the chapter and section
            // numbers are actually in the sub-section number, so just use that number
            // such as 302.1.1: Chapter 3, section 2, subsec 1, subsubsec 1
           
            // check if section number is in sub-section number
            if(checkForPureIRCRecursiveListing(ce)){
//                System.out.println("CodeCoordinator.buildOrdinanceHeaderString | Found pure IRC");

                sb.append(FMT_SECTION_ENTITY);
                sb.append(FMT_SPACE);
                if(ce.getOrdSubSubSecNum() != null 
                        && !ce.getOrdSubSubSecNum().equals("''")){
                    // eg 302.1.1
                    sb.append(ce.getOrdSubSubSecNum());
                } else { // we don't have a recursive sub-sub sec number, so use sub-sec number
                    sb.append(ce.getOrdSubSecNum());
                    sb.append(FMT_COLON);
                    sb.append(FMT_SPACE);
                }
                // now insert the titles
                // NOTE we are not using chapter titles in the standard IRC
                // they are implied by the section and subsection titles
                
                if(ce.getOrdSecTitle() != null){
                    sb.append(ce.getOrdSecTitle());
                    sb.append(FMT_SPACE);
                    sb.append(FMT_DASH);
                    sb.append(FMT_SPACE);
                }
                if(ce.getOrdSubSecTitle() != null){
                    sb.append(ce.getOrdSubSecTitle());
                }
            } else { // We do not have a pure IRC, so piece the header together straight across
                // we have a non-zero chapter number
                if(ce.getOrdchapterNo() != 0){
                    sb.append(FMT_CH);
                    sb.append(ce.getOrdchapterNo());
                    if(ce.getOrdchapterTitle()!= null && !ce.getOrdchapterTitle().equals("''")){
                        sb.append(FMT_COLON);
                        sb.append(ce.getOrdchapterTitle());
                    }
                }
                // setup our section
                // if we have a section number, put it in
                if(ce.getOrdSecNum() != null && !ce.getOrdSecNum().equals("''")){
                    sb.append(FMT_SPACE);
                    sb.append(FMT_SECTION_ENTITY);
                    sb.append(FMT_SPACE);
                    sb.append(ce.getOrdSecNum());
                    // if we have a sub sec number, it follows in parents (a)
                    if(ce.getOrdSubSecNum() != null && !ce.getOrdSubSecNum().equals("''")){
                        sb.append(FMT_PAREN_L);
                        sb.append(ce.getOrdSubSecNum());
                        sb.append(FMT_PAREN_R);
                        // if we have a sub sub sec number, it follows in parents (1)
                        if(ce.getOrdSubSubSecNum() != null && !ce.getOrdSubSubSecNum().equals("''")){
                            sb.append(FMT_PAREN_L);
                            sb.append(ce.getOrdSubSecNum());
                            sb.append(FMT_PAREN_R);
                        }
                    }
                } // done with section numbers, now for section titles
                
                if(ce.getOrdSecTitle() != null && !ce.getOrdSecTitle().equals("''")){
                    sb.append(FMT_SPACE);
                    sb.append(ce.getOrdSecTitle());
                }
                if(ce.getOrdSubSecTitle() != null && !ce.getOrdSubSecTitle().equals("''")){
                    sb.append(FMT_SPACE);
                    sb.append(FMT_DASH);
                    sb.append(FMT_SPACE);
                    sb.append(ce.getOrdSubSecTitle());
                }
            } // end outer if check on standard IRC 
//            System.out.println("CodeCoordinator.buildOrdinanceHeaderString | eleID:  " + ce.getElementID() );
//            System.out.println("CodeCoordinator.buildOrdinanceHeaderString | headerString:  " + sb.toString() );
        } // element not null
        
        return sb.toString();
    }
    
    /**
     * Logic method for checking if a code element has been entered 
     * into the system using standard IRC recursive listing
     * such that the chapter and section numbers are in the sub-section
     * number, e.g. Chapter 7, Section 703, subsection 703.1: We only 
     * want to use 703.1 in the display, since inclusion of the ch & sec
     * is redundant
     * @param ele to check
     * @return true if the ch + sec are in the sub-section, and if
     * sub-sub is not null, the sub-section is in the sub-sub section
     */
    private boolean checkForPureIRCRecursiveListing(CodeElement ce){
        
        boolean isPureIRCFormat = true;
        
        // nulls or zero for ch, sec, and subsec violates purity
        if(ce.getOrdchapterNo() == 0 
                || ce.getOrdSecNum() == null
                || ce.getOrdSubSecNum() == null){
            return false;
        }
        
        // no ch, sec, or sub-sec titles violates purity
        if(ce.getOrdchapterTitle() == null
                || ce.getOrdSecTitle() == null
                || ce.getOrdSubSecTitle() == null){
            return false;
        }
            
        // chapter No. should be in our section #
        if(!ce.getOrdSecNum().contains(String.valueOf(ce.getOrdchapterNo()))){
            return false;    
        }
        
        // sec number should be in our sub-section #
        if(!ce.getOrdSubSecNum().contains(ce.getOrdSecNum())){
            return false;
        }
        
        // if there is a sub-sub number, section should be in there, too
        if(ce.getOrdSubSubSecNum() != null && !ce.getOrdSubSecNum().equals("''")){
            if(!ce.getOrdSubSubSecNum().contains(ce.getOrdSubSecNum())){
                return false;
            }
        }
           
        return isPureIRCFormat;
    }
    
    
    /**
     * Extracts all CodeElements in a given Source
     * @param src
     * @return of CodeElements in that source
     * @throws IntegrationException 
     */
    public List<CodeElement> getCodeElemements(CodeSource src) throws IntegrationException{
        if(src ==  null){
            return new ArrayList<>();
        }
        CodeIntegrator ci = getCodeIntegrator();
        try {
            return ci.getCodeElements(src.getSourceID());
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }
    
    
    
    
   /**
    * Logic intermediary for insertion of new CodeElements
    * 
    * @param ele to be inserted
    * @param ua doing the insertion
    * @return the ID of the freshly inserted record
    * @throws IntegrationException 
    */
    public int addCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        int freshId = 0;
        if(ua == null || ele == null) return 0;
        ele.setCreatedBy(ua);
        ele.setLastupdatedBy(ua);
        freshId = ci.insertCodeElement(ele);
        return freshId;
        
    }
    
    
    /**
     * Logic intermediary for updates to a code element
     * 
     * @param ele with fields updated
     * @param ua doing the updating
     * @throws IntegrationException 
     */
    public void updateCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ua == null || ele == null) return;
        ele.setLastupdatedBy(ua);
        ci.updateCodeElement(ele);
        
    }
    
    
    /**
     * Logic intermediary for deactivation of CodeElements
     * 
     * @param ele to deactivate
     * @param ua doing the deactivation
     * @throws IntegrationException 
     */
    public void deactivateCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        
        CodeIntegrator ci = getCodeIntegrator();
        if(ua == null || ele == null) return;
        ele.setDeactivatedBy(ua);
        
        ci.deactivateCodeElement(ele);
        
        
        
    }
    
    
    // *************************************************************
    // **************CODE SETS (CODE BOOKS)*************************
    // *************************************************************

    
    /**
     * Retrieval portal for CodeSet objects
     * @param setID not zero
     * @return the code set object
     * @throws BObStatusException if setID is zero
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeSet getCodeSet(int setID) throws BObStatusException, IntegrationException{
        if(setID == 0){

            throw new BObStatusException("0 is an invalid code set ID");
        }
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeSetBySetID(setID);
        
    }
    
    /**
     * Factory method for CodeSet objects
     * @return the object, with no injected values
     */
    public CodeSet getCodeSetSkeleton(){
        
        return new CodeSet();
    }
    
    
    /**
     * Logic intermediary for insertion of new code sets
     * @param set with the variables set
     * @return the PK of the new set
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertCodeSet(CodeSet set) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.insertCodeSetMetadata(set);
        
    }
    
    
    /**
     * Logic intermediary for updates to CodeSet objects
     * @param set 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateCodeSetMetadata(CodeSet set) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        ci.updateCodeSetMetadata(set);
        
        
    }
    
    /**
     * Maps a municipality to a given code set, making it the muni's active set
     * @param set
     * @param muni 
     */
    public void activateCodeSetAsMuniDefault(CodeSet set, Municipality muni) throws BObStatusException, IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mi.mapCodeSetAsMuniDefault(set, muni);
        
        
    }
    
    /**
     * Logic Intermediary for deactivating a code set; checks to make sure
     * this code set is not the default for any muni
     * @param set 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void deactivateCodeSet(CodeSet set) throws IntegrationException, BObStatusException{
        CodeIntegrator ci = getCodeIntegrator();
        HashMap<Municipality, CodeSet> muniSetMap = ci.getMuniDefaultCodeSetMap();
        if(muniSetMap.containsValue(set)){

            throw new BObStatusException("Cannot deactivate a code set which is a default in ANY muni");
        } else {
            ci.deactivateCodeSet(set);
        }
        
    }
    
    /**
     * Extracts a mapping of municipalities and their default code sets 
     * @return
     * @throws IntegrationException 
     */
    public Map<Municipality, CodeSet> getMuniCodeSetDefaultMap() throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        
        try {
            return ci.getMuniDefaultCodeSetMap();
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        
        
    }
   
    
    /**
     * Extracts a complete list of code sets from DB for configuration
     * @return
     * @throws IntegrationException 
     */
    public List<CodeSet> getCodeSetListComplete() throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        List<CodeSet> setList;
        try {
            setList = ci.getCodeSets();
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        return setList;
        
    }
    
  
    /**
     * Extracts CodeSets by muni
     * @param muniCode
     * @return 
     */
    public List<CodeSet> getCodeSetsFromMuniID(int muniCode) {
        
        CodeIntegrator integrator = getCodeIntegrator();
        
        try {
            return integrator.getCodeSets(muniCode);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex.toString());
        }
        
        return new ArrayList<>();
        
    }
    /**
     * Factory method for ECE skeletons
     * @param ele will be injected into ECE constructor if not null
     * @return the skeleton, possibly with injected Element, if passed in
     * and default values hard-coded into coordinator injected
     */
    public EnforcableCodeElement getEnforcableCodeElementSkeleton(CodeElement ele){
        EnforcableCodeElement ece = null;
        if(ele != null){
            ece = new EnforcableCodeElement(ele);
        } else {
            ece = new EnforcableCodeElement();
        }
        
        ece.setMaxPenalty(ECE_DEFAULT_PENALTY_MAX);
        ece.setNormPenalty(ECE_DEFAULT_PENALTY_NORM);
        ece.setMinPenalty(ECE_DEFAULT_PENALTY_MIN);
        
        ece.setNormDaysToComply(ECE_DEFAULT_DAYS_TO_COMPLY);
        ece.setDaysToComplyNotes(ECE_DEFAULT_DAYSTOCOMPLY_NOTES);
        
        return ece;
    }
    
 
    /**
     * Logic pass through for insertion of an ECE
     * 
     * @param ece to insert
     * @param cs to which the ECE should be linked
     * @param ua doing the insertion
     * @return ID of the fresh object in DB
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public int insertEnforcableCodeElement(EnforcableCodeElement ece, 
            CodeSet cs, 
            UserAuthorized ua,
            EnforcableCodeElement dece) 
            throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ece == null || ua == null || cs == null){
            throw new BObStatusException("Cannot insert ECE with null ECE or User");
        }
        ece.setCodeSetID(cs.getCodeSetID());
        //Take skeleton enforceable values and inject them into defaults before adding to database
        ece.setMaxPenalty(dece.getMaxPenalty());
        ece.setMinPenalty(dece.getMinPenalty());
        ece.setNormPenalty(dece.getNormPenalty());
        ece.setPenaltyNotes(dece.getPenaltyNotes());
        ece.setNormDaysToComply(dece.getNormDaysToComply());
        ece.setDaysToComplyNotes(dece.getDaysToComplyNotes());
        ece.setMuniSpecificNotes(dece.getMuniSpecificNotes());
        ece.setDefaultViolationSeverity(dece.getDefaultViolationSeverity());
        ece.setFeeList(dece.getFeeList());
        ece.setDefaultViolationDescription(dece.getDefaultViolationDescription());
        
       
        
        
       
        
        ece.setEceCreatedBy(ua);
        ece.setEceLastupdatedBy(ua);
        return ci.insertEnforcableCodeElementToCodeSet(ece);
        
    }
    
    /**
     * Logic pass through for updates to an ECE
     * 
     * @param ece with updated fields populated
     * @param ua doing the updating
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateEnforcableCodeElement(EnforcableCodeElement ece, UserAuthorized ua) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ece == null || ua == null){
            throw new BObStatusException("Cannot update ECE with null ECE or UA");
        }
        
        
        ece.setEceLastupdatedBy(ua);
        ci.updateEnforcableCodeElement(ece);
        
    }
    
    
      /**
     * Internal operational code to determine which muni's the user has
     * enforcement official permissions (or better), and update their codebooks
     * that contain this same ordinance with default findings
     *
     * @param defFindings the new string to become the default findings
     * @param ece into which I'll inject the new text before updating
     * @param ua
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void updateEnfCodeElementMakeFindingsDefault(String defFindings, EnforcableCodeElement ece, UserAuthorized ua) throws IntegrationException, BObStatusException {
        if(defFindings == null || ece == null || ua == null){
            throw new BObStatusException("Cannot update findings with null inputs");
        }
        System.out.println("CodeCoordinator.updateEnfCodeElementMakeFindingsDefault | updating ECE ID: " + ece.getCodeSetElementID() + " findings to " + defFindings);
        CodeIntegrator ci = getCodeIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        ece.setDefaultViolationDescription(defFindings);
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(ua);
        mbp.setExistingContent(ece.getNotes());

        StringBuilder sb = new StringBuilder();
        sb.append("Default findings changed to: ");
        sb.append(defFindings);
        sb.append(" from: ");
        sb.append(ece.getDefaultViolationDescription());
        mbp.setNewMessageContent(sb.toString());

        ece.setMuniSpecificNotes(sc.appendNoteBlock(mbp));
        ece.setDefaultViolationDescription(defFindings);
        ece.setLastupdatedBy(ua);
        ci.updateEnforcableCodeElement(ece);

    }
    
    /**
     * Logic pass through for deactivation of ECEs
     * 
     * @param ece to deactivate
     * @param ua doing the deactivation
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void deactivateEnforcableCodeElement(EnforcableCodeElement ece, UserAuthorized ua) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ece == null || ua == null){
            throw new BObStatusException("Cannot deactivate an ECE with null ECE or UA");
        }
        
        ece.setEceDeactivatedBy(ua);
        System.out.println("CodeCoordinator.deacECE");
        ci.deactivateEnforcableCodeElement(ece);
        
    }
    
    
    /**
     * Logic passthrough for retrieving ECEs
     * @param eceID
     * @return
     * @throws IntegrationException 
     */
    public EnforcableCodeElement getEnforcableCodeElement(int eceID) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        try {
            return ci.getEnforcableCodeElement(eceID);
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        
    }
    
  
    
    /**
     * Extracts ECEs in a code set (code book)
     * @param setID
     * @return 
     */
    public List<EnforcableCodeElement> getCodeElementsFromCodeSetID(int setID){
        
        CodeIntegrator integrator = getCodeIntegrator();
        
        try {
            return integrator.getEnforcableCodeElementList(setID);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex.toString());
        }
        return new ArrayList<>();
    }
    
    
    
    // *************************************************************
    // ************************ CODE GUIDE *************************
    // *************************************************************
    
    
    /**
     * Factory method for guide entry skeletons (i.e. ID = 0)
     * @return 
     */
    public CodeElementGuideEntry getCodeElementGuideEntrySkeleton(){
        return new CodeElementGuideEntry();
    }
    
    /**
     * Logic intermediary for CodeElementGuideEntry object inserts
     * @param cege
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void insertCodeElementGuideEntry(CodeElementGuideEntry cege) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(cege == null){
            throw new BObStatusException("Cannot insert null guide entry");
        }
        ci.insertCodeElementGuideEntry(cege);
    }
    
    
    /**
     * Logic intermediary for CodeElementGuideEntry objects
     * @param cegeid
     * @return
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public CodeElementGuideEntry getCodeElementGuideEntry(int cegeid) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(cegeid == 0){
            throw new BObStatusException("Cannot get entry with id Zero");
        }
        return ci.getCodeElementGuideEntry(cegeid);
        
    }
    
    /**
     * Extracts al code guide entries from the DB
     * @return
     * @throws IntegrationException 
     */
    public List<CodeElementGuideEntry> getCodeElementGuideEntryListComplete() throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeElementGuideEntries();
    }
    
    /**
     * Logic intermediary for updates to CodeGuideEntry objects
     * @param cege
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateCodeElementGuideEntry(CodeElementGuideEntry cege) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(cege == null){
            throw new BObStatusException("Cannot update entry with null input");
        }
        ci.updateCodeElementGuideEntry(cege);
        
    }
    
    /**
     * Convenience method for linking a batch of CodeElements to a single guide entry.
     * I iterate and call linkCodeElementToGuideEntry
     * @param cel
     * @param cege 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void linkCodeElementListToCodeGuideEntry(List<CodeElement> cel, CodeElementGuideEntry cege) throws BObStatusException, IntegrationException{
        if(cel == null || cel.isEmpty() || cege == null){
            throw new BObStatusException("Cannot link code elements to guide with null or empty list or null guide entry");
        }
        for(CodeElement ce: cel){
            ce.setGuideEntry(cege);
            linkCodeElementToGuideEntry(ce);
        }
        
    }
    
    /**
     * Iterates over teh list of CodeElements; if they have a non-null
     * code guide entry, write that to the DB. If null, remove any link
     * @param eleList 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateAllCodeGuideEntryLinks(List<CodeElement> eleList) throws BObStatusException, IntegrationException{
        if(eleList == null || eleList.isEmpty()){
            throw new BObStatusException("Cannot update code guide entries with null or empty element list");
        }
        for(CodeElement ele: eleList){
            linkCodeElementToGuideEntry(ele);
        }
    }
    
    
    /**
     * LInks or unlinks CodeElement to guide entry. 
     * @param ce
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void linkCodeElementToGuideEntry(CodeElement ce) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ce == null ){
            throw new BObStatusException("Cannot link code guide to element with null element or entry");
        }
        
        ci.linkElementToCodeGuideEntry(ce);
        
    }
    
    /**
     * Logic container for removing a code guide entry
     * @param cege 
     */
    public void removeCodeElementGuideEntry(CodeElementGuideEntry cege) throws BObStatusException, IntegrationException{
        if(cege == null){
            throw new BObStatusException("Cannot remove a null guide entry");
        }
            
        CodeIntegrator ci = getCodeIntegrator();
        ci.deleteCodeElementGuideEntry(cege);
        
    }

    public void insertEnforcableCodeElement(EnforcableCodeElement ece, CodeSet currentCodeSet, UserAuthorized sessUser) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
