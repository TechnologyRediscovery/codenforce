#!/bin/bash
ps -f
TEMPDIR=/home/techred4/codenforce 
cd "$TEMPDIR"
sqlfiles=("$TEMPDIR"/patchcnf*.sql )
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
[[ -z $USER1 ]] && USER1=c42
[[ -z $HOST1 ]] && HOST1=localhost
[[ -z $PORT1 ]] && PORT1=5432
[[ -z $NAME1 ]] && NAME1='cp-520'
[[ -z $OPT1 ]]  && OPT1='sslmode=disable'

[[ -z $USER2 ]] && USER2=c42
[[ -z $HOST2 ]] && HOST2=localhost
[[ -z $PORT2 ]] && PORT2=5432
[[ -z $NAME2 ]] && NAME2=cp-pentest
[[ -z $OPT2 ]]  && OPT2='sslmode=disable'

echo "This is the reference database:"
echo "   ${USER1}@${HOST1}:${PORT1}/$NAME1"
read -sp "Enter DB password: " passw
PASS1=$passw
PASS2=$passw

echo
echo "This database may be changed (if you choose):"
echo "   ${USER2}@${HOST2}:${PORT2}/$NAME2"
read -sp "Enter DB password (defaults to previous password): " passw
[[ -n $passw ]] && PASS2=$passw
echo

let i=0
function rundiff() {
    ((i++))
    local TYPE=$1
    local sqlFile="${i}-${TYPE}.sql"
    local rerun=yes
    while [[ $rerun == yes ]]; do
        rerun=no
        echo "Generating diff for $TYPE... "
        ./pgdiff -U "$USER1" -W "$PASS1" -H "$HOST1" -P "$PORT1" -D "$NAME1" -O "$OPT1" \
                 -u "$USER2" -w "$PASS2" -h "$HOST2" -p "$PORT2" -d "$NAME2" -o "$OPT2" \
                 $TYPE > "$sqlFile"
        rc=$? && [[ $rc != 0 ]] && exit $rc
        if [[ $(cat "$sqlFile" | wc -l) -gt 4 ]]; then
            vi "$sqlFile"
            read -p "Do you wish to run this against ${NAME2}? [yN]: " yn 
            if [[ $yn =~ ^y ]]; then
                PGPASSWORD="$PASS2" ./pgrun -U $USER2 -h $HOST2 -p $PORT2 -d $NAME2 -O "$OPT2" -f "$sqlFile"
                read -p "Rerun diff for $TYPE? [yN]: " yn
                [[ $yn =~ ^[yY] ]] && rerun=yes
            fi
        else
            read -p "No changes found for $TYPE (Press Enter) " x
        fi
    done
    echo
}

rundiff ROLE
rundiff FUNCTION
rundiff SCHEMA
rundiff SEQUENCE
rundiff TABLE
rundiff COLUMN
rundiff MATVIEW
rundiff INDEX
rundiff VIEW
rundiff TRIGGER
rundiff OWNER
rundiff FOREIGN_KEY
rundiff GRANT_RELATIONSHIP
rundiff GRANT_ATTRIBUTE
done 
