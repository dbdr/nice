# Definite assignment violations
tcltest::test 16.1.3-definite-assignment-fail-1 { V is DA after a || b when
        false iff V is DA after b when false } {
    empty_main T1613daf1 {
        boolean x, y = false;
        if (y || y);
        else y = x;
    }
} FAIL

tcltest::test 16.1.3-definite-assignment-fail-2 { V is DA after a || b when
        true iff V is DA after a when true and V is DA after b when true } {
    empty_main T1613daf2 {
        boolean x, y = false;
        if (y || false) // DA after b, but not after a
            y = x;
    }
} FAIL

tcltest::test 16.1.3-definite-assignment-fail-3 { V is DA after a || b when
        true iff V is DA after a when true and V is DA after b when true } {
    empty_main T1613daf3 {
        boolean x, y = false;
        if (false || y) // DA after a, but not after b
            y = x;
    }
} FAIL

tcltest::test 16.1.3-definite-assignment-fail-4 { V is DA before a iff V is
        DA before a || b } {
    empty_main T1613daf4 {
        boolean x;
        boolean y = x || false;
    }
} FAIL

tcltest::test 16.1.3-definite-assignment-fail-5 { V is DA before b iff V is
        DA after a when false } {
    empty_main T1613daf5 {
        boolean x, y = false;
        y = y || x;
    }
} FAIL

tcltest::test 16.1.3-definite-assignment-fail-6 { V is DA after a || b iff
        V is DA after a || b when true and when false } {
    empty_main T1613daf6 {
        boolean x, y = false;
        y = y || (x = false) // DA after a || b when false, but not when true
        y = x;
    }
} FAIL

tcltest::test 16.1.3-definite-assignment-fail-7 { V is DA after a || b iff
        V is DA after a || b when true and when false } {
    empty_main T1613daf7 {
        boolean x, y = true;
        y = false || (false ? y : false) // DA after a || b when true,
                                         // but not when false
        y = x;
    }
} FAIL

# Definite assignment okay
tcltest::test 16.1.3-definite-assignment-pass-1 { V is DA after a || b when
        false iff V is DA after b when false } {
    empty_main T1613dap1 {
        boolean x, y = false;
        if (y || true);
        else y = x;
    }
} PASS

tcltest::test 16.1.3-definite-assignment-pass-2 { V is DA after a || b when
        false iff V is DA after b when false } {
    empty_main T1613dap2 {
        boolean x, y = false;
        if (true || y);
        else y = x;
    }
} PASS

tcltest::test 16.1.3-definite-assignment-pass-3 { V is DA after a || b when
        true iff V is DA after a when true and V is DA after b when true } {
    empty_main T1613dap3 {
        boolean x, y = false;
        if (false || (false ? y : false))
            y = x;
    }
} PASS

tcltest::test 16.1.3-definite-assignment-pass-4 { V is DA before a iff V is
        DA before a || b } {
    empty_main T1613dap4 {
        boolean x = true;
        boolean y = x || false;
    }
} PASS

tcltest::test 16.1.3-definite-assignment-pass-5 { V is DA before b iff V is
        DA after a when false } {
    empty_main T1613dap5 {
        boolean x;
        boolean y = true || x;
    }
} PASS

tcltest::test 16.1.3-definite-assignment-pass-6 { V is DA after a || b iff
        V is DA after a || b when true and when false } {
    empty_main T1613dap6 {
        boolean x, y = false;
        y = (x = false) || y;
        y = x;
    }
} PASS

# Definite unassignment violations
tcltest::test 16.1.3-definite-unassignment-fail-1 { V is DU after a || b when
        false iff V is DU after b when false } {
    empty_main T1613duf1 {
        final boolean x;
        boolean y = x = false;
        if (y || false);
        else x = y;
    }
} FAIL

tcltest::test 16.1.3-definite-unassignment-fail-2 { V is DU after a || b when
        true iff V is DU after a when true and V is DU after b when true } {
    empty_main T1613duf2 {
        final boolean x;
        boolean y = false;
        if ((x = false) || false) // DU after b, but not after a
            x = y;
    }
} FAIL

tcltest::test 16.1.3-definite-unassignment-fail-3 { V is DU after a || b when
        true iff V is DU after a when true and V is DU after b when true } {
    empty_main T1613duf3 {
        final boolean x, y;
        if (false || (x = true)) // DU after a, but not after b
            x = y;
    }
} FAIL

tcltest::test 16.1.3-definite-unassignment-fail-4 { V is DU before a iff V is
        DU before a || b } {
    empty_main T1613duf4 {
        final boolean x;
        x = false;
        boolean y = (x = false) || false;
    }
} FAIL

tcltest::test 16.1.3-definite-unassignment-fail-5 { V is DU before b iff V is
        DU after a when false } {
    empty_main T1613duf5 {
        final boolean x;
        boolean y = (x = true) || (x = false);
    }
} FAIL

tcltest::test 16.1.3-definite-unassignment-fail-6 { V is DU after a || b iff
        V is DU after a || b when true and when false } {
    empty_main T1613duf6 {
        final boolean x;
        boolean y = true || ((x = false) && false);
        // DU after a || b when true, but not when false
        x = y;
    }
} FAIL

tcltest::test 16.1.3-definite-unassignment-fail-7 { V is DU after a || b iff
        V is DU after a || b when true and when false } {
    empty_main T1613duf7 {
        final boolean x;
        boolean y = (x = false) || true;
        // DU after a || b when false, but not when true
        x = y;
    }
} FAIL

# Definite unassignment okay
tcltest::test 16.1.3-definite-unassignment-pass-1 { V is DU after a || b when
        false iff V is DU after b when false } {
    empty_main T1613dup1 {
        final boolean x;
        if ((x = false) || true);
        else x = false;
    }
} PASS

tcltest::test 16.1.3-definite-unassignment-pass-2 { V is DU after a || b when
        true iff V is DU after a when true and V is DU after b when true } {
    empty_main T1613dup2 {
        final boolean x;
        if (false || ((x = true) && false))
            x = true;
    }
} PASS

tcltest::test 16.1.3-definite-unassignment-pass-3 { V is DU before a iff V is
        DU before a || b } {
    empty_main T1613dup3 {
        final boolean x;
        boolean y = (x = false) || false;
    }
} PASS

tcltest::test 16.1.3-definite-unassignment-pass-4 { V is DU before b iff V is
        DU after a when false } {
    empty_main T1613dup4 {
        final boolean x;
        x = false;
        boolean y = true || (x = true);
    }
} PASS

tcltest::test 16.1.3-definite-unassignment-pass-5 { V is DU after a || b iff
        V is DU after a || b when true and when false } {
    empty_main T1613dup5 {
        final boolean x;
        boolean y = true;
        y = false || y;
        x = y;
    }
} PASS
