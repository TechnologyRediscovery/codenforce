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
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.integration.LogIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.DateTimeUtil;
import com.tcvcog.tcvce.util.LogEntry;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 * Logic holder for all sorts of misc cross-object
 * and system objects
 * 
 * @author ellen bascomb of apt 31y
 */
public class SystemCoordinator extends BackingBeanUtils implements Serializable {

    private final RoleType MIN_RANK_TO_APPEND_NOTES = RoleType.MuniReader;
    private Map<Integer, String> muniCodeNameMap;
    

    /**
     * Creates a new instance of LoggingCoordinator
     */
    public SystemCoordinator() {
    }

    @PostConstruct
    public void initBean() {

    }

    /**
     * Logic pass through method for calls to the system integrator which will
     * make database inserts recording any exploration of one of our BObs
     *
     * @param u
     * @param ob
     * @throws IntegrationException
     */
    public void logObjectView(User u, IFace_Loggable ob) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        si.logObjectView(u, ob);

    }

    /**
     * Central access point for writing all LogEntry objects to the DB
     *
     * @param entry
     * @return
     */
    public int makeLogEntry(LogEntry entry) {
        LogIntegrator li = getLogIntegrator();
        return li.writeLogEntry(entry);
    }

    /**
     * Generates a rich-text (contains HTML to be NOT escaped) given various
     * input values
     *
     * @param objectID
     * @param BObName
     * @param formerVal
     * @param updatedVal
     * @return
     */
    public String generateFieldUpdateNoteBody(int objectID,
            String BObName,
            String formerVal,
            String updatedVal) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append("FIELD UPDATE");
        sb.append(" of ");
        sb.append(BObName);
        sb.append(" ID: ");
        sb.append(objectID);
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append("Prev val: ");
        sb.append(formerVal);
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append("New val: ");
        sb.append(updatedVal);
        sb.append(Constants.FMT_HTML_BREAK);
        return sb.toString();

    }

    /**
     * Skeleton of a system that may be needed to generate and release carefully
     * some level of "internal guest" level access Credential
     *
     * @param ua
     */
    protected void requestBaseInternalAccessCredential(UserAuthorized ua) {
        // TODO: Finish guts
    }
    
    /**
     * Iterates through all values in LinkedObjectSchemaEnum and returns
     * only those that are of the inputted family (e.g. human links or address links)
     * @param fam not null
     * @return a list, perhaps with instances of LinkedObjectFamilyEnum
     * @throws BObStatusException for null input
     */
    public List<LinkedObjectSchemaEnum> assembleLinkedObjectSchemaEnumListByFamily(LinkedObjectFamilyEnum fam) throws BObStatusException{
        if(fam == null){
            throw new BObStatusException("Cannot assemble linked object schema list with null family");
        }
        
        List<LinkedObjectSchemaEnum> rawSchemaList = new ArrayList<>();
        List<LinkedObjectSchemaEnum> chosenSchemas = new ArrayList<>();
        rawSchemaList.addAll(Arrays.asList(LinkedObjectSchemaEnum.values()));
        if(!rawSchemaList.isEmpty()){
            for(LinkedObjectSchemaEnum lose: rawSchemaList){
                if(lose.getLinkedObjectFamilyEnum() == fam){
                    chosenSchemas.add(lose);
                }
            }
        }
        return chosenSchemas;
    }
    
    
    
    /**
     * Coordinates the creation of all LinkedObjectRoles given a link schema enum instance
     * @param lose which set of roles you want
     * @return a list, possibly containing LinkedObjectRoles ; never null
     */
    public List<LinkedObjectRole> assembleLinkedObjectRolesBySchema(LinkedObjectSchemaEnum lose) throws IntegrationException, BObStatusException{
        List<LinkedObjectRole> roleList = new ArrayList<>();
        SystemIntegrator si = getSystemIntegrator();
        
        List<Integer> idl = si.getLinkedObjectRoleListBySchemaFamily(lose);
        if(idl != null && !idl.isEmpty()){
            for(Integer i: idl){
                roleList.add(si.getLinkedObjectRole(i));
            }
        }
        return roleList;
    }

    /**
     * The official note appending tool of the entire codeNforce system!
     * Consider all other appendNoteXXX methods scattered about to be rogue
     * agents, operating without warrant, independent of any meaningful
     * standards or oversight
     *
     * @param mbp containing as much information as possible which will be
     * formatted into a nice note block
     * @return the exact text of the new note in HTML, with any previous text
     * included in the message builder params object post-pended to the incoming
     * note
     */
    public String appendNoteBlock(MessageBuilderParams mbp) {
        StringBuilder sb = new StringBuilder();
        if (mbp == null) {
            return sb.toString();
        }

        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_NOTE_START);

        // TITLE
        if (mbp.getHeader() != null) {
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(mbp.getHeader());
        }

        if (mbp.getExplanation() != null) {
            sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
            sb.append(mbp.getExplanation());
        }
        
        // NOTE content
         if (mbp.getNewMessageContent() != null) {
            sb.append(Constants.FMT_HTML_BREAK);
            // don't prepend the characters "Content: "!!!
//            sb.append(Constants.FMT_CONTENT);
            sb.append(mbp.getNewMessageContent());
        }
         
        // CREATOR INFO
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_NOTEBYLINE);
        if (mbp.getUser() != null) {

            if (mbp.getUser().getHuman() != null) {
                sb.append(mbp.getUser().getHuman().getName());
                sb.append(Constants.FMT_SPACE_LITERAL);
            }
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(Constants.FMT_USER);
            sb.append(mbp.getUser().getUsername());
            sb.append(Constants.FMT_DTYPE_OBJECTID_INLINEOPEN);
            sb.append(Constants.FMT_ID);
            sb.append(mbp.getUser().getUserID());
            sb.append(Constants.FMT_DTYPE_OBJECTID_INLINECLOSED);
        }
        sb.append(Constants.FMT_AT);
        sb.append(stampCurrentTimeForNote());

        if (mbp.getCred() != null && mbp.isIncludeCredentialSig()) {
            sb.append(Constants.FMT_SIGNATURELEAD);
            sb.append(mbp.getCred().getSignature());
        }

        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_NOTE_END);
        sb.append(Constants.FMT_HTML_BREAK);

        if (mbp.getExistingContent() != null) {
            sb.append(mbp.getExistingContent());
        }
        return sb.toString();
    }
    
    /**
     * Logic pass through for note writing using the standardized interface
     * 
     * @param nh Caller must prepare the note holder with all the correct content
     * of its note field so this method and the integrator can just yank and go
     * @param ua doing the noting
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void writeNotes(IFace_noteHolder nh, UserAuthorized ua) throws IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
        if(nh == null || ua == null || nh.getDBKey() == 0){
            throw new BObStatusException("Cannot append notes with null notes, user, or PK of 0");
        }
        // enforce minimum permissions
        if(ua.getKeyCard().getGoverningAuthPeriod().getRole().getRank() >= MIN_RANK_TO_APPEND_NOTES.getRank() ){
            si.writeNotes(nh);
        }
        
    }
    
    /**
     * Utility for getting patch table to UI
     * @return the Patch IDs
     */
    public String getDBPatchIDList() throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        return si.getDatabasePatchRecord();
    }

    /**
     * Utility method for creating a string of the current date
     *
     * @return
     */
    public String stampCurrentTimeForNote() {
        return DateTimeUtil.getPrettyDate(LocalDateTime.now());
    }

    /**
     * Assembles an intensity schema with only active classes inside
     *
     * @param schemaName
     * @return
     * @throws IntegrationException
     */
    public IntensitySchema getIntensitySchemaWithClasses(String schemaName) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        IntensitySchema is = new IntensitySchema(schemaName);
        List<IntensityClass> classList = si.getIntensityClassList(schemaName);
        List<IntensityClass> classListFinal = new ArrayList<>();
        if (classList != null && !classList.isEmpty()) {
            for (IntensityClass ic : classList) {
                if (ic.isActive()) {
                    classListFinal.add(ic);

                }
            }
        }
        if (!classListFinal.isEmpty()) {
            Collections.sort(classListFinal);
        }
        is.setClassList(classListFinal);
        return is;
    }
    
    /**
     * Creates a random rbg color in the form
     * rgb(xxx,xxx,xxx)
     * @return 
     */
    public String generateRandomRGBColor(){
         
        Random r = new Random();
        int red = r.nextInt(256);
            int green = r.nextInt(256);
            int blue = r.nextInt(256);
            StringBuilder sb = new StringBuilder();
            sb.append("rgb(");
            sb.append(red);
            sb.append(",");
            sb.append(green);
            sb.append(",");
            sb.append(blue);
            sb.append(")");
            return sb.toString();
        
        
    }
    

    /**
     * Builds a complete list of BOb sources for drop downs
     *
     * @return
     */
    public List<BOBSource> getBobSourceListComplete() {
        List<BOBSource> sourceList = new ArrayList<>();
        SystemIntegrator si = getSystemIntegrator();
        List<Integer> idl = null;
        try {
            idl = si.getBobSourceListComplete();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        if (idl != null && !idl.isEmpty()) {
            for (Integer i : idl) {
                try {
                    BOBSource s = si.getBOBSource(i);
                    if(s.isUserattributable()){
                        sourceList.add(s);
                    }
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            }
        }
        return sourceList;
    }
    
    
    /**
     * Logic pass through for bob source objects
     * @param sourceid
     * @return
     * @throws IntegrationException 
     */
   public BOBSource getBObSource(int sourceid) throws IntegrationException{
       if(sourceid==0){
           return null;
       }
       SystemIntegrator si = getSystemIntegrator();
       
       return si.getBOBSource(sourceid);
       
   }
    

    /**
     * Adapter method for taking in simple note info, not in Object format and
     * creating the populated MessageBuilderParams instance required by the
     * official note appending tool of the entire codeNforce system
     *
     * @param u
     * @param noteToAppend
     * @param existingText
     * @return
     */
    public String formatAndAppendNote(User u, String noteToAppend, String existingText) {
        MessageBuilderParams mbp = new MessageBuilderParams(existingText,
                noteToAppend,
                null,
                null,
                u,
                null);
        return appendNoteBlock(mbp);

    }

    /**
     * Adapter method for taking in simple note info, not in Object format and
     * creating the populated MessageBuilderParams instance required by the
     * official note appending tool of the entire codeNforce system
     *
     * @param u
     * @param cred
     * @param noteToAppend
     * @param existingText
     * @return
     */
    public String formatAndAppendNote(User u, Credential cred, String noteToAppend, String existingText) {
        MessageBuilderParams mbp = new MessageBuilderParams(existingText,
                noteToAppend,
                null,
                null,
                u,
                cred);

        return appendNoteBlock(mbp);

    }

    /**
     * @return the muniCodeNameMap
     */
    public Map<Integer, String> getMuniCodeNameMap() {
        if (muniCodeNameMap == null) {

            Map<Integer, String> m = null;
            MunicipalityIntegrator mi = getMunicipalityIntegrator();
            try {
                m = mi.getMunicipalityMap();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            muniCodeNameMap = m;
        }
        return muniCodeNameMap;
    }


    /**
     * @param muniCodeNameMap the muniCodeNameMap to set
     */
    public void setMuniCodeNameMap(Map<Integer, String> muniCodeNameMap) {
        this.muniCodeNameMap = muniCodeNameMap;
    }

    //xiaohong add
    //Store SubNav Items into List: Dashboard
    public List<NavigationSubItem> getDashboardNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        return navList;
    }

    /*
     * Note that these sub-items do not contain a page URL yet, 
     * since the sub-pages had not been created at the time of Xiaohong creating
     * the nav system.
     */
    //Nav Bar
    //Sub NavItem: Property
    private final NavigationSubItem propertySearch = getNavSubItem("Search", "/restricted/cogstaff/prop/propertySearchProfile.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem propertyInfo = getNavSubItem("Info", "/restricted/cogstaff/prop/propertySearchProfile.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem propertyUnits = getNavSubItem("Units", "/restricted/cogstaff/prop/propertyUnits.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem propertyUnitChanges = getNavSubItem("Unit Changes", "/restricted/cogstaff/prop/propertyUnitChanges.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Property
    public List<NavigationSubItem> getPropertyNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(propertySearch);
        navList.add(propertyInfo);
        navList.add(propertyUnits);
        navList.add(propertyUnitChanges);
        return navList;
    }

    //Sub NavItem: Code Enf
    private final NavigationSubItem CECaseSearch = getNavSubItem("Search for Cases", "/restricted/cogstaff/ce/ceCaseSearch.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem CECaseProfile = getNavSubItem("Case Profile", "/restricted/cogstaff/ce/ceCaseProfile.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem CEViolations = getNavSubItem("Violations", "/restricted/cogstaff/ce/ceCaseViolations.xhtml", "fa fa-sign-in", false);
    // removed during cecase page collapse
//    private final NavigationSubItem CENotices = getNavSubItem("Notices", "/restricted/cogstaff/ce/ceCaseNotices.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem CECitations = getNavSubItem("Citations", "/restricted/cogstaff/ce/ceCaseCitations.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem CERequests = getNavSubItem("Requests", "/restricted/cogstaff/ce/ceActionRequests.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Code Enf
    public List<NavigationSubItem> getCENavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(CECaseSearch);
        navList.add(CECaseProfile);
        navList.add(CEViolations);
//        navList.add(CENotices);
        navList.add(CECitations);
        navList.add(CERequests);
        return navList;
    }

    //Sub NavItem: Occupancy
    private final NavigationSubItem occPeriodStatus = getNavSubItem("Period Status", "/restricted/cogstaff/occ/occPeriodWorkflow.xhtml", "fa fa-sign-in", true);
    private final NavigationSubItem occPermits = getNavSubItem("Permits", "/restricted/cogstaff/occ/occPeriodPermits.xhtml", "fa fa-sign-in", true);
    private final NavigationSubItem occPermitApplications = getNavSubItem("Permit Applications", "/restricted/cogstaff/occ/occPermitApplications.xhtml", "fa fa-sign-in", true);
    private final NavigationSubItem occInspections = getNavSubItem("Inspections", "/restricted/cogstaff/occ/occPeriodInspections.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem occDocuments = getNavSubItem("Payments", "/restricted/cogstaff/occ/occPeriodPayments.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Occupancy
    public List<NavigationSubItem> getOccNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(occPeriodStatus);
        navList.add(occPermits);
        navList.add(occPermitApplications);
        navList.add(occInspections);
        navList.add(occDocuments);
        return navList;
    }

    //Sub NavItem: Persons
    // listed in file order
    private final NavigationSubItem personCECases = getNavSubItem("CE Cases", "/restricted/cogstaff/person/personCECases.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem personChanges = getNavSubItem("Changes", "/restricted/cogstaff/person/personChanges.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem personInfo = getNavSubItem("Info", "/restricted/cogstaff/person/personInfo.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem personOccPeriods = getNavSubItem("Occ periods", "/restricted/cogstaff/person/personOccPeriods.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem personProperties = getNavSubItem("Properties", "/restricted/cogstaff/person/personProperties.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem personPublic = getNavSubItem("Public", "/restricted/cogstaff/person/personPublic.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem personSearch = getNavSubItem("Search", "/restricted/cogstaff/person/personSearch.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Person
    // listed in display order
    public List<NavigationSubItem> getPersonNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(personSearch);
        navList.add(personInfo);
        navList.add(personProperties);
        navList.add(personOccPeriods);
        navList.add(personCECases);
        navList.add(personPublic);
        navList.add(personChanges);
        return navList;
    }

    //Sub NavItem: Code
    private final NavigationSubItem codeSources = getNavSubItem("Sources", "/restricted/cogstaff/code/codeSourceManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem codeDetails = getNavSubItem("Details", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Code
    public List<NavigationSubItem> getCodeNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(codeSources);
        navList.add(codeDetails);
        return navList;
    }

    public List<NavigationItem> navList() {

        ArrayList<NavigationItem> navList;
        navList = new ArrayList<>();
        try {
            //NavItem: Dashboard
            NavigationItem dashboardItem = getNavItem("/restricted/missionControl.xhtml", "Dashboard", "fa fa-dashboard", getDashboardNavList());
            //NavItem: Property
            NavigationItem propertyItem = getNavItem("/restricted/cogstaff/prop/propertySearchProfile.xhtml", "Property", "fa fa-home", getPropertyNavList());
            //NavItem: Code Enf
            NavigationItem CEItem = getNavItem("/restricted/cogstaff/ce/ceCaseSearchProfile.xhtml", "Code Enf", "fa fa-balance-scale", getCENavList());
            //NavItem: Occupancy
            NavigationItem occItem = getNavItem("/restricted/cogstaff/occ/occPeriodSearch.xhtml", "Occupancy", "fa fa-list-alt", getOccNavList());
            //NavItem: Persons
            NavigationItem personItem = getNavItem("/restricted/cogstaff/person/personSearch.xhtml", "Person", "fa fa-female", getPersonNavList());
            //NavItem: Code
            NavigationItem codeItem = getNavItem("/restricted/cogstaff/code/codeSourceManage.xhtml", "Code", "fa fa-book", getCodeNavList());

            navList.add(dashboardItem);
            navList.add(propertyItem);
            navList.add(personItem);
            navList.add(occItem);
//            navList.add(CEItem);
            navList.add(codeItem);
        } catch (Exception e) {
            System.out.println(e);
        }
        return navList;

    }

    //Side Tool Bar
    //Sidebar Sub Nav Item: Municipal Code
    private final NavigationSubItem codeSource = getNavSubItem("Ordinances", "/restricted/cogstaff/code/codeElementManage.xhtml", "fa fa-university", false);
    private final NavigationSubItem codeBook = getNavSubItem("Code Books", "/restricted/cogstaff/code/codeSetManage.xhtml", "fa fa-gavel", false);

    //Store SubNav Items into List:Code
    public List<NavigationSubItem> getSidebarCodeConfigList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(codeSource);
        navList.add(codeBook);
        return navList;
    }

    //Sidebar Sub Nav Item: CE
    private final NavigationSubItem caseEvent = getNavSubItem("Case Event", "/restricted/cogstaff/ce/eventConfiguration.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem courtEntity = getNavSubItem("Court Entity", "/restricted/cogstaff/ce/courtEntityManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem notice = getNavSubItem("Notice", "/restricted/cogstaff/ce/textBlockManage.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List:Code
    public List<NavigationSubItem> getSidebarCEConfigList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(caseEvent);
        navList.add(courtEntity);
        navList.add(notice);
        return navList;
    }

    private final NavigationSubItem feeManage = getNavSubItem("Fees", "/restricted/cogstaff/occ/feeManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem feeTypeManage = getNavSubItem("Fee types", "/restricted/cogstaff/occ/feeTypeManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem feePermissions = getNavSubItem("Occ Fees", "/restricted/cogstaff/occ/feePermissions", "fa fa-sign-in", false);

    //Store SubNav Items into List:Payment
    public List<NavigationSubItem> getSidebarPaymentList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(feeManage);
        navList.add(feeTypeManage);
        navList.add(feePermissions);
        return navList;
    }

    //Sidebar Sub Nav Item: Occ
    private final NavigationSubItem checklist = getNavSubItem("Checklist", "/restricted/cogstaff/occ/checklists.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem payment = getNavSubItem("Payment", "/restricted/cogstaff/occ/checklists.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem feeType = getNavSubItem("Fee Type", "/restricted/cogstaff/occ/paymentManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem permitType = getNavSubItem("Permit Type", "/restricted/cogstaff/occ/occPermitTypeManage.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List:Code
    public List<NavigationSubItem> getSidebarOccConfigList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(checklist);
        navList.add(payment);
        navList.add(feeType);
        navList.add(permitType);
        return navList;
    }

    //Sidebar Sub Nav Item: Reports
    private final NavigationSubItem events = getNavSubItem("Event Activity", "/restricted/cogstaff/event/events.xhtml", "fa fa-flag", false);
    private final NavigationSubItem eventConfig = getNavSubItem("Event Setup", "/restricted/cogstaff/event/eventConfiguration.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem eventRuleConfig = getNavSubItem("Event Rules", "/restricted/cogstaff/event/eventRuleConfiguration.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Reports
    public List<NavigationSubItem> getSidebarReportList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(events);
        navList.add(eventConfig);
        navList.add(eventRuleConfig);
        return navList;
    }

    //Sidebar Sub Nav Item: System
    private final NavigationSubItem users = getNavSubItem("Users", "/restricted/cogadmin/userConfig.xhtml", "fa fa-user-o", false);
    private final NavigationSubItem icons = getNavSubItem("Icons", "/restricted/cogadmin/iconManage.xhtml", "fa fa-rebel", false);
    private final NavigationSubItem blobs = getNavSubItem("Files", "/restricted/cogadmin/manageBlob.xhtml", "fa fa-folder", false);

    //Store SubNav Items into List: Reports
    public List<NavigationSubItem> getSidebarSystemList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(users);
        navList.add(icons);
        navList.add(blobs);
        return navList;
    }

    //Sidebar Sub Nav Item: Help
    private final NavigationSubItem howto = getNavSubItem("How-To", "/public/system/documentation/howtos/howtos.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Help
    public List<NavigationSubItem> getSidebarHelpList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(howto);
        return navList;
    }

    public List<NavigationItem> sideBarNavList() {

        ArrayList<NavigationItem> navList;
        navList = new ArrayList<>();
        // note: no page URLs are stored for side meaning - meanng side bar items don't 
        // get selected automatically based on current page
        // and are not related to a currently loaded object
        try {
            //NavItem: CE
            NavigationItem CEconfigItem = getNavItem("", "Code Enforcement", "fa fa-balance-scale", getSidebarCEConfigList());
            //NavItem: Occ
            NavigationItem OccconfigItem = getNavItem("", "Occupancy", "fa fa-list-alt", getSidebarOccConfigList());
            //NavItem: Code
            NavigationItem codeconfigItem = getNavItem("", "Municipal Code", "fa fa-book", getSidebarCodeConfigList());
            //NavItem: System
            NavigationItem reportItem = getNavItem("", "Report", "fa fa-bullhorn", getSidebarReportList());
            //NavItem: Payments
            NavigationItem paymentsItem = getNavItem("", "Payments", "fa fa-cogs", getSidebarPaymentList());
            //NavItem: Reports
            NavigationItem systemItem = getNavItem("", "System", "fa fa-cogs", getSidebarSystemList());
            //NavItem: Help
            NavigationItem helpItem = getNavItem("", "Help", "fa fa-question-circle", getSidebarHelpList());

            navList.add(CEconfigItem);
            navList.add(OccconfigItem);
            navList.add(codeconfigItem);
            navList.add(reportItem);
            navList.add(paymentsItem);
            navList.add(systemItem);
            navList.add(helpItem);

        } catch (Exception e) {
            System.out.println(e);

        }
        return navList;

    }

    public NavigationSubItem getNavSubItem(String value, String path, String icon, boolean disable) {
        NavigationSubItem mn = new NavigationSubItem();
        mn.setValue(value);
        mn.setPagePath(path);
        mn.setIcon(icon);
        mn.setDisable(disable);
        return mn;
    }

    public NavigationItem getNavItem(String searchPageUrl, String navCategory, String icon, List navSubList) {
        NavigationItem ni = new NavigationItem();
        ni.setValue(navCategory);
        ni.setIcon(icon);
        ni.setSubNavitem(navSubList);
        ni.setSearchpageurl(searchPageUrl);
        return ni;
    }

    public ArrayList<PrintStyle> getPrintStyleList() throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        return si.getPrintStyle();
    }

    public PrintStyle getPrintStyle(int styleid) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        return si.getPrintStyle(styleid);
    }

}
