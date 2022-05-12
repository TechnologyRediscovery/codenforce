#!/bin/bash
ps -f
#Define User and Pass variable
#
#
#Add call to reach
DBNAME=null
set PGPASSWORD=[changeme]
chmod u+x ./pgdiff.sh 
TEMPDIR=/home/techred4/codenforce 
SCHEMAFILE=null
PTCHCNT=0
DIFFVAL=SPACES
FileDiff=20
cd "$TEMPDIR"
#$CURRENT_USERS=$(who)
HOSTNAME=hostname
#*********************************Remove*********************
#PGCOMMAND2="SELECT schema.functionname(${myid});"
#PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"`
#RETCODE2=$?
#******************************************************
function getpatchs () {
#Dump current cogdb database
PTCHDIR=/home/techred4/tmp/direct
#response=$(curl -w --create-dirs -0 --output-dir /tmp/directory  "https://github.com/TechnologyRediscovery/codenforce/cnfpatch[1-9].sql"
#   #1_#2 $TEMPDIR)
#response=$(curl -w --create-dirs -0 --output-dir /home/techred4/tmp/direct $TEMPDIR)
#echo "$response" 
#if [ response != "200" ]; then
#    echo "handle error"
#else
#    echo "Server returned:"
#    cat response.txt 
#fi
echo "finished getpath"
 } 
function CompareDB () {
#compare database
# t stores $1 argument passed to fresh()
t=$1
echo "Total number of arguments is $#" 
TEMPDIRPG=/home/techred4/codenforce/pgdiff
cd "$TEMPDIRPG"
USER1=changeme HOST1=localhost NAME1=cogdb  USER2=changeme HOST2=localhost NAME2=$DBNAME ./pgdiff.sh 
echo "done"
$FileDiff=$($(wc -l  $TEMPDIRPG/2-FUNCTION.sql))
if [FileDiff) > 20]; then
  return 1
  $DIFFVAL = "DIFFR"
else
  return 0 
  $DIFFVAL = "SAME" 

fi 
}   
#count number of file that *.sql
cd $PTCHDIR
cp *.sql $TEMPDIR
cd $TEMPDIR
ls $TEMPDIR/*.sql
sqlfiles=($TEMPDIR/patchcnfv0*.sql )}
echo "start diffs"
getpatchs
cd $PTCHDIR
FileNo=0
for f in *;
do
    DBNAM='COG_DB'
    echo "in for loop $f" 
#   test if file name is empty
    if test -z "$f"; then
        continue 
    else  
        echo "$f"       
	SCHEMAFILE=$f
        FileNo=$((FileNo+1))
	echo "FN-$FileNo"
	DBNAME="${DBNAM}${FileNo}"
        echo "$DBNAME"
#	$PGPASSFILE="/home/user/.pgpass"
	PGCOMMAND1="set PGPASSWORD=[changeme] psql -U changeme changeme $DBNAME < $SCHEMAFILE"
	PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND1"`
	RETCODE1=$?
	PGCONNECT="set PGPASSWORD=[changeme] psql -U changeme  -w -h localhost -d $DBNAME -p 5432"
	PGCOMMAND2="SELECT *.* "
	PGRESULT=`$PGCONNECT -A -F ";" -t -c "$PGCOMMAND2"`
	RETCODE2=$?
	#Check return code of this command
        echo "$RETCODE1 and $RETCODE2"
	if [ $RETCODE1 -eq 0 ] &&  [ $RETCODE2 -eq 0 ]; then
#          	mailx -s sendme_errormailwith retcode1=2
#       compare database
          	CompareDB $DBNAME
                $result=$($(CompareDB))

	else
#          	echo -e "ret1: $RETCODE1\n" >> $MAIL_FILE
#          	echo -e "ret2: $RETCODE2\n" >> $MAIL_FILE
#          	mailx -s sendme_errormailwith retcode1=2
                echo "error in diff call in db loop"
          	continue
	fi
        if [$result = 0]; then
           echo  "now=$(date)"
           echo "Patch No : $SCHEMAFILE\n"
           echo "Patch Diff : $(FileDiff)" 
           echo "Patch Lines : $(DIFFVAL)" 
           echo "Who : $(CURRENT_USERS)"
	   echo "here"
        fi
     fi   
done
