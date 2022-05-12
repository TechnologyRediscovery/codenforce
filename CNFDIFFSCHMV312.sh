#!/bin/bash
ps -f
#Define User and Pass variable
#
#
DBNAME=null
chmod u+x ./pgdiff.sh 
TEMPDIR=/home/techred4/codenforce 
SCHEMAFILE=null
PTCHCNT=1 
DIFFVAL=SPACES
cd "$TEMPDIR"
$ CURRENT_USERS=$(who)
HOSTNAME=hostname
#*********************************Remove*********************
#PGCOMMAND2="SELECT schema.functionname(${myid});"
#PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"`
#RETCODE2=$?
#******************************************************
function getpatchs () {
#Dump current cogdb database
PTCHDIR=/home/temp/directory/
#response=$(curl -w --create-dirs -0 --output-dir /tmp/directory  "https://github.com/TechnologyRediscovery/codenforce/cnfpatch[1-9].sql"
   #1_#2 $TEMPDIR)
response=$(curl -w --create-dirs -0 --output-dir /tmp/directory $TEMPDIR)
if [ response != "200" ]; then
    echo "handle error"
else
    echo "Server returned:"
    cat response.txt 
fi } 
function CompareDB () {
#compare database
TEMPDIRPG=/home/techred4/codenforce/pgdiff
cd "$TEMPDIRPG"
USER1=changeme HOST1=localhost NAME1=cogdb  USER2=changeme HOST2=localhost NAME2=$DBNAME ./pgdiff.sh 
echo "done"
FileDiff=$(wc -l  $TEMPDIRPG/2-FUNCTION.sql)
if [$FileDiff > 20]; then
  return 1
  $DIFFVAL = "DIFFR"
else
  return 0 
  $DIFFVAL = "SAME" 

fi }   
#count number of file that *.sql
sqlfiles=("$TEMPDIR"/patchcnf*.sql )}
echo "$sqlfiles"
getpatchs
for [file in $PTCHDIR];
do
    if [-f "$file"]; then
       echo "$file"       
	$SCHEMAFILE=$file;
	FileNo =$($PTCHCNT)
	DBNAME="COG_DB" + $FileNo
	PGPASSFILE="/home/user/.pgpass"
	PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND1"`
	PGCOMMAND1="psql -U db_user $DBNAME < $SCHEMAFILE"
	RETCODE1=$?
	PGCONNECT=" psql -U changeme -w -h localhost -d $DBNAME -p 5432"
	PGCOMMAND2="SELECT *.* "
	PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"`
	RETCODE2=$?
	#Check return code of this command
	if [ $RETCODE1 -eq 0 ] &&  [ $RETCODE2 -eq 0 ]; then
          	mailx -s sendme_errormailwith retcode1=2
#       compare database
          	CompareDB
	else
          	echo -e "ret1: $RETCODE1\n" >> $MAIL_FILE
          	echo -e "ret2: $RETCODE2\n" >> $MAIL_FILE
          	mailx -s sendme_errormailwith retcode1=2
          	break
	fi
        echo  now=$(date)
        echo "Patch No - $SCHEMAFILE"
        echo "Patch Diff - $FileDiff" 
        echo "Patch Lines- $DIFFVAL" 
        echo "Who -$CURRENT_USERS"
	echo "here"
     fi   
done
