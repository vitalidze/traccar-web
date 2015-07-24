---
layout: default
title: Installation
---

### Version 3.x

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Put downloaded `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`). I recommend to do a backup of existing `traccar-web.war` just in case.

4) Update configuration file (located in `conf\traccar.xml` of traccar installation folder):

* add following lines:

Right after `<entry key='web.path'>/opt/traccar/web</entry>` line

     <entry key='web.old'>true</entry>
     <entry key='web.application'>/opt/traccar/traccar-web.war</entry>
    
Replace `/opt/traccar/traccar-web.war` path with the path to your traccar installation (usually it will be same on linux/mac, on windows it is most probably `c:\Program Files\Traccar\traccar-web.war`).

* update following lines:

  - SQL query to select list of GPS tracking devices

Old:

    <entry key='database.selectDevicesAll'>
        SELECT * FROM device;
    </entry>
    
New:

    <entry key='database.selectDevicesAll'>
        SELECT * FROM devices;
    </entry>

  - SQL query to insert a single position record into the database

Old:

    <entry key='database.insertPosition'>
        INSERT INTO position (deviceId, serverTime, deviceTime, fixTime, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, NOW(), :time, :time, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :other);
    </entry>
    
New:

    <entry key='database.insertPosition'>
        INSERT INTO positions (device_id, time, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, :time, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :other);
    </entry>

  - SQL query to select latest positions
  
Old:

    <entry key='database.selectLatestPositions'>
        SELECT * FROM position WHERE id IN (SELECT positionId FROM device);
    </entry>
    
New:
    
    <entry key='database.selectLatestPositions'>
        SELECT * FROM positions WHERE id IN (SELECT latestPosition_id FROM devices);
    </entry>

  - SQL query to update database with latest info from device

Old:

    <entry key='database.updateLatestPosition'>
        UPDATE device SET positionId = :id WHERE id = :deviceId;
    </entry>

New:

    <entry key='database.updateLatestPosition'>
        UPDATE devices SET latestPosition_id = :id WHERE id = :deviceId;
    </entry>
    
4) Start Traccar service

5) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)

**IMPORTANT NOTE**: existing data of original web interface will not be accessible because it is stored in other tables. It can be copied manually with SQL queries.

### Version 2.x

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Replace `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`) with downloaded one. I recommend to do a backup of existing `traccar-web.war` just in case.

4) Start Traccar service

5) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)