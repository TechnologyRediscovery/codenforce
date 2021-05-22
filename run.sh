#!/bin/sh
docker build . -t techred/codenforce
docker run -p 8080:8080 -p 9990:9990 techred/codenforce