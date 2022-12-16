FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

COPY . .
Expose 4400
CMD [ "python3", "-m" , "flask", "run", "--host=0.0.0.0", "--port=4400"]