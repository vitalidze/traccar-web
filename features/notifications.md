---
layout: feature
title: Notifications
---

Simple Email notifications system that sends messages when devices goes offline.

Setup
-----

First of all, each user that wants to receive notifications must have email field filled and at least one notification event type selected in profile. This is done in `Account` screen opened from the top navigation bar.

![Account settings menu](http://i67.tinypic.com/1e9ipv.png)

![Account settings dialog](http://i67.tinypic.com/2v34pas.png)

Then user with either 'administrator' or 'manager' role in Settings >> Notifications menu should set up one of the following:
 
![Notification settings menu](http://i67.tinypic.com/15j1op.png)

* SMTP server settings

![Notification settings dialog - Email](http://i66.tinypic.com/2ywd6ar.png)

* [Pushbullet](https://www.pushbullet.com/) Access Token

![Notification settings dialog - Pushbullet](http://i66.tinypic.com/28rm7bt.png)

Use 'Test' button to check settings validity. It will just test the connection. No test email or push is sent.

Templates
---------

In Settings >> Notifications menu it is also possible to customize notification message with template. Message template is defined per event type.

![Notification settings dialog - Message templates](http://i64.tinypic.com/15dt8bp.png)

There are placeholders, which are replaced during message generation with actual values. Placeholders may be used both in subject and body of message. Full list of available placeholders is shown at bottom of this dialog window. There is an option to apply custom formatting to the date and time fields using rules from [SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) and following notation:

    ${dateField;date(FORMAT)}
    
For example:

    ${eventTime;date(yyyy.MM.dd HH:mm:ss)}
    
Each template can be tested with sample values using 'Test' button.

Usage
-----

Every minute starts an 'Offline' check for devices. If any device goes offline then an 'Event' is posted, which is scheduled for notification. Then every minute starts notification sender, which finds all available 'Offline' events and sends them to the users that have access to these devices.

Also there are two [geo-fence](geofences.html) events: on enter and on exit.

All new events are always put into a single message with title `[traccar-web] Notification`.

Important notes
---------------

 * Administrators receive notifications for all devices.
 * Notification settings are taken first from current user and then from all managers hierarchy. If none of managers have notification settings then they are taken from some administrator. If no notification settings found then no email will be sent.
 * Once any notification for event is sent it will NEVER be sent again. This also means that until ANY notification is sent event is in 'pending' outbox.
 * User receives only events of types selected in his account profile.

Disable event recording
-----------------------

By default all devices events are always recorded. However, this can be disabled from in Global settings menu (Settings >> Global).

![Disabling event recording](http://i67.tinypic.com/15qvt54.png)

Troubleshooting
---------------

If you are using GMail then by default security settings don't allow access from third party applications (see [#133](https://github.com/vitalidze/traccar-web/issues/133)). This security check can be disabled [here](https://www.google.com/settings/security/lesssecureapps) - select 'turn off' here. You must be logged in to google account before accessing that page.
