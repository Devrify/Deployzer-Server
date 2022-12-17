#!/bin/bash
image_name=python/pipeline
container_name=pipeline

kill_container()
{
    docker rm -f -v ${container_name}
}

cd /root/pythonProject/lightweight-pipeline
git pull
kill_container || true
docker build -t ${image_name} .
docker run -d -p 4400:4400 --restart always --name ${container_name} -v ~/.ssh:/root/.ssh:ro ${image_name}