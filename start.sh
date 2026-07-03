#!/bin/bash
mkdir -p /home/ubuntu/data
pkill -f weddingcard-0.0.1 || true
sleep 2
nohup java -Xmx200m -Xms128m -jar /home/ubuntu/weddingcard-0.0.1-SNAPSHOT.jar \
  --kakao.rest-key=996cf710fe1532d4985e565d52840965 \
  --kakao.js-key=a48a5635827fe8d12042f1903e5c4cb5 \
  --spring.datasource.url="jdbc:h2:file:/home/ubuntu/data/weddingcard" \
  > /home/ubuntu/app.log 2>&1 &
echo "Started with PID=$!"
