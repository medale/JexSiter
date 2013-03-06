JexSiter
========

# Design and Concept of Operations

## Manual Setup
* Generate rsa public/private key pairs (rsa.prv/rsa.pub) and set up on target machine
 * On target machine in "user" account:
  * mkdir ~/.ssh
  * cd ~/.ssh
  * cat rsa.pub > authorized_keys
  * chmod 400 authorized_keys
 * test from local machine: ssh -i rsa.prv user@remote-machine (prompts for private key password, not remote login password)


## Configuration
* Default config under $USER_HOME/.exsiter/application.conf
 * privateKeyLocation - rsa private key (must set up as .ssh/authorized_keys on target machine)
 * privateKeyPassphrase - password for rsa private key
 * knownHostsLocation - must contain target machine (log in to target via ssh, could point to $USER_HOME/.ssh/known_hosts)
 * username - username for target machine
 * hostname - full host name of target machine
 * gitTagsToRetain - number of tag directories to retain (default 2)
 * gitDir - location of local git repository root 
  * git dirs are subdirectories of this dir
  * exsiter-backup subdirectory contains last backup
  * exister-yyyyMMMdd - tag that represents latest git version for that date

## Concept of Operations

* Run Exsiter Main class as cron job (TODO: run via Quartz?)
* Load default configuration from $USER_HOME/.exsiter/application.conf
* Runs SshShellCommandExecutor.LIST_ALL_FILES_AND_THEIR_CHECKSUMS (find . -type f | xargs md5sum)
 * Returns listing of all files as pairs: 56a329926a92460b9b6ac1377f610e48 ./web/newsletter/grip-it.jpg
 * Parse into FilePathChecksumTriple and create Map<String, FilePathChecksumTriple> fileWithPath to triple
* Looks for $gitDir/exsiter-backup/fileNameToHashMap.csv to load previous map
* Iterates through all entries in the new map
 * If filename/md5 hash are unchanged against previous map, do nothing
 * If filename did not exist in previous version or had a different md5 hash (file was updated)
  * Download new/updated file via ScpTool
  * Add downloaded file under $gitDir/exsiter-backup/target-web-dir/ with correct file path and name
* Iterates through diff of keys (file names) that were in the previous map but not the new (deleted files on target)
 * Delete corresponding file under target-web-dir/
* Write new map to $gitDir/exsiter-backup/fileNameToHashMap.csv
* Execute logical "git add $gitDir/exsiter-backup/" to add all the new/deleted files
* Execute git commit
* Execute git tag yyyyMMMdd to make rollback easy
* Execute git clone to $gitDir/exister-yyyyMMMdd

# TODO and Future Enhancements
* Automate initial manual setup
* Run Exsiter via Quartz
* Execute search through current $gitDir/exsiter-backup/target-web-dir/ for suspicious content and flag those files
* Download and analyze logs for suspicious content?
 
 


