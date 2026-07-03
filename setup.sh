#!/bin/bash
# 스왑 파일 생성 (1GB)
if [ ! -f /swapfile ]; then
  sudo fallocate -l 1G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  echo "스왑 생성 완료"
else
  echo "스왑 이미 존재"
fi
free -m
