---
layout: news
---

I am happy announce a little update (but important from my point of view) - [Official Self-Contained Docker Image](https://hub.docker.com/r/vitalidze/traccar-web/) for Traccar Web UI Mod installed over Traccar 3.9.

First of all I would like to thank all users, who still use the Traccar Web UI Mod. Despite being in "pause" state of project, it is still being actively used, installed and so on. The questions on the installation do still arise and the time has come to move the process on next stage with help of modern technologies. 

To simplify the installation now everyone can use the docker. With this approach a fully running version can be started within seconds even with MySQL server.

There are two containers: with [built-in H2 database](https://github.com/vitalidze/traccar-web-docker/raw/master/h2/docker-compose.yml) (not recommended for production use) and [with MySQL](https://github.com/vitalidze/traccar-web-docker/raw/master/mysql/docker-compose.yml). Both by default expose only 8082 port. Ports for devices can be exposed by adding lines in docker compose file either one-by-one or all at once (will slow down container startup a bit):

All at once:

```yml
    ports:
      - "8082:8082"
      - "5000-5150/tcp"
      - "5000-5150/udp"
```

One-by-one:

```yml
    ports:
      - "8082:8082"
      - "5000:5000"
      - "5016:5016"
      - "5023:5023/upd"
```

Hope you will enjoy new modern way of installing the Traccar Web UI Mod. Feel free to ask any questions in the [Official Github Repository](https://github.com/vitalidze/traccar-web-docker/issues).
