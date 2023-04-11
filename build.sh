#!/bin/bash
image_name="python/pipeline"
repository_tag="python-pipeline"
repository_name="nopepsi/app"
full_repository_path="${repository_name}:${repository_tag}"
container_name="pipeline"
ssh_path_in_container=/root/.ssh
ssh_path_in_host=~/.ssh

# git pull
cd /root/pythonProject/lightweight-pipeline
git pull

# remove container
docker rm -f -v ${container_name}
sleep 1

# remove image
docker image rm ${image_name}
sleep 1
docker image rm ${full_repository_path}
sleep 1

docker build -t ${image_name} .
docker tag ${image_name}:latest ${full_repository_path}
docker push ${full_repository_path}
docker run -d -p 4400:4400 --restart always --name ${container_name} -v ${ssh_path_in_host}:${ssh_path_in_container}:ro ${image_name}