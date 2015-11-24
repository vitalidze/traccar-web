---
layout: default
title: Features
---

This project has following features, which don't exist in original `traccar-web` project:

* converted traccar-web to maven project to simplify builds
* following device
* printing device name
* recording device trace
* printing time stamps at device trace points (frequency is configured in Settings >> Preferences) both for archive and recorded trace
* translations to
    * [russian language](russian.html) (available via ?locale=ru URL parameters)
    * [german language](german.html) (available via ?locale=de URL parameter)
    * [italian language](italian.html) (available via ?locale=it URL parameter)
    * [spanish language](spanish.html) (available via ?locale=es URL parameter)
    * [polish language](polish.html) (available via ?locale=pl URL parameter)
    * tagalog language (available via ?locale=tl URL parameter)
    * [hungarian](hungarian.html) language (available via ?locale=hu URL parameter)
    * [portuguese language](portuguese.html) (available via ?locale=pt_PT URL parameter)
    * [lithuanian language](lithuanian.html) (available via ?locale=lt URL parameter)
    * [dutch language](dutch.html) (available via ?locale=nl URL parameter)
    * [swedish language](swedish.html) (available via ?locale=sv_SE URL parameter)
    * [bulgarian language](bulgarian.html) (available via ?locale=bg URL parameter)
    * [french language](french.html) (available via ?locale=fr URL parameter)
    * [portuguese brazilian language](portuguese-brazilian.html) (available via ?locale=pt_BR URL parameter)
    * [danish language](danish.html) (available via ?locale=da URL parameter)
    * [czech language](czech.html) (available via ?locale=cs URL parameter)
    * [serbian language](serbian.html) (available via ?locale=sr URL parameter)
    * [hebrew language](hebrew.html) (available via ?locale=he URL parameter)
    * [persian language](persian.html) (available via ?locale=fa URL parameter)
    * [greek language](greek.html) (available via ?locale=el_GR URL parameter)
* 'show server log' menu for viewing `tracker-server.log` file on Web
* 'show wrapper.log' menu for viewing `wrapper.log` file on Web 
* [archive filtering](archive-filtering.html)
* change default map center position, zoom, provider (Google, OSM, Bing) (in Settings >> Preferences)
* replaced 'current state' panel with popups. They appear either when you hover a row in 'devices' table or when you hover a marker on map
* archive panel is collapsed by default. There is a button in upper right hand corner to expand it
* new 'managers' functionality:
* added new role to traccar - manager. Managers can add users and can manage access between devices of their users. So they have access to their devices and to devices of all managed users.
* administrators have full access to everything. They can manage all devices, all users and access between devices and users
* there is a new menu item called 'Share' to manage access to selected device
* improved performance of positions loading when DB grows up
* fixed issue when devices are not updated between different instances of web browser
* moved DB transaction management and user rights checks outside of data service implementation (AOP) with [google guice](https://github.com/google/guice)
* possibility to detect 'offline' devices - when signal hasn't came for some time (set up in device settings). They will be shown on a map with a marker of different colour and there will be some sign in popup that they are actually offline.
* new device status - idle. It is shown in popup and also there will be a time of idling. Each device got new setting 'Idle when speed is <=', which is zero by default. It may be changed to some reasonable value, which is then used to consider device idle (for example to handle 'satelite drift compansation').
* possibility to restrict ordinary users to manage (i.e. add/edit/delete) devices. Configured in global application settings.
* [possibility to mark user as read-only](read-only-users.html)
* save grid columns settings (sort order, column order, visibility, etc.) in the database
* mobile web UI based on [framework7](http://www.idangero.us/framework7/) with general functions for GPS tracking, available at `m/` URL, i.e. `http://server-ip:8082/m/` Desktop version will redirect automatically to the mobile version when accessed from mobile device unless `nomobileredirect=1` url parameter is not set.
* [RESTful API](rest-api.html). Available at `/traccar/rest/{methodName}` - allows to call methods from `DataService` class.
* ['Address' column in archive (hidden by default)](address-column-in-archive-grid.html)
* 'Distance' column in archive with total distance at the bottom
* [possibility to export archive](archive-export.html) to [CSV](http://en.wikipedia.org/wiki/Comma-separated_values) and [GPX](http://en.wikipedia.org/wiki/GPS_Exchange_Format)
* possibility to import archive from [GPX](http://en.wikipedia.org/wiki/GPS_Exchange_Format)
* [possibility to log in via GET request to a separate servlet](automatic-login.html) (for example, for integration on external sites) - `http://server-ip:8082/traccar/s/login?user=your_username&password=your_password`
* possibility to change other user's password for administrators and managers
* [possibility to change device marker icon](change-device-icon.html)
* put selected device to the center of map every time it's row is clicked by user
* [possibility to change password hashing function to store passwords in database passwords as hash sums instead of plain text](password-hashing.html)
* [notifications via email/pushbullet](notifications.html)
* [archive line and markers style customization](archive-styling.html)
* disable login dialog moving and resizing
* prohibit empty device name and id
* do not create user with name 'admin' when any other user with 'administrator' role present
* [possibility to fit line of archived track the current map view](zoom-to-track.html)
* [language selector at login screen](language-selector-on-login-screen.html)
* [Geo-fences](geofences.html)
* [possibility to upload photo of device/vehicle](upload-device-photo.html)
* [device sensors](sensors.html)
* [device/vehicle maintenance](maintenance.html)
* [snap to roads](snap-to-roads.html)
* added [MapQuest](http://www.mapquest.com/) and [Stamen toner](http://maps.stamen.com/toner/) mapping providers
* added [OpenSeaMap](http://openseamap.org/) seamarks layer
* [reports](reports.html)
* added 'overview' map with the possibility to save it's maximized/minimized state to user's preferences ('take from map' is also working)