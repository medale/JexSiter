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
 * backupRootDir - absolute path to the root of the backup directory
  * contains exsiter-backup dir which is the git repo maintaining
 * backupCronFrequency - cron expression for frequency of backups
  * sec min hrs day-of-month month day-of-week optYear
  * JAN-DEC (1-12), SUN-SAT(1-7), * all values, ? unspec day, L last
  * Example 10 o'clock last Friday of month: 0 0 10 ? * 6L
  * For details see Quartz CronExpression API, e.g. http://quartz-scheduler.org/api/2.0.0/org/quartz/CronExpression.html
* Note: application.conf, id_rsa and id_rsa.pub permissions should be chmod 400!

## Concept of Operations

* Run Exsiter Main class as cron job: java -jar exister.jar (ExsiterScheduler is default main in manifest)
 * Loads default configuration from $USER_HOME/.exsiter/application.conf

### Startup
* Currently Exsiter doesn't do the initial download using bulk. Running Exister backup would download individual files one by one,
which would be very timeconsuming/expensive.
 * Therefore manually download initial bulk files via scp -r $user@remote-machine:~/remote-dir (e.g. remote-dir = web) . 
 * Note: Beware of symlinks to avoid duplicate downloads. Typically download whole web directory, let Exsiter pick up small files.

#### Hash local repository and remote backup sites
* Create local md5/file pair hashes in $backupRootDir/exsiter-backup/localFileNameToMd5Map.csv
* Get remote md5/file pair hashes SshShellCommandExecutor.LIST_ALL_FILES_AND_THEIR_CHECKSUMS 
 * Executes find . -type f | xargs md5sum
 * Returns listing of all files as pairs: 56a329926a92460b9b6ac1377f610e48 ./web/newsletter/grip-it.jpg
 * Parse into FileLocationMd5Pair to create Map<String, String> fileWithPath to its MD5 hash sum in hex
 * Store in $backupRootDir/exsiter-backup/remoteFileNameToMd5Map.csv file

#### Compare local and remote hashes
* Do comparison of localFileNameToMd5Map.csv and remoteFileNameToMd5Map.csv
 * Iterates through all entries in the remote map
  * If filename/md5 hash are unchanged against local map, do nothing
  * If filename did not exist in local version or had a different md5 hash (file was updated)
   * Download new/updated file via ScpTool
   * Add downloaded file under $backupRootDir/exsiter-backup/remote-content-dir with correct file path and name
  * Iterates through diff of keys (file names) that were in the local map but not the remote (deleted files on target)
   * Delete corresponding file under remote-content-dir

#### Perform git add/commit/tag
* Execute logical "git add -u $gitDir/exsiter-backup/" to add all the new/deleted files
* Execute git commit
* Execute git tag "v" + yyyyMMMdd to make rollback easy (if backup gets called multiple times add -1,-2, etc.)

#### Create HTML Report
$backupRootDir/exsiter-backup/backupReport.html

# TODO and Future Enhancements
* Automate initial manual setup
* Execute search through current $gitDir/exsiter-backup/target-web-dir/ for suspicious content and flag those files
* Download and analyze logs for suspicious content (big files!)

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
* org.medale.exsiter.ExsiterScheduler - main entry point for running Quartz scheduler, which calls main backup
 
 


