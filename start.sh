#!/bin/sh

/usr/bin/rm /opt/software/ink-photo-album/cache

while true
do
        cd /opt/software/ink-photo-album
        /usr/bin/sudo /usr/bin/java -jar /opt/software/ink-photo-album/ink-photo-album-1.0-SNAPSHOT-all.jar
        sleep 5m
done