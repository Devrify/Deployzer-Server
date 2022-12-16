FROM python:3.10-slim

WORKDIR /app

COPY ./requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

COPY . /app
Expose 4400
ENTRYPOINT [ "python" ]

CMD [ "app.py" ]