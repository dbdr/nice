# This sets up a list of all 256 cases for byte
proc exhaust_list { } {
    set count -128
    set labels ""
    while {$count < 128} {
        set labels [append {$labels} "\tcase $count:\n"]
        incr count
    }
    return $labels
}



tcltest::test 14.10-invalid-syntax-1 { SwitchStatement must have () around Expression } {
    compile [saveas T1410is1.java {
class T1410is1 {
    void foo(int i) {
        switch i {
            default:
        }
    }
}
    }]
} FAIL

tcltest::test 14.10-invalid-syntax-2 { SwitchBlock must have {} around statements } {
    compile [saveas T1410is2.java {
class T1410is2 {
    void foo(int i) {
        switch (i)
            default:
    }
}
    }]
} FAIL

tcltest::test 14.10-invalid-syntax-3 { SwitchBlockStatementGroup must begin with
         a SwitchLabel} {
    compile [saveas T1410is3.java {
class T1410is3 {
    void foo(int i) {
        switch (i) {
            i = 0;
        }
    }
}
    }]
} FAIL

tcltest::test 14.10-invalid-syntax-4 { Duff's device is not valid } {
    compile [saveas T1410is4.java {
class T1410is4 {
    void foo(int n) {
        int q = (n+7)/8;
        switch (n%8) {
            case 0:         do {    foo();          // Great C hack, Tom,
            case 7:                 foo();          // but it's not valid here.
            case 6:                 foo();
            case 5:                 foo();
            case 4:                 foo();
            case 3:                 foo();
            case 2:                 foo();
            case 1:                 foo();
                            } while (--q >= 0);
        }
    }
}
    }]
} FAIL


# We expect the next two to always pass. If they do not, the
# rest of our tests could be hosed.

tcltest::test 14.10-valid-1 { SwitchBlockStatementGroups and SwitchLabels are
        optional inside a SwitchBlock } {
    switch_labels T1410v1 int {}
} PASS

tcltest::test 14.10-valid-2 { default SwitchLabel by itself is acceptable } {
    switch_labels T1410v2 int {default:}
} PASS


# Check type of the Expression in a swtich statement!

tcltest::test 14.10-invalid-type-1 { Expression type must be char, byte, short, or int } {
    switch_labels T1410it1 boolean
} FAIL

tcltest::test 14.10-invalid-type-2 { Expression type must be char, byte, short, or int } {
    switch_labels T1410it2 float
} FAIL

tcltest::test 14.10-invalid-type-3 { Expression type must be char, byte, short, or int } {
    switch_labels T1410it3 double
} FAIL

tcltest::test 14.10-invalid-type-4 { Expression type must be char, byte, short, or int } {
    switch_labels T1410it4 long
} FAIL

tcltest::test 14.10-invalid-type-5 { Expression type must be char, byte, short, or int } {
    switch_labels T1410it5 Object
} FAIL

tcltest::test 14.10-invalid-type-6 { Expression type must be char, byte, short, or int } {
    switch_labels T1410it6 String
} FAIL


tcltest::test 14.10-valid-type-1 { Expression type must be char, byte, short, or int } {
    switch_labels T1410vt1 char
} PASS

tcltest::test 14.10-valid-type-2 { Expression type must be char, byte, short, or int } {
    switch_labels T1410vt2 byte
} PASS

tcltest::test 14.10-valid-type-3 { Expression type must be char, byte, short, or int } {
    switch_labels T1410vt3 short
} PASS

tcltest::test 14.10-valid-type-4 { Expression type must be char, byte, short, or int } {
    switch_labels T1410vt4 int
} PASS



tcltest::test 14.10-switchlabel-1 { case label must have a ConstantExpression } {
    switch_labels T1410sl1 int {case 0: case 1:}
} PASS

tcltest::test 14.10-switchlabel-2 { case label must have a ConstantExpression,
        i++ is not a constant expression as defined by 15.28} {
    switch_labels T1410sl2 int {case (i++):}
} FAIL

tcltest::test 14.10-switchlabel-3 { case label must have a ConstantExpression,
        the additive operator - produces a constant expression (see 15.28)} {
    switch_labels T1410sl3 int {case 0: case (2-1): case (3-1): default:}
} PASS


tcltest::test 14.10-assignable-1 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a1 int {case 0:}
} PASS

tcltest::test 14.10-assignable-2 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a2 int {case 0L:}
} FAIL

tcltest::test 14.10-assignable-3 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a3 int {case 0.0D:}
} FAIL

tcltest::test 14.10-assignable-4 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a4 int {case true:}
} FAIL

tcltest::test 14.10-assignable-5 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a5 byte {case 128:}
} FAIL

tcltest::test 14.10-assignable-6 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a6 byte {case 127:}
} PASS

tcltest::test 14.10-assignable-7 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a7 int {case null:}
} FAIL

tcltest::test 14.10-assignable-8 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a8 byte {case (short) 1:}
} PASS

tcltest::test 14.10-assignable-9 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a9 byte {case '1':}
} PASS

tcltest::test 14.10-assignable-10 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a10 char {case (short) 1:}
} PASS

tcltest::test 14.10-assignable-11 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a11 short {case '1':}
} PASS

tcltest::test 14.10-assignable-12 { ConstantExpression must be assignable to Expression } {
    switch_labels T1410a12 char {case (byte) 1:}
} PASS

tcltest::test 14.10-duplicate-1 { No two case statements may have the same
        ConstantExpression value } {
    switch_labels T1410d1 int {case 0: case 1: case 0:}
} FAIL

tcltest::test 14.10-duplicate-2 { No two case statements may have the same
        ConstantExpression value } {
    switch_labels T1410d2 int {case 0: case 1: case (2-2):}
} FAIL

tcltest::test 14.10-duplicate-3 { No two case statements may have the same
        ConstantExpression value } {
    switch_labels T1410d3 int {case 0: case 1: case ((2==2) ? 0 : 2):}
} FAIL

tcltest::test 14.10-duplicate-4 { At most 1 default statement can be associated
        with the same switch statement } {
    switch_labels T1410d4 int {default: case 0: default:}
} FAIL


tcltest::test 14.10-abrupt-completion-1 { break is a valid abrupt completion } {
    switch_labels T1410ac1 int {case 0:
                                  break;
                                case 1:
                                  break;}
} PASS

tcltest::test 14.10-abrupt-completion-2 { continue is not a valid abrupt completion } {
    switch_labels T1410ac2 int {case 0:
                                  break;
                                case 1:
                                  continue;}
} FAIL

tcltest::test 14.10-exhaustion-1 { When all 256 cases explicitly listed, the
        specs are unclear as to whether default is reachable } {
    switch_labels T1410e1 byte [exhaust_list] "break; default:"
} PASS

tcltest::test 14.10-exhaustion-2 { When all 256 cases explicitly listed, the
        specs are unclear as to whether the switch can complete normally } {
    switch_labels T1410e2 byte [exhaust_list] \
            "throw new RuntimeException();\n        \}\n        \{System.out.println();"
} PASS

tcltest::test 14.10-example-1 { This example causes an assertion in Jikes } {
    compile -g [saveas T1410e1.java {
class T1410e1 {
    void foo() {
        int i;
        switch (i = 1) {
            case 1:
        }
    }
}
    }]
} PASS
