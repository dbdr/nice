tcltest::test 15.23-optimize-1 { Test that a compiler that optimizes && does
        so correctly } {
    empty_main T1523o1 {
	boolean b = true;
	if (b && true);
	if (!b && true);
	b = false;
    }
} PASS

tcltest::test 15.23-optimize-2 { Test that a compiler that optimizes && does
        so correctly } {
    empty_main T1523o2 {
	boolean b = true;
	if (true && b);
	if (true && !b);
	b = false;
    }
} PASS

tcltest::test 15.23-optimize-3 { Test that a compiler that optimizes && does
        so correctly } {
    empty_main T1523o3 {
	boolean b = true;
	if (b && false);
	if (!b && false);
	b = false;
    }
} PASS

tcltest::test 15.23-optimize-4 { Test that a compiler that optimizes && does
        so correctly } {
    empty_main T1523o4 {
	boolean b = true;
	if (false && b);
	if (false && !b);
	b = false;
    }
} PASS

