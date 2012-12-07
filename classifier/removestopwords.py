#!/usr/bin/env python

import sys

stop_words = set([])
with open('./classifier/en.txt') as stopf:
    for line in stopf:
        stop_words.add(line.strip())
print ' '.join(w for w in open(sys.argv[1]).read().split() if w.strip().lower() not in stop_words)
