# See related sections: 14.10 and 4.5.3. Also, see
# http://www.ergnosis.com/java-spec-report/java-language/jls-4.5.4.html
# for explanation of some of these tests.

tcltest::test 16.2.8-final-1 { A blank final may be assigned in different block
        statement groups, provided it not assigned twice. Even with a
        non-constant initializer, the variable declaration is treated as
        a blank final } {
    switch_labels T1628f1 int {
            case 0:
                final int j = "".length();
                break;
            case 1:
                j = 1;
    }
} PASS

tcltest::test 16.2.8-final-2 { A blank final may be assigned in different block
        statement groups, provided it not assigned twice } {
    switch_labels T1628f2 int {
            case 0:
                final int b;
                b = 0;
                break;
            case 1:
                b = 1;
    }
} PASS

tcltest::test 16.2.8-final-3 { A blank final may be assigned in different block
        statement groups, provided it not assigned twice. However, a
        variable initialized with a constant is inlined, and is not a blank
        final. } {
    switch_labels T1628f3 int {
            case 0:
                final byte b = 0;
                break;
            case 1:
                b = 1;
    }
} FAIL

tcltest::test 16.2.8-final-4 { A blank final may not be referenced unless
        it is definitely assigned. However, a variable initialized with a
        constant is inlined, and is not a blank final. } {
    switch_labels T1628f4 int {
            case 0:
                final byte b = 0;
                break;
            case 1:
                byte c = b;
    }
} PASS

tcltest::test 16.2.8-final-5 { A final variable declared with a constant
        initializer qualifies as a constant, and may be used as a label } {
    switch_labels T1628f5 int {
        case 0:
            final byte b = 1;
        case b:
    }
} PASS

tcltest::test 16.2.8-final-6 { A final variable declared with a constant
        initializer qualifies as a constant, and may be used as a label,
        but only after its declaration } {
    switch_labels T1628f6 int {
        case b:
            break;
        case 0:
            final byte b = 1;
    }
} FAIL

tcltest::test 16.2.8-unassigned-1 { A variable must have been assigned
        in each switch block if it is going to be accessed } {

    switch_labels T1628u1 int {
            case 0:
                byte b = 0;
                break;
            default:
                b++;
    }
} FAIL

tcltest::test 16.2.8-unassigned-2 { A variable that is not definitely assigned
        before entering the switch must be definitely assigned in each switch
        block if it is to be definitely assigned after the switch block } {

    compile [saveas T1628u2.java {
class T1628u2 {
    void foo(int i) {
        int j;
        switch (i) {
            case 0: j = 0; break;
            case 1: j = 1; break;
            default: j = 2; break;
        }
        j++;
    }
}
    }]
} PASS

tcltest::test 16.2.8-unassigned-3 { A variable that is not definitely assigned
        before entering the switch must be definitely assigned in each switch
        block if it is to be definitely assigned after the switch block } {

    compile [saveas T1628u3.java {
class T1628u3 {
    void foo(int i) {
        int j;
        switch (i) {
            case 0: j = 0; break;
            case 1: break;
            default: j = 2; break;
        }
        j++;
    }
}
    }]
} FAIL

tcltest::test 16.2.8-unassigned-4 { A variable that is not definitely assigned
        before entering the switch must be definitely assigned in each switch
        block (including the default block) if it is to be definitely assigned
        after the switch block } {

    compile [saveas T1628u4.java {
class T1628u4 {
    void foo(int i) {
        int j;
        switch (i) {
            case 0: j = 0; break;
            case 1: j = 0; break;
        }
        j++;
    }
}
    }]
} FAIL

tcltest::test 16.2.8-unassigned-5 { Switch blocks that fall through to default:
        that definitely assigns a variable ensures that the variable is
        definitely assigned after the switch } {

    compile [saveas T1628u5.java {
class T1628u5 {
    void foo(int i) {
        int j;
        switch (i) {
            case 0:
            case 1:
            default: j = 0;
        }
        j++;
    }
}
    }]
} PASS

tcltest::test 16.2.8-unassigned-6 { A variable within a switch statement must
        be assigned before use within any swith block } {
    switch_labels T1628u6 int {
            case 0:
                byte b = 1;
                break;
            case 1:
                byte c = b;
    }
} FAIL

tcltest::test 16.2.8-unassigned-7 { A variable declared in a switch statement
        may not be referenced before its declaration, even if the declaration
        will be skipped } {
    switch_labels T1628u7 int {
            case 0:
                byte c = b;
                break;
            case 1:
                byte b = 0;
    }
} FAIL

tcltest::test 16.2.8-unassigned-8 { v is DU if it is DU before breaks which
        exit the switch } {
    empty_main T1628u8 {
	int i = 1;
	final int j;
	switch (i) {
	    case 0:
	    try {
		j = 1;
		break; // doesn't exit switch
	    } finally {
		return;
	    }
	}
	j = 2;
    }
} PASS
