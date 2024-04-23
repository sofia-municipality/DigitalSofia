#Author: Kurian Benoy
FROM python:3.9-slim-buster

WORKDIR /forms-flow-documents/app

# install curl, gnupg2 and unzip
RUN  apt-get update \
    && apt-get install -y gnupg2 \
    && apt-get install -y curl \
    && apt-get install -y unzip \
    && apt-get install -y git \
    && apt-get install -y jq \
    && apt-get install -y wget \
    && rm -rf /var/lib/apt/lists/*

# Install Chrome WebDriver - old way
# RUN CHROMEDRIVER_VERSION=`curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE` && \
#     mkdir -p /opt/chromedriver-$CHROMEDRIVER_VERSION && \
#     curl -sS -o /tmp/chromedriver_linux64.zip http://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip && \
#     unzip -qq /tmp/chromedriver_linux64.zip -d /opt/chromedriver-$CHROMEDRIVER_VERSION && \
#     rm /tmp/chromedriver_linux64.zip && \
#     chmod +x /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver && \
#     ln -fs /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver /usr/local/bin/chromedriver

# Install Chrome WebDriver latest version - after version 115^
# RUN CHROMEDRIVER_VERSION=`curl https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions.json | jq --raw-output .channels.Stable.version` && \
#     mkdir -p /opt/chromedriver-$CHROMEDRIVER_VERSION && \
#     curl -sS -o /tmp/chromedriver-linux64.zip https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/$CHROMEDRIVER_VERSION/linux64/chromedriver-linux64.zip && \
#     unzip -qq /tmp/chromedriver-linux64.zip -d /opt/chromedriver-$CHROMEDRIVER_VERSION && \
#     rm /tmp/chromedriver-linux64.zip && \
#     chmod +x /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver-linux64/chromedriver && \
#     ln -fs /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver

# # Install Google Chrome
# RUN curl -sS -o - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
#     echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
#     apt-get -yqq update && \
#     apt-get -yqq install google-chrome-stable && \
#     rm -rf /var/lib/apt/lists/*


# Install Chrome WebDriver specific version - after version 115^
ENV CHROMEDRIVER_VERSION=118.0.5993.70
ENV CHROME_VERSION=118.0.5993.70-1
RUN mkdir -p /opt/chromedriver-$CHROMEDRIVER_VERSION && \
    curl -sS -o /tmp/chromedriver-linux64.zip https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/$CHROMEDRIVER_VERSION/linux64/chromedriver-linux64.zip && \
    unzip -qq /tmp/chromedriver-linux64.zip -d /opt/chromedriver-$CHROMEDRIVER_VERSION && \
    rm /tmp/chromedriver-linux64.zip && \
    chmod +x /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver-linux64/chromedriver && \
    ln -fs /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver

# Install Google Chrome
RUN wget -q --no-check-certificate https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_${CHROME_VERSION}_amd64.deb
RUN apt-get -y update
RUN apt-get install -y ./google-chrome-stable_${CHROME_VERSION}_amd64.deb
RUN rm google-chrome-stable_${CHROME_VERSION}_amd64.deb


# set display port to avoid crash
ENV DISPLAY=:99

COPY requirements.txt .
ENV PATH=/venv/bin:$PATH

RUN : \
    && python3 -m venv /venv \
    && pip install --upgrade pip \
    && pip install -r requirements.txt 

ADD . /forms-flow-documents/app
RUN pip install -e .

EXPOSE 5006
RUN chmod u+x ./entrypoint
ENTRYPOINT ["/bin/sh", "entrypoint"]
