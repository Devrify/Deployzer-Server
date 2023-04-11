class Pipeline_Command:
    @staticmethod
    def cd(path):
        return 'cd {}'.format(path)
        
    @staticmethod
    def update_git():
        return 'git pull'
    
    @staticmethod
    def maven_build():
        return 'mvn clean package'
        
    @staticmethod
    def kill_container_if_exist(container_name):
        return 'docker rm -f -v {}'.format(container_name)
    
    @staticmethod
    def local_image_name_to_repositry_image_name(image_name:str, repository_name:str):
        image_name_dash = image_name.replace('/', '-')
        return repository_name + ':' + image_name_dash

    @staticmethod
    def build_image(image_name):
        return 'docker build -t {}:latest .'.format(image_name)
    
    @staticmethod
    def tag_image_to_docker_hub(image_name:str, repository_name:str):
        full_name = Pipeline_Command.local_image_name_to_repositry_image_name(image_name, repository_name)
        return 'docker tag {}:latest {}'.format(image_name, full_name)
    
    @staticmethod
    def push_image_to_docker_hub(image_name:str, repository_name:str):
        full_name = Pipeline_Command.local_image_name_to_repositry_image_name(image_name, repository_name)
        return 'docker push {}'.format(full_name)
    
    @staticmethod
    def pull_image_from_private_repository(image_name:str, repository_name:str):
        full_name = Pipeline_Command.local_image_name_to_repositry_image_name(image_name, repository_name)
        return 'docker pull {}'.format(full_name)
    
    @staticmethod
    def delete_local_image(image_name):
        return 'docker image rm {}'.format(image_name)
    
    @staticmethod
    def start_container_use_latest_image(image_name, port, container_name):
        return 'docker run -d -p {}:{} --restart always --name {} {}'.format(port, port, container_name, image_name)
        
    @staticmethod
    def start_container(image_name, image_tag, port, container_name):
        return 'docker run -d -p {}:{} --restart always --name {} {}:{}'.format(port, port, container_name, image_name, image_tag)