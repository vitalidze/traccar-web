---
layout: feature
title: Device visibility management
---

New column named 'visibility' (with eye icon) was added to the devices table. In each row there is a check box, which allows to control visibility of the device on map. The state of the selection is saved into the currently logged in user profile.

Also it is possible to define visibility depending on the state of device/vehicle. All of them are available in the context menu of the visibility column.

![Context menu of visibility column](http://i66.tinypic.com/317jk82.png)

For now there are following options available:

* Idle - hides/shows all devices which have 'Idle' state
* Moving - hides/shows all devices which have non-idle state (i.e. moving)
* Offline - hides/shows all devices which have 'Offline' state, or which are not yet timed out (depending on settings in device profile)
* Online - hides/shows all devices which have non-offline state (i.e. online/connected devices)
* Groups - hides/shows all devices within selected group(s)

All these settings are saved in current user's profile as well. Once unchecked and then depending on the status of device it can be hidden/shown on each status update automatically. Also it is possible to force visibility/invisibility of some device nevertheless of it's status, to do it just check/uncheck the box in 'visibility' column in a row of that device.

It is possible to check/uncheck multiple device states. Between such rules the 'OR' is applied. It means that if 'offline' and 'idle' is unchecked then if device is either idle OR offline it will be hidden.
 
Devices, which not yet have any positions are considered both idle and offline.