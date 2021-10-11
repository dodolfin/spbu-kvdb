#!/bin/bash

FILE=big_script.txt

echo new data/generated.json > $FILE
for i in $(seq 1 $1); do
  echo store key${i} value${i} >> $FILE
  echo close >> $FILE
  echo open generated.json >> $FILE
done
