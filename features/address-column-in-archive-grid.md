---
layout: feature
title: Address column in archive grid
---

This column displays address value from archived positions. It is hidden by default, to make it visible use controls of archive grid (available by clicking down arrow button in any column header):

![Columns settings](http://i61.tinypic.com/35irqyc.png)

Please note that by default traccar does not fill that column. To make it work the 'reverse geocoding' must be enabled in traccar configuration file:

    <!-- Geocoder options -->
    <entry key='geocoder.enable'>true</entry>
    <entry key='geocoder.type'>nominatim</entry>
    <entry key='geocoder.url'>http://nominatim.openstreetmap.org/reverse</entry>

IMPORTANT NOTE: nominatim geocoder is not intended for heavy usage. For heavy usage you should host your own instance of reverse geocoder. Please see their [usage policy](http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy).