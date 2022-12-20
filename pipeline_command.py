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
    def build_image(image_name, image_tag):
        return 'docker build -t {}:{} .'.format(image_name, image_tag)
    
    @staticmethod
    def build_image_replace_old(image_name):
        return 'docker build -t {} .'.format(image_name)
    
    @staticmethod
    def start_container_use_latest_image(image_name, port, container_name):
        return 'docker run -d -p {}:{} --restart always --name {} {}'.format(port, port, container_name, image_name)
        
    @staticmethod
    def start_container(image_name, image_tag, port, container_name):
        return 'docker run -d -p {}:{} --restart always --name {} {}:{}'.format(port, port, container_name, image_name, image_tag)