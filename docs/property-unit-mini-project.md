# Propert units

## Goal:
Create a user interface which allows internal users AND folks filling out an occupancy permit application to create unit objects and link them to a particular property in the database

Example: I have a rental duplex. It's property is 300 Gas Line Way. It has two units: A and B. Unit B's address is actually 280 Gas Line Way since it has a separate door. When I apply for a permit, I want to be able to enter in each unit into the system and register them as rental units.

## Work steps
1. Study search for properties page used in the ce action request flow `requestCEActionFlow_chooseProperty.xhtml` You'll want to start with this page, allow the user to select a property. Dump that property into a new member variable on the SessionBean
2. Study the `PropertyUnit` class which already exists. Its member variables map to the database columns of the `propertyunit` table.
3. Study existing methods on `PropertyIntegrator` related to units. There are two unfinished methods that you can write: `updatePropertyUnit` and `deletePropertyUnit`
4. Flesh out the page called `unitManage.xhtml` and build your GUI on this page. So, the user selects a property and then the next button should send the user to `unitManage.xhtml` and its backing bean should know what property is currently active.
5. On this `unitManage.xhtml` we want a data table of all existing units already registered on the property (use the appropriate method on the PropertyIntegrator called `getPropertyUnitList`). Then give the user a button for adding a new Unit. Supply the necessary fields to load up each of the member variables on `PropertyUnit` business object.
6. When the user clicks a submit button, write the unit to the DB in the integrator class and update the table for the user

Study the `textBlockManage.xhtml` page as a reference: This page combines data table of current text blocks (paragraphs), an add form, and an update form. Notice the use of the `<p:ajax...` primefaces tag for ajax updating fields. Read about prime faces ajax on line.