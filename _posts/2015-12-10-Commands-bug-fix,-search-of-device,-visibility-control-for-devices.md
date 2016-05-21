---
layout: news
---

Today's release contains fixes for the following issues:

* [#380](https://github.com/vitalidze/traccar-web/issues/380) - send command was always greyed out because presence of backend API was detected incorrectly
* when user tried to share newly added groups there were a 'Remote Procedure Call error'. Now it is prohibited until the group is saved
* [#393](https://github.com/vitalidze/traccar-web/issues/393) - user's timezone was not considered during report generation
* [#396](https://github.com/vitalidze/traccar-web/issues/396) - button 'Style' was disappearing on devices that have screens with small width, which leads to toolbar collapsing

Also there were two major features added:

* [#311](https://github.com/vitalidze/traccar-web/issues/311) filter/search field above the list of devices
* [#118](https://github.com/vitalidze/traccar-web/issues/118) [#205](https://github.com/vitalidze/traccar-web/issues/205) [#382](https://github.com/vitalidze/traccar-web/issues/382) - manage visibility of devices. Read [this article](/features/visibility.html) for additional information
* Added [serbian (latin)](/features/serbian_latin.html) translation

Updated following translations:

* [Dutch](/features/dutch.html)
* [German](/features/german.html)
* [Greek](/features/greek.html)
* [Italian](/features/italian.html)
* [Macedonian](/features/macedonian.html)
* [Polish](/features/polish.html)
* [Portuguese (Brazil)](/features/portuguese-brazilian.html)
* [Portuguese (Portugal)](/features/portuguese.html)
* [Russian](/features/russian.html)
* [Spanish](/features/spanish.html)

**Reminder**

The translations are now reside on [transifex](https://www.transifex.com/traccar-web-ui-mod/traccar-web/) project. Everyone interested in updating existing/submitting new translations are welcome to the web site. Of course, it is still possible to send translations as the text files by email. But the project on transifex is the preferred and I believe simpler way to help with translation.

Since translations makes compilation time much longer and many of them are outdated I decided to start removing translations, which have less than 50% strings translated. If starting from this moment no one will update the following translations within one week they will be removed:

* lithuanian
* swedish
* tagalog