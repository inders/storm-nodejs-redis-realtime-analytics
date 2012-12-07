#!/bin/bash
classifierpath=./classifier/inmobi_nbayes.classifier
mallet=~/src/buzz/mallet-2.0.7/bin/mallet
tempdir=`mktemp -d XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`
echo $1 > $tempdir/pre_stop_words_file.txt
./classifier/removestopwords.py $tempdir/pre_stop_words_file.txt > $tempdir/file.txt
$mallet classify-dir --input $tempdir --classifier $classifierpath --output $tempdir/o.txt
#cat $tempdir/o.txt
awk -F, 'BEGIN { FS = " " } ; { if($3 > $5) {print $2 , $3;} else {print $4, $5;}}' $tempdir/o.txt ; 
rm -rf $tempdir
