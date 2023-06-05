# Public action request portal (CEARs)

codeNforce version 1.6.0 and later includes a public-facing series of forms which allows users to submit a code enforcement action request (CEAR). The submission process provides the following features:
* Requests are attributable to either a specific parcel in the chosen municipality or a location describable in a text box.
* Users categorize their requests into simple bins such as "Tall grass" or "Excessive trash" or "Other / not listed" and can flag the issue as relating to human safety
* Photo and document uploads are permitted (3 max)
* Requesters are required to include their name and either an email address or phone number (or both)
* A 6-digit control code is provided on a confirmation screen for easy location of their request when calling for follow-up

## Page sequence
The following sections focus on each phase of the submission process with details aimed at municipal staff, support staff, and code officers who support public users and internal users reviewing the submitted requests

### Step 1: Begin and choose municipality
The CEAR submission begins with a link in the left side bar of the codeNforce home page. The only input for step 1 is a selection of the municipality in which the concern exists.

![public complaint portal first page](img/cearlanding.png)

### Step 2: Choose property or describe location
Step 2 asks users to search for and select a property in the selected municipality. Underneath the property search results is a prompt and check box for cases in which the property of concern cannot be found or the concern is not at a specific property. 

![choose a location](img/step2-prop.png)

When the check box is enabled, the property search components is replaced by a text input box to capture a free-form description of the concern location.

![choose a location](img/step2-prop-noprop.png)

As of v1.6.0, users who do select a property AND check the box for "not located at a specific property" will not receive an error. During internal review both the linked properties and the described location will be displayed.

### Step 3: Describe concern
Users are asked to choose from a short list of general concern categories, including an "other" option. Users can also turn on a "human safety hazard" flag whose only function on the internal review process is to append their description with the words ""

![describe concern](img/step3-concern.png)

### Step 4: Photos or documents
Users can attach up to 3 photos or documents of to their concern and can describe those objects with a description of 100 characters or less.

![describe concern](img/step4-blob1.png)
![describe concern](img/step4-blob2.png)
![describe concern](img/step4-blob3.png)

### Step 5: Requester details
If users of any rank except public are logged in to codeNforce when completing the CEAR form, step 5 will display a big blue button extending across the top of the content box that says "You're logged in! Submit this request as yourself: [displays your person name and user name]".

Clicking this button will skip the need to complete the requester info form and dump you directly on the review and submit screen. 

Users not logged in do not see this big blue button and instead must complete at least the Full name field and either the phone number or email field (or both)

![describe concern](img/step5-requsetor.png)

### Step 6: Review and submit
The final step displays a compilation of all their form data from steps 1-5 in a single page with links to jump back and edit any of their form info.

Note that by this step, the users's form data has already been written to the database and assigned both an internal ID and a public access code. (In fact, the database receives the request after property selection and then updates the record with additional info during each step's "next" button click.) 

Thus even without clicking the final submit button internal users will be notified of the action request. In fact, internal users see no difference in the request info between requesters who click through all the way to the final submission screen and those who stop after adding their contact information.

![describe concern](img/step6-review.png)

### Confirmation
After confirming request details, users are provided a random 6 digit integer for comfort. This number can be used by internal users to look up the action request regardless of its routing status.

![describe concern](img/step7-complete.png)

