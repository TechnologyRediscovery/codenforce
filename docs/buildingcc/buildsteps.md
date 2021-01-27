# Build stack and steps
## Stack

* Linux-Ubuntu OS
* PostgreSQL database v.10.0+
* Java Wildfly/JBoss EE webserver
* Java ServerFaces view tools
* Primefaces JSF drop-in view component library

## Build steps: 35,000 ft
Steps for creating test system only; no security setup steps are included
1. Get Linux (XUbuntu is our common distro) with root privileges
2. Install postgreSQL
3. Install Java EE JDK
4. Install NetBeans 8.2 (Later versions not yet supported by codeNforce)
5. "Install" Wildfly (copy binary files)
6. Configure Wildfly for deploying codeNforce
7. Inject database brain into PostgreSQL
8. Build codeNforce with Maven and test deployment in browser





Directions for inserting the postgres driver in wildfly:

"Install" wildfly 14 by inserting its directory into a sensible place, like in your home directory.

In the same directory as this readme, you see a module.xml file.

GO AND DOWNLOAD the current postgreSQL database driver from 

https://jdbc.postgresql.org/download.html

The jar should be named:

postgresql-42.2.5.jar

Then, we need to copy BOTH the jar you just downloaded and the xml into the appropriate modules directory inside wildfly's home. In the author's sample build, a path was built (by adding directories as needed) so that these two files can be stored in:

modules/system/layers/base/org/postgresql/main/

When restarted wildfly will index its module diretory tree so that when our config file asks for classes inside the postgres jar, wildfly will know where to look.

You can restart wildfly inside netbeans or with the command line configuration tools.