import logging
import os

import paramiko

from pipeline_command.pipeline_command import Pipeline_Command as cmd


class SSH_Client:
    """
    host : 远端服务器 ip
    account : 远端服务器账号
    ssh_file_path : 流水线可以访问的 ssh 密钥父目录
    key_type : 密钥类型
    git_repository_path : github 地址 xxxx.git
    """
    def __init__(self, host, account, ssh_file_path, key_type, git_repository_path, ssh_port=22):
        
        self.logger = logging.getLogger(__name__)
        
        self.path = git_repository_path
        
        pkey = None

        # 设置密钥位置, 根据服务器不同设置
        self.client = paramiko.SSHClient()
        self.client.load_host_keys(os.path.join(ssh_file_path, 'known_hosts'))
        self.client.set_missing_host_key_policy(paramiko.WarningPolicy())
        
        # 根据密钥类型, 设置密钥文件
        if key_type == 'rsa':
            pkey = paramiko.RSAKey.from_private_key_file(os.path.join(ssh_file_path, 'id_rsa'))
        elif key_type == 'ed25519':
            pkey = paramiko.Ed25519Key.from_private_key_file(os.path.join(ssh_file_path, 'id_ed25519'))

        # 连接
        self.client.connect(host, username=account, pkey=pkey, port=ssh_port)
    
    '''
    执行命令
    '''
    def excute_command(self, command):
        
        appendPathCommand = cmd.cd(self.path) + ';' + command
        
        self.logger.info('command is : {}'.format(appendPathCommand))
        
        _stdin, stdout, _stderr = self.client.exec_command(appendPathCommand)
        
        lines = stdout.read().decode(encoding='utf-8')
        
        for line in lines.split("\n"):
            self.logger.info(line)