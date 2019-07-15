# 244 Final Project
For my final I am modelling a file upload and validation system for the purpose of tracking associations between uploaded photographs & documents, and entities relevant to the case-lifecycle (People, Properties, ActionRequests from the public, etc.). The core object-modelling is finished, but much of the code is incomplete and as such this branch will not compile (as of 5/8/19).  

## Use Cases
Through CodeConnect, Code Enforcemnt Officers can create, track, and close Code Enforcement Cases and track the neccesity and completion of Occupancy Inspections.  Much of the evidence used in these cases will be photographs taken during inspections.  The intention of this branch is to add support for the upload of images taken by enforcement officials, and properly track links between uploaded files and relevant entities.  This branch has since expanded scope to allow for the upload of any file type, with the intentions of enabling officals to upload and "attach" PDF files to entities as well.  

Future plans for this branch include validating uploading files, and displaying images and PDF's attached to an entity on-demand.

## What You Can Do Now
This is very much a work-in-progress at the moment.  In it's current state the project will not compile.  If you wish to see the work done to date, relavant files include:
..* Blob.java
..* BlobIntegrator.java
..* BlobCoordinator.java
..* BlobTypeException.java
..* BlobCorruptedException.java

With the widening of project scope to include PDF's and not solely images, I am refactoring from the old Photograph object to a generic Blob object.  Relevant project files that were once functional, but are now to be refactored (broken) include:
..* ImageServices.java
..* Photograph.java
..* PhotoBB.java
..* requestCEActionFlow_photoUpload.xhtml
