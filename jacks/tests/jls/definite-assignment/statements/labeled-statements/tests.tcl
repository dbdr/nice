# definite unassignment pass
tcltest::test 16.2.5-definite-unassign-pass-1 { V is DU after L:S iff V is DU
        after S and before all breaks in S that exit L } {
    empty_main T1625dup1 {
	final int i;
	l: {
	    if (true)
	        break l;
	    try {
		i = 1;
		break l; // cannot exit l
	    } finally {
		return;
	    }
	}
	i = 2;
    }
} PASS

tcltest::test 16.2.5-definite-unassign-pass-2 { V is DU after L:S iff V is DU
        after S and before all breaks in S that exit L } {
    empty_main T1625dup2 {
	final int i;
	l: {
	    if (false)
	        break l;
	    try {
		i = 1;
		break l; // cannot exit l
	    } finally {
		return;
	    }
	}
	i = 2;
    }
} PASS

tcltest::test 16.2.5-definite-unassign-pass-3 { V is DU after L:S iff V is DU
        after S and before all breaks in S that exit L } {
    empty_main T1625dup3 {
	final int i;
	boolean b = true;
	l: {
	    if (b)
	        break l;
	    try {
		i = 1;
		break l; // cannot exit l
	    } finally {
		return;
	    }
	}
	i = 2;
    }
} PASS

tcltest::test 16.2.5-definite-unassign-pass-4 { V is DU after L:S iff V is DU
        after S and before all breaks in S that exit L } {
    empty_main T1625dup4 {
	final int i;
	l: {}
	i = 1;
    }
} PASS

# definite unassignment fail
tcltest::test 16.2.5-definite-unassign-fail-1 { V is DU after L:S iff V is DU
        after S and before all breaks in S that exit L } {
    empty_main T1625duf1 {
	final int i;
	l: {
	    i = 1;
	    break l;
	}
	i = 2;
    }
} FAIL
	    
# definite assignment pass
tcltest::test 16.2.5-definite-assign-pass-1 { V is DA after L:S iff V is DA
        after S and before all breaks in S that exit L } {
    empty_main T1625dap1 {
	int i;
	l: i = 1;
	i++;
    }
} PASS

tcltest::test 16.2.5-definite-assign-pass-2 { V is DA after L:S iff V is DA
        after S and before all breaks in S that exit L } {
    empty_main T1625dap2 {
	int i;
	l: { i = 1; }
	i++;
    }
} PASS

tcltest::test 16.2.5-definite-assign-pass-3 { V is DA after L:S iff V is DA
        after S and before all breaks in S that exit L } {
    empty_main T1625dap3 {
	int i;
	l: {
	    i = 1;
	    break l;
	}
	i++;
    }
} PASS

tcltest::test 16.2.5-definite-assign-pass-4 { V is DA after L:S iff V is DA
        after S and before all breaks in S that exit L } {
    empty_main T1625dap4 {
	int i;
	l: {
	    if (false)
		break l;
	    return;
	}
	i++;
    }
} PASS

# definite assignment fail
tcltest::test 16.2.5-definite-assign-fail-1 { V is DA after L:S iff V is DA
        after S and before all breaks in S that exit L } {
    empty_main T1625daf1 {
	int i;
	l: break l;
	i++;
    }
} FAIL

tcltest::test 16.2.5-definite-assign-fail-2 { V is DA after L:S iff V is DA
        after S and before all breaks in S that exit L } {
    empty_main T1625daf2 {
	int i;
	boolean b = true;
	l: {
	    if (b)
	        break l;
	    i = 1;
	}
	i++;
    }
} FAIL

