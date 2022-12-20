import logging
import os

import paramiko
from pipeline_command import Pipeline_Command as cmd

class SSH_Client:
    
    def __init__(self, host, account, ssh_file_path, key_type, git_repository_path):
        
        self.logger = logging.getLogger(__name__)
        
        self.path = git_repository_path
        
        pkey = None

        self.client = paramiko.SSHClient()
        self.client.load_host_keys(os.path.join(ssh_file_path, 'known_hosts'))
        self.client.set_missing_host_key_policy(paramiko.WarningPolicy())
        
        if key_type == 'rsa':
            pkey = paramiko.RSAKey.from_private_key_file(os.path.join(ssh_file_path, 'id_rsa'))
        elif key_type == 'ed25519':
            pkey = paramiko.Ed25519Key.from_private_key_file(os.path.join(ssh_file_path, 'id_ed25519'))

        self.client.connect(host, username=account, pkey=pkey)
        
    def excute_command(self, command):
        
        appendPathCommand = cmd.cd(self.path) + ';' + command
        
        self.logger.info('command is : {}'.format(appendPathCommand))
        
        _stdin, stdout, _stderr = self.client.exec_command(appendPathCommand)
        
        lines = stdout.read().decode(encoding='utf-8')
        
        self.logger.info('output is : ')
        
        for line in lines.split("\n"):
            self.logger.info(line)