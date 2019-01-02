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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
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
        System.out.println("PersonIntegrator.PersonIntegrator - constructor");
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
                    "  person.persontype, \n" +
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
                    "  person.addressofresidence, \n" +
                    "  person.mailing_address_street, \n" +
                    "  person.mailing_address_city, \n" +
                    "  person.mailing_address_zip, \n" +
                    "  person.mailing_address_state, \n" +
                    "  person.mailingsameasresidence, \n" +
                    "  person.expirynotes, \n" +
                    "  personsource.sourceid, \n" +
                    "  personsource.title\n" +
                    "FROM \n" +
                    "  public.person, \n" +
                    "  public.personsource\n" +
                    "WHERE \n" +
                    "  person.sourceid = personsource.sourceid \n"+
                    "  AND personid = ?;";
            
            stmt = con.prepareStatement(s);
            stmt.setInt(1, personId);
            System.out.println("PersonIntegrator.getPerson | sql: " + s);

            rs = stmt.executeQuery();

            if (rs.next()) {
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
        MunicipalityIntegrator muniIntegrator = getMunicipalityIntegrator();
        UserIntegrator ui = getUserIntegrator();
        
        try {
            newPerson.setPersonID(rs.getInt("personid"));
            System.out.println("PersonIntegrator.generatePersonFromResultSet | person Type from db: " + rs.getString("personType"));
            newPerson.setPersonType(PersonType.valueOf(rs.getString("personType")));
            Municipality m = muniIntegrator.getMuniFromMuniCode(rs.getInt("muni_muniCode"));
            newPerson.setMuni(m);
            
            newPerson.setSourceID(rs.getInt("sourceid"));
            newPerson.setSourceTitle(rs.getString("title"));
            newPerson.setCreator(ui.getUser(rs.getInt("creator")));
            
            newPerson.setFirstName(rs.getString("fName"));
            newPerson.setLastName(rs.getString("lName"));
            newPerson.setCompositeLastName(rs.getBoolean("compositelname"));
            newPerson.setBusinessEntity(rs.getBoolean("businessentity"));
            
            newPerson.setJobTitle(rs.getString("jobtitle"));

            newPerson.setPhoneCell(rs.getString("phoneCell"));
            newPerson.setPhoneHome(rs.getString("phoneHome"));
            newPerson.setPhoneWork(rs.getString("phoneWork"));

            newPerson.setEmail(rs.getString("email"));
            newPerson.setAddress_street(rs.getString("address_street"));
            newPerson.setAddress_city(rs.getString("address_city"));

            newPerson.setAddress_state(rs.getString("address_state"));
            newPerson.setAddress_zip(rs.getString("address_zip"));
            newPerson.setAddressOfResidence(rs.getBoolean("addressofresidence"));
            
            newPerson.setMailing_address_street(rs.getString("mailing_address_street"));
            newPerson.setMailing_address_city(rs.getString("mailing_address_city"));
            newPerson.setMailing_address_zip(rs.getString("mailing_address_zip"));
            
            newPerson.setMailing_address_state(rs.getString("mailing_address_state"));
            newPerson.setMailingSameAsResidence(rs.getBoolean("mailingsameasresidence"));
            newPerson.setNotes(rs.getString("notes"));

            newPerson.setLastUpdated(rs.getTimestamp("lastupdated").toLocalDateTime());

            java.sql.Timestamp s = rs.getTimestamp("expirydate");
            if (s != null) {
                newPerson.setExpiryDate(s.toLocalDateTime());

            } else {
                newPerson.setExpiryDate(null);
            }
            newPerson.setExpiryNotes(rs.getString("expirynotes"));
            newPerson.setActive(rs.getBoolean("isactive"));

            newPerson.setUnder18(rs.getBoolean("isunder18"));
            newPerson.setVerifiedBy(ui.getUser(rs.getInt("humanverifiedby")));

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
        ArrayList<Person> ll = new ArrayList<>();
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
                sb.append("AND address_streetILIKE ");
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
            
            System.out.println("PersonIntegrator.searchForPerson | sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if(params.isLimitResultCountTo100()){
                 maxResults = Integer.parseInt(getResourceBundle(
                        Constants.DB_FIXED_VALUE_BUNDLE).getString("defaultMaxQueryResults"));
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            
            while (rs.next() && counter < maxResults ) {

                // note that rs.next() is called and the cursor
                // is advanced to the first row in the rs
                ll.add(generatePersonFromResultSet(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot search for person", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ll;

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

        String query = "INSERT INTO public.propertyperson(\n"
                + " property_propertyid, person_personid)\n"
                + " VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getPropertyID());
            stmt.setInt(2, person.getPersonID());

            System.out.println("PersonIntegrator.connectPersonToProperty | sql: " + stmt.toString());
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
        Connection con = getPostgresCon();
        StringBuilder query = new StringBuilder();
        ResultSet rs = null;
        int lastID;

        query.append("INSERT INTO public.person(\n"
                + "            personid, persontype, muni_municode, fname, lname, jobtitle, \n"
                + "            phonecell, phonehome, phonework, email, address_street, address_city, \n"
                + "            address_zip, address_state, notes, lastupdated, expirydate, isactive, \n"
                + "            isunder18)\n"
                + "    VALUES (DEFAULT, CAST (? AS persontype), ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, now(), ?, ?, \n"
                + "            ?);");

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query.toString());
            // default ID generated by sequence in PostGres
            stmt.setString(1, personToStore.getPersonType().toString());
            stmt.setInt(2, personToStore.getMuniCode());

            stmt.setString(3, personToStore.getFirstName());
            stmt.setString(4, personToStore.getLastName());
            stmt.setString(5, personToStore.getJobTitle());

            stmt.setString(6, personToStore.getPhoneCell());
            stmt.setString(7, personToStore.getPhoneHome());
            stmt.setString(8, personToStore.getPhoneWork());
            stmt.setString(9, personToStore.getEmail());
            stmt.setString(10, personToStore.getAddress_street());
            stmt.setString(11, personToStore.getAddress_city());

            stmt.setString(12, personToStore.getAddress_zip());
            stmt.setString(13, personToStore.getAddress_state());
            stmt.setString(14, personToStore.getNotes());

            if (personToStore.getExpiryDate() != null) {
                stmt.setTimestamp(15, java.sql.Timestamp.valueOf(personToStore.getExpiryDate()));

            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }

            stmt.setBoolean(16, personToStore.isActive());

            stmt.setBoolean(17, personToStore.isUnder18());

            System.out.println("PersonIntegrator.insertPerson | sql: " + stmt.toString());

            stmt.execute();

            // now get the most recent Unique ID added to this person to pass 
            // to client methods who want to connect a record to this person
            String idNumQuery = "SELECT currval('person_personIDSeq');";
            Statement s = con.createStatement();
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return lastID;

    } // close insertPerson()

    public void connectPersonsToEvent(EventCECase ev, List<Person> personList) throws IntegrationException {
        ListIterator li = personList.listIterator();
        while (li.hasNext()) {
            connectPersonToEvent(ev, (Person) li.next());
        }
    }

    public void connectPersonToEvent(EventCECase ev, Person p) throws IntegrationException {

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

    /**
     * Generates a linked list of Person objects given an array of person ID
     * numbers. This is accomplished by making repeated calls to the
     * createPersonFromResultSet() method.
     *
     * @return a linked list of person objects that can be used for display and
     * selection
     * @param people an integer array containing Person id numbers to be
     * converted into a ArrayList of Person objects for display in the view
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    
   public ArrayList<Person> getPersonList(int[] people) throws IntegrationException {
        ArrayList<Person> list = new ArrayList<>();

        // loop through the array of integers provided and ask
        // our getPersonByID() method for a person object associated with
        // each id
        for (int i = 0; i < people.length; i++) {
            list.add(PersonIntegrator.this.getPerson(people[i]));
        }
        return list;
    } // close getPersonListe()

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

        query = "UPDATE public.person SET "
                + "personType = CAST(? AS personType),"
                + "muni_muniCode = ?,"
                + "fName = ?,"
                + "lName = ?,"
                + "jobtitle = ?,"
                + "phonecell = ?,"
                + "phonehome = ?,"
                + "phonework = ?,"
                + "email = ?,"
                + "address_street = ?,"
                + "address_city = ?,"
                + "address_zip = ?,"
                + "address_state = ?,"
                + "notes = ?,"
                + "lastUpdated = now(),"
                + "expiryDate = ?,"
                + "isactive = ?,"
                + "isunder18 = ?"
                + "WHERE personId = ?;";

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
            stmt.setString(10, personToUpdate.getAddress_street());
            stmt.setString(11, personToUpdate.getAddress_city());

            stmt.setString(12, personToUpdate.getAddress_zip());
            stmt.setString(13, personToUpdate.getAddress_state());
            stmt.setString(14, personToUpdate.getNotes());

            // Last updated set with a call to now() inside postgres
            if (personToUpdate.getExpiryDate() == null) {
                System.out.println("PersonIntegrator.updatePerson | expiry date is null in the personToUpdate");
            } else {
                System.out.println("PersonIntegrator.updatePerson | personToUpdateExpiryDate: " + personToUpdate.getExpiryDate().toString());
                stmt.setTimestamp(15, java.sql.Timestamp.valueOf(personToUpdate.getExpiryDate()));
            }

            stmt.setBoolean(16, personToUpdate.isActive());
            stmt.setBoolean(17, personToUpdate.isUnder18());

            stmt.setInt(18, personToUpdate.getPersonID());
            System.out.println("PersonIntegrator.updatePerson | sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update person");

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } // close updatePerson

    public ArrayList getPersonList(Property p) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Person> al = new ArrayList();

        try {
            String s = "SELECT * FROM public.propertyperson WHERE property_propertyid = ?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, p.getPropertyID());
            System.out.println("PersonIntegrator.getPersonListByPropertyID | sql: " + stmt.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                Person pers = getPerson(rs.getInt("person_personid"));
                System.out.println("PersonIntegrator.getPersonListByPropertyID | adding person: " + pers.getFirstName());
                al.add(pers);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return al;

    }
    
    public ArrayList<Person> getPersonList(int propertyID) throws IntegrationException{
        ArrayList<Person> personList = new ArrayList();
        String query =  "SELECT person_personid\n" +
                        "  FROM public.propertyunitperson WHERE propertyunit_unitid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, propertyID);
            System.out.println("PropertyIntegrator.getProperty | sql: " + stmt.toString());
            rs = stmt.executeQuery();
            while(rs.next()){
                personList.add(getPerson(rs.getInt("person_personid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PropertyIntegrator.getProperty | Unable to retrieve property by ID number", ex);
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return personList;
        
    }

    public HashMap getPersonMapByCaseID(int caseID) {

        return new HashMap();
    }

    public void deletePerson(int personToDeleteID) throws IntegrationException {
        Connection con = getPostgresCon();
        String query = "DELETE FROM person WHERE personid = ?";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, personToDeleteID);
            System.out.println("PersonIntegrator.deletePerson | sql: " + stmt.toString());
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

} // close class
