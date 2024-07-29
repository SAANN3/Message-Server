#!/bin/bash
cd docker
docker compose up &
cd ..
DB_URL=jdbc:postgresql://localhost:5000/server-message  ./build/install/com.example.message-server/bin/com.example.message-server 


