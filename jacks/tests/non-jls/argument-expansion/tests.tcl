# Write out filenames to be expanded before running tests.
saveas C1.java "class C1 {}"
saveas C2.java "class C2 {}"
saveas D3.java "class D3 {}"

# Note that on cygwin, the @file on the command line is expanded before
# jikes even has a chance to begins execution. So some of these tests will
# fail as is.  They would work if we used `compile "@file"', but then
# javac would fail, as cygwin strips the quotes for jikes but javac is
# not based on cygwin. I tried all sorts of quoting schemes to try to get
# the same results for both compilers, but failed.

tcltest::test non-jls-argument-expansion-1 { Under Windows, the compiler
        should expand command line arguments } {pc} {
    compile C*.java D3.java
} PASS

tcltest::test non-jls-argument-expansion-2 { The @ symbol indicates that
        the compiler should read file names from the given file} {atfiles} {
    saveas list2 "C1.java"
    compile @list2
} PASS

tcltest::test non-jls-argument-expansion-3 { EOL chars in @ file
        argument } {atfiles} {
    saveas list3 "C1.java\nC2.java"
    compile @list3
} PASS

tcltest::test non-jls-argument-expansion-4 { EOL chars in @ file
        argument } {atfiles} {
    saveas list4 "C1.java\r\nC2.java"
    compile @list4
} PASS

tcltest::test non-jls-argument-expansion-5 { EOL chars in @ file
        argument } {atfiles} {
    saveas list5 "C1.java\rC2.java"
    compile @list5
} PASS

tcltest::test non-jls-argument-expansion-6 { Empty line in @ file
        argument } {atfiles} {
    saveas list6 "C1.java\n\rC2.java"
    compile @list6
} PASS

tcltest::test non-jls-argument-expansion-7 { Spaces in a @ argument file
        should not cause the compile to fail } {atfiles} {
    saveas list7 "C1.java \n C2.java "
    compile @list7
} PASS

tcltest::test non-jls-argument-expansion-8 { Tabs in a @ argument file
        should not cause the compile to fail } {atfiles} {
    saveas list8 "\tC1.java\nC2.java\t"
    compile @list8
} PASS

tcltest::test non-jls-argument-expansion-9 { Tabs and spaces in a @ argument
        file should not cause the compile to fail } {atfiles} {
    saveas list9 "\t C1.java \n C2.java \t"
    compile @list9
} PASS

tcltest::test non-jls-argument-expansion-10 { Tabs and spaces in empty line
        of a @ argument should be ignored } {atfiles} {
    saveas list10 "C1.java\n\t \nC2.java"
    compile @list10
} PASS

tcltest::test non-jls-argument-expansion-11 { Expand empty @ argument.
        this will terminate without doing anything for JDK 1.3
        compatibility } {atfiles} {
    saveas list11 ""
    compile @list11
} PASS

tcltest::test non-jls-argument-expansion-12 { Expand empty @ argument
    } {atfiles} {
    saveas list12 "\n\n"
    compile @list12
} PASS

# FIXME: it might be good to add tests for "\r\n  \t \n\r   \t\n" and so on
# see the patch for option.cpp

tcltest::test non-jls-argument-expansion-13 { Expand empty @ argument
    } {atfiles} {
    saveas list13 ""
    compile C1.java @list13
} PASS

# Error conditions:

tcltest::test non-jls-argument-expansion-error-1 { Error: Same class compiled
    twice } {atfiles} {
    saveas error1 "C1.java"
    compile C1.java @error1
} FAIL

tcltest::test non-jls-argument-expansion-error-2 { Error: Same class compiled
    twice } {atfiles} {
    saveas error2 "C1.java C1.java"
    compile @error2
} FAIL

tcltest::test non-jls-argument-expansion-error-3 { Error: @ file does not exist
    } {atfiles} {
    compile @notthere
} FAIL

tcltest::test non-jls-argument-expansion-tokens-1 { Two files on
    the same line are NOT parsed as two tokens } {atfiles} {
    saveas tokens1 "C1.java C2.java"
    compile @tokens1
} FAIL

tcltest::test non-jls-argument-expansion-tokens-2 { A file name
    surrounded by double quotes is valid } {atfiles} {
    saveas tokens2 "\"C1.java\""
    compile @tokens2
} PASS

tcltest::test non-jls-argument-expansion-tokens-3 { A empty
    file name is not valid } {atfiles} {
    saveas tokens3 "\"\""
    compile @tokens3
} FAIL

tcltest::test non-jls-argument-expansion-tokens-4 { A file name
    the contains a space surrounded by double quotes is valid } {atfiles} {
    saveas "With Space.java" {class WithSpace {}}
    saveas tokens4 "\"With Space.java\""
    compile @tokens4
} PASS

tcltest::test non-jls-argument-expansion-tokens-5 { A file name
    the contains a space surrounded by double quotes is valid } {atfiles} {
    saveas "Dir With Space/Space2.java" {class Space2 {}}
    saveas tokens5 "\"Dir With Space/Space2.java\""
    compile @tokens5
} PASS

tcltest::test non-jls-argument-expansion-tokens-6 { @ files may not invoke
    other @ files } {atfiles} {
    saveas tokens6 "C1.java"
    saveas tokens6a "@tokens10"
    compile @tokens6a
} FAIL
