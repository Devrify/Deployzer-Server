FROM python:3.10-slim

RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list

RUN apt-get update && apt-get install -y openssh-client
COPY /.ssh /

WORKDIR /app

COPY ./requirements.txt requirements.txt
RUN pip3 install -r requirements.txt -i http://mirrors.cloud.tencent.com/pypi/simple --trusted-host mirrors.cloud.tencent.com

COPY . /app
Expose 4400
ENTRYPOINT [ "python" ]

CMD [ "app.py" ]