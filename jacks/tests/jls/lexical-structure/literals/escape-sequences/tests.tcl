proc sequence { name sequence } {
    set data "class $name {char c = '${sequence}';}\n"
    return [compile [saveas $name.java $data]]
}


tcltest::test 3.10.7-invalid-1 { \a (Alert, Ctrl-G) is not an escape sequence } {
    sequence T3107i1 {\a}
} FAIL

tcltest::test 3.10.7-invalid-2 { \v (Vertical tab, Ctrl-K) is not an escape sequence } {
    sequence T3107i2 {\v}
} FAIL

tcltest::test 3.10.7-invalid-3 { \x (hex sequence) is not an escape sequence } {
    sequence T3107i3 {\x0}
} FAIL

tcltest::test 3.10.7-invalid-4 { \LineTerminator is not a valid line continuation } {
    sequence T3107i4 a\\\n
} FAIL

tcltest::test 3.10.7-invalid-5 { \u is not an escape sequence, since unicode
        is already expanded } {
    sequence T3107i5 {\u005cu}
} FAIL

tcltest::test 3.10.7-invalid-6 { octal escape sequences are limited at 0377 } {
    sequence T3107i6 {\400}
} FAIL

tcltest::test 3.10.7-invalid-7 { escape sequences are case-sensitive } {
    sequence T3107i7 {\N}
} FAIL

tcltest::test 3.10.7-invalid-8 { escape sequences only occur within character
        and string literals } {
    compile [saveas T3107i8.java "class T3107i8 { \\t }"]
} FAIL

# valid escape sequences

tcltest::test 3.10.7-valid-1 { \b (Backspace, Ctrl-H, \u0008) is an escape sequence } {
    sequence T3107v1 {\b}
} PASS

tcltest::test 3.10.7-valid-2 { \t (Tab, Ctrl-I, \u0009) is an escape sequence } {
    sequence T3107v2 {\t}
} PASS

tcltest::test 3.10.7-valid-3 { \n (Linefeed, Ctrl-J, \u000a) is an escape sequence } {
    sequence T3107v3 {\n}
} PASS

tcltest::test 3.10.7-valid-4 { \f (Formfeed, Ctrl-L, \u000c) is an escape sequence } {
    sequence T3107v4 {\f}
} PASS

tcltest::test 3.10.7-valid-5 { \r (Carriage Return, Ctrl-M, \u000d)
        is an escape sequence } {
    sequence T3107v5 {\r}
} PASS

tcltest::test 3.10.7-valid-6 { \" (\u0022) is an escape sequence } {
    sequence T3107v6 {\"}
} PASS

tcltest::test 3.10.7-valid-7 { \' (\u0027) is an escape sequence } {
    sequence T3107v7 {\'}
} PASS

tcltest::test 3.10.7-valid-8 { \\ (\u005c) is an escape sequence } {
    sequence T3107v8 {\\}
} PASS

tcltest::test 3.10.7-valid-9 { octal sequences can have one digit } {
    sequence T3107v9 {\5}
} PASS

tcltest::test 3.10.7-valid-10 { octal sequences can have two digits } {
    sequence T3107v10 {\40}
} PASS

tcltest::test 3.10.7-valid-11 { octal sequences can have three digits,
        if the first is < 4 } {
    sequence T3107v11 {\012}
} PASS

tcltest::test 3.10.7-valid-12 { escape sequences may be written in unicode } {
    sequence T3107v12 {\u005c\u005c}
} PASS

tcltest::test 3.10.7-valid-13 { an octal sequence with the first
        digit >= 4 has at most two digits } {
    compile [saveas T3107v13.java {
class T3107v13 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case (("\477" == "'7") ? 1 : 0):
	}
    }
}
    }]
} PASS

tcltest::test 3.10.7-valid-14 { escape sequences represent characters
        with identical value } {
    compile [saveas T3107v14.java {
class T3107v14 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case (('\t' == 9) ? 1 : 0):
	    case (('\t' == '\011') ? 2 : 0):
	}
    }
}
    }]
} PASS

tcltest::test 3.10.7-valid-15 { escape sequences represent the same
        character in a String } {
    compile [saveas T3107v15.java {
class T3107v15 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case (("\b" == "\u0008") ? 1 : 0):
	    case (("\10" == "\u0008") ? 2 : 0):
	}
    }
}
    }]
} PASS

tcltest::test 3.10.7-valid-16 { escape sequences only occur within
        character and string literals } {
    compile [saveas T3107v16.java {
class T3107v16 {
    // Comment not ended here: \n int i =
}
    }]
} PASS

