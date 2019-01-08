# User-Muni association mini-project

## Goal:
Create a faces page that allows the user to assign system users to municipalities

## Work process
1. Studying the tables and classses associated with user authentication: find any references to these tables in the integrators. Using full-project find tool in NetBeans is handy (control + shift + f). There currently is an integration method called getUserAuthMunis() which returns the listing of muni-user mappings. Building interaction with this data is the project.

2. Familairize yourself with the two methods which return a ArrayList<Municipality> objects. These lists of Municipality objects are passed into all sorts of select one UI elements. For example, the main muni list which we can see in missionControl.xhtml is wired up in the template facelets page called [navContainer_restricted.xhtml](/blob/src/main/webapp/restricted/navContainer_restricted.xhtml "nav conatiner")

        <p:selectOneMenu id="muniSelectMenu" tabindex="1" value="#{missionControlBB.selectedMuni}">
            <f:converter converterId="muniConverter"/>
            <f:selectItem itemLabel="select one..." noSelectionOption="true" itemDisabled="true"/>
            <f:selectItems id="muniNameList" value="#{sessionBean.facesUser.authMunis}" var="m" itemValue="#{m}" itemLabel="#{m.muniName}" />
        </p:selectOneMenu>

    Zooming in on the value for the selectitems, we see

        value="#{sessionBean.facesUser.authMunis}"

    Which sais: go to the java class called SessionBean, find the property called FacesUser, which stores a User object. The user has in it an ArrayList of Municipality objects, which is what the `<f:selectItems...` knows how to read and display as a list of Strings for the user to select. (facesUser is in the BackingBeanUtils!)

    This list of Municipality objects comes from the `UserIntegrator` class's `getUserAuthMunis(int id)` method. Study this:

        user.setAuthMuis(getUserAuthMunis(userID));

3. Notice that there is a method on the `MunicipalityIntegrator` class that will return a complete list. This will be handy creating your mapping. 

        public ArrayList<Municipality> getCompleteMuniList() throws IntegrationException{

4. Start in the UserIntegrator class and write setUserAuthMunis() method that probably takes in a User object and a Municipality object, extracts the IDs from each, assembles an INSERT statement in SQL, and executes it.

5. Build the pair of files: the backing bean class, and the facelets page (.xhtml). A good pair to start with is the facility for working with text blocks: `textBlockManage.xhtml` and the backing bean class: `TextBlockBB`

## Design ideas:
* Break the page into two parts: view user-muni mappings as a data table on the top, and then an Add mappings section on the bottom which allows the user to select a single user and a single muni and create the mapping
* This utility will only be used infrequently to adjust user-muni mappings: no need for mul
* Study the use of a <p:commandButton> inside a data table to send a selected row to a method on a BackingBean. Study ceActionRequests.xhtml

    	<p:column width="8%">
    	    <p:commandButton actionListener="#{cEActionRequestsBB.manageActionRequest(r)}"

Notice that the `r` sent in the parameter comes from the assigning of the object used to create each row the arbitrary variable name `r` in the header for the `<p:dataTable...` component 

        <p:dataTable
            id="actionRequestTable"
            var="r"




