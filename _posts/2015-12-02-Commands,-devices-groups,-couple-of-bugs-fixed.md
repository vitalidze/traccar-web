---
layout: news
---

Today's major release includes two new big features:

* sending of [commands](/features/commands.html) - works only with traccar v3.2 (and I hope above)
* define [groups](/features/groups.html) for devices

Couple of other features added:

* [Macedonian](/features/macedonian.html) translation (thanks to Ivan)
* default notification templates for the 'Stopped'/'Moving' events
* new placeholders with information from position for notification messages templates ([#364](https://github.com/vitalidze/traccar-web/issues/364))

Fixed bugs:

* issue with 'Sopped'/'Moving' events detection. It just wasn't working properly in a long time period
* 'Edit' button in 'Objects' view was enabled even without any device selection

Updated following translations:

* [Bulgarian](/features/bulgarian.html)
* [Czech](/features/czech.html)
* [Dutch](/features/dutch.html)
* [German](/features/german.html)
* [Greek](/features/greek.html)
* [French](/features/french.html)
* [Italian](/features/italian.html)
* [Polish](/features/polish.html)
* [Portuguese (Brazil)](/portuguese-brazilian.html)
* [Portuguese (Portugal)](/features/portuguese.html)
* [Russian](/features/russian.html)
* [Spanish](/features/spanish.html)

**Reminder**

The translations are now reside on [transifex](https://www.transifex.com/traccar-web-ui-mod/traccar-web/) project. Everyone interested in updating existing/submitting new translations are welcome to the web site. Of course, it is still possible to send translations as the text files by email. But the project on transifex is the preferred and I believe simpler way to help with translation.

Since translations makes compilation time much longer and many of them are outdated I decided to start removing translations, which have less than 50% strings translated. If starting from this moment no one will update the following translations within two weeks they will be removed:

* lithuanian
* tagalog

**Request for help**

Right now besides the development much time is being spent for "paper" work:

* building the release
* writing release notes
* writing articles
* updating the web site
* closing resolved issues with references

If someone wants to take part in doing this work it would be much appreciated. The email for contact can be found in [this github profile](https://github.com/vitalidze).