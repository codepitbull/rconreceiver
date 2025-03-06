#!/bin/bash
VERSION=2.0.32
docker pull factoriotools/factorio:$VERSION
docker rm -f factorio
#rm saves/*
#rm scenarios/*
#rm script-output/*
#rm -R temp/*
docker run -d \
  -p 34197:34197/udp \
  -p 27015:27015/tcp \
  -v ./:/factorio \
  --name factorio \
  --restart=unless-stopped \
  factoriotools/factorio:$VERSION
