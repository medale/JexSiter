#!/bin/bash
/usr/bin/find $START_DIR -type f | /usr/bin/xargs /usr/bin/md5sum
