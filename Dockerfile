FROM python:3.10-slim

ENV TZ=Asia/Shanghai
RUN sed -i 's/deb.debian.org/mirrors.cloud.tencent.com/g' /etc/apt/sources.list && apt-get update && apt-get install -y openssh-client && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY ./requirements.txt requirements.txt
RUN pip3 install -r requirements.txt -i http://mirrors.cloud.tencent.com/pypi/simple --trusted-host mirrors.cloud.tencent.com

COPY . /app
Expose 4400
ENTRYPOINT [ "python" ]

CMD [ "app.py" ]