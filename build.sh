git pull;
image_name="nopepsi/app:deployzer-server";
container_name="deployzer-server";
docker rm -f $container_name;
docker image rm -f $image_name;
docker build -t $image_name .;
docker run -d -p 10810:8080 --name $container_name $image_name;