tcltest::test 3.3-invalid-1 { A unicode sequence must have four hex digits } {
    compile [saveas T33i1.java {class T33i1 { char c = '\u20'; }}]
} FAIL

tcltest::test 3.3-invalid-2 { A unicode sequence does not recursively
        form more unicode sequences } {
    compile [saveas T33i2.java {
class T33i2 {
    String s = "\u005cu005a"; // this sequence is not 'z'
}
    }]
} FAIL

tcltest::test 3.3-invalid-3 { An even number of \ never form a
        unicode escape } {
    compile [saveas T33i3.java {class T33i3 {}/* \\u002a/}]
} FAIL

tcltest::test 3.3-invalid-4 { Unicode escapes begin with 'u' only } {
    compile [saveas T33i4.java {class T33i4{ char c = '\Uabcd'; }}]
} FAIL

tcltest::test 3.3-invalid-5 { Test unicode escape cut short by file end } {
    compile [saveas T33i5.java {class T33i5{} \u}]
} FAIL

tcltest::test 3.3-invalid-6 { Test unicode escape cut short by file end } {
    compile [saveas T33i6.java {class T33i6{} \uuuua}]
} FAIL

tcltest::test 3.3-invalid-7 { Test unicode escape cut short by file end } {
    compile [saveas T33i7.java {class T33i7{} \u0a}]
} FAIL

tcltest::test 3.3-invalid-8 { Test unicode escape cut short by file end } {
    compile [saveas T33i8.java {class T33i8{} \u00a}]
} FAIL

# Valid unicode escapes

tcltest::test 3.3-valid-1 { Multiple u's are allowed in unicode escapes } {
    compile [saveas T33v1.java {class T33v1 { char c = '\uuuuuuuuuuabcd'; }}]
} PASS

tcltest::test 3.3-valid-2 { Unicode sequences are case insensitive } {
    compile [saveas T33v2.java {
class T33v2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (('\uABcD' == '\uabCd') ? 1 : 0):
        }
    }
}
    }]
} PASS

tcltest::test 3.3-valid-3 { Check that \u replacement matches single quoted char } {
    compile [saveas T33v3.java {
class T33v3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (('\u0061' == 'a') ? 1 : 0):
        }
    }
}
    }]
} PASS

tcltest::test 3.3-valid-4 { Check \u replacements in the ASCII range } {

    set class "
class T33v4 \{
    void foo(int i) \{
        switch (i) \{
            case 0:
"

    set total_cases 0

    # Avoid 10 (\n), 13 (\r), 39 ('), and 92 (\)

    foreach {low high} {0 9
                        11 12
                        14 38
                        40 91
                        93 127} {

        for {set i $low} {$i <= $high} {incr i} {
            incr total_cases
            append class [format "%scase ((%d == '\\u%04x') ? %d : 0): // '%c'\n" \
                "            " \
                $i $i $total_cases $i]
        }
    }

    append class "        \}\n    \}\n\}\n"

    compile [saveas T33v4.java $class]
} PASS

tcltest::test 3.3-valid-5 { Unicode \u005c ('\') does not suppress next \ } {
    constant_expression T33v5 {'\u005c\u0030' == 0}
} PASS
