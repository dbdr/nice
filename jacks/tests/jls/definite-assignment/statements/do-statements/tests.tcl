tcltest::test 16.2.10-definite-unassign-pass-1 { v is DU after the do if it
        is DU before every break which exits the do } {
    empty_main T16210dup1 {
	final int i;
	boolean b = true;
	do {
	    if (b)
                break;
	    try {
		i = 1;
		break; // doesn't exit do
	    } finally {
		return;
	    }
	} while (true);
	i = 2;
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-2 { v is DU before the condition
        if it is DU before every continue which exits the statement } {
    empty_main T16210dup2 {
	final int i;
	do {
	    try {
		i = 2;
		continue; // doesn't continue loop
	    } finally {
		return;
	    }
	} while (true);
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-3 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210dup3 {
	final int i;
	do
	    if (false)
	        i = 1; // assignment not reachable
	while (true);
	// the fact that i is not DU before the loop doesn't matter
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-4 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210dup4 {
	final int i;
	do
	    i = 1;
	while (false);
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-5 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210dup5 {
	final int i;
	do {
	    i = 1;
	    break;
	} while (true);
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-6 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210dup6 {
	final boolean b;
	do;
	while ((b = true) && false);
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-7 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210dup7 {
	final boolean b;
	do
	    continue;
	while ((b = true) && false);
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-8 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210dup8 {
	final int i;
	boolean b = true;
	do
	    i = 1;
	while (b && false);
    }
} PASS

tcltest::test 16.2.10-definite-unassign-pass-9 { v is DU after do S while(e);
        iff v is DU after e when false and v is DU before every exiting
        break } {
    empty_main T16210dup9 {
	final boolean b;
	b = true;
	do;
	while (b || true);
	b = false;
    }
} PASS

tcltest::test 16.2.10-definite-unassign-fail-1 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210duf1 {
	final int i;
	do
	    i = 1;
	while (true);
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-2 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210duf2 {
	final int i;
	do {
	    i = 1;
	    continue
	} while (true);
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-3 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210duf3 {
	final boolean b;
	do;
	while (b = false);
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-4 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16210duf4 {
	final boolean b;
	do
	    continue;
	while (b = false);
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-5 { v is DU after do S while(e);
        iff v is DU after e when false and v is DU before every exiting
        break } {
    empty_main T16210duf5 {
	final boolean b;
	do
	    break;
	while ((b = true) && false);
	b = true;
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-6 { v is DU after do S while(e);
        iff v is DU after e when false and v is DU before every exiting
        break } {
    empty_main T16210duf6 {
	final int i;
	do {
	    if (false)
                i = 1;
	    break;
	} while (true);
	i = 1;
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-7 { v is DU after do S while(e);
        iff v is DU after e when false and v is DU before every exiting
        break } {
    empty_main T16210duf7 {
	final int i;
	boolean b = true;
	do
	    if (false)
                i = 1;
	while (b && false);
	i = 1;
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-8 { v is DU after do S while(e);
        iff v is DU after e when false and v is DU before every exiting
        break } {
    empty_main T16210duf8 {
	final int i;
	do
	    if (true)
	        break;
	    else
                i = 1;
	while (false);
	i = 1;
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-9 { v is DU after do S while(e);
        iff v is DU after e when false and v is DU before every exiting
        break } {
    empty_main T16210duf9 {
	final int i;
	do
	    if (true)
	        break;
	    else
                i = 1;
	while (true);
	i = 1;
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-10 { V is DU before S iff V is
        DU before do S while(e); and V is DU after e when true } {
    empty_main T16210duf10 {
	final int i;
	i = 1;
	do
	    i = 2;
	while (true);
    }
} FAIL

tcltest::test 16.2.10-definite-unassign-fail-11 { V is DU before S iff V is
        DU before do S while(e); and V is DU after e when true } {
    empty_main T16210duf11 {
	final int i;
	i = 1;
	do
	    i = 2;
	while (false);
    }
} FAIL

tcltest::test 16.2.10-definite-assign-pass-1 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210dap1 {
	int i;
	boolean b = true;
	do;
	while (b || true);
	int j = i;
    }
} PASS

tcltest::test 16.2.10-definite-assign-pass-2 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210dap2 {
	int i;
	do
	    try {
		break; // not an exiting break;
	    } finally {
		i = 1;
		break;
	    }
        while (true);
        int j = i;
    }
} PASS

tcltest::test 16.2.10-definite-assign-pass-3 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210dap3 {
	boolean b;
	do;
	while (b = false);
	boolean x = b;
    }
} PASS

tcltest::test 16.2.10-definite-assign-pass-4 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210dap4 {
	int i;
	do
	    i = 1;
	while (false);
	int j = i;
    }
} PASS

tcltest::test 16.2.10-definite-assign-pass-5 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210dap5 {
	int i;
	do
	    if (false)
	        break;
	while (true);
	int j = i;
    }
} PASS

tcltest::test 16.2.10-definite-assign-fail-1 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210daf1 {
	int i;
	do
	    break;
	while (true);
	int j = i;
    }
} FAIL

tcltest::test 16.2.10-definite-assign-fail-2 { v is DA after do S while(e);
        iff v is DA after e when false and v is DA before every exiting
        break } {
    empty_main T16210daf2 {
	int i;
	boolean b;
	do
	    if (false)
	        break;
	while (b = true);
	int j = i;
    }
} FAIL

