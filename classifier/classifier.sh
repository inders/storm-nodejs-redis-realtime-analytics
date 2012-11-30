#!/bin/bash
classifierpath=./classifier/naivebayes.classifier
mallet=$HOME"/mallet-2.0.7/bin/mallet"
tempdir=`mktemp -d XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`
echo $1 > $tempdir/file.txt
$mallet classify-dir --input $tempdir --classifier $classifierpath --output $tempdir/o.txt
awk -F, 'BEGIN { FS = " " } ; { if($3 > $5) {print $2 , $3;} else {print $4, $5;}}' $tempdir/o.txt ; 
rm -rf $tempdir
