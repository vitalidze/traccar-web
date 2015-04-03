---
layout: feature
title: Notifications
---

Simple Email notifications system that sends messages when devices goes offline.

Setup
-----

First of all, all users that want to receive notifications must have email field filled and 'notifications' checked in profile. This is done in Settings >> Account menu.

![Account settings menu](http://i60.tinypic.com/14kaam1.png)

![Account settings dialog](http://i60.tinypic.com/vg4hzq.png)

Then user with either 'administrator' or 'manager' role should set up SMTP server settings in Settings >> Notifications menu.

![Notification settings menu](http://i62.tinypic.com/w6tklu.png)

![Notification settings dialog](http://i58.tinypic.com/sgn7r5.png)

Use 'Test' button to check settings validity. It will just test the connection. No test email is sent.

Usage
-----

Every minute starts an 'Offline' check for devices. If any device goes offline then an 'Event' is posted, which is scheduled for notification. Then every minute starts notification sender, which finds all available 'Offline' events and sends them to the users that have access to these devices.

Important notes:

 * Administrators receives notifications for all devices.
 * Notification settings are taken first from current user and then from all managers hierarchy. If none of managers have notification settings then they are taken from some administrator. If no notification settings found then no email will be sent.
 * Once any notification for event is sent it will NEVER be sent again. This also means that until ANY notification is sent event is in 'pending' outbox.

Disable event recording
-----------------------

By default all devices events are always recorded. However, this can be disabled from in Global settings menu (Settings >> Global).

![Disabling event recording](http://i58.tinypic.com/wulzti.png)

Troubleshooting
---------------

If you are using GMail then by default security settings don't allow access from third party applications (see [#133](https://github.com/vitalidze/traccar-web/issues/133)). This security check can be disabled [here](https://www.google.com/settings/security/lesssecureapps) - select 'turn off' here. You must be logged in to google account before accessing that page.
