# Definite assignment violations
tcltest::test 16.1.2-definite-assignment-fail-1 { V is DA after a && b when
        true iff V is DA after b when true } {
    empty_main T1612daf1 {
        boolean x, y = true;
        if (y && y)
            y = x;
    }
} FAIL

tcltest::test 16.1.2-definite-assignment-fail-2 { V is DA after a && b when
        false iff V is DA after a when false and V is DA after b when false } {
    empty_main T1612daf2 {
        boolean x, y = true;
        if (y && true); // DA after b, but not after a
        else y = x;
    }
} FAIL

tcltest::test 16.1.2-definite-assignment-fail-3 { V is DA after a && b when
        false iff V is DA after a when false and V is DA after b when false } {
    empty_main T1612daf3 {
        boolean x, y = true;
        if (true && y); // DA after a, but not after b
        else y = x;
    }
} FAIL

tcltest::test 16.1.2-definite-assignment-fail-4 { V is DA before a iff V is
        DA before a && b } {
    empty_main T1612daf4 {
        boolean x;
        boolean y = x && true;
    }
} FAIL

tcltest::test 16.1.2-definite-assignment-fail-5 { V is DA before b iff V is
        DA after a when true } {
    empty_main T1612daf5 {
        boolean x, y = true;
        y = y && x;
    }
} FAIL

tcltest::test 16.1.2-definite-assignment-fail-6 { V is DA after a && b iff
        V is DA after a && b when true and when false } {
    empty_main T1612daf6 {
        boolean x, y = true;
        y = y && (x = true) // DA after a && b when true, but not when false
        y = x;
    }
} FAIL

tcltest::test 16.1.2-definite-assignment-fail-7 { V is DA after a && b iff
        V is DA after a && b when true and when false } {
    empty_main T1612daf7 {
        boolean x, y = true;
        y = true && (true ? true : y) // DA after a && b when false,
                                      // but not when true
        y = x;
    }
} FAIL

# Definite assignment okay
tcltest::test 16.1.2-definite-assignment-pass-1 { V is DA after a && b when
        true iff V is DA after b when true } {
    empty_main T1612dap1 {
        boolean x, y = true;
        if (y && false)
            y = x;
    }
} PASS

tcltest::test 16.1.2-definite-assignment-pass-2 { V is DA after a && b when
        true iff V is DA after b when true } {
    empty_main T1612dap2 {
        boolean x, y = true;
        if (false && y)
            y = x;
    }
} PASS

tcltest::test 16.1.2-definite-assignment-pass-3 { V is DA after a && b when
        false iff V is DA after a when false and V is DA after b when false } {
    empty_main T1612dap3 {
        boolean x, y = true;
        if (true && (true ? true : y));
        else y = x;
    }
} PASS

tcltest::test 16.1.2-definite-assignment-pass-4 { V is DA before a iff V is
        DA before a && b } {
    empty_main T1612dap4 {
        boolean x = false;
        boolean y = x && true;
    }
} PASS

tcltest::test 16.1.2-definite-assignment-pass-5 { V is DA before b iff V is
        DA after a when true } {
    empty_main T1612dap5 {
        boolean x;
        boolean y = false && x;
    }
} PASS

tcltest::test 16.1.2-definite-assignment-pass-6 { V is DA after a && b iff
        V is DA after a && b when true and when false } {
    empty_main T1612dap6 {
        boolean x, y = true;
        y = (x = true) && y;
        y = x;
    }
} PASS

# Definite unassignment violations
tcltest::test 16.1.2-definite-unassignment-fail-1 { V is DU after a && b when
        true iff V is DU after b when true } {
    empty_main T1612duf1 {
        final boolean x;
        boolean y = x = true;
        if (y && true)
            x = y;
    }
} FAIL

tcltest::test 16.1.2-definite-unassignment-fail-2 { V is DU after a && b when
        false iff V is DU after a when false and V is DU after b when false } {
    empty_main T1612duf2 {
        final boolean x;
        boolean y = true;
        if ((x = true) && true); // DU after b, but not after a
        else x = y;
    }
} FAIL

tcltest::test 16.1.2-definite-unassignment-fail-3 { V is DU after a && b when
        false iff V is DU after a when false and V is DU after b when false } {
    empty_main T1612duf3 {
        final boolean x, y;
        if (true && (x = false)); // DU after a, but not after b
        else x = y;
    }
} FAIL

tcltest::test 16.1.2-definite-unassignment-fail-4 { V is DU before a iff V is
        DU before a && b } {
    empty_main T1612duf4 {
        final boolean x;
        x = true;
        boolean y = (x = true) && true;
    }
} FAIL

tcltest::test 16.1.2-definite-unassignment-fail-5 { V is DU before b iff V is
        DU after a when true } {
    empty_main T1612duf5 {
        final boolean x;
        boolean y = (x = false) && (x = true);
    }
} FAIL

tcltest::test 16.1.2-definite-unassignment-fail-6 { V is DU after a && b iff
        V is DU after a && b when true and when false } {
    empty_main T1612duf6 {
        final boolean x;
        boolean y = false && ((x = true) || true);
        // DU after a && b when false, but not when true
        x = y;
    }
} FAIL

tcltest::test 16.1.2-definite-unassignment-fail-7 { V is DU after a && b iff
        V is DU after a && b when true and when false } {
    empty_main T1612duf7 {
        final boolean x;
        boolean y = (x = true) && false;
        // DU after a && b when true, but not when false
        x = y;
    }
} FAIL

# Definite unassignment okay
tcltest::test 16.1.2-definite-unassignment-pass-1 { V is DU after a && b when
        true iff V is DU after b when true } {
    empty_main T1612dup1 {
        final boolean x;
        if ((x = true) && false)
            x = true;
    }
} PASS

tcltest::test 16.1.2-definite-unassignment-pass-2 { V is DU after a && b when
        false iff V is DU after a when false and V is DU after b when false } {
    empty_main T1612dup2 {
        final boolean x;
        if (true && ((x = false) || true));
        else x = true;
    }
} PASS

tcltest::test 16.1.2-definite-unassignment-pass-3 { V is DU before a iff V is
        DU before a && b } {
    empty_main T1612dup3 {
        final boolean x;
        boolean y = (x = true) && true;
    }
} PASS

tcltest::test 16.1.2-definite-unassignment-pass-4 { V is DU before b iff V is
        DU after a when true } {
    empty_main T1612dup4 {
        final boolean x;
        x = true;
        boolean y = false && (x = false);
    }
} PASS

tcltest::test 16.1.2-definite-unassignment-pass-5 { V is DU after a && b iff
        V is DU after a && b when true and when false } {
    empty_main T1612dup5 {
        final boolean x;
        boolean y = false;
        y = true && y;
        x = y;
    }
} PASS
