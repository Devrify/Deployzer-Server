FROM python:3.10-slim

RUN cat > /etc/apt/sources.list << EOF
deb http://mirrors.tencentyun.com/debian/ bullseye main contrib non-free
deb-src http://mirrors.tencentyun.com/debian/ bullseye main contrib non-free

deb http://mirrors.tencentyun.com/debian/ bullseye-updates main contrib non-free
deb-src http://mirrors.tencentyun.com/debian/ bullseye-updates main contrib non-free

deb http://mirrors.tencentyun.com/debian/ bullseye-backports main contrib non-free
deb-src http://mirrors.tencentyun.com/debian/ bullseye-backports main contrib non-free

deb http://mirrors.tencentyun.com/debian-security/ bullseye-security main contrib non-free
deb-src http://mirrors.tencentyun.com/debian-security/ bullseye-security main contrib non-free
EOF

RUN apt-get update && apt-get install -y openssh-client
COPY /.ssh /

WORKDIR /app

COPY ./requirements.txt requirements.txt
RUN pip3 install -r requirements.txt -i http://mirrors.cloud.tencent.com/pypi/simple --trusted-host mirrors.cloud.tencent.com

COPY . /app
Expose 4400
ENTRYPOINT [ "python" ]

CMD [ "app.py" ]