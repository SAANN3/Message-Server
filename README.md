
# Message-server
A server for sending and recieving messages via websocket written in kotlin with ktor. Something similar to social networks

# Docs
If you want to use Swagger, there are  api docs on ```[serverUrl]/swagger/index.html``` (for example http://0.0.0.0:8080/swagger/index.html). Documented all routes except websocket one. Also swagger is weird at showing kotlinx instance data type.

There are also same [docs](https://github.com/SAANN3/Message-Server/blob/main/docs/api.md) on github, but also with documented websocket and fixed data types.


# Building on linux

```sh
    git clone https://github.com/SAANN3/Message-Server
    cd Message-Server/
    ./gradlew installDist
```
Scheme for database are located in ```./docker/db.sql```.
If you want, you can import it into local installation of postgres(database name should be ```server-message```), but there is also a docker compose file  inside ```./docker``` , that sets everything, related to db, up.


# Run server
If you want to start postgres container with installed scheme and then launch server, connected to docker db
```
./dockerRun.sh
```

Or if you want to run only server, that by default, connects to local database
```sh
./build/install/com.example.message-server/bin/com.example.message-server 
```
# TODO
1: test that everything works

2: ~~make a docker container with postgres db~~

3:~~document websocket in GitHub~~ and maybe swagger(probably no)

4: Implement unread messages system
