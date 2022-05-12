#!/bin/bash
ps -f
TEMPDIR=/home/techred4/codenforce 
cd "$TEMPDIR"
#sqlfiles=("$TEMPDIR"/patchcnf*.sql )
#for ((i = 0; i < ${#sqlfiles[@]}; i++))
do
#echo "here"
echo "${#sqlfiles[@]}"
##newfile = {${sqlfiles[i]}:0:-1};
#echo "${newfile}"
#Do for loop to open, and scan for each file in the dump
#set file contents to varibale name
#find the file content in the cogdbsearch.tar
#    input="${sqlfiles[i]}"
input="${newfile}"
USER1=changeme HOST1=localhost NAME1=cogdb  USER2=changme HOST2=localhost NAME2=cog_db7 ./pgdiff.sh
echo "done"
#pgdiff -U changeme -H localhost -D cog_db7  -O "sslmode=disable" -S public \
#       -u changeme -h localhost -d cog_db   -o "sslmode=disable" -s public \
#       SCHEMA

