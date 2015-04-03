---
layout: feature
title: Geo-fences
---

This long-waiting issue was added in scope of [issue #100](https://github.com/vitalidze/traccar-web/issues/100). List of geo-fences is available at the left side of screen on 'Geo-fences' tab.

![Geo-fences tab](http://i59.tinypic.com/kdmp34.png)

Once switched to that tab 'Add/Edit/Share/Delete' buttons at top are acting for Geo-fences. When current tab is 'Devices' then they are working for devices.

Adding new geo-fence
--------------------

To start adding new geo-fence click on 'Add' button when 'Geo-fences' tab is selected. New window pops up with geo-fence settings.

![Add geo-fence](http://i62.tinypic.com/5fhn9y.png)

Select it's type (line, polygon or circle) and start drawing. Depending on type of geo-fence drawing is done as follows:

* line - put line points on map with mouse pointer. Double-click on last point to finish drawing.

![Drawing line](http://i60.tinypic.com/bj62wp.png)

* polygon - put polygon points on map with mouse pointer. Double-click on last point to finish drawing.

![Drawing polygon](http://i58.tinypic.com/t5hpae.png)

* circle - put circle center on map with mouse pointer. Once center point is put drawing finishes.

![Drawing circle](http://i62.tinypic.com/24dpkyt.png)

Formatting (color, line width) is not applied while drawing from scratch. Once drawing is finished it goes to 'edit' mode, which has formatting applied. There are two ways to get back to 'drawing' mode from here: 'clear' drawing or change geo-fence type (which also clears drawing).

**IMPORTANT NOTE**: Check box 'Apply to all devices' changes class of geo-fence. For now there are two classes:

* global - when 'Apply to all devices' is checked. Geo-fence will be applied to all accessible devices despite any settings on 'Devices' tab. This works for notifications as well - user will receive notification when any accessible device enters/exits this geo-fence. On map these geo-fences are always visible. This class is selected by default.

* device-specific - when 'Apply to all devices' is unchecked. Geo-fence will be applied to devices selected on 'devices' tab. On map these geo-fences are visible only when either it is selected in geo-fences list or when specific device is selected (in such case all geo-fences for the device will be shown). This also works for notifications - geo-fence enter/exit events are posted only for configured devices.

![Configuring devices for geo-fence](http://i62.tinypic.com/245blo8.png)

Once finished click 'Save' to save changes to the database.

Editing geo-fence
-----------------

To start editing select geo-fence in a list on 'Geo-fences' tab and click 'edit' button. Also 'edit' mode is activated once drawing is finished. All changes in formatting (like width/radius or color) are reflected immediately on geo-fence drawing.

![Editing polygon](http://i57.tinypic.com/35ic3rl.png)

Once finished click 'Save' to save changes to the database.

Deleting geo-fence
------------------

To delete a geo-fence select in a list on 'Geo-fences' tab, click 'Remove' on toolbar and confirm it's removal.

Sharing geo-fence
-----------------

First of all access to geo-fences is a bit different from access to devices:

* user has access to his geo-fences, and all geo-fences of users in his upper hierarchy (his manager, manager of his manager, etc.). Difference with devices access is that user sees only his devices.
* manager in addition to geo-fences as an ordinary user has access to geo-fences of all users managed by him.
* administrator has access to all geo-fences of all users.

To manage access to geo-fence administrator or manager should select it in a list on 'Geo-fences' tab and click 'Share' button as he uses for access management to devices.

![Sharing geo-fence](http://i62.tinypic.com/1zme8o3.png)

Viewing geo-fence
-----------------

As for now geo-fence is displayed in popup of device if it's currently in that geo-fence.

![Geo-fence in popup](http://i58.tinypic.com/6jpvh4.png)

Then it is displayed in side bar of mobile UI:

![Geo-fence in mobile UI sidebar](http://i62.tinypic.com/kak08y.png)

Notifications
-------------

For now there are two types of notifications:

* device entered geo-fence
* device exited geo-fence

Couple of words on what is under cover. Events are recorded by scanning latest positions assuming their identifiers are growing. When server starts all geo-fence events are recorded since latest positions. Positions processing starts every minute.

[Notifications](notifications.html) are configured in separate screen. User will receive notification only if he has access to both device and geo-fence. Administrators receive all notifications.