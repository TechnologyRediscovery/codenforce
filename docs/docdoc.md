# Workflow Steps for Committing Updates to Documentation
1. Compose markdown page with desired instructions- note that images should be placed in sub-directory called img
2. Open up Terminal and navigate to the documentation repository directory on my harddrive
3. Check that git has found your new files with command `git status`. Changed files should be under untracked files list.
4. Stage the documentation files, including images, for committing with `git add [path to new file]`
5. Check your staging with `git status`, should see the files under the heading "changes to be committed"
6. When all files have been added to the staging ground, commit those changes with `git commit -m "[brief description of new changes]"`
7. Send changes up to remote server with the following command `git push origin documentation`
8. Authenticate with prompts
9. Navigate to github.com in browser, select documentation branch, and verify that the files have made it up
10. Make a note that the files are ready to be linked in live system (make tracker in github)

ssh -f cogadmin@198.199.84.209 -L 50000:localhost:8000 -N
ps aux | grep ssh   *to kill
kill (PROCESS ID)   *use the left number
