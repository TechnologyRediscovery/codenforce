/*
 * Copyright (C) 2017 Eric C. Darsow
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
import com.tcvcog.tcvce.coordinators.SessionSystemCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Connects Person objects to the data store
 *
 * @author Eric C. Darsow
 */
public class PersonIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of PersonIntegrator
     */
    public PersonIntegrator() {
    }

    /**
     * Looks up a person given a personID and creates a returns a new instance
     * of Person with all the available information loaded about that person
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
            
            String s = "SELECT \n" +
                    "  person.personid, \n" +
                    "  person.persontype AS persontype, \n" +
                    "  person.muni_municode, \n" +
                    "  person.fname, \n" +
                    "  person.lname, \n" +
                    "  person.jobtitle, \n" +
                    "  person.phonecell, \n" +
                    "  person.phonework, \n" +
                    "  person.phonehome, \n" +
                    "  person.email, \n" +
                    "  person.address_street, \n" +
                    "  person.address_city, \n" +
                    "  person.address_state, \n" +
                    "  person.address_zip, \n" +
                    "  person.notes, \n" +
                    "  person.lastupdated, \n" +
                    "  person.expirydate, \n" +
                    "  person.isactive, \n" +
                    "  person.isunder18, \n" +
                    "  person.humanverifiedby, \n" +
                    "  person.compositelname, \n" +
                    "  person.creator, \n" +
                    "  person.businessentity, \n" +
                    "  person.mailing_address_street, \n" +
                    "  person.mailing_address_city, \n" +
                    "  person.mailing_address_zip, \n" +
                    "  person.mailing_address_state, \n" +
                    "  person.useseparatemailingaddr, \n" +
                    "  person.expirynotes, \n" +
                    "  person.creationtimestamp, \n" +
                    "  person.canexpire, \n" +
                    "  person.userlink, \n" +
                    "  personsource.sourceid, \n" +
                    "  personsource.title\n" +
                    "FROM \n" +
                    "  public.person LEFT OUTER JOIN public.personsource ON person.sourceid = personsource.sourceid \n" +
                    "WHERE \n" +
                    "  personid = ?;";
            
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
        SessionSystemCoordinator ssc = getSessionSystemCoordinator();
        
        try {
            newPerson.setPersonID(rs.getInt("personid"));
            newPerson.setPersonType(PersonType.valueOf(rs.getString("persontype")));
            newPerson.setMuniCode(rs.getInt("muni_municode"));
            newPerson.setMuniName(ssc.getMuniCodeNameMap().get(rs.getInt("muni_municode")));

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
            newPerson.setLastUpdated(rs.getTimestamp("lastupdated").toLocalDateTime());
            java.sql.Timestamp s = rs.getTimestamp("expirydate");
            if (s != null) {
                newPerson.setExpiryDate(s.toLocalDateTime());

            } else {
                newPerson.setExpiryDate(null);
            }
            newPerson.setActive(rs.getBoolean("isactive"));

            newPerson.setUnder18(rs.getBoolean("isunder18"));
            newPerson.setVerifiedByUserID(rs.getInt("humanverifiedby"));
            newPerson.setCompositeLastName(rs.getBoolean("compositelname"));

            newPerson.setSourceID(rs.getInt("sourceid"));
            newPerson.setSourceTitle(rs.getString("title"));
            
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
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating person from ResultSet", ex);
        }
        return newPerson;
    } // close method

    /**
     * Implements a basic person search by first and last name parts and returns
     * a Person object based on those results. If more than one person is
     * matched on the query, only the first one is returned since the cursor is
     * only moved to the first row when passed to the
     * createPersonFromResultSet() method
     *
     * @param params
     * @return the new Person() object generated from the query
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public ArrayList<Person> getPersonList(SearchParamsPersons params) throws IntegrationException {
        Connection con = getPostgresCon();
        ArrayList<Person> personAL = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT personid FROM public.person ");   // < -- don't for get
        
        if(!params.isFilterByObjectID()){
            sb.append("WHERE muni_municode = ? ");              // < -- trailing spaces!
            if(params.isFilterByFirstName()){
                sb.append("AND fname ILIKE ");
                sb.append("'%");
                sb.append(params.getFirstNameSS());
                sb.append("%'");
            }
            if(params.isFilterByLastName()){
                sb.append("AND lname ILIKE ");
                sb.append("'%");
                sb.append(params.getLastNameSS());
                sb.append("%'");
            }
            if(params.isFilterByEmail()){
                sb.append("AND email ILIKE ");
                sb.append("'%");
                sb.append(params.getEmailSS());
                sb.append("%'");
            }
            if(params.isFilterByAddressStreet()){
                sb.append("AND address_street ILIKE ");
                sb.append("'%");
                sb.append(params.getAddrStreetSS());
                sb.append("%'");
            }
            if(params.isFilterByActiveSwitch()){
                if(params.isActiveSwitch()){
                    sb.append("AND isactive = TRUE ");
                } else {
                    sb.append("AND isactive = FALSE ");
                }
            }
            if(params.isFilterByVerifiedSwitch()){
                if(params.isVerifiedSwitch()){
                    sb.append("AND humanverifiedby IS NOT NULL");
                } else {
                    sb.append("AND humanverifiedby IS NULL");
                }
            }
        } else { // if we're searching by personID, ignore all other criteria
            sb.append("WHERE personid = ?;"); // param 2 with key search
        }
        
        try {
            stmt = con.prepareStatement(sb.toString());
            
            if(!params.isFilterByObjectID()){
                stmt.setInt(1, params.getMuni().getMuniCode());
            } else {
                stmt.setInt(1, params.getObjectID()); // and this is the only param after muni!
            }
            
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if(params.isLimitResultCountTo100()){
                 maxResults = Integer.parseInt(getResourceBundle(
                        Constants.DB_FIXED_VALUE_BUNDLE).getString("defaultMaxQueryResults"));
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            Person p;
            int id;
            
            while (rs.next() && counter < maxResults ) {
                id=rs.getInt("personid");
                p = getPerson(id);
                personAL.add(p);
                counter++;
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for person", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return personAL;

    } // close method

    /**
     * Distributor method for handling requests to connect a person to a
     * property
     *
     * @param person
     * @param prop
     * @throws IntegrationException
     */
    public void insertPersonAndConnectToProperty(Person person, Property prop) throws IntegrationException {

        int newPersonID = insertPerson(person);
        // Now that th person is in the DB, i've got the person ID to store in the
        // bridge table: propertyperson
        person.setPersonID(newPersonID);
        // now send in a fully-baked person to an entry in the db
        connectPersonToProperty(person, prop);

    }
    
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
                    " expirynotes, creationtimestamp, canexpire, userlink) " +
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
                    + "?, ?, ?, NULL);"; // through 32

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
            
            
            if(personToStore.getSourceID() != 0){
                stmt.setInt(21, personToStore.getSourceID());
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

    public void connectPersonsToEvent(CECaseEvent ev, List<Person> personList) throws IntegrationException {
        ListIterator li = personList.listIterator();
        while (li.hasNext()) {
            connectPersonToEvent(ev, (Person) li.next());
        }
    }

    public void connectPersonToEvent(CECaseEvent ev, Person p) throws IntegrationException {

        String query = "INSERT INTO public.ceeventperson(\n"
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
            connectPersonToCitation( c, (Person) iter.next());
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
     * numbers. This is accomplished by making repeated calls to the
     * createPersonFromResultSet() method.
     *
     * @return a linked list of person objects that can be used for display and
     * selection
     * @param peopleIDs
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    
   public ArrayList<Person> getPersonList(ArrayList<Integer> peopleIDs) throws IntegrationException {
        ArrayList<Person> list = new ArrayList<>();
        
        for (int personId: peopleIDs){
            list.add(getPerson(personId));
        }
        return list;
    } // close getPersonList()

    /**
     * Updates a given record for a person in the database. Will throw an error
     * if the person does not exist. Note that this method will retrieve the
     * Person object's ID number to determine which record to update in the db
     *
     * @param personToUpdate the Person object with the updated data to be
     * stored in the database. All old information will be overwritten
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updatePerson(Person personToUpdate) throws IntegrationException {
        Connection con = getPostgresCon();
        String query;

        query = "UPDATE public.person\n" +
                "   SET persontype= CAST (? AS persontype), muni_municode=?, \n" +
                "	fname=?, lname=?, jobtitle=?, \n" +
                "	phonecell=?, phonehome=?, phonework=?, \n" +
                "	email=?, address_street=?, address_city=?, \n" +
                "	address_state=?, address_zip=?, \n" +
                "	lastupdated=now(), expirydate=?, isactive=?, \n" +
                "	isunder18=?, compositelname=?, \n" +
                "       sourceid=?, businessentity=?, \n" +
                "       mailing_address_street=?, mailing_address_city=?, mailing_address_zip=?, \n" +
                "       mailing_address_state=?, useseparatemailingaddr=?, expirynotes=?,  \n" +
                "       canexpire=? \n" +
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
            
            if(personToUpdate.getSourceID() == 0){
                stmt.setNull(18, java.sql.Types.NULL);
            } else {
                stmt.setInt(18, personToUpdate.getSourceID());
            }
            stmt.setBoolean(19, personToUpdate.isBusinessEntity());
            
            stmt.setString(20, personToUpdate.getMailingAddressStreet());
            stmt.setString(21, personToUpdate.getMailingAddressCity());
            stmt.setString(22, personToUpdate.getMailingAddressZip());
            
            stmt.setString(23, personToUpdate.getMailingAddressState());
            stmt.setBoolean(24, personToUpdate.isUseSeparateMailingAddress());
            stmt.setString(25, personToUpdate.getExpiryNotes());
            
            stmt.setBoolean(26, personToUpdate.isCanExpire());
            stmt.setInt(27, personToUpdate.getPersonID());

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
    
    public List<Person> getPersonList(Event ev) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Person> list = new ArrayList<>();

        try {
            String s = "SELECT person_personid FROM public.ceeventperson WHERE ceevent_eventid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, ev.getEventID());

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


    public List<Person> getPersonHistory(User u) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Person> al = new ArrayList<>();

        try {
            String s = "SELECT person_personid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND person_personid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, u.getUserID());

            rs = stmt.executeQuery();
            int MAX_RES = 20;  //behold a MAGICAL number
            int iter = 0;
            
            while (rs.next() && iter < MAX_RES) {
                Person pers = getPerson(rs.getInt("person_personid"));
                al.add(pers);
                iter++;
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

    public int createGhost(Person p, User u) throws IntegrationException {
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

    
    
    public ArrayList<Person> getOccPermitAppPersons(int applicationID) throws IntegrationException{
        String query = "SELECT person_personid FROM occpermitapplicationperson WHERE permitapp_applicationid = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;        
        ArrayList<Integer> personIDs = new ArrayList();
        ArrayList<Person> persons = new ArrayList();
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, applicationID);
            rs = stmt.executeQuery();
            while (rs.next()){
                personIDs.add(rs.getInt("person_personid"));
            }
            persons = getPersonList(personIDs);
            
            
        } catch (SQLException ex) {
            throw new IntegrationException("PersonIntegrator.getOccPermitAppPersons | Unable to "
                    + "retrieve person(s) for given applicationID ", ex);
        }
        
        return persons;
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
    
    public void deletePerson(int personToDeleteID) throws IntegrationException {
        Connection con = getPostgresCon();
        String query = "DELETE FROM person WHERE personid = ?";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, personToDeleteID);
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
    
    public List<Person> queryPersons(SearchParamsPersons params) throws IntegrationException {
        ArrayList<Person> personList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        String defaultQuery = "SELECT personId "
                + "FROM person "
                + "WHERE ";
        StringBuilder query = new StringBuilder(defaultQuery);
        boolean notFirstCriteria = false;
        
        
        if (!params.isFilterByObjectID()){
            if (params.isFilterByLastName()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("lname ILIKE ? ");
            }
            if (params.isFilterByFirstName()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("fname ILIKE ? ");
            }
            if (params.isFilterByAddressStreet()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("address_street ILIKE ? ");
            }
            if (params.isFilterByCity()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("address_city ILIKE ? ");
            }
            if (params.isFilterByZipCode()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("address_zip ILIKE ? ");
            }            
            if (params.isFilterByEmail()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("email ILIKE ? ");
            }
            
            if (params.isFilterByPhoneNumber()){
                if(notFirstCriteria){query.append("AND ");} else {notFirstCriteria = true;}
                query.append("phonecell ILIKE ? OR phonework ILIKE ? OR phonehome ILIKE ? ");
            }
            
        } else {
            query.append("caseid = ? ");
        }
        
        int paramCounter = 0;
        
        try {
            stmt = con.prepareStatement(query.toString());
            if (!params.isFilterByObjectID()){
                if (params.isFilterByLastName()){
                    stmt.setString(++paramCounter, params.getLastNameSS());
                }
                if (params.isFilterByFirstName()){
                    stmt.setString(++paramCounter, params.getFirstNameSS());
                }
                if (params.isFilterByAddressStreet()){
                    stmt.setString(++paramCounter, params.getAddrStreetSS());
                }
                if (params.isFilterByCity()){
                    stmt.setString(++paramCounter, params.getCity());
                }
                if (params.isFilterByZipCode()){
                    stmt.setString(++paramCounter, params.getZipCode());
                }            
                if (params.isFilterByEmail()){
                    stmt.setString(++paramCounter, params.getEmailSS());
                }
                if (params.isFilterByPhoneNumber()){
                    stmt.setString(++paramCounter, params.getPhoneNumber());
                }
            } else {
                stmt.setInt(++paramCounter, params.getObjectID());
            }
            
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if (params.isLimitResultCountTo100()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                personList.add(getPerson(rs.getInt("personid")));
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
       
        return personList;
    }

} // close class
