/cvs (status|server):/ { dir=$4; }
/File: no file/ { print dir "/" $4 " : " $6 " " $7; }
/File:/ && !/File: no file/ && !/Up-to-date/ { print dir "/" $2 " : " $4 " " $5; }
