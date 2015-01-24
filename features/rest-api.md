---
layout: feature
title: RESTful API
---

There is a simplified version of RESTful API, which can be accessed by third-party applications. Current implementation may differ from an ordinary RESTful API. But it works for sure in mobile version of traccar web UI.

So I will describe core concepts here.

1) Prefix for all method calls is in `/traccar/rest`

2) API exposes all methods from [DataService interface](https://github.com/vitalidze/traccar-web/blob/dev/src/main/java/org/traccar/web/client/model/DataService.java) Each method is available by URL, for example `login` method is available at `/traccar/rest/login` URL

3) Both `POST` and `GET` methods are supported.

For `POST` requests body should contain method arguments serialised in JSON array. For example, `login` method accepts `user` and `password` parameters, so to authenticate the following parameters must be provided within request body:

    [“your_username”,”your_password”]

For `GET` requests JSON array must be provided as `payload` parameter. For example: `/traccar/rest/login?payload=[“your_username”,”your_password”]`

4) Errors are returned as `HTTP` status codes. Status code `200` means that request completed successfully, any different code means that it failed.

5) Method call results are serialised in JSON.

6) Most of method calls require authentication. For now authentication data is stored in HTTP session, so prior to any method call the `login` method must be invoked.

Please refer to [mobile web UI javascript code](https://github.com/vitalidze/traccar-web/blob/dev/src/main/webapp/m/js/traccar-mobile.js#L101) as an example of working with current RESTful API

Hope this helps you get started. Feel free to ask me questions.

Examples with cURL
------------------

Authentication info is held in HTTP session, so you should have corresponding cookie in every request after logging in. Below are examples of logging in an fetching data with `curl` (with demo site at http://d.traccar.litvak.su).

* Login (`login` method)

{% highlight bash %}
 ~  curl -v -X POST --data "[\"user\",\"password\"]" http://d.traccar.litvak.su/traccar/rest/login
* Hostname was NOT found in DNS cache
*   Trying 188.166.50.6...
* Connected to d.traccar.litvak.su (188.166.50.6) port 80 (#0)
> POST /traccar/rest/login HTTP/1.1
> User-Agent: curl/7.37.1
> Host: d.traccar.litvak.su
> Accept: */*
> Content-Length: 25
> Content-Type: application/x-www-form-urlencoded
>
* upload completely sent off: 25 out of 25 bytes
< HTTP/1.1 200 OK
* Server nginx/1.2.1 is not blacklisted
< Server: nginx/1.2.1
< Date: Fri, 16 Jan 2015 11:50:26 GMT
< Content-Type: application/json;charset=UTF-8
< Content-Length: 217
< Connection: keep-alive
< Set-Cookie: JSESSIONID=xxxxxxxxxxxx;Path=/
< Expires: Thu, 01 Jan 1970 00:00:00 GMT
<
* Connection #0 to host d.traccar.litvak.su left intact
{"id":2,"login":"user","password_hash_method":"MD5","admin":true,"manager":true,"userSettings":{"speedUnit":"knots","timePrintInterval":10,"zoomLevel":1,"centerLongitude":12.5,"centerLatitude":41.9,"mapType":"OSM"}}%
{% endhighlight %}

Note `Set-Cookie` header with `JSESSIONID`. This should be put in all next requests.

* Load list of devices (`getDevices` method)

{% highlight bash %}
curl -v --cookie "JSESSIONID=xxxxxxxxxxx" http://d.traccar.litvak.su/traccar/rest/getDevices
* Hostname was NOT found in DNS cache
*   Trying 188.166.50.6...
* Connected to d.traccar.litvak.su (188.166.50.6) port 80 (#0)
> GET /traccar/rest/getDevices HTTP/1.1
> User-Agent: curl/7.37.1
> Host: d.traccar.litvak.su
> Accept: */*
> Cookie: JSESSIONID=xxxxxxxxxx
>
< HTTP/1.1 200 OK
* Server nginx/1.2.1 is not blacklisted
< Server: nginx/1.2.1
< Date: Fri, 16 Jan 2015 11:51:57 GMT
< Content-Type: application/json;charset=UTF-8
< Content-Length: 5593
< Connection: keep-alive
<
[{"id":3,"uniqueId":"123","name":"Phone","timeout":300,"idleSpeedThreshold":0.0,"iconType":{"ARCHIVE":{"width":21,"height":25,"urls":["http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker-blue.png","http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker-gold.png"]},"OFFLINE":{"width":63,"height":25,"urls":["/img/long-truck-white.png","/img/long-truck-green.png"]},"LATEST":{"width":63,"height":25,"urls":["/img/long-truck-red.png","/img/long-truck-green.png"]}}}]%
{% endhighlight %}

Take full JSON of device and put it to `getPositions` method.

* Load archived positions (`getPositions` method)

{% highlight bash %}
curl -v --cookie "JSESSIONID=xxxxxxxxx" -X POST --data "[{\"id\":3,\"uniqueId\":\"123\",\"name\":\"Phone\"},\"2015-01-01 00:00:00 GMT\",\"2015-01-01 23:59:59 GMT\",false]" http://d.traccar.litvak.su/traccar/rest/getPositions
* Hostname was NOT found in DNS cache
*   Trying 188.166.50.6...
* Connected to d.traccar.litvak.su (188.166.50.6) port 80 (#0)
> POST /traccar/rest/getPositions HTTP/1.1
> User-Agent: curl/7.37.1
> Host: d.traccar.litvak.su
> Accept: */*
> Cookie: JSESSIONID=xxxxxxx
> Content-Length: 109
> Content-Type: application/x-www-form-urlencoded
>
* upload completely sent off: 109 out of 109 bytes
< HTTP/1.1 200 OK
* Server nginx/1.2.1 is not blacklisted
< Server: nginx/1.2.1
< Date: Fri, 16 Jan 2015 11:56:41 GMT
< Content-Type: application/json;charset=UTF-8
< Content-Length: 2
< Connection: keep-alive
<
* Connection #0 to host d.traccar.litvak.su left intact
[]%
{% endhighlight %}

In this concrete example there are no positions in reply.

* log out (`logout` method)

{% highlight bash %}
~  curl -v --cookie "JSESSIONID=xxxxxxxx" http://d.traccar.litvak.su/traccar/rest/logout
* Hostname was NOT found in DNS cache
*   Trying 188.166.50.6...
* Connected to d.traccar.litvak.su (188.166.50.6) port 80 (#0)
> GET /traccar/rest/logout HTTP/1.1
> User-Agent: curl/7.37.1
> Host: d.traccar.litvak.su
> Accept: */*
> Cookie: JSESSIONID=xxxxxxxx
>
< HTTP/1.1 200 OK
* Server nginx/1.2.1 is not blacklisted
< Server: nginx/1.2.1
< Date: Fri, 16 Jan 2015 11:57:57 GMT
< Content-Type: application/json;charset=UTF-8
< Content-Length: 4
< Connection: keep-alive
<
* Connection #0 to host d.traccar.litvak.su left intact
true%
{% endhighlight %}