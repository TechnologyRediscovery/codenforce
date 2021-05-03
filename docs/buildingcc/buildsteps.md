# Build stack and steps
## Stack

* Linux-Ubuntu OS
* PostgreSQL database v.10.0+
* Java Wildfly/JBoss EE webserver
* Java ServerFaces view tools
* Primefaces JSF drop-in view component library

## Build steps: 35,000 ft
Steps for creating test system only; no security setup steps are included
1. [Get Linux](#Linux) (XUbuntu is our common distro) with root privileges
2. Install postgreSQL and pgadmin 4
3. Install Java EE JDK
4. Install NetBeans 8.2 (Later versions not yet supported by codeNforce)
5. "Install" Wildfly (copy binary files)
6. Configure Wildfly for deploying codeNforce
7. Inject database brain into PostgreSQL
8. Build codeNforce with Maven and test deployment in browser


### Linux
Most folks on the team use [XUbuntu 18.04LTS+](https://xubuntu.org/). You'll need root privileges. We've also had folks attempt the entire build on Linux confined inside the Microsoft Corporations Windows Subsystem for Linux, but eventually jumped to a non-virtualized box. We've also built in a containerized Ubuntu in VirtualBox.

### PostgreSQL and pgadmin4
Install your favorite version of postgresql

### Java EE JDK
Eric has the following JDK running in Netbeans as of Jan-2021 `jdk1.8.0_221`. 
You'll need to get the Oracle distribution, which will require creating an Oracle account to get the download. The exact package file for Ubuntu used with Joanne Jan-2021 was `jdk-8u281-linux-x64.rpm`. Note that the `8u` part will be the same, but the three-digit number after the `u` might be a higher number.

Once you get the tarball downloaded, extract it with
`tar -zxfv [tarball]tar.gz`

### Netbeans 8.2
Netbeans will come with an older version of Maven, so to get it to work with the HTTPS requirements of the maven central repo, we neeed to add the following to our `pom.xml` file inside the cloned repository.

[See this Stack Overflow](https://stackoverflow.com/questions/59763531/maven-dependencies-are-failing-with-a-501-error)

The XML we need are the HTTPS URLs to the maven central repo and the maven plugin repo:

## Postgres Config steps

Ubuntu comes with postgres at the current release version. Check that you have it with `psql` and if you get a help output, you've got postgres. 

Become the `postgres` user that was created when postgres was installed.
`sudo su postgres`

This will allow you to make high-level configuration changes to the postgres system and modify user permissions.

Enter the interactive with `psql`

Get your hands on the database dump from the current version of cNf's live system. Copy that sql script into a subdirectory in your home directory. Make sure the database file has a `.sql` extension

then `psql -f file.sql` will read in the data into the DB

You'll need to run
`CREATE USER xxxxx WITH PASSWORD 'xxxxxx';`
`CREATE ROLE xxxxx;`
THEN
`GRANT ALL PRIVILEGES ON DATABASE xxx to xxx;`

Enter the databse as your local user and run

`GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO xxxx;`

In PG admin, create new server
Name: `cogdb`



Then copy the extracted files 

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