---
layout: default
title: Installation
---

### Version 3.7

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Put the downloaded `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`). I recommend to do a backup of existing `traccar-web.war` just in case.

4) Update the configuration file (located in `conf\traccar.xml` of traccar installation folder):

* add the following lines right after `<entry key='web.path'>/opt/traccar/web</entry>`:

      <entry key='web.type'>old</entry>
      <entry key='web.application'>/opt/traccar/traccar-web.war</entry>

* Replace `/opt/traccar/traccar-web.war` path with the path to your traccar installation (usually it will be the same folder on linux/mac, on windows it is most probably `c:\Program Files\Traccar\traccar-web.war`).

5) Disable notification system

Old:

    <entry key='event.enable'>true</entry>
    <entry key='event.overspeedHandler'>true</entry>
    <entry key='event.overspeed.notRepeat'>true</entry>
    <entry key='event.motionHandler'>true</entry>
    <entry key='event.geofenceHandler'>true</entry>
    <entry key='event.alertHandler'>true</entry>
    <entry key='event.ignitionHandler'>true</entry>

New:

    <entry key='event.enable'>false</entry>
    <entry key='event.overspeedHandler'>false</entry>
    <entry key='event.overspeed.notRepeat'>true</entry>
    <entry key='event.motionHandler'>false</entry>
    <entry key='event.geofenceHandler'>false</entry>
    <entry key='event.alertHandler'>false</entry>
    <entry key='event.ignitionHandler'>false</entry>

6) Disable database migrations made by the backend by commenting out the following configuration file entry:

Old:

    <entry key='database.changelog'>/opt/traccar/schema/changelog-master.xml</entry>

New: ( comment out or remove this entry )

    <!-- <entry key='database.changelog'>/opt/traccar/schema/changelog-master.xml</entry> -->

**IMPORTANT NOTE :**
Your database must be empty before first startup. To ensure this please drop and re-create the existing database:

* for a default H2 database this can be done by removing contents of the `data` folder under the traccar installation folder. The database will be automatically re-created on first service start.

* for any other databases like MySQL there are queries to drop and create them. Also this can be done via GUI management tools (like [MySQL Workbench](https://www.mysql.com/products/workbench/)).

**IMPORTANT NOTE :** This will delete all existing data. If it needed to be preserved, then instead of dropping database just use a brand new database with a different name. Then data can be copied between databases using SQL queries or some scripts.

7) Update the following queries:

Old:

    <entry key='database.insertPosition'>
       INSERT INTO positions (deviceId, protocol, serverTime, deviceTime, fixTime, valid, latitude, longitude, altitude, speed, course, address, attributes)
       VALUES (:deviceId, :protocol, :now, :deviceTime, :fixTime, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

New:

    <entry key='database.insertPosition'>
        INSERT INTO positions (device_id, protocol, serverTime, time, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, :protocol, :now, :deviceTime, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

-------------------

Old:

    <entry key='database.selectLatestPositions'>
        SELECT * FROM positions WHERE id IN (SELECT positionId FROM devices);
    </entry>

New:

    <entry key='database.selectLatestPositions'>
        SELECT id, protocol, device_id AS deviceId, serverTime, time AS deviceTime, time AS fixTime,
        valid, latitude, longitude, altitude, speed, course, address, other AS attributes
        FROM positions WHERE id IN (SELECT latestPosition_id FROM devices);
    </entry>

-------------------

Old:

    <entry key='database.updateLatestPosition'>
        UPDATE devices SET positionId = :id WHERE id = :deviceId;
    </entry>

New:

    <entry key='database.updateLatestPosition'>
        UPDATE devices SET latestPosition_id = :id WHERE id = :deviceId;
    </entry>

--------------------

Old:

    <entry key='database.selectUsersAll'>
        SELECT * FROM users;
    </entry>

New:

    <entry key='database.selectUsersAll'>
        SELECT id, login AS name, email, readOnly AS readonly, admin FROM users;
    </entry>

------------------

Old:

    <entry key='database.selectDevicePermissions'>
        SELECT userId, deviceId FROM user_device;
    </entry>

New:

    <entry key='database.selectDevicePermissions'>
        SELECT u.id AS userId, d.id AS deviceId FROM users u, devices d WHERE u.admin=1
        UNION
        SELECT ud.users_id AS userId, ud.devices_id AS deviceId FROM users_devices ud
        INNER JOIN users u ON ud.users_id=u.id
        WHERE u.admin=0 AND u.readOnly=0
    </entry>

-------------------

Old:

    <entry key='database.linkDevice'>
        INSERT INTO user_device (userId, deviceId) VALUES (:userId, :deviceId);
    </entry>

New:

    <!-- ( comment out or remove this query )
    <entry key='database.linkDevice'>
        INSERT INTO user_device (userId, deviceId) VALUES (:userId, :deviceId);
    </entry> -->
    
-------------------

Old:

    <entry key='database.updateDeviceStatus'>
        UPDATE devices SET status = :status, lastUpdate = :lastUpdate, motion = :motion WHERE id = :id
    </entry>

New:

    <entry key='database.updateDeviceStatus'>
        UPDATE devices SET status = :status, lastUpdate = :lastUpdate WHERE id = :id;
    </entry>
    
-------------------

Old:

    <entry key='database.ignoreUnknown'>true</entry>
    
New:

    <entry key='database.ignoreUnknown'>false</entry>


7a) **Only for the first time installation, i.e. not when upgrading from previous versions when the database is already present**

Temporarily comment out the following queries.

-------------------

Old:

    <entry key='database.selectDevicesAll'>
        SELECT * FROM devices;
    </entry>
    
New:

    <!-- entry key='database.selectDevicesAll'>
        SELECT * FROM devices;
    </entry -->
    
-------------------

Old:

    <entry key='database.selectGroupsAll'>
        SELECT * FROM groups;
    </entry>
    
New:

    <!-- entry key='database.selectGroupsAll'>
        SELECT * FROM groups;
    </entry -->


8) Start Traccar service

8a) **Only for the first time installation, i.e. not when upgrading from previous versions when the database is already present**

Stop Traccar service. Then uncomment queries, which were commented out in step 7a:

-------------------

Old:

    <!-- entry key='database.selectDevicesAll'>
        SELECT * FROM devices;
    </entry -->
    
New:

    <entry key='database.selectDevicesAll'>
        SELECT * FROM devices;
    </entry>
    
-------------------

Old:

    <!-- entry key='database.selectGroupsAll'>
        SELECT * FROM groups;
    </entry -->
    
New:

    <entry key='database.selectGroupsAll'>
        SELECT * FROM groups;
    </entry>
    
-------------------

Start Traccar service.

9) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)

### Version 3.6

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Put the downloaded `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`). I recommend to do a backup of existing `traccar-web.war` just in case.

4) Update the configuration file (located in `conf\traccar.xml` of traccar installation folder):

* add the following lines right after `<entry key='web.path'>/opt/traccar/web</entry>`:

      <entry key='web.type'>old</entry>
      <entry key='web.application'>/opt/traccar/traccar-web.war</entry>

* Replace `/opt/traccar/traccar-web.war` path with the path to your traccar installation (usually it will be the same folder on linux/mac, on windows it is most probably `c:\Program Files\Traccar\traccar-web.war`).

5) Disable notification system

Old:

    <entry key='event.enable'>true</entry>
    <entry key='event.suppressRepeated'>60</entry>
    <entry key='event.overspeedHandler'>true</entry>
    <entry key='event.globalSpeedLimit'>90</entry>
    <entry key='event.motionHandler'>true</entry>
    <entry key='event.geofenceHandler'>true</entry>

New:

    <entry key='event.enable'>false</entry>
    <entry key='event.suppressRepeated'>60</entry>
    <entry key='event.overspeedHandler'>false</entry>
    <entry key='event.globalSpeedLimit'>90</entry>
    <entry key='event.motionHandler'>false</entry>
    <entry key='event.geofenceHandler'>false</entry>

6) Disable database migrations made by the backend by commenting out the following configuration file entry:

Old:

    <entry key='database.changelog'>/opt/traccar/schema/changelog-master.xml</entry>

New: ( comment out or remove this entry )

    <!-- <entry key='database.changelog'>/opt/traccar/schema/changelog-master.xml</entry> -->

**IMPORTANT NOTE :**
Your database must be empty before first startup. To ensure this please drop and re-create the existing database:

* for a default H2 database this can be done by removing contents of the `data` folder under the traccar installation folder. The database will be automatically re-created on first service start.

* for any other databases like MySQL there are queries to drop and create them. Also this can be done via GUI management tools (like [MySQL Workbench](https://www.mysql.com/products/workbench/)).

**IMPORTANT NOTE :** This will delete all existing data. If it needed to be preserved, then instead of dropping database just use a brand new database with a different name. Then data can be copied between databases using SQL queries or some scripts.

7) Update the following queries:

Old:

    <entry key='database.insertPosition'>
       INSERT INTO positions (deviceId, protocol, serverTime, deviceTime, fixTime, valid, latitude, longitude, altitude, speed, course, address, attributes)
       VALUES (:deviceId, :protocol, :now, :deviceTime, :fixTime, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

New:

    <entry key='database.insertPosition'>
        INSERT INTO positions (device_id, protocol, serverTime, time, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, :protocol, :now, :deviceTime, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

-------------------

Old:

    <entry key='database.selectLatestPositions'>
        SELECT * FROM positions WHERE id IN (SELECT positionId FROM devices);
    </entry>

New:

    <entry key='database.selectLatestPositions'>
        SELECT id, protocol, device_id AS deviceId, serverTime, time AS deviceTime, time AS fixTime,
        valid, latitude, longitude, altitude, speed, course, address, other AS attributes
        FROM positions WHERE id IN (SELECT latestPosition_id FROM devices);
    </entry>

-------------------

Old:

    <entry key='database.updateLatestPosition'>
        UPDATE devices SET positionId = :id WHERE id = :deviceId;
    </entry>

New:

    <entry key='database.updateLatestPosition'>
        UPDATE devices SET latestPosition_id = :id WHERE id = :deviceId;
    </entry>

--------------------

Old:

    <entry key='database.selectUsersAll'>
        SELECT * FROM users;
    </entry>

New:

    <entry key='database.selectUsersAll'>
        SELECT id, login AS name, email, readOnly AS readonly, admin FROM users;
    </entry>

------------------

Old:

    <entry key='database.selectDevicePermissions'>
        SELECT userId, deviceId FROM user_device;
    </entry>

New:

    <entry key='database.selectDevicePermissions'>
        SELECT u.id AS userId, d.id AS deviceId FROM users u, devices d WHERE u.admin=1
        UNION
        SELECT ud.users_id AS userId, ud.devices_id AS deviceId FROM users_devices ud
        INNER JOIN users u ON ud.users_id=u.id
        WHERE u.admin=0 AND u.readOnly=0
    </entry>

-------------------

Old:

    <entry key='database.linkDevice'>
        INSERT INTO user_device (userId, deviceId) VALUES (:userId, :deviceId);
    </entry>

New:

    <!-- ( comment out or remove this query )
    <entry key='database.linkDevice'>
        INSERT INTO user_device (userId, deviceId) VALUES (:userId, :deviceId);
    </entry> -->
    
-------------------

Old:

    <entry key='database.updateDeviceStatus'>
        UPDATE devices SET status = :status, lastUpdate = :lastUpdate, motion = :motion WHERE id = :id
    </entry>

New:

    <entry key='database.updateDeviceStatus'>
        UPDATE devices SET status = :status, lastUpdate = :lastUpdate WHERE id = :id;
    </entry>

8) Start Traccar service

9) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)

### Version 3.x - 3.5

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Put downloaded `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`). I recommend to do a backup of existing `traccar-web.war` just in case.

4) Update configuration file (located in `conf\traccar.xml` of traccar installation folder):

* add following lines:

For v.3.0 and 3.1 right after `<entry key='web.path'>/opt/traccar/web</entry>` line:

    <entry key='web.old'>true</entry>
    <entry key='web.application'>/opt/traccar/traccar-web.war</entry>

For v. 3.2 and later right after `<entry key='web.path'>/opt/traccar/web</entry>` line:     

    <entry key='web.type'>old</entry>
    <entry key='web.application'>/opt/traccar/traccar-web.war</entry>   

Replace `/opt/traccar/traccar-web.war` path with the path to your traccar installation (usually it will be same on linux/mac, on windows it is most probably `c:\Program Files\Traccar\traccar-web.war`).

* update following lines:

  - SQL query to select list of GPS tracking devices (does not apply to 3.3 and higher (3.4, 3.5, etc.))

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
        INSERT INTO position (deviceId, protocol, serverTime, deviceTime, fixTime, valid, latitude, longitude, altitude, speed, course, address, attributes)
        VALUES (:deviceId, :protocol, CURRENT_TIMESTAMP(), :time, :time, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

New (depending on version of traccar):

    <!-- for 3.0 and 3.1 -->
    <entry key='database.insertPosition'>
        INSERT INTO positions (device_id, protocol, serverTime, time, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, :protocol, CURRENT_TIMESTAMP(), :time, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :other);
    </entry>

    <!-- for 3.2 and 3.3 and 3.4 -->
    <entry key='database.insertPosition'>
        INSERT INTO positions (device_id, protocol, serverTime, time, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, :protocol, CURRENT_TIMESTAMP(), :time, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

    <!-- for 3.5 -->
    <entry key='database.insertPosition'>
        INSERT INTO positions (device_id, protocol, serverTime, time, valid, latitude, longitude, altitude, speed, course, address, other)
        VALUES (:deviceId, :protocol, :now, :deviceTime, :valid, :latitude, :longitude, :altitude, :speed, :course, :address, :attributes);
    </entry>

  - SQL query to select latest positions

Old:

    <entry key='database.selectLatestPositions'>
        SELECT * FROM position WHERE id IN (SELECT positionId FROM device);
    </entry>

New (depending on version of traccar):

    <!-- for 3.0 and 3.1 -->
    <entry key='database.selectLatestPositions'>
        SELECT id, protocol, device_id AS deviceId, serverTime, time AS deviceTime, time AS fixTime,
        valid, latitude, longitude, altitude, speed, course, address, other
        FROM positions WHERE id IN (SELECT latestPosition_id FROM devices);
    </entry>

    <!-- for 3.2 and 3.3 and 3.4 and 3.5 -->
    <entry key='database.selectLatestPositions'>
        SELECT id, protocol, device_id AS deviceId, serverTime, time AS deviceTime, time AS fixTime,
        valid, latitude, longitude, altitude, speed, course, address, other AS attributes
        FROM positions WHERE id IN (SELECT latestPosition_id FROM devices);
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

4.2) **Specific to v3.2 (and higher: 3.3, 3.4, 3.5, etc.) of traccar**

To enable 'Send commands' functionality it is necessary to update the configuration file entries as follows

  - SQL query to select all available users

Old:

    <entry key='database.selectUsersAll'>
        SELECT * FROM "user";
    </entry>

New:

    <entry key='database.selectUsersAll'>
        SELECT id, login AS name, email, readOnly AS readonly, admin FROM users;
    </entry>

  - SQL query to select permissions for users

Old:

    <entry key='database.getPermissionsAll'>
        SELECT userId, deviceId FROM user_device;
    </entry>

New (depending on version of traccar):

    <!-- for 3.2, 3.3 and 3.4 -->
    <entry key='database.getPermissionsAll'>
        SELECT u.id AS userId, d.id AS deviceId FROM users u, devices d WHERE u.admin=1
        UNION
        SELECT ud.users_id AS userId, ud.devices_id AS deviceId FROM users_devices ud
        INNER JOIN users u ON ud.users_id=u.id
        WHERE u.admin=0 AND u.readOnly=0
    </entry>

    <!-- for 3.5 -->
    <entry key='database.selectDevicePermissions'>
        SELECT u.id AS userId, d.id AS deviceId FROM users u, devices d WHERE u.admin=1
        UNION
        SELECT ud.users_id AS userId, ud.devices_id AS deviceId FROM users_devices ud
        INNER JOIN users u ON ud.users_id=u.id
        WHERE u.admin=0 AND u.readOnly=0
    </entry>

  - SQL query to link device and user

Old:

    <entry key='database.linkDevice'>
        INSERT INTO user_device (userId, deviceId) VALUES (:userId, :deviceId);
    </entry>

New: ( **comment out or remove this query** )

    <!-- entry key='database.linkDevice'>
        INSERT INTO user_device (userId, deviceId) VALUES (:userId, :deviceId);
    </entry -->

4.3) **Specific to v. 3.3 (and higher: 3.4, 3.5, etc.) of traccar**

For new installation over Traccar 3.3 (and higher: 3.4, 3.5, etc.) and after upgrade of Traccar backend (i.e. from 3.2 to 3.3/3.4/3.5) there are two additional steps:

a) Disable database migrations made by the backend by commenting out the following configuration file entry:

Old:

    <entry key='database.changelog'>./database/changelog-master.xml</entry>

New: ( **comment out or remove this entry** )

    <!-- entry key='database.changelog'>./database/changelog-master.xml</entry -->

b) Database must be empty before first startup. To ensure this please drop and re-create existing database:

* for default H2 database this can be done by removing contents of `data` folder under the traccar installation folder. This database will be created automatically when service starts.

* for any other databases like MySQL there are queries to drop and create them. Also this can be done via GUI management tools (like MySQL workbench).

**IMPORTANT NOTE** : this will delete all existing data. If it needed to be preserved, then instead of dropping database just use a brand new database with a different name. Then data can be copied between databases using SQL queries or some scripts.


5) Start Traccar service

6) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)

**IMPORTANT NOTE**: existing data of original web interface will not be accessible because it is stored in other tables. It can be copied manually with SQL queries.

### Version 2.x

1) Download latest build from [http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war](http://myultrashare.appspot.com/s/traccar-web/dev/latest/traccar-web.war)

2) Stop Traccar service.

3) Replace `traccar-web.war` in Traccar installation folder (`/opt/traccar` or `c:\Program Files\Traccar`) with downloaded one. I recommend to do a backup of existing `traccar-web.war` just in case.

4) Start Traccar service

5) If necessary clear web browser cookies related to your traccar web UI. In chrome this can be done like said [here](http://superuser.com/questions/548096/how-can-i-clear-cookies-for-a-single-site)
