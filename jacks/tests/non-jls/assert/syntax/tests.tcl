# These tests cover the assert statement, added in JLS 1.4 by JSR 41.

tcltest::test non-jls-jsr41.2-syntax-1 { the one-argument form of
        assert } {assert} {
    empty_main T412s1 {
	assert false;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-2 { the one-argument form of assert must
        have a boolean condition } {assert} {
    empty_main T412s2 {
	assert 1;
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-3 { the one-argument form of assert must
        have a boolean condition } {assert} {
    empty_main T412s3 {
	assert null;
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-4 { the one-argument form of assert must
        have a boolean condition } {assert} {
    empty_main T412s4 {
	assert new Object();
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-5 { the one-argument form of assert must
        have a boolean condition } {assert} {
    empty_main T412s5 {
	assert System.out.println();
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-6 { the two-argument form of
        assert } {assert} {
    empty_main T412s6 {
	assert false : "Bad code!";
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-7 { in the two-argument form of assert,
        the first argument must be boolean } {assert} {
    empty_main T412s7 {
	assert 1 : "Bad code!";
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-8 { in the two-argument form of assert,
        the first argument must be boolean } {assert} {
    empty_main T412s8 {
	assert null : "Bad code!";
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-9 { in the two-argument form of assert,
        the first argument must be boolean } {assert} {
    empty_main T412s9 {
	assert new Object() : "Bad code!";
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-10 { in the two-argument form of assert,
        the first argument must be boolean } {assert} {
    empty_main T412s10 {
	assert System.out.println() : "Bad code!";
    }
} FAIL

tcltest::test non-jls-jsr41.2-syntax-11 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s11 {
	assert false : null;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-12 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s12 {
	assert false : new Object();
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-13 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s13 {
	assert false : "String";
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-14 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s14 {
	assert false : true;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-15 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s15 {
	assert false : (byte) 1;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-16 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s16 {
	assert false : (short) 1;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-17 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s17 {
	assert false : '1';
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-18 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s18 {
	assert false : 1;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-19 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s19 {
	assert false : 1L;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-20 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s20 {
	assert false : 1f;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-21 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s21 {
	assert false : 1.0;
    }
} PASS

tcltest::test non-jls-jsr41.2-syntax-22 { in the two-argument form of assert,
        the second argument may be anything but void } {assert} {
    empty_main T412s22 {
	assert false : System.out.println();
    }
} FAIL

tcltest::test non-jls-jsr41.2-nesting-1 { The assert statement has no trailing
        substatement.  This tests that a source-to-source translation
        doesn't mismatch the implicit if statement of the assert with
        the trailing else statements, provided definite assignment
        works correctly. } {assert} {
    empty_main T412n1 {
        final int i;
        i = 1;
	if (true)
	    assert false;
        else
            i = 1; // definite assignment allows this in provably dead branch
    }
} PASS

tcltest::test non-jls-jsr41.2-nesting-2 { The four legal uses of : nested in
        one statement } {assert} {
    empty_main T412n2 {
	int i = 1;
	switch (i) {
	case 1: label: assert i==1 ? true : false : i==1 ? "yes" : "no";
	}
    }
} PASS

tcltest::test non-jls-jsr41.2-nesting-3 { The arguments of assert may be any
        expression } {assert} {
    empty_main T412n3 {
	boolean b = true;
	int i;
	assert b &= false ^ (1L * 4 >>> 3 < 17.5) || "" != null : i = 1;
    }
} PASS

tcltest::test non-jls-jsr41.2-nesting-4 { The arguments of assert may be any
        expression (see Sun bug 4446137 for this example) } {assert} {
    empty_main T412n4 {
	int i = 1;
	assert (i & 1) == 0;
	assert ((i & 1) == 0);
	assert 0 == (i & 1);
    }
} PASS

tcltest::test non-jls-jsr41.2-clash-1 { The synthetic name used by the compiler
        should not clash with user names (if only the JLS would give compilers
        a reserved namespace...) } {assert} {
    ok_pass_or_warn [empty_class T412c1 {
	int $assertionsDisabled = 1;
        void foo() {
	    assert false : $assertionsDisabled + 1;
	}
    }]
} OK
