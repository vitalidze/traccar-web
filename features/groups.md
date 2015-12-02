---
layout: feature
title: Device groups
---

Managing groups
---------------

To manage groups use the new 'Groups' menu from the toolbar at the top of screen. Once opened there will be a table of currently defined groups and their descriptions.

![Groups table](http://i67.tinypic.com/4qiuyv.png)

Use 'Add' and 'Remove' buttons to add or remove new groups. Set up name and description right in table.

Sharing group
-------------

First of all access to groups is a bit different from access to devices (and is similar to the access policy of geo-fences):

* user has access to his groups, and all groups of users in his upper hierarchy (his manager, manager of his manager, etc.). Difference with devices access is that user sees only his devices.
* manager in addition to groups as an ordinary user has access to groups of all users managed by him.
* administrator has access to all groups of all users.

To manage access to groups administrator or manager should select it in a 'Groups' screen and click 'Share' button.

For now user will still see a group even if he don't have direct access to it. But he won't be able to neither change group nor assign another group to the device. Moreover, by default in most cases user will have access to the group created by the user from the upper-level (i.e. administrator or manager).

Set up group for device
-----------------------

In the device profile screen there is a drop-down list with all available groups. If at least one device has a group defined then the device list in 'Objects' panel will show group names with possibility to fold/unfold. All devices without group are moved to a special 'No group' group.