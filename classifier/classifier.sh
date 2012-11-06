#!/bin/bash
classifierpath=/home/netra/tweet_maxent.classifier
mallet=$HOME"/mallet-2.0.7/bin/mallet"
mkdir buzztest
echo $1 > buzztest/file.txt
$mallet classify-dir --input buzztest --classifier $classifierpath --output buzztest/o.txt
awk -F, 'BEGIN { FS = " " } ; { if($3 > $5) {print $2 , $3;} else {print $4, $5;}}' buzztest/o.txt ; 
rm -rf buzztest
