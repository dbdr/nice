# The following zip tests assume a .class file created
# from the following .java file is on the CLASSPATH.
#
# // File pkg/Example.java
# package pkg;
# public class Example {}
#


#
# Zip file created like so:
#
# % javac pkg/Example.java
# % zip -0 ZipNoComment.zip pkg/Example.class

tcltest::test non-jls-zip-1 {Load .class file from zip without comment} {
    saveas ZipNoComment.java {
class ZipNoComment {
  pkg.Example inst;
}
}
    compile -classpath ZipNoComment.zip ZipNoComment.java
} PASS

#
# Zip file created like so:
#
# % javac pkg/Example.java
# % echo "A ZIP FILE COMMENT" > comment
# % zip -z0 ZipComment.zip pkg/Example.class < comment

tcltest::test non-jls-zip-2 {Load .class file from zip with comment} {
    saveas ZipComment.java {
class ZipComment {
  pkg.Example inst;
}
}
    compile -classpath ZipComment.zip ZipComment.java
} PASS

#
# Zip file created like so:
#
# % javac pkg/Example.java
# % zip ZipCompressed.zip pkg/Example.class

tcltest::test non-jls-zip-3 {Load .class file from a compressed zip} {
    saveas ZipCompressed.java {
class ZipCompressed {
  pkg.Example inst;
}
}
    compile -classpath ZipCompressed.zip ZipCompressed.java
} PASS

tcltest::test non-jls-zip-4 {ignore zip file that does not exist} {
    saveas ZipNotThere.java {class ZipNotThere {}}
    ok_pass_or_warn \
      [compile -classpath ZipNotThere.zip ZipNotThere.java]
} OK
