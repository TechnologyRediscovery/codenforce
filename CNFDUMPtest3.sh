#!/bin/bash
ps -f
#echo pwd
#TEMPDIR=/home/techred/codenforce/ 
#cd $TEMPDIR
#echo $TEMPDIR
#Dump the cogdb database into a json file 
#sudo pg_dump --schema-only --format=p cogdb > 'cogdbsearch.sql' 
#pg_dump --schema-only --format=p -U changeme -d cogdb -W > 'bk_name.sql'
#ps -f
TEMPDIR=/home/techred4/codenforce 
cd "$TEMPDIR"
#Postgres bin directory: 
#Dump the cogdb database into a json file 
#TEMPDIR= "/home/techred/codenforce" 
#cd "$TEMPDIR"
#echo $TEMPDIR
#pg_dump --schema-only --format=p -U changeme -d cogdb -W > 'bk_name.sql'
#grep for patch file
#grep -l -R <cnfpatch> <codenforce>
#grep read the grep and the patch file present in the directory)
#Patcharray = readarray -d '' -t targets < <(grep --null -l -R <cnfpatch> <codenforce> .) 
#get all patch files
sqlfiles=("$TEMPDIR"/patchcnf*.sql )
#printf "$sqlfiles" 
echo ${sqlfiles[*]}
#Show the matches-get array of patchfiles in directory
#[[-e $sqlfiles ]] || { echo "Matched no files" >&2; exit 1; }
echo "Found the following file:" ${sqlfiles[@]}
#printf  "${sqlfiles[@]}"
#change the working director 
#loop through array of patch files and for each one check db.tar file
#for str in "${sqlfiles[@]}"
for ((i = 0; i < ${#sqlfiles[@]}; i++))
do
echo "here"
echo "${#sqlfiles[@]}"
newfile = {${sqlfiles[i]}:0:-1};
echo "${newfile}"
#Do for loop to open, and scan for each file in the dump
#set file contents to varibale name
#find the file content in the cogdbsearch.tar
#    input="${sqlfiles[i]}"
input="${newfile}"
     while IFS = read -r line; 
       do 
       echo "$line"
#perform search for regex here in db dump
    done <$input.
done 

