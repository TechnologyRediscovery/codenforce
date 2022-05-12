#!/bin/bash
ps -f
#Define User and Pass variable
#
#
TEMPDIR=/home/techred4/codenforce 
SCHEMAFILE=null
PTCHCNT=1 
cd "$TEMPDIR"
#*********************************Remove*********************
#PGCOMMAND2="SELECT schema.functionname(${myid});"
#PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"`
#RETCODE2=$?
#******************************************************
function getpatchs () {
#Dump current cogdb database
PTCHDIR=/home/temp/directory/
response=$(curl --create-dirs -0 --output-dir /tmp/directory  "https://github.com/TechnologyRediscovery/codenforce/cnfpatch[1-9].sql"   #1_#2 $TEMPDIR)
if [["$response" -ne 200]]; then 
  echo "status change"
else
  exit 0
fi  
#count number of file that *.sql
sqlfiles=("$TEMPDIR"/patchcnf*.sql )}
echo "$sqlfiles"
function CompareDB ($DBNAME) {
#compare database
TEMPDIRPG=/home/techred4/codenforce/pgdiff
cd "$TEMPDIRPG"
USER1=changeme HOST1=localhost NAME1=cogdb  USER2=changeme HOST2=localhost NAME2=$DBNAME ./pgdiff.sh 
echo "done"
FileDiff=$(wc -l  $TEMPDIRPG/2-FUNCTION.sql)
if $FileDiff > 20 then
  return 1
else
  return 0 
}

for file in $PTCHDIR; do
    if [-f "$file"] then
       echo "$file"       
	$SCHEMAFILE=$file;
	FileNo =$($PTCHCNT)
	DBNAME="COG_DB" + $FileNo
	PGPASSFILE="/home/user/.pgpass"
	PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND1"`
	PGCOMMAND1="psql -U db_user $DBNAME < $SCHEMAFILE"
	RETCODE1=$?
	PGCONNECT=" psql -U changeme -w -h localhost -d $DBNAME -p 5432"
	PGCOMMAND2="SELECT *.*"
	PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"`
	RETCODE2=$?
	#Check return code of this command
	if [ $RETCODE1 -eq 0 ] &&  [ $RETCODE2 -eq 0 ]; then
          	mailx -s sendme_errormailwith retcode1=2
         	 #compare database
          	 CompareDB($DBNAME)
	else
          	echo -e "ret1: $RETCODE1\n" >> $MAIL_FILE
          	echo -e "ret2: $RETCODE2\n" >> $MAIL_FILE
          	mailx -s sendme_errormailwith retcode1=2
          	break
	fi
#Add the auditable information
echo "here"
echo "$SCHEMAFILE"
done




