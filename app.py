import paramiko
from flask import Flask, request

app = Flask(__name__)

client = None

def key_based_connect(host, account, known_host_file, private_key_path):
    
    global client

    real_client = paramiko.SSHClient()
    real_client.load_host_keys(known_host_file)
    pkey = paramiko.Ed25519Key.from_private_key_file(private_key_path)
    policy = paramiko.AutoAddPolicy()
    real_client.set_missing_host_key_policy(policy)
    real_client.connect(host, username=account, pkey=pkey)
    client = real_client

def perform_log(command, variable_name:list, variable:list):
    
    
    if variable_name is None:
        print('perform {} command'.format(command))
        return
    
    result = []
    
    for index, value in enumerate(variable_name):
        result.append('{} is {}'.format(value, variable[index]))
        
    
    print('perform {} command, '.format(command) + ', '.join(result))

def cd(git_repository_path):
    perform_log('cd', ['path'], [git_repository_path])
    excute_command('cd {}'.format(git_repository_path))
    
def update_git():
    perform_log('update_git', None, None)
    excute_command('git pull')
    
def maven_build():
    perform_log('maven package', None, None)
    excute_command('mvn clean package')
    
def kill_container_if_exist(container_name):
    perform_log('kill container if exist', ['container name'], [container_name])
    excute_command('docker rm -f -v {}'.format(container_name))

def build_image(image_name, image_tag):
    perform_log('build image', ['image_name', 'tag'], [image_name, image_tag])
    excute_command('docker build -t {}:{} .'.format(image_name, image_tag))
    
def start_container(image_name, image_tag, port, container_name):
    perform_log('docker start container', ['image_name', 'tag', 'port', 'container_name'], [image_name, image_tag, port, container_name])
    excute_command('docker run -d -p {}:{} --restart always --name {} {}:{}'.format(port, port, container_name, image_name, image_tag))

def excute_command(command):
    
    print('command is : {}'.format(command))
    
    _stdin, stdout, _stderr = client.exec_command(command)
    
    lines = stdout.read().decode()
    
    print('output is : ')
    
    for line in lines.split("\n"):
        print(line)
        
        
def build_and_deploy(path, image_name, image_tag, container_name, port):
    cd(path)
    update_git()
    maven_build()
    kill_container_if_exist(container_name)
    build_image(image_name, image_tag)
    start_container(image_name, image_tag, port, container_name)
    

@app.route('/start', methods=['POST'])
def pipeline():
    
    content = request.get_json(silent=False)
    
    print('receive content is : {}'.format(content))
    
    key_based_connect(content['host'], content['account'], content['private_key_path'])
    
    build_and_deploy(content['path'], content['image_name'], content['image_tag'], content['container_name'], content['port'])

    return "Start building"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=4400)