---
layout: default
---

Today's release contains new translation to [Czech language](/features/czech.html)  (thanks to [RobertBenedikt](https://github.com/RobertBenedikt) github user). Following new functions were added as well:

* if for some reason server goes down or restarted web UI will show appropriate warning and stop polling server for latest position data
* fixed issue with 'read-only' user, which had 'Add'/'Edit' and 'Delete' button visible (issue [#191](https://github.com/vitalidze/traccar-web/issues/191))
* updated [bulgarian translation](/features/bulgarian.html) (thanks to Boko Kid)
* added logging for the 'login' and 'register' events (issue [#189](https://github.com/vitalidze/traccar-web/issues/191))
* added possibility to block user, set up an expiration date for user account and restrict number of devices a user can add (issue [#175](https://github.com/vitalidze/traccar-web/issues/175))
* added new informative fields to the user account profile: First Name, Last Name, Company Name, Phone Number.

Please note that I will be on vacation since 25.06.2015 to 06.07.2015 so:

1) before upgarding to this build take a backup of the current version (as I always recommend to do just in case)
2) if you experience any issues with latest build please submit them to the [issue tracker](https://github.com/vitalidze/traccar-web/issues) and if they are critical then just roll back to the previous version.

For the brave ones that want to contribute their translations here is the list of strings that were added since previous release (on 14th June 2015, lines starting with '+' were added):

     register = Register
     errUsernameOrPasswordEmpty = User name and password must not be empty
     errInvalidUsernameOrPassword = User name or password is invalid
    +errUserSessionExpired = Session expired
    +errUserDisconnected = Server is not responding
     # application settings dialog
     globalSettings = Global Settings
     registration = Registration
     @@ -101,6 +106,12 @@ 
     deviceEventType[OFFLINE] = Went offline
     deviceEventType[GEO_FENCE_ENTER] = Enter geo-fence
     deviceEventType[GEO_FENCE_EXIT] = Exit geo-fence
     deviceEventType[MAINTENANCE_REQUIRED] = Maintenance required
    +firstName = First name
    +lastName = Last name
    +companyName = Company name
    +expirationDate = Expiration date
    +maxNumOfDevices = Maximum number of devices
    +blocked = Blocked
     # users dialog
     confirmUserRemoval = Are you sure you want remove user?
     readOnly = Read only