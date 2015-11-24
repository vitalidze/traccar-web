---
layout: default
---

Today's release contains several new features:

* possibility to preview reports in new tab without downloading resulting file, just check the 'preview' box in the report profile ([#327](https://github.com/vitalidze/traccar-web/issues/327))
* possibility to disable filter in reports
* report name will be set up automatically equal to selected type name
* events of stopping (idling) and moving along with the appropriate notifications ([#317](https://github.com/vitalidze/traccar-web/issues/317))
* state of 'snap to roads' and 'disable filter' settings from the archive toolbar is now saved into database tied to the user so it is reloaded every time a user logs in ([#350](https://github.com/vitalidze/traccar-web/issues/350))
* added 'overview' map with the possibility to save it's maximized/minimized state to user's preferences ('take from map' is also working) ([#354](https://github.com/vitalidze/traccar-web/issues/354))
* for exporting to CSV separator will be chosen depending on the current user's locale (i.e. language selected), if it uses `,` as the decimal separator then `;` is used as the field separator, in all other cases `,` is used to separate fields in CSV. Also now each exported CSV file will contain a line at the beginning indicating currently used separator character ([#352](https://github.com/vitalidze/traccar-web/issues/352))

Updated translations:

* [German](/features/german.html)
* [French](/features/french.html)
* [Portuguese (Portugal)](/features/portuguese.html)
* [Portuguese (Brazilian)](/features/portuguese-brazilian.html)
* [Spanish](/features/spanish.html)

Also there were several bugs fixed:

* forced UTF-8 encoding for the subject field in emails ([#347](https://github.com/vitalidze/traccar-web/issues/347))
* mileage report wasn't working for devices which don't have speed limit setting ([#353](https://github.com/vitalidze/traccar-web/issues/353))
* positions scanning in 'no markers' mode with 'snap to roads' was producing errors
* devices with very big timeout setting weren't correctly detected as 'offline'

For the countries, where PayPal does not work I have added an option to [donate](/donate.html) through Skrill/Moneybookers.

**Reminder**:

The translations are now reside on [transifex](https://www.transifex.com/traccar-web-ui-mod/traccar-web/) project. Everyone interested in updating existing/submitting new translations are welcome to the web site. Of course, it is still possible to send translations as the text files by email. But the project on transifex is the preferred and I believe simpler way to help with translation.

Since translations makes compilation time much longer and many of them are outdated I decided to start removing translations, which have less than 50% strings translated. If starting from this moment noone will update the following translations within three weeks they will be removed:

* polish
* italian
* lithuanian
* tagalog

**Important note about translations**

If your translation uses single quote characters `'` then they must be escaped with two single quotes, for example in french:

    Erreur d'appel de procédure distante

should be

    Erreur d''appel de procédure distante

This is a major format requirement, please try to follow it during translation.

**Important note about java version upgrade**

This is the first build, which requires minimum java 7 to run. This requirement is also present on original traccar project so I don't think it will cause any trouble to upgrade the version of java.