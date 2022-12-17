#!/bin/bash
image_name=python/pipeline
container_name=pipeline
ssh_path_in_container=/root/.ssh
ssh_path_in_host=~/.ssh

kill_container()
{
    docker rm -f -v ${container_name}
}

cd /root/pythonProject/lightweight-pipeline
git pull
kill_container || true
docker build -t ${image_name} .
docker run -d -p 4400:4400 --restart always --name ${container_name} -v ${ssh_path_in_host}:${ssh_path_in_container}:ro ${image_name}