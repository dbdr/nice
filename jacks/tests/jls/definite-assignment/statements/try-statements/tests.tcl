# Note that JLS2 has some errors. The correct rules are tested here, with
# the definition that a reachable assignment is one where a variable is not
# simultaneously DA and DU
tcltest::test 16.2.14-definite-unassign-fail-1 { v is DU before a catch block
        iff it is DU before the try block and the try block has no reachable
        assignments to v } {
    empty_main T16214duf1 {
	final int i;
	int count = 0;
	while (true) {
	    try {
		i = count++;
		count /= 0; // throws ArithmeticException
		while (true);
	    } catch (Exception e) {
		// i not DU, since try block had reachable assignment
	    }
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-2 { v is DU before a finally block
        iff it is DU before the try block and the try block and all catch
        blocks have no reachable assignments to v } {
    empty_main T16214duf2 {
	final int i;
	int count = 0;
	while (true) {
	    try {
		i = count++;
		count /= 0; // throws ArithmeticException
		while (true);
	    } finally {
		// i not DU, since try block had reachable assignment
		continue; // discards exception
	    }
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-3 { v is DU before a finally block
        iff it is DU before the try block and the try block and all catch
        blocks have no reachable assignments to v } {
    empty_main T16214duf3 {
	final int i;
	int count = 0;
	while (true) {
	    try {
		i = count++;
		count /= 0; // throws ArithmeticException
		while (true);
	    } catch (Exception e) {
	    } finally {
		// i not DU, since try block had reachable assignment
	    }
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-4 { v is DU before a finally block
        iff it is DU before the try block and the try block and all catch
        blocks have no reachable assignments to v } {
    empty_main T16214duf4 {
	final int i;
	int count = 0;
	while (true) {
	    try {
		throw new Exception();
	    } catch (Exception e) {
		i = count++;
		count /= 0; // throws ArithmeticException
		while (true);
	    } finally {
		// i not DU, since catch block had reachable assignment
		continue; // discards exception
	    }
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-5 { v is DU before a catch block
        iff it is DU before the try block and the try block has no reachable
        assignments to v } {
    empty_class T16214duf5 {
	static final int i;
	static void m() { throw new RuntimeException(); }
	static {
	    try {
		i = 1;
		m();
		while (true);
	    } catch (RuntimeException e) {
		i = 2;
	    }
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-6 { v is DU before a finally block
        iff it is DU before the try block and the try block and all catch
        blocks have no reachable assignments to v } {
    empty_class T16214duf6 {
	static final int i;
	static void m() { throw new RuntimeException(); }
	static {
	    l: try {
		i = 1;
		m();
		while (true);
	    } finally {
		i = 2;
		break l;
	    }
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-7 { v is DU before a finally block
        iff it is DU before the try block and the try block and all catch
        blocks have no reachable assignments to v } {
    empty_class T16214duf7 {
	static final int i;
	static void m() { throw new RuntimeException(); }
	static {
	    try {
		i = 1;
		m();
		while (true);
	    } catch (RuntimeException e) {
	    } finally {
		i = 2;
	    }
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-8 { v is DU before a finally block
        iff it is DU before the try block and the try block and all catch
        blocks have no reachable assignments to v } {
    empty_class T16214duf8 {
	static final int i;
	static void m() { throw new RuntimeException(); }
	static {
	    l: try {
		m();
	    } catch (RuntimeException e) {
		i = 1;
		m();
		while (true);
	    } finally {
		i = 2;
		break l;
	    }
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-9 { the DA/DU status before a
        break is dependent on a finally that completes normally } {
    empty_main T16214duf9 {
	final int i;
	while (true) {
	    l: {
		try {
		    if (true)
		        break l; // intervening finally assigns i
		} finally {
		    i = 1;
		}
		return;
	    }
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-10 { the DA/DU status before a
        break is dependent on a finally that completes normally } {
    empty_main T16214duf10 {
	final int i;
	for ( ; ; ) {
	    l: {
		try {
		    if (true)
		        break l; // intervening finally assigns i
		} finally {
		    i = 1;
		}
		return;
	    }
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-11 { the DA/DU status before a
        break is dependent on a finally that completes normally } {
    empty_main T16214duf11 {
	final int i;
	do
	    l: {
		try {
		    if (true)
		        break l; // intervening finally assigns i
		} finally {
		    i = 1;
		}
		return;
	    }
	    // i multiply assigned in loop
	while (true);
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-12 { the DA/DU status before a
        break is dependent on a finally that completes normally } {
    empty_main T16214duf12 {
	final int i;
	int j = 1;
	switch (j) {
	    case 0:
	    try {
		if (true)
		    break; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	    return;
	}
	i = 2; // i multiply assigned
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-13 { the DA/DU status before a
        break is dependent on a finally that completes normally } {
    empty_main T16214duf13 {
	final int i;
	l: {
	    try {
		if (true)
		    break l; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	    return;
	}
	i = 2; // i multiply assigned
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-14 { the DA/DU status before a
        continue is dependent on a finally that completes normally } {
    empty_main T16214duf14 {
	final int i;
	while (true) {
	    try {
		if (true)
		    continue; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	    return;
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-15 { the DA/DU status before a
        continue is dependent on a finally that completes normally } {
    empty_main T16214duf15 {
	final int i;
	for ( ; ; ) {
	    try {
		if (true)
		    continue; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	    return;
	    // i multiply assigned in loop
	}
    }
} FAIL

tcltest::test 16.2.14-definite-unassign-fail-16 { the DA/DU status before a
        continue is dependent on a finally that completes normally } {
    empty_main T16214duf16 {
	final int i;
	do {
	    try {
		if (true)
		    continue; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	    return;
	    // i multiply assigned in loop
	} while (true);
    }
} FAIL

