tcltest::test 8.3.1.2-final-1 { instance and class fields may be final } {
    empty_class T8312f1 {
	final int i = 1;
	static final int j = 2;
    }
} PASS

tcltest::test 8.3.1.2-final-2 { blank final class variables must be assigned
        in an initializer } {
    empty_class T8312f2 {
	static final int i;
    }
} FAIL

tcltest::test 8.3.1.2-final-3 { blank final class variables must be assigned
        in an initializer } {
    empty_class T8312f3 {
	static final int i;
	static {
	    if (false)
                i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-4 { blank final class variables must be assigned
        in an initializer } {
    empty_class T8312f4 {
	static final int i;
	static int j = (true ? 1 : (i = 2));
    }
} FAIL

tcltest::test 8.3.1.2-final-5 { blank final class variables must be assigned
        in an initializer } {
    empty_class T8312f5 {
	static final int i;
	static {
	    i = 1;
	}
    }
} PASS

tcltest::test 8.3.1.2-final-6 { blank final class variables must be assigned
        in an initializer } {
    empty_class T8312f6 {
	static final int i;
	static int j = i = 1;
    }
} PASS

tcltest::test 8.3.1.2-final-7 { blank final instance variables must be assigned
        by the end of every constructor } {
    empty_class T8312f7 {
	final int i;
    }
} FAIL

tcltest::test 8.3.1.2-final-8 { blank final instance variables must be assigned
        by the end of every constructor } {
    empty_class T8312f8 {
	final int i;
	{
	    if (false)
                i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-9 { blank final instance variables must be assigned
        by the end of every constructor } {
    empty_class T8312f9 {
	final int i;
	T8312f9() {
	    if (false)
                i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-10 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f10 {
	final int i;
	{
	    if (false)
                i = 1;
	}
	T8312f10() {
	    if (false)
                i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-11 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f11 {
	final int i;
	T8312f11(int j){
	}
	T8312f11() {
	    i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-12 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f12 {
	final int i;
	int j = (true ? 1 : (i = 2));
    }
} FAIL

tcltest::test 8.3.1.2-final-13 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f13 {
	final int i;
	int j = i = 1;
    }
} PASS

tcltest::test 8.3.1.2-final-14 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f14 {
	final int i;
	{ i = 1; }
    }
} PASS

tcltest::test 8.3.1.2-final-15 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f15 {
	final int i;
	T8312f15() {
	    i = 1;
	}
    }
} PASS

tcltest::test 8.3.1.2-final-16 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f16 {
	final int i;
	T8312f16(int j) {
	    this();
	}
	T8312f16() {
	    i = 1;
	}
    }
} PASS

tcltest::test 8.3.1.2-final-17 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f17 {
	final int i;
	T8312f17() {
	    if (true)
	        return;
	    i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-18 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f18 {
	final int i;
	T8312f18() {
	    if (false)
	        return;
	    i = 1;
	}
    }
} PASS

tcltest::test 8.3.1.2-final-19 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f19 {
	final int i;
	T8312f19(boolean b) {
	    if (b)
	        return;
	    i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-20 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f20 {
	final int i;
	T8312f20() {
	    l: try {
		return; // discarded by abrupt finally
	    } finally {
		break l;
	    }
	    i = 1;
	}
    }
} PASS

tcltest::test 8.3.1.2-final-21 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f21 {
	final int i;
	T8312f21(boolean b) {
	    try {
		if (b)
		    return;
	    } finally {
	    }
	    i = 1;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-22 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f22 {
	final int i;
	T8312f22() {
	    while (true);
	}
    }
} PASS

tcltest::test 8.3.1.2-final-23 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f23 {
	final int i;
	T8312f23(boolean b) {
	    while (b || true);
	}
    }
} PASS

tcltest::test 8.3.1.2-final-24 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f24 {
	final int i;
	T8312f24() {
	    return;
	}
    }
} FAIL

tcltest::test 8.3.1.2-final-25 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f25 {
	final int i;
	T8312f25() {
	    try {
		return; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	}
    }
} PASS

tcltest::test 8.3.1.2-final-26 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f26 {
	final int i;
	T8312f26(boolean b) {
	    try {
		if (b)
		    return; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	}
    }
} PASS

tcltest::test 8.3.1.2-final-27 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f27 {
	final int i;
	T8312f27(boolean b) {
	    l: try {
		if (b)
		    return; // intervening finally assigns i
		else
		    break l; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	}
    }
} PASS

tcltest::test 8.3.1.2-final-28 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f28 {
	final int i;
	T8312f28(boolean b) {
	    l: try {
		break l; // intervening finally assigns i
	    } finally {
		i = 1;
	    }
	}
    }
} PASS

tcltest::test 8.3.1.2-final-29 { blank final instance variables must be
        assigned by the end of every constructor } {
    empty_class T8312f29 {
	final int i;
	T8312f29(boolean b) {
	    l: do
	        try {
		    continue l; // intervening finally assigns i
		} finally {
		    i = 1;
		}
	    while (false);
	}
    }
} PASS

