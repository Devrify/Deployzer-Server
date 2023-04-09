import datetime
import logging.config

from flask import Flask, request

from pipeline_command.pipeline_command import Pipeline_Command as cmd
from ssh.ssh_client import SSH_Client

logging.config.fileConfig('logging.conf')

app = Flask(__name__)

def build_and_upload_image(client:SSH_Client, image_name:str, repository_name:str):
    client.excute_command(cmd.update_git())
    client.excute_command(cmd.maven_build())
    client.excute_command(cmd.delete_image(image_name=image_name))
    client.excute_command(cmd.build_image(image_name=image_name))
    client.excute_command(cmd.tag_image_to_docker_hub(image_name, repository_name))
    client.excute_command(cmd.push_image_to_docker_hub(image_name, repository_name))

    """
    image_name : 构建的 image 名称
    container_name : 构建的容器名称
    port : 绑定远端服务器的端口
    repository_name : docker hub 仓库名称 -> xxxx/yy
    """
def build_and_deploy(client:SSH_Client, image_name, container_name, port, repository_name:str):
    
    client.excute_command(cmd.update_git())
    client.excute_command(cmd.maven_build())
    client.excute_command(cmd.kill_container_if_exist(container_name=container_name))
    client.excute_command(cmd.delete_image(image_name=image_name))
    client.excute_command(cmd.build_image(image_name=image_name))
    client.excute_command(cmd.tag_image_to_docker_hub(image_name, repository_name))
    client.excute_command(cmd.push_image_to_docker_hub(image_name, repository_name))
    client.excute_command(cmd.start_container_use_latest_image(image_name=image_name, port=port, container_name=container_name))
    

@app.route('/start', methods=['POST'])
def pipeline():
    
    logger = logging.getLogger(__name__)
    
    content = request.get_json(silent=False)
    
    client = SSH_Client(content['host'], content['account'], content['ssh_file_path'], content['key_type'], content['path'])
    
    logger.info('receive content is : {}'.format(content))
    
    if content['mode'] == 0:
        build_and_deploy(client, content['image_name'], content['container_name'], content['port'], content['repository_name'])
    elif content['mode'] == 1:
        build_and_upload_image(client, content['image_name'], content['repository_name'])

    return {"message":"Finsh building"}

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=4400)