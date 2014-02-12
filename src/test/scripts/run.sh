#!/bin/bash
PORT=3002
WHICH=${1:-1}
POST=post$WHICH.json
echo "Posting $POST"
curl -i -X POST --data @$POST -H "Content-Type: application/json" http://localhost:$PORT/api/corpus
