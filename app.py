import logging.config

from flask import Flask, request
from pipeline_command.pipeline_command import Pipeline_Command as cmd
from ssh.ssh_client import SSH_Client

logging.config.fileConfig('logging.conf')

logger = logging.getLogger(__name__)

app = Flask(__name__)


def build_and_deploy(client:SSH_Client, image_name, container_name, port):
    
    client.excute_command(cmd.update_git())
    client.excute_command(cmd.maven_build())
    client.excute_command(cmd.kill_container_if_exist(container_name=container_name))
    client.excute_command(cmd.delete_image(image_name=image_name))
    client.excute_command(cmd.build_image(image_name=image_name))
    client.excute_command(cmd.start_container_use_latest_image(image_name=image_name, port=port, container_name=container_name))
    

@app.route('/start', methods=['POST'])
def pipeline():
    
    content = request.get_json(silent=False)
    
    client = SSH_Client(content['host'], content['account'], content['ssh_file_path'], content['key_type'], content['path'])
    
    logger.info('receive content is : {}'.format(content))
    
    build_and_deploy(client, content['image_name'], content['container_name'], content['port'])

    return "Finsh building"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=4400)