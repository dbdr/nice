/cvs status/ { dir=$4; }
/File:/ && !/Up-to-date/ { print dir "/" $2 " : " $4 " " $5; }
