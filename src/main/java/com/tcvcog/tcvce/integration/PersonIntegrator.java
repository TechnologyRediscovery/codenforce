/*
 * Copyright (C) 2017 ellen bascomb of apt 31y
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
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonChangeOrder;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.PersonWithChanges;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Connects Person objects to the data store
 *
 * @author ellen bascomb of apt 31y
 */
public class PersonIntegrator extends BackingBeanUtils implements Serializable {

    final String ACTIVE_FIELD = "person.isactive";
    /**
     * Creates a new instance of PersonIntegrator
     */
    public PersonIntegrator() {
    }

    /**
     * Looks up a person given a personID and creates a returns a new instance
 of Person with all the available information loaded about that person
     *
     * @param personId the id of the person to look up
     * @return a Person object with all of the available data loaded
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Person getPerson(int personId) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Person person = null;

        try {
            
            String s = "SELECT personid, persontype, muni_municode, fname, lname, jobtitle, \n" +
                        "       phonecell, phonehome, phonework, email, address_street, address_city, \n" +
                        "       address_state, address_zip, notes, lastupdated, expirydate, isactive, \n" +
                        "       isunder18, humanverifiedby, compositelname, sourceid, creator, \n" +
                        "       businessentity, mailing_address_street, mailing_address_city, \n" +
                        "       mailing_address_zip, mailing_address_state, useseparatemailingaddr, \n" +
                        "       expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, \n" +
                        "       ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, \n" +
                        "       referenceperson\n" +
                        "  FROM public.person WHERE personid=?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, personId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                person = generatePersonFromResultSet(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return person;
    }

    /**
     * Accepts a result set and creates a new Person object. Note that this
     * method does not move the cursor position on the result set passed in;
     * rather, it will create a person based on the row to which the cursor is
     * pointing when it is passed in. The client method is responsible for
     * managing cursor position
     *
     * Also note that the client method is responsible for closing the result
     * set that is passed to this method with rs.close();
     *
     * @param rs the result set from which to extract data for creating a person
     * @return the new person object created from this single row of the rs
     */
    private Person generatePersonFromResultSet(ResultSet rs) throws IntegrationException {
        // Instantiates the new person object
        Person newPerson = new Person();
        SystemCoordinator ssc = getSystemCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        
        try {
            newPerson.setPersonID(rs.getInt("personid"));
            newPerson.setPersonType(PersonType.valueOf(rs.getString("persontype")));
            newPerson.setMuniCode(rs.getInt("muni_municode"));
            newPerson.setMuni(mi.getMuni(newPerson.getMuniCode()));
            newPerson.setFirstName(rs.getString("fName"));
            newPerson.setLastName(rs.getString("lName"));
            newPerson.setJobTitle(rs.getString("jobtitle"));
            
            newPerson.setPhoneCell(rs.getString("phoneCell"));
            newPerson.setPhoneHome(rs.getString("phoneHome"));
            newPerson.setPhoneWork(rs.getString("phoneWork"));
            newPerson.setEmail(rs.getString("email"));
            newPerson.setAddressStreet(rs.getString("address_street"));
            newPerson.setAddressCity(rs.getString("address_city"));

            newPerson.setAddressState(rs.getString("address_state"));
            newPerson.setAddressZip(rs.getString("address_zip"));
            newPerson.setNotes(rs.getString("notes"));

            if(rs.getTimestamp("lastupdated") != null){
                newPerson.setLastUpdated(rs.getTimestamp("lastupdated").toLocalDateTime());
            }
            
            if (rs.getTimestamp("expirydate") != null) {
                newPerson.setExpiryDate(rs.getTimestamp("expirydate").toLocalDateTime());
            }
            newPerson.setActive(rs.getBoolean("isactive"));

            newPerson.setUnder18(rs.getBoolean("isunder18"));
            newPerson.setVerifiedByUserID(rs.getInt("humanverifiedby"));
            newPerson.setCompositeLastName(rs.getBoolean("compositelname"));
            newPerson.setSource(si.getBOBSource(rs.getInt("sourceid")));
            
            // avoiding cycles here by not storing the object
            newPerson.setCreatorUserID(rs.getInt("creator"));
            
            newPerson.setBusinessEntity(rs.getBoolean("businessentity"));
            newPerson.setMailingAddressStreet(rs.getString("mailing_address_street"));
            newPerson.setMailingAddressCity(rs.getString("mailing_address_city"));
            newPerson.setMailingAddressZip(rs.getString("mailing_address_zip"));
            newPerson.setMailingAddressState(rs.getString("mailing_address_state"));
            newPerson.setUseSeparateMailingAddress(rs.getBoolean("useseparatemailingaddr"));

            newPerson.setExpiryNotes(rs.getString("expirynotes"));
            if(!(rs.getTimestamp("creationtimestamp")==null)){
                newPerson.setCreationTimeStamp(rs.getTimestamp("creationtimestamp").toLocalDateTime());
            } else {
                newPerson.setCreationTimeStamp(null);
            }
            newPerson.setCanExpire(rs.getBoolean("canexpire"));
            newPerson.setLinkedUserID(rs.getInt("userlink"));
            newPerson.setMailingAddressThirdLine(rs.getString("mailing_address_thirdline"));
            
            // HAVE NOT IMPLEMENTED GHOSTS AND CLONES YET!
            
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating person from ResultSet", ex);
        }
        return newPerson;
    } // close method
    
    /**
     * We have a special type of Person which are those who have been attached to an application
     * or OccPeriod, and this method fetches those connected to an OccPeriod
     * 
     * @param application
     * @return
     * @throws IntegrationException 
     */
    public List<PersonOccApplication> getPersonOccApplicationList(OccPermitApplication application) throws IntegrationException{
        List<PersonOccApplication> personList = new ArrayList<>();
        String selectQuery =  "SELECT person_personid, occpermitapplication_applicationid, applicant, preferredcontact, \n" +
                                "   applicationpersontype, active\n" +
                                "   FROM public.occpermitapplicationperson WHERE occpermitapplication_applicationid=? AND active=true;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, application.getId());
            rs = stmt.executeQuery();
            while(rs.next()){
                personList.add(generatePersonOccPeriod(rs));
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to OccPermitApplication", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return personList;
    }
    
    /**
     * We have a special type of Person which are those who have been attached to an application
     * or OccPeriod, and this method fetches those connected to an OccApplication
     * 
     * @param personID
     * @param applicationID
     * @return
     * @throws IntegrationException 
     */
    public PersonOccApplication getPersonOccApplication(int personID, int applicationID) throws IntegrationException{
        String selectQuery =  "SELECT person_personid, applicant, preferredcontact, \n" +
                                "   applicationpersontype, active\n" +
                                "   FROM public.occpermitapplicationperson "
                              + "WHERE occpermitapplication_applicationid=? AND person_personID=?;";
        PersonOccApplication output = null;
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, applicationID);
            stmt.setInt(2, personID);
            rs = stmt.executeQuery();
            while(rs.next()){
                output = generatePersonOccPeriod(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to OccPermitApplication", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return output;
        
    }
    
    /**
     * Gets a list that includes inactive links
     * 
     * @param application
     * @return
     * @throws IntegrationException 
     */
    public List<PersonOccApplication> getPersonOccApplicationListWithInactive(OccPermitApplication application) throws IntegrationException{
        List<PersonOccApplication> personList = new ArrayList<>();
        String selectQuery =  "SELECT person_personid, occpermitapplication_applicationid, applicant, preferredcontact, \n" +
                                "   applicationpersontype, active\n" +
                                "   FROM public.occpermitapplicationperson WHERE occpermitapplication_applicationid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, application.getId());
            rs = stmt.executeQuery();
            while(rs.next()){
                personList.add(generatePersonOccPeriod(rs));
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get persons connected to OccPermitApplication", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return personList;
    }
    
    private PersonOccApplication generatePersonOccPeriod(ResultSet rs) throws SQLException, IntegrationException{
        PersonOccApplication pop = new PersonOccApplication(getPerson(rs.getInt("person_personid")));
        pop.setApplicationID(rs.getInt("occpermitapplication_applicationid"));
        pop.setApplicant(rs.getBoolean("applicant"));
        pop.setPreferredContact(rs.getBoolean("preferredcontact"));
        pop.setApplicationPersonType(PersonType.valueOf(rs.getString("applicationpersontype")));
        pop.setLinkActive(rs.getBoolean("active"));
        return pop;        
    }   
    
    /**
     * Creates a record in the person-property linking table, after checking that it does not exist
     * @param person
     * @param prop
     * @throws IntegrationException 
     */
    public void connectPersonToProperty(Person person, Property prop) throws IntegrationException {

        String selectQuery = "SELECT property_propertyid, person_personid\n" +
                    "  FROM public.propertyperson WHERE property_propertyid = ? AND person_personid = ?;";

        String query = "INSERT INTO public.propertyperson(\n"
                + " property_propertyid, person_personid)\n"
                + " VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, prop.getPropertyID());
            stmt.setInt(2, person.getPersonID());
            rs = stmt.executeQuery();
            
            if(!rs.first()){
                stmt = con.prepareStatement(query);
                stmt.setInt(1, prop.getPropertyID());
                stmt.setInt(2, person.getPersonID());

                stmt.execute();
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert person and connect to property", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

    }
    /**
     * Creates a record in the person-property linking table, after checking that it does not exist
     * @param person
     * @param prop
     * @throws IntegrationException 
     */
    public void connectRemovePersonToProperty(Person person, Property prop) throws IntegrationException {

        
        String query = "DELETE from propertyperson WHERE property_propertyid=? AND person_personid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getPropertyID());
            stmt.setInt(2, person.getPersonID());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert person and connect to property", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    /**
     * Accepts a Person object to store in the database.
     *
     * @param personToStore the person to store
     * @return the unique id number assigned to the record just created in
     * postgres through a call to currval('[sequence_name]')
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertPerson(Person personToStore) throws IntegrationException {
        int unknownPersonSourceID = Integer.parseInt(getResourceBundle(
                Constants.DB_FIXED_VALUE_BUNDLE).getString("unknownPersonSource"));
        System.out.println("PersonIntegrator.insertPerson");
        Connection con = getPostgresCon();
        System.out.println("PersonIntegrator.insertPerson | after pgcon");
        ResultSet rs = null;
        int lastID;
        
            String query = "INSERT INTO public.person(" +
                    " personid, persontype, muni_municode, " +
                    " fname, lname, jobtitle, " +
                    " phonecell, phonehome, phonework, " +
                    " email, address_street, address_city, " +
                    " address_state, address_zip, notes, " +
                    " lastupdated, expirydate, isactive, " +
                    " isunder18, humanverifiedby, compositelname, " +
                    " sourceid, creator, businessentity, " +
                    " mailing_address_street, mailing_address_city, " +
                    " mailing_address_zip, mailing_address_state, useseparatemailingaddr, " +
                    " expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline) " +
                    " VALUES (DEFAULT, CAST (? AS persontype), ?, " //1-2
                    + "?, ?, ?, " + //3-5, through jobtitle
                    " ?, ?, ?, "
                    + "?, ?, ?, " +
                    " ?, ?, ?, " // through notes
                    + "?, ?, ?, " +
                    " ?, ?, ?," // through 20
                    + "?, ?, ?, " 
                    + "?, ?, " // through 25, mailing_address_city
                    + "?, ?, ?, "
                    + "?, ?, ?, NULL, ?);"; // through 32

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            // default ID generated by sequence in PostGres

            if (personToStore.getPersonType() != null) {
                stmt.setString(1, personToStore.getPersonType().toString());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
                
            }
            stmt.setInt(2, personToStore.getMuniCode());

            stmt.setString(3, personToStore.getFirstName());
            stmt.setString(4, personToStore.getLastName());
            stmt.setString(5, personToStore.getJobTitle());

            stmt.setString(6, personToStore.getPhoneCell());
            stmt.setString(7, personToStore.getPhoneHome());
            stmt.setString(8, personToStore.getPhoneWork());
            
            stmt.setString(9, personToStore.getEmail());
            stmt.setString(10, personToStore.getAddressStreet());
            stmt.setString(11, personToStore.getAddressCity());

            stmt.setString(12, personToStore.getAddressState());
            stmt.setString(13, personToStore.getAddressZip());
            stmt.setString(14, personToStore.getNotes());
            
            // last updated
            stmt.setTimestamp(15, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if (personToStore.getExpiryDate() != null) {
                stmt.setTimestamp(16, java.sql.Timestamp.valueOf(personToStore.getExpiryDate()));

            } else {
                stmt.setNull(16, java.sql.Types.NULL);
            }
            stmt.setBoolean(17, personToStore.isActive());
            
            
            stmt.setBoolean(18, personToStore.isUnder18());
            stmt.setInt(19, personToStore.getVerifiedByUserID());
            stmt.setBoolean(20, personToStore.isCompositeLastName());
            
            
            if(personToStore.getSource() != null){
                stmt.setInt(21, personToStore.getSource().getSourceid());
            } else {
                stmt.setInt(21, unknownPersonSourceID);
            }
            stmt.setInt(22, personToStore.getCreatorUserID());
            stmt.setBoolean(23, personToStore.isBusinessEntity());
            
            stmt.setString(24, personToStore.getMailingAddressStreet());
            stmt.setString(25, personToStore.getMailingAddressCity());
            stmt.setString(26, personToStore.getMailingAddressZip());
            stmt.setString(27, personToStore.getMailingAddressState());
            
            stmt.setBoolean(28, personToStore.isUseSeparateMailingAddress());
            
            stmt.setString(29, personToStore.getExpiryNotes());
            stmt.setTimestamp(30, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(31, personToStore.isCanExpire());
            
//            stmt.setInt(32, personToStore.getLinkedUserID());

            stmt.setString(32, personToStore.getMailingAddressThirdLine());
            
            stmt.execute();

            // now get the most recent Unique ID added to this person to pass 
            // to client methods who want to connect a record to this person
            String idNumQuery = "SELECT currval('person_personIDSeq');";
            Statement s = con.createStatement();
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt("currval");

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        System.out.println("PersonIntegrator.insertPerson  | returned ID " + lastID);
        return lastID;

    } // close insertPerson()

    /**
     * Convenience method for iterating over a List<Person> and calling
     * eventPersonConnect for each one
     * 
     * @param ev
     
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void eventPersonConnect(EventCnF ev) throws IntegrationException, BObStatusException {
        if(ev != null && !ev.getPersonList().isEmpty()){
            
            for(Person p: ev.getPersonList())
                if(!checkForExistingEventPersonConnections(ev, p)){
                    eventPersonConnect(ev, p);
                }
        }
    }
    
    /**
     * Organ for avoiding duplicate connections in eventperson
     * @param ev the event against which to check the Person argument for links
     * @param p the Person to check
     * @return true if there IS an existing connection
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public boolean checkForExistingEventPersonConnections(EventCnF ev, Person p) throws BObStatusException, IntegrationException{
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Person> list = new ArrayList<>();
        if(ev == null || p == null){
            throw new BObStatusException("cannot check for links with null person or event");
        }
        
        try {
            String s = "SELECT ceevent_eventid, person_personid FROM public.eventperson WHERE person_personid=?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, p.getPersonID());

            rs = stmt.executeQuery();

            while (rs.next()) {
                return true;
            }
            return false;

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
    }

    private void eventPersonConnect(EventCnF ev, Person p) throws IntegrationException {

        String query = "INSERT INTO public.eventperson(\n"
                + " ceevent_eventid, person_personid)\n"
                + " VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ev.getEventID());
            stmt.setInt(2, p.getPersonID());

            System.out.println("PersonIntegrator.connectPersonToEvent | sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert person and connect to property", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
   
    /**
     * Drops all event-person connections  from eventperson
     * @param ev 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void eventPersonClear(EventCnF ev) throws IntegrationException{
        String query = "DELETE FROM eventperson WHERE ceevent_eventid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ev.getEventID());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert person and connect to property", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
  
    
    
    
    public void connectPersonToMunicipalities(List<Municipality> munuiList, Person p) throws IntegrationException {

        Iterator<Municipality> iter = munuiList.iterator();
        while (iter.hasNext()) {
            connectPersonToMunicipality( (Municipality) iter.next(), p);
        }
    }
    
    
    public void connectPersonToMunicipality(Municipality munui, Person p) throws IntegrationException {

        String query =  "INSERT INTO public.personmunilink(\n" +
                        "            muni_municode, person_personid)\n" +
                        "    VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, munui.getMuniCode());
            stmt.setInt(2, p.getPersonID());

            System.out.println("PersonIntegrator.connectPersonToMunicipality | sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to connect person to muni ", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    
    public void connectPersonsToCitation(Citation c, List<Person> persList) throws IntegrationException {
        Iterator<Person> iter = persList.iterator();
        while (iter.hasNext()) {
            connectPersonToCitation(c, (Person) iter.next());
        }
    }
    
    
    public void connectPersonToCitation(Citation c, Person p) throws IntegrationException {

        String query =  "INSERT INTO public.citationperson(\n" +
                        "    citation_citationid, person_personid)\n" +
                        "    VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, c.getCitationID());
            stmt.setInt(2, p.getPersonID());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to connect person to citation ", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    

    /**
     * Generates a linked list of Person objects given an array of person ID
 numbers. This is accomplished by making repeated calls to the
     * createPersonFromResultSet() method.
     *
     * @return a linked list of person objects that can be used for display and
     * selection
     * @param peopleIDs
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    
   public List<Person> getPersonList(List<Integer> peopleIDs) throws IntegrationException {
        ArrayList<Person> list = new ArrayList<>();
        
        for (int personId: peopleIDs){
            list.add(getPerson(personId));
        }
        return list;
    } // close getPersonOccApplicationList()

    /**
     * Updates a given record for a person in the database. Will throw an error
 if the person does not exist. Note that this method will retrieve the
 Person object's ID number to determine which record to update in the db
     *
     * @param personToUpdate the Person object with the updated data to be
 stored in the database. All old information will be overwritten
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updatePerson(Person personToUpdate) throws IntegrationException {
        Connection con = getPostgresCon();
        String query;

        query = "UPDATE public.person\n" +
                "   SET persontype= CAST (? AS persontype), muni_municode=?, \n" +  // 1-2
                "	fname=?, lname=?, jobtitle=?, \n" + // 3-5
                "	phonecell=?, phonehome=?, phonework=?, \n" + // 6-8
                "	email=?, address_street=?, address_city=?, \n" + // 9-11
                "	address_state=?, address_zip=?, " + // 12-13
                "	lastupdated=now(), expirydate=?, isactive=?, \n" +  // 14-15
                "	isunder18=?, compositelname=?, \n" + // 16-17
                "       sourceid=?, businessentity=?, \n" + // 18-19
                "       mailing_address_street=?, mailing_address_city=?, mailing_address_zip=?, \n" +// 20-22
                "       mailing_address_state=?, useseparatemailingaddr=?, expirynotes=?,  \n" + // 23-25
                "       canexpire=?, mailing_address_thirdline=?  \n" +// 26-27
                " WHERE personid=?;";

       
        
        
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            // default ID generated by sequence in PostGres
            stmt.setString(1, personToUpdate.getPersonType().toString());
            stmt.setInt(2, personToUpdate.getMuniCode());

            stmt.setString(3, personToUpdate.getFirstName());
            stmt.setString(4, personToUpdate.getLastName());
            stmt.setString(5, personToUpdate.getJobTitle());

            stmt.setString(6, personToUpdate.getPhoneCell());
            stmt.setString(7, personToUpdate.getPhoneHome());
            stmt.setString(8, personToUpdate.getPhoneWork());

            stmt.setString(9, personToUpdate.getEmail());
            stmt.setString(10, personToUpdate.getAddressStreet());
            stmt.setString(11, personToUpdate.getAddressCity());

            stmt.setString(12, personToUpdate.getAddressState());
            stmt.setString(13, personToUpdate.getAddressZip());

            // Last updated set with a call to now() inside postgres
            if (personToUpdate.getExpiryDate() == null) {
                stmt.setNull(14, java.sql.Types.NULL);
            } else {
                stmt.setTimestamp(14, java.sql.Timestamp.valueOf(personToUpdate.getExpiryDate()));
            }
            stmt.setBoolean(15, personToUpdate.isActive());
            
            stmt.setBoolean(16, personToUpdate.isUnder18());          
            stmt.setBoolean(17, personToUpdate.isCompositeLastName());
            
             if(personToUpdate.getSource() != null){
                stmt.setInt(18, personToUpdate.getSource().getSourceid());
            } else {
                stmt.setNull(18, java.sql.Types.NULL);
            }
            stmt.setBoolean(19, personToUpdate.isBusinessEntity());
            
            stmt.setString(20, personToUpdate.getMailingAddressStreet());
            stmt.setString(21, personToUpdate.getMailingAddressCity());
            stmt.setString(22, personToUpdate.getMailingAddressZip());
            
            stmt.setString(23, personToUpdate.getMailingAddressState());
            stmt.setBoolean(24, personToUpdate.isUseSeparateMailingAddress());
            stmt.setString(25, personToUpdate.getExpiryNotes());
            
            stmt.setBoolean(26, personToUpdate.isCanExpire());
            stmt.setString(27, personToUpdate.getMailingAddressThirdLine());
            
            stmt.setInt(28, personToUpdate.getPersonID());

            stmt.execute();
            System.out.println("PersonIntegrator.updatePerson : Excecuted update");
                    

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update person");

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } // close updatePerson

    /**
     * Deprecated first gen person extraction method; replaced by searchForPersons
     * @deprecated 
     * @param p
     * @return
     * @throws IntegrationException 
     */
    public List<Person> getPersonList(Property p) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Person> list = new ArrayList<>();

        try {
            String s = "SELECT person_personid FROM public.propertyperson WHERE property_propertyid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, p.getPropertyID());

            rs = stmt.executeQuery();

            while (rs.next()) {
                Person pers = getPerson(rs.getInt("person_personid"));
                list.add(pers);
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return list;

    }
    
    /**
     * Dump-style retrieval for all Persons associated with a given event
     * @param ev
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> eventPersonAssembleList(EventCnF ev) throws IntegrationException {
        Connection con = getPostgresCon();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> list = new ArrayList<>();

        try {
            String s = "SELECT person_personid FROM public.eventperson WHERE ceevent_eventid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, ev.getEventID());

            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(rs.getInt("person_personid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return list;

    }


    public List<Integer> getPersonHistory(int userID) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> al = new ArrayList<>();

        try {
            String s = "SELECT person_personid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND person_personid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, userID);

            rs = stmt.executeQuery();
            while (rs.next()) {
                al.add(rs.getInt("person_personid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        System.out.println("PersonIntegrator Retrieved history of size: " + al.size());
        return al;

    }

    /**
     * Calls native database method to make a Ghost from a given Person--which
     * is a copy of a person at a given time to be used in recreating official documents
     * with addresses and such, like Citations, NOVs, etc.
     * @param p of which you would like to create a Ghost
     * @param u doing the connecting
     * @return the database identifier of the sent in Person's very own ghost
     * @throws IntegrationException 
     */
    public int createGhost(Person p, User u) throws IntegrationException, BObStatusException {
        if(p == null || u == null){
            throw new BObStatusException("Cannot make ghost with null peson or U");
            
        }
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int newGhostID = 0;

        try {
            String s = "select createghostperson(p.*, ? ) from person AS p where personid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, p.getPersonID());

            rs = stmt.executeQuery();
            
            while (rs.next()) {
                newGhostID = rs.getInt("createghostperson");
                System.out.println("PersonIntegrator.createGhostPerson | new ghost ID: " + newGhostID );
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getGhost | Unable create ghost person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return newGhostID;

    }

    /**
     * Calls database native function to make a clone of a person, which is an exact copy of 
     * a Person at a given point in time that is used to safely edit person info 
     * without allowing certain levels of users access to the primary Person record
     * from which there is no recovery of core info
     * 
     * Ghosts are friendly and invited; clones are an unedesirable manifestation
     * of the modern biotechnological era
     * 
     * @param p of which you would like to create a clone
     * @param u doing the creating of clone
     * @return the database identifer of the inputted Person's clone
     * @throws IntegrationException 
     */
    public int createClone(Person p, User u) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int newGhostID = 0;

        try {
            String s = "select createcloneperson(p.*, ? ) from person AS p where personid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, p.getPersonID());

            rs = stmt.executeQuery();
            
            while (rs.next()) {
                newGhostID = rs.getInt("createcloneperson");
                System.out.println("PersonIntegrator.createClone| new clone ID: " + newGhostID );
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.createClone | Unable to make Clone person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return newGhostID;

    }
    
    
    /**
     * TODO: Finish!
     * @param occPeriodID
     * @return
     * @throws IntegrationException 
     */
    public ArrayList<Person> getPersonList(int occPeriodID) throws IntegrationException{
        ArrayList<Person> personList = new ArrayList();
        String query =  "SELECT person_personid\n" +
                        "  FROM public.occperiodperson WHERE period_periodid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, occPeriodID);
            rs = stmt.executeQuery();
            while(rs.next()){
                personList.add(getPerson(rs.getInt("person_personid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator | Unable to retrieve person list by occ period", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return personList;
        
    }

    public void updatePersonNotes(Person p) throws IntegrationException {
        Connection con = getPostgresCon();
        String query = "UPDATE person SET notes = ? WHERE personid = ?;";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, p.getNotes() );
            stmt.setInt(2, p.getPersonID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to add note to person");
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void deletePerson(Person pers) throws IntegrationException {
        if(pers == null){ return; }
        
        Connection con = getPostgresCon();
        String query = "DELETE FROM person WHERE personid = ?";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pers.getPersonID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to delete person--probably because"
                    + "this person has been attached to another database entity"
                    + "such as a case or event. (Best leave this person in.)");
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Single point of entry for all queries against the person table, one SearchParamsPerson
     * at a time
     * @param params
     * @return the PKs of person records selected by the SQL
     * @throws IntegrationException 
     */
    public List<Integer> searchForPersons(SearchParamsPerson params) throws IntegrationException {
        SearchCoordinator sc = getSearchCoordinator();
        List<Integer> persIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        params.appendSQL    ("SELECT DISTINCT personid FROM public.person \n");
        params.appendSQL    ("LEFT OUTER JOIN public.propertyperson ON (person.personid = propertyperson.person_personid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.property ON (property.propertyid = propertyperson.person_personid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.occperiodperson ON (person.personid = occperiodperson.person_personid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.eventperson ON (person.personid = eventperson.person_personid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.citationperson ON (person.personid = citationperson.person_personid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.personmergehistory ON (person.personid = personmergehistory.mergetarget_personid)\n");
        params.appendSQL    ("LEFT OUTER JOIN public.personmunilink ON (person.personid = personmunilink.person_personid)\n");
        params.appendSQL    ("WHERE personid IS NOT NULL \n");
        
        
        // ***********************************
        // **    FILTER COM-4 OBJECT ID     **
        // ***********************************
        if (!params.isBobID_ctl()){
            
            
            //******************************************************************
           // **   FILTERS COM-1, COM-2, COM-3, COM-6 MUNI,DATES,USER,ACTIVE  **
           // ******************************************************************
            params = (SearchParamsPerson) sc.assembleBObSearchSQL_muniDatesUserActive(
                                                                    params, 
                                                                    SearchParamsPerson.MUNI_DBFIELD,
                                                                    ACTIVE_FIELD);
            

            // ***********************************
            // **    FILTER PERS-1              **
            // ***********************************
            if (params.isName_first_ctl()){
                params.appendSQL("AND fname ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-2              **
            // ***********************************
            if (params.isName_last_ctl()){
                params.appendSQL("AND lname ILIKE ? ");
            }

            // ***********************************
            // **    FILTER PERS-3              **
            // ***********************************
            
            if(params.isName_compositeLNameOnly_ctl()){
                params.appendSQL("AND compositelname=");
                if(params.isName_compositeLNameOnly_val()){
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }
          
            // ***********************************
            // **    FILTER PERS-4              **
            // ***********************************
            if (params.isPhoneNumber_ctl()){
                params.appendSQL("AND phonecell ILIKE ? OR phonework ILIKE ? OR phonehome ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-5              **
            // ***********************************
            if (params.isEmail_ctl()){
                params.appendSQL("AND email ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-6              **
            // ***********************************
            if (params.isAddress_streetNum_ctl()){
                params.appendSQL("AND address_street ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-7              **
            // ***********************************
            if (params.isAddress_city_ctl()){
                params.appendSQL("AND address_city ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-8              **
            // ***********************************
            if (params.isAddress_zip_ctl()){
                params.appendSQL("AND  address_zip ILIKE ? ");
            }
            
            // ***********************************
            // **    FILTER PERS-9              **
            // ***********************************
            if(params.isPersonType_ctl()){
                params.appendSQL("AND persontype = CAST(? AS persontype) ");
            }
            
            // ***********************************
            // **    FILTER PERS-10              **
            // ***********************************
            if(params.isVerified_ctl()){
                params.appendSQL("AND humanverifiedby IS ");
                if(params.isVerified_val()){
                    params.appendSQL("NOT NULL ");
                } else {
                    params.appendSQL("NULL ");
                }
            }
            
            // ***********************************************
            // **       FILTER PERS-11: BOb SOURCE          **
            // ***********************************************
             if (params.isSource_ctl()) {
                if(params.getSource_val() != null){
                    params.appendSQL("AND bobsource_sourceid=? ");
                } else {
                    params.setSource_ctl(false);
                    params.appendToParamLog("SOURCE: no BOb source object; source filter disabled");
                }
            }
            
             
             
            // *******************************************
            // **    FILTER PERS-12: PROPERTY           **
            // *******************************************
             if (params.isProperty_ctl()) {
                if(params.getProperty_val()!= null){
                    params.appendSQL("AND propertyperson.property_propertyid=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("PROPERTY: no Property object; prop filter disabled");
                }
            }
            
            // ***********************************************
            // **       FILTER PERS-13: PROPERTY UNIT       **
            // ***********************************************
             if (params.isPropertyUnit_ctl()) {
                if(params.getPropertyUnit_val()!= null){
                    params.appendSQL("AND propertyunit_unitid=? ");
                } else {
                    params.setPropertyUnit_ctl(false);
                    params.appendToParamLog("PROPERTY UNIT: no PropertyUnit object; propunit filter disabled");
                }
            }
            
            // ***********************************
            // **    FILTER PERS-14             **
            // ***********************************
             if(params.isOccPeriod_ctl()){
                if(params.getOccPeriod_val() != null){
                    params.appendSQL("AND occperiodperson.period_periodid=? ");
                } else {
                    params.setOccPeriod_ctl(false);
                    params.appendToParamLog("OCC PERIOD: no OccPeriod object; occ period filter disabled");
                }
            }
            
            // ***********************************
            // **    FILTER PERS-15             **
            // ***********************************
            if(params.isEvent_ctl()){
                if(params.getEvent_Val() != null){
                    params.appendSQL("AND event.ceevent_eventid=? ");
                } else {
                    params.setEvent_ctl(false);
                    params.appendToParamLog("EVENT: no EventCnF object; event filter disabled");
                }
            }
            // ***********************************
            // **    FILTER PERS-16             **
            // ***********************************
            if(params.isCitation_ctl()){
                if(params.getCitation_val() != null){
                    params.appendSQL("AND citation.citation_citationid=? ");
                } else {
                    params.setCitation_ctl(false);
                    params.appendToParamLog("CITATION: no Citation object; citation filter disabled");
                }
            }
            // ***********************************
            // **    FILTER PERS-17             **
            // ***********************************
            if(params.isMergeTarget_ctl()){
                if(params.getMergeTarget_val() != null){
                    params.appendSQL("AND personmergehistory.mergetarget_personid=? ");
                } else {
                    params.setMergeTarget_ctl(false);
                    params.appendToParamLog("MergeTarget: no Person object as tartget; merge target filter disabled");
                }
            }
            // ***********************************
            // **    FILTER PERS-18             **
            // ***********************************
//            if(params.isMergeTarget_ctl()){
//                if(params.getMergeTarget_val() != null){
//                    params.appendSQL("AND personmunilink.person_personid=? ");
//                } else {
//                    params.setMergeTarget_ctl(false);
//                    params.appendToParamLog("MUNICIPALITY: no MUNI object; muni filter disabled");
//                }
//            }
            
        } else {
            params.appendSQL("AND personid=? ");
        }
        params.appendSQL(";");
        
        int paramCounter = 0;
        StringBuilder str = null;
        
        try {
            stmt = con.prepareStatement(params.extractRawSQL());
            
            // filter COM-4
            if (!params.isBobID_ctl()){
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                // filter COM-2
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                // filter COM-3
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }
                
                // filter PERS-1
                if (params.isName_first_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getName_first_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-2
                if (params.isName_last_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getName_last_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-4
                if (params.isPhoneNumber_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getPhoneNumber_val());
                    str.append("%");
                    //There are three phone number parameters, so we will set all three
                    stmt.setString(++paramCounter, str.toString());
                    stmt.setString(++paramCounter, str.toString());
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-5
                if (params.isEmail_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getEmail_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-6
                if (params.isAddress_streetNum_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getAddress_streetNum_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-7
                if (params.isAddress_city_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getAddress_city_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-8
                if (params.isAddress_zip_ctl()){
                    str = new StringBuilder();
                    str.append("%");
                    str.append(params.getAddress_zip_val());
                    str.append("%");
                    stmt.setString(++paramCounter, str.toString());
                }
                
                // filter PERS-9
                if(params.isPersonType_ctl()){
                    if(params.getPersonType_val() != null){
                        stmt.setString(++paramCounter, params.getPersonType_val().name());
                    } else {
                        // choose arbitrarily
                        stmt.setString(++paramCounter, PersonType.User.name());
                    }
                }
                
                // PERS-10 take zero arguments
                
                // filter PERS-11
                if (params.isSource_ctl()) {
                     stmt.setInt(++paramCounter, params.getSource_val().getSourceid());
                }
                
                // filter PERS-12
                if (params.isProperty_ctl()) {
                     stmt.setInt(++paramCounter, params.getProperty_val().getPropertyID());
                }
                
                // filter PERS-13
                if (params.isPropertyUnit_ctl()) {
                     stmt.setInt(++paramCounter, params.getPropertyUnit_val().getUnitID());
                }
                // filter PERS-14
                if (params.isOccPeriod_ctl()) {
                     stmt.setInt(++paramCounter, params.getOccPeriod_val().getPeriodID());
                }
                // filter PERS-15
                if (params.isEvent_ctl()) {
                     stmt.setInt(++paramCounter, params.getEvent_Val().getEventID());
                }
                // filter PERS-16
                if (params.isCitation_ctl()) {
                     stmt.setInt(++paramCounter, params.getCitation_val().getCitationID());
                }
                // filter PERS-17
                if (params.isMergeTarget_ctl()) {
                     stmt.setInt(++paramCounter, params.getMergeTarget_val().getPersonID());
                }
//                // filter PERS-18
//                if (params.isMuni_ctl()) {
//                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
//                }
                
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }
            
            params.appendToParamLog("PersonIntegrator SQL before execution: ");
            params.appendToParamLog(stmt.toString());
            
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                persIDList.add(rs.getInt("personid"));
                counter++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.queryPersons | Unable to search for "
                    + "persons, ", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        }        
       
        return persIDList;
    }
    
    public PersonChangeOrder generatePersonChangeOrder(ResultSet rs){
        PersonChangeOrder skeleton = new PersonChangeOrder();
        UserCoordinator uc = getUserCoordinator();
        
        try{
        skeleton.setPersonChangeID(rs.getInt("personchangeid"));
        skeleton.setPersonID(rs.getInt("person_personid"));
        skeleton.setFirstName(rs.getString("firstname"));
        skeleton.setLastName(rs.getString("lastname"));
        skeleton.setCompositeLastName(rs.getBoolean("compositelastname"));
        skeleton.setPhoneCell(rs.getString("phonecell"));
        skeleton.setPhoneHome(rs.getString("phonehome"));
        skeleton.setPhoneWork(rs.getString("phonework"));
        skeleton.setEmail(rs.getString("email"));
        skeleton.setAddressStreet(rs.getString("addressstreet"));
        skeleton.setAddressCity(rs.getString("addresscity"));
        skeleton.setAddressZip(rs.getString("addresszip"));
        skeleton.setAddressState(rs.getString("addressstate"));
        skeleton.setUseSeparateMailingAddress(rs.getString("useseparatemailingaddress"));
        skeleton.setMailingAddressStreet(rs.getString("mailingaddressstreet"));
        skeleton.setMailingAddressThirdLine(rs.getString("mailingaddresthirdline"));
        skeleton.setMailingAddressCity(rs.getString("mailingaddresscity"));
        skeleton.setMailingAddressZip(rs.getString("mailingaddresszip"));
        skeleton.setMailingAddressState(rs.getString("mailingaddressstate"));
        
        skeleton.setRemoved(rs.getBoolean("removed"));
        skeleton.setAdded(rs.getBoolean("added"));
        skeleton.setChangedOn(rs.getTimestamp("entryts"));
        skeleton.setApprovedOn(rs.getTimestamp("approvedondate"));
        skeleton.setApprovedBy(uc.user_getUser(rs.getInt("approvedby_userid")));
        skeleton.setChangedBy(rs.getInt("changedby_personid"));
        skeleton.setActive(rs.getBoolean("active"));
        
        } catch(SQLException | IntegrationException ex){
            System.out.println("PersonIntegrator.generatePersonChangeOrder() | ERROR: " + ex);
        }
        
        return skeleton;
        
    }
    
    public void insertPersonChangeOrder(PersonChangeOrder order) throws IntegrationException{
        
        String query =  "INSERT INTO public.personchange(\n" +
"            personchangeid, person_personid, firstname, lastname, compositelastname, \n" +
"            phonecell, phonehome, phonework, email, addressstreet, addresscity, \n" +
"            addresszip, addressstate, useseparatemailingaddress, mailingaddressstreet, \n" +
"            mailingaddresthirdline, mailingaddresscity, mailingaddresszip, \n" +
"            mailingaddressstate, removed, added, entryts, \n" +
"            changedby_personid)\n" +
"    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?, ?, \n" +
"            ?, ?, ?, ?, \n" +
"            ?, ?, ?, \n" +
"            ?, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, order.getPersonID());
            stmt.setString(2,order.getFirstName());
            stmt.setString(3, order.getLastName());
            stmt.setBoolean(4, order.isCompositeLastName());
            stmt.setString(5, order.getPhoneCell());
            stmt.setString(6, order.getPhoneHome());
            stmt.setString(7, order.getPhoneWork());
            stmt.setString(8, order.getEmail());
            stmt.setString(9, order.getAddressStreet());
            stmt.setString(10, order.getAddressCity());
            stmt.setString(11, order.getAddressZip());
            stmt.setString(12, order.getAddressState());
            stmt.setBoolean(13, order.isUseSeparateMailingAddress());
            stmt.setString(14, order.getMailingAddressStreet());
            stmt.setString(15, order.getMailingAddressThirdLine());
            stmt.setString(16, order.getMailingAddressCity());
            stmt.setString(17, order.getMailingAddressZip());
            stmt.setString(18, order.getMailingAddressState());
            stmt.setBoolean(19, order.isRemoved());
            stmt.setBoolean(20, order.isAdded());
            stmt.setTimestamp(21, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(22, order.getPersonID());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert person change order ", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void updatePersonChangeOrder(PersonChangeOrder order) throws IntegrationException{
        
        String query =  "UPDATE public.personchange\n" +
"   SET person_personid=?, firstname=?, lastname=?, \n" +
"       compositelastname=?, phonecell=?, phonehome=?, phonework=?, email=?, \n" +
"       addressstreet=?, addresscity=?, addresszip=?, addressstate=?, \n" +
"       useseparatemailingaddress=?, mailingaddressstreet=?, mailingaddresthirdline=?, \n" +
"       mailingaddresscity=?, mailingaddresszip=?, mailingaddressstate=?, \n" +
"       removed=?, added=?, approvedondate=?, approvedby_userid=?, \n"
                + "changedby_personid=?, active=?\n" +
" WHERE personchangeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, order.getPersonID());
            stmt.setString(2, order.getFirstName());
            stmt.setString(3, order.getLastName());
            stmt.setBoolean(4, order.isCompositeLastName());
            stmt.setString(5, order.getPhoneCell());
            stmt.setString(6, order.getPhoneHome());
            stmt.setString(7, order.getPhoneWork());
            stmt.setString(8, order.getEmail());
            stmt.setString(9, order.getAddressStreet());
            stmt.setString(10, order.getAddressCity());
            stmt.setString(11, order.getAddressZip());
            stmt.setString(12, order.getAddressState());
            stmt.setBoolean(13, order.isUseSeparateMailingAddress());
            stmt.setString(14, order.getMailingAddressStreet());
            stmt.setString(15, order.getMailingAddressThirdLine());
            stmt.setString(16, order.getMailingAddressCity());
            stmt.setString(17, order.getMailingAddressZip());
            stmt.setString(18, order.getMailingAddressState());
            stmt.setBoolean(19, order.isRemoved());
            stmt.setBoolean(20, order.isAdded());
            stmt.setTimestamp(21, order.getApprovedOn());
            if (order.getApprovedBy() != null) {
                stmt.setInt(22, order.getApprovedBy().getUserID());
            } else {
                stmt.setNull(22, java.sql.Types.NULL);
            }
            stmt.setInt(23, order.getChangedBy());
            stmt.setBoolean(24, order.isActive());
            stmt.setInt(25, order.getPersonChangeID());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update person change order ", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public List<PersonChangeOrder> getPersonChangeOrderList(int personID) throws IntegrationException{
        
        String query =  "SELECT personchangeid, person_personid, firstname, lastname, compositelastname, \n" +
"       phonecell, phonehome, phonework, email, addressstreet, addresscity, \n" +
"       addresszip, addressstate, useseparatemailingaddress, mailingaddressstreet, \n" +
"       mailingaddresthirdline, mailingaddresscity, mailingaddresszip, \n" +
"       mailingaddressstate, removed, added, entryts, approvedondate, \n" +
"       approvedby_userid, changedby_userid, changedby_personid, active\n" +
"  FROM public.personchange\n" +
"  where person_personid = ? AND active = true AND approvedon IS NULL;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        List<PersonChangeOrder> orderList = new ArrayList<>();

        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, personID);
            
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                orderList.add(generatePersonChangeOrder(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get person change order list", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return orderList;
        
    }
    
        public List<PersonChangeOrder> getPersonChangeOrderListAll(int personID) throws IntegrationException{
        
        String query =  "SELECT personchangeid, person_personid, firstname, lastname, compositelastname, \n" +
"       phonecell, phonehome, phonework, email, addressstreet, addresscity, \n" +
"       addresszip, addressstate, useseparatemailingaddress, mailingaddressstreet, \n" +
"       mailingaddresthirdline, mailingaddresscity, mailingaddresszip, \n" +
"       mailingaddressstate, removed, added, entryts, approvedondate, \n" +
"       approvedby_userid, changedby_userid, changedby_personid, active\n" +
"  FROM public.personchange\n" +
"  where person_personid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        List<PersonChangeOrder> orderList = new ArrayList<>();

        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, personID);
            
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                orderList.add(generatePersonChangeOrder(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to get person change order list", ex);

        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
           if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return orderList;
        
    }
    
} // close class