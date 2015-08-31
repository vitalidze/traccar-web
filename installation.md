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
    
4.1) **Specific to v3.1 of traccar**

* download [jetty-jndi-9.2.13.v20150730.jar](https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jndi/9.2.13.v20150730/jetty-jndi-9.2.13.v20150730.jar) and put it to the `lib` folder in traccar installation folder

* Add following line to the service configuration file (located in `conf/wrapper.conf` of traccar installation folder):

Right after:

    # Java Classpath (include wrapper.jar)  Add class path elements as
    #  needed starting from 1

add line:

    wrapper.java.classpath.3=../lib/jetty-jndi-9.2.13.v20150730.jar
     
* For the first time there will be warnings similar to:


    2015-08-31 17:03:44  WARN: Table 'traccar.positions' doesn't exist - MySQLSyntaxErrorException (... < QueryBuilder.java:289 < DataManager.java:349 < ConnectionManager.java:41 < ...)

or

    2015-08-31 16:48:10  WARN: Table "POSITIONS" not found; SQL statement:
    SELECT * FROM positions WHERE id IN (SELECT latestPosition_id FROM devices); [42102-187] - JdbcSQLException (... < QueryBuilder.java:62 < *:132 < DataManager.java:349 < ConnectionManager.java:41 < ...)
    
This is because necessary tables will be created after the initialisation of `ConnectionManager` on Traccar's backend. To solve it please **restart the Traccar service twice**. 
    
5) Start Traccar service

6) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)

**IMPORTANT NOTE**: existing data of original web interface will not be accessible because it is stored in other tables. It can be copied manually with SQL queries.

### Version 2.x

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Replace `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`) with downloaded one. I recommend to do a backup of existing `traccar-web.war` just in case.

4) Start Traccar service

5) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)