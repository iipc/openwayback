#!/bin/bash
# TODO: make this able to remove all other locations of an ARC also:
DOCROOT=$1
HTTPROOT=$2
POSTURL=$3

# remove trailing '/' from HTTPROOT:
HTTPROOT=$(echo "$HTTPROOT" | sed -e 's@/*$@@;')

find $DOCROOT -name "*.arc.gz" -type f -printf "%P\n" | while read l; do
	ARCURL="$HTTPROOT/$l"
	ARCBASE=$(basename $l);
	SUBMITURL=$(printf "%s?operation=add&name=%s&url=%s\n" $POSTURL $ARCBASE $ARCURL)
	echo SUBMITTING $ARCBASE with $ARCURL...
	curl "$SUBMITURL"
	echo
	echo
done

