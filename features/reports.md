---
layout: feature
title: Reports
---

Reports provide additional information/statistics about object activity for the selected period of time. 

Reports screen
--------------

The Reports screen is accessed by clicking `Reports` button on the top panel of the web UI.
 
![Reports screen](http://i63.tinypic.com/9s9chd.png)

Define report settings:

* **Name** - name of report, will be printed at the top
* **Type** - type of report, defines the layout and data printed on report. For now the following types are available:
    * **General information** - cumulative information like route length, top speed, stops/movement duration, etc. It is possible to include map with drawing of route in the report.
    * **Drives and stops** - objects stops and driving information: period, duration, stop position (with address if available). The cumulative information like on 'General information' report is printed as well.
    * **Mileage detail** - objects daily mileage along with time of movement start and end.
    * **Overspeeds** - shows periods of time when objects were exceeding set up speed limit (in profile) along with top/average speed and position
    * **Geo-fence In/Out** - entry/exit time of each zone, time spent in zone
    * **Events** - objects events information. This report is based on events saved in database, which are also used to send notifications.
* **Include map** - check this box to include map with graphic information based on data from report. For now this is supported only for **General information** report type and it will include map with route for the specified period of time.
* **Devices** - select devices to be included on report. *Important note*: if no devices are selected then report will run for all devices, which are accessible by current user.
* **Geo-fences** - select geo-fences for the report. If report does not support geo-fences then this list will be grayed out. *Important note*: if no geo-fences are selected then report will run for all geo-fences, which are accessible by current user.
* **Time period** - specify the period of the report. It is also possible to select one of the predefined periods in the drop-down list. *Important note*: if report details are saved and there is something other than `Custom` selected in drop-down list then when report is loaded the period will always be recalculated based on the current date and time.
   
Managing report settings
------------------------
   
All settings defined for report may be saved in database to be re-used later. Just click 'Save' button to store the settings. All stored report settings will be listed right under Report screen toolbar. To remove settings select necessary line and click on 'Remove'. 

The access policy for the reports is similar to the access to devices: 
* every ordinary user will see a list of stored reports available only for him. 
* 'Manager' will see both his reports and reports of underlying users
* 'Administrator' has access to all reports of all users

Generating reports
------------------

To generate report click on 'Generate' button at the top-right corner of the screen. It is not necessary to save report settings to database before generation. The new screen will pop up with the output file of report.

Several notable points about report calculation:

* during history processing the same filter as for archive is applied. It is possible to configure it from the screen opened by clicking 'Filter...' button on the archive toolbar. Most probably in future it will be possible to configure filtering per report.
* as already mentioned in report fields list the empty selection of devices/geo-fences means that all available entities must be included on report
* the 'Speed limit' setting from the device profile is used for overspeeding detection
* the 'Min. idle time' setting from the device profile is used to detect stop/movement