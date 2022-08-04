# CodeNForce system documentation
CodeNForce is a JavaEE-based, open source web database application for managing municpal government code enforcement and occupancy permitting workflows. Built by Technology Rediscovery LLC and students attending the Community College of Allgheny County, CodeNForce's geographic center is the Turtle Creek Valley on the outskirts of Pittsburgh, PA.

## About this documentation
As technical documentation designed for developers, the primary audience for these pages are system administrators and developers who wish to build, edit, or maintain an existing CodeConnect instance. Sub-components of the system each have their own briefing page which links to specific files in the repo as they are explained and indexes how-to pages for various tasks. 

## Building codeNforce
[You can build codeNforce on most any Linux system with these instructions.](buildingcc/buildsteps.md) As a barely launched prototype, the build process is not always smooth like it might be in a fully packaged docker container. If you're trying to build as an non project team member, you will probably need to be quite handy at the terminal.

## Contents by subsystem

### N [User](ss_User.md)
### I [Municipality](ss_Municipality.md)
### II [Codebook](ss_Codebook.md)
### III [Property](ss_Property.md)
### IV [Person](ss_Person.md)
### V [Event](ss_Event.md)
### VI [Occperiod](ss_Occperiod.md)
### VII [Cecase](ss_Cecase.md)
### VIII [Ceactionrequest](ss_Ceactionrequest.md)
### VIV [Occapp](ss_Occapp.md)
### X [Payment](ss_Payment.md)
### XI [Report](ss_Report.md)
### XII [Blob](ss_Blob.md)
### XIII [Publicinfobundle](ss_Publicinfobundle.md)
### XIV [Search](ss_Search.md)
### XV [Workflow](ss_Workflow.md)
### XVI [Analytics](ss_analytics.md)
### XVII [Data integrity](ss_dataintegrity.md)
### XVIII [Help](ss_help.md)
### XVIV [UX](ss_ux.md)
### XX [External Data](ss_externaldata.md)
### XXI [Session](ss_session.md)

## Legacy doc pages

[Code enforcement phase management and requested events](cecasephases.md)

[Notice of violation letters](novs.md)

[Coding norms](codingNorms.md)

[Essential documentation links and topic references](references.md)

[Adding utility table and new columns to a postgres table](pg-add-table-columns.md)

[Creating user-modifyable database queries(i.e. serching)](searching.md)

[Managing user-muni connections mini-project](muni-user-project.md)

[Property unit management mini-project](property-unit-mini-project.md)

![CodeConnect system components](img/ccoverview.jpg)

## Writing documentation
All developer documentation is written in markdown and exists on the mater branch directory called `docs` Since we are not actively working on the master branch, follow these steps
1. Create a new directory on your computer and clone down this entire repo a second time.
2. Open a new terminal window and navigate into this repo, and checkout the `master` branch (which is no longer the default branch, the default branch is `recovered`)
3. Open sublime >> open folder >> choose this new second repo which is where you'll write the documentation and push like normal, but this time to the master branch
4. Create one or more pages with an `.md` ending inside this directory structure to store your validation rules
5. You  must commit your local changes and push them up to master to see the markdown rendered into html by the markdown engine on the github server
6. When your page is ready, create a link to it on the `index.md` main page so others can see it

## accessing the documentation
The documentation root is at 
https://technologyrediscovery.github.io/codenforce/
# Table of Contents

[Code Enforcement Case Profile](case/casescreen.md)
