---
layout: feature
title: Automatic login
---

This feature allows to login to traccar web UI using special url `http://server-ip:8082/traccar/s/login?user=your_username&password=your_password`. This may be useful when integrating with some external web site (or "Single Sign On"). If authentication was successful browser is redirected to the "desktop" version of traccar web UI, otherwise it prints "Authentication failed" message (can be used to check auth via XHR).

Also there is a possibility to not specify password itself and to use it's hash sum instead. To do that:

1) Your web UI should have non-plain [password hashing setting](password-hashing.html)

2) URL must contain additional parameter `password_hashed` with value `1`: `http://server-ip:8082/traccar/s/login?user=your_username&password=your_password_hash_sum&password_hashed=1`