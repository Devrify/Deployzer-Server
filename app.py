from flask import Flask, request
import logging.config
from ssh_client import SSH_Client
from pipeline_command import Pipeline_Command as cmd

LOGGING_CONFIG = {
    "version": 1,
    "formatters": {
        "default": {
            "format": "[%(asctime)s] %(levelname)s in %(module)s: %(message)s",
            "datefmt": '%Y-%m-%d,%H:%M:%S',
        }
    },
    "handlers": {
        "console": {
            "class": "logging.StreamHandler",
            "stream": "ext://sys.stdout",
            "formatter": "default",
            "level": "INFO",
        }
    },
}

logging.config.dictConfig(LOGGING_CONFIG)

logger = logging.getLogger(__name__)

app = Flask(__name__)


def build_and_deploy(client:SSH_Client, image_name, image_tag, container_name, port):
    
    client.excute_command(cmd.update_git())
    client.excute_command(cmd.maven_build())
    client.excute_command(cmd.kill_container_if_exist(container_name=container_name))
    client.excute_command(cmd.build_image(image_name=image_name, image_tag=image_tag))
    client.excute_command(cmd.start_container(image_name=image_name, image_tag=image_tag, port=port, container_name=container_name))
    

@app.route('/start', methods=['POST'])
def pipeline():
    
    content = request.get_json(silent=False)
    
    client = SSH_Client(content['host'], content['account'], content['ssh_file_path'], content['key_type'], content['path'])
    
    logger.info('receive content is : {}'.format(content))
    
    build_and_deploy(client, content['image_name'], content['image_tag'], content['container_name'], content['port'])

    return "Finsh building"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=4400)