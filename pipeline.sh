#!/bin/bash
tag=iot/backend
name=rasHome

kill_container()
{
    docker rm -f -v ${name}
}

cd /root/javaProject/rasberry-Home-Backend/rashome
git pull
mvn clean package
kill_container || true
docker build -t ${tag} .
docker run -d -p 12580:12580 --restart always --name ${name} ${tag}