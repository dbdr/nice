/cvs (status|server):/ { dir=$4; }
/File: no file/ { print dir "/" $4 " :\t" $6 " " $7; }
/File:/ && !/File: no file/ && !/Up-to-date/ { print dir "/" $2 " :\t" $4 " " $5 " " $6 " " $7 " " $8 " " $9; }
