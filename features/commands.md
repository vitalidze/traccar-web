---
layout: feature
title: Commands
---

Requirements
------------

First of all the version of traccar backend installed must be at least 3.2. There is no proper/stable way to communicate with devices directly from the web UI. Thus, to send commands it uses the backend API, which can be exposed with simultaneously running web UI mod only since version 3.2. Please follow [installation instructions](/installation.html) specific to the version 3.2 before using the 'Commands' functionality.

If backend API is not available then the 'Command' button will be grayed out.

Sending command
---------------

Select the necessary device in the 'Objects' view and click on the 'Command' button. Then select a command in the drop down list, fill parameters (if any) and click send.

![Commands dialog](http://i68.tinypic.com/24glp94.png)

The result of sending command will appear in a popped up window

![Result of sending command](http://i65.tinypic.com/2nklap0.png)

Additional parameters
---------------------

At the moment following commands support additional parameters:

* Position periodic - frequency and frequency unit (second, minute, hour)
* Set timezone - the actual time zone to set
* Custom - a field to enter generic raw command, which will then be sent to the device

Limitations
-----------

Currently not all protocols even support sending of commands. Most of them support just a subset of all available commands. For now the web UI does not limit this, so if command is not supported the result of sending should contain some error message.

The access to the device is checked by the backend. However, it's permissions schema is different from the schema defined by web UI mod. In particular:
 
* there is no concept of 'manager'. Thus, if manager does not have direct access to the device he won't be able to send command to it. In terms of sending commands 'manager' user is considered as ordinary user.
* 'Administrator' should be able to send command to any device
* ordinary user will be able to send command only to devices, which he has access to. 