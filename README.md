JexSiter
========

# Design and Concept of Operations

## Manual Setup
* Generate rsa public/private key pairs (id_rsa/id_rsa.pub) and set up on target machine
 * ssh-keygen -t rsa -C "your-email@server" - generates id_rsa and id_rsa.pub (in designated directory with password)
 * scp id_rsa.pub to target machine
 * On target machine in "user" account:
  * mkdir ~/.ssh
  * mv id_rsa.pub ~/.ssh
  * cd ~/.ssh
  * cat id_rsa.pub > authorized_keys
  * chmod 400 authorized_keys
  * rm id_rsa.pub
  * cd ..
  * chmod 700 .ssh
 * test from local machine: ssh -i id_rsa user@remote-machine (prompts for private key password, not remote login password)

## Configuration
* Default config under $USER_HOME/.exsiter/application.conf
 * privateKeyLocation - id_rsa private key (id_rsa.pub must set up as .ssh/authorized_keys on target machine)
 * privateKeyPassphrase - password for id_rsa private key
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

# Integration Test
Set up local user/fake directories. Use $USER_HOME/.exsiter/test/test-application.conf to contain test key/password.

Note: Ubuntu openssh server config at /etc/ssh/sshd_config had the following line, which must be commented out to make JSch
work
!#HostKey /etc/ssh/ssh_host_ecdsa_key

Do a local login via ssh localhost and then copy .ssh/known_hosts to $USER_HOME/.exsiter/test/known_hosts

# Design
* org.medale.exsiter.Main - main entry point with two different types of functionality
 * init - brand-new Exsiter setup. Downloads target files for the first time to initialize local repository
 * backup - incremental backup against existing local repository
 * Either function can be run in test mode
 
 


