#!/bin/bash

# Erzeugt eine ZIP-Datei mit PNG-Bildern einer PDF-Datei

PDFFILE=$1
PDFFILENAME=`basename "$PDFFILE"`
WD=$2
if [[ $WD == "" ]]
then
  WD=`dirname "$0"`
fi

OUTDIR=$WD/`basename "$PDFFILE" .pdf`-slides
OUTFILE="$WD/$PDFFILENAME"

mkdir $OUTDIR

env pdfsam-console -f "$PDFFILE" -o "$OUTDIR" -s BURST -overwrite -pdfversion 5 split

cd $OUTDIR

find ./ -type f -name \*.pdf | xargs -i env gs -q -dNOPAUSE -dBATCH -sDEVICE=pngalpha -sOutputFile={}.png {}

tar cfz ../$OUTDIR.tgz ./*.png

rm *.pdf
rm *.png

cd ..

rmdir $OUTDIR