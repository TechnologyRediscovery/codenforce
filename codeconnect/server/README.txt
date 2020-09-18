Directions for inserting the postgres driver in wildfly:
--------------------------------------------------------
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