#!/bin/bash
ps -f
#*****************************************************************************************************
#
#           The Purpose of the script is to validate the Patch level
#
#*****************************************************************************************************
#Define User and Pass variable
#*****************************************************************************************************
#
#Add call to reach
#ssh -f jwright@pgdroplet -L 56000:localhost:5432 -N 
DBNAME=null
set PGPASSWORD=[changeme]
chmod u+x ./pgdiff.sh 
#****************************************************************************************************
#* Initialize variables
#****************************************************************************************************
TEMPDIR=/home/techred4/codenforce 
SCHEMAFILE=null
PTCHCNT=0
DIFFVAL=SPACES
PGCONNECT= SPACES 
FileDiff=20
cd "$TEMPDIR"
#$CURRENT_USERS=$(who)
HOSTNAME=hostname
#*********************************Remove************************************************************
#***************************************************************************************************
#* Compare Getpatch Function to get remote files from directory
#*************************************************************************************************** 
function getpatchs () {
   #Dump current cogdb database
   PTCHDIR=/home/techred4/tmp/direct/
#response=$(curl -w --create-dirs -0 --output-dir /tmp/directory  "https://github.com/TechnologyRediscovery/codenforce/cnfpatch[1-9].sql"
#   #1_#2 $TEMPDIR)
   response=$(curl -w --create-dirs -0 --output-dir /home/techred4/tmp/direct $TEMPDIR)
   echo "$response" 
   if [ response != "200" ]; then
      echo "handle error"
   else
     echo "Server returned:"
    cat response.txt 
   fi
   echo "finished getpatchs"
} 
#***************************************************************************************************
#* Compare DB Function 
#*************************************************************************************************** 
function CompareDB () {
   #compare database
   echo "Compare db"
   # t stores $1 argument passed to fresh()
   t=$1
   echo "Total number of arguments is $#"
   TEMPDIRPG=/home/techred4/codenforce/pgdiff
   cd "$TEMPDIRPG"
   USER1=changeme PORT1=5432 HOST1=localhost NAME1=cogdb  USER2=changeme PORT2=56000 HOST2=localhost NAME2=$DBNAME ./pgdiff.sh 
   echo "done"
   $FileDiff=$($(wc -l  $TEMPDIRPG/2-FUNCTION.sql))
   if [ $($FileDiff) > 20 ]; then
     return 1
     $DIFFVAL = "DIFFR"
   else
     return 0 
     $DIFFVAL = "SAME" 
   fi 
} 
#**************************************************************************************************
#* Main section of the script                                                                     *
#**************************************************************************************************  
#count number of file that *.sql
cd $PTCHDIR
#cp droplet*.sql $TEMPDIR
#cd $TEMPDIR
ls $TEMPDIR/*.sql
sqlfiles=($TEMPDIR/droplet*.sql )}
echo "start diffs"
# the getpatch function will get the database version used for comparison
getpatchs
$f= spaces
FileNo=0
cd $PTCHDIR
ls $PTCHDIR
echo "at loop"
#************************************************************************************************
#* Iterate through DBName and do comparision                                                    *
#************************************************************************************************
for f in *; 
   do echo "filename -$f"    
   if test -f "$f"; then
#    test if file name is empty
#    DBNAM=$f
#    echo "in for loop $f" 
#    else  
       echo "filename -$f"       
       SCHEMAFILE=$f
       FileNo=$((FileNo+1))
       echo "FN-$FileNo"
       $DBNAME="${fullfile##*/}"
       echo "$DBNAME"
#************************do DBcompare****************************************************************
#*												    *
#***************************for files (var f) in directory do the following**************************
#for f in *; 
#Build connection strings: 
#       echo "$DBNAME"
       export PGPASSWORD='pgdr@plet22'
       PGCONNECT= 'psql -U jwright  -w -h localhost -d $DBNAME -p 56000'
#      PGCONNECT= 'set PGPASSWORD=[pgdr@plet22] psql -U jwright  -w -h localhost -d $DBNAME -p 56000'
       PGCOMMAND2="SELECT *.* "
       PGRESULT='$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"'
       RETCODE2=$?
#Check return code of this command
       echo "$RETCODE2"
#Check the return codes
       if [ $RETCODE2 -eq 0 ]; then
#**************************************************************************************************
#         compare database function below will look to make sure that the database schema matches *
#**************************************************************************************************
          CompareDB $DBNAME
          $result=$($(CompareDB))
       else
           echo "error in diff call in db loop"
           break
       fi
       echo "$(CompareDB $num)"
#***********************************************************************************************
# if the third party pgm compare is similar value < 20 differences then show the ptchlevel and *
# leave loop                                                                                   *
#***********************************************************************************************
        if [ $(CompareDB $num) -eq 0 ]; then
           echo "now- $($date)\n"
           echo "Patch No : $SCHEMAFILE\n"
           echo "Patch Diff : $($FileDiff)\n" 
           echo "Patch Lines: $($DIFFVAL)\n"
        fi 
   else
        continue
   fi;
   done
exit 0
