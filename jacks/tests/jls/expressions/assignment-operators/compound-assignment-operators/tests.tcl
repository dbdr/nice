tcltest::test 15.26.2-string-1 { String += null is valid } {
    compile [saveas T15262s1.java {
class T15262s1 {
    void foo() {
	String s = "";
	s += null;
    }
}
    }]
} PASS

tcltest::test 15.26.2-string-2 { String += void method is invalid } {
  compile [saveas T15262s2.java {
class T15262s2 {
    void foo() {
	String s = "";
	s += System.out.println();
    }
}
    }]
} FAIL

tcltest::test 15.26.2-string-3 { left-hand side of += should be String } {
    # Sun seems to have relaxed this to left-hand side of += should be
    # assignment compatible if right-hand side is type String (or null)
    compile [saveas T15262s3.java {
class T15262s3 {
    void foo() {
	Object o = null;
	o += "";
    }
}
    }]
} FAIL

tcltest::test 15.26.2-string-4 { left-hand side of += should be String } {
    # Sun seems to have relaxed this to left-hand side of += should be
    # assignment compatible if right-hand side is type String (or null)
    compile [saveas T15262s4.java {
class T15262s4 {
    void foo() {
	Comparable[] c = new Integer[1];
	c[0] += "";
    }
}
    }]
} FAIL

tcltest::test 15.26.2-string-5 { test String += when right side is not String,
        jikes bug 3066 } {
    empty_main T15262s5 {
	String s = "";
	s += true;
	s += 1;
	s += '1';
	s += 1L;
	s += 1.0f;
	s += 1.0;
    }
} PASS

tcltest::test 15.26.2-string-6 { test String += when right side is not String,
        jikes bug 3066 } {
    empty_main T15262s5 {
	String s = "";
	s += true + s;
	s += 1 + s;
	s += '1' + s;
	s += 1L + s;
	s += 1.0f + s;
	s += 1.0 + s;
    }
} PASS

# Test *=
tcltest::test 15.26.2-mult-1 { test legal identity *= assignments } {
    empty_main T15262mult1 {
	byte b = 1;
	b *= 1;
	short s = 1;
	s *= 1;
	char c = 1;
	c *= 1;
	int i = 1;
	i *= 1;
	long l = 1;
	l *= 1;
	float f = 1;
	f *= 1;
	double d = 1;
	d *= 1;
    }
} PASS

tcltest::test 15.26.2-mult-2 { test illegal *= assignments: boolean argument } {
    empty_main T15262mult2 {
	int i = 1;
	i *= false;
    }
} FAIL

tcltest::test 15.26.2-mult-3 { test illegal *= assignments: void argument } {
    empty_class T15262mult3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i *= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-mult-4 { test illegal *= assignments: Object argument } {
    empty_main T15262mult4 {
	int i = 1;
	i *= new Object();
    }
} FAIL

tcltest::test 15.26.2-mult-5 { test illegal *= assignments: String argument } {
    empty_main T15262mult5 {
	int i = 1;
	i *= "";
    }
} FAIL

tcltest::test 15.26.2-mult-6 { test illegal *= assignments: null argument } {
    empty_main T15262mult6 {
	int i = 1;
	i *= null;
    }
} FAIL

tcltest::test 15.26.2-mult-7 { test illegal *= assignments: Object variable } {
    empty_main T15262mult7 {
	Object o = new Object();
	o *= 1;
    }
} FAIL

tcltest::test 15.26.2-mult-8 { test illegal *= assignments: String variable } {
    empty_main T15262mult8 {
	String s = "";
	s *= 1;
    }
} FAIL

tcltest::test 15.26.2-mult-9 { test illegal *= assignments: null variable } {
    empty_main T15262mult9 {
	null *= 1;
    }
} FAIL

tcltest::test 15.26.2-mult-10 { test illegal *= assignments: boolean variable } {
    empty_main T15262mult10 {
	boolean b = true;
	b *= true;
    }
} FAIL

tcltest::test 15.26.2-mult-11 { test illegal *= assignments: non-variable } {
    empty_main T15262mult11 {
	1 *= 1;
    }
} FAIL

tcltest::test 15.26.2-mult-12 { test illegal *= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262mult12 {
	int i = 1;
	(i) *= 1;
    }]
} OK

tcltest::test 15.26.2-mult-13 { test widening *= assignments } {
    empty_main T15262mult13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	s *= b;
	i *= b;
	i *= s;
	i *= c;
	l *= b;
	l *= s;
	l *= c;
	l *= i;
	f *= b;
	f *= s;
	f *= c;
	f *= i;
	f *= l;
	d *= b;
	d *= s;
	d *= c;
	d *= i;
	d *= l;
	d *= f;
    }
} PASS

tcltest::test 15.26.2-mult-14 { test narrowing *= assignments } {
    empty_main T15262mult14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	b *= s;
	b *= c;
	b *= i;
	b *= l;
	b *= f;
	b *= d;
	s *= c;
	s *= i;
	s *= l;
	s *= f;
	s *= d;
	c *= b;
	c *= s;
	c *= i;
	c *= l;
	c *= f;
	c *= d;
	i *= l;
	i *= f;
	i *= d;
	l *= f;
	l *= d;
	f *= d;
    }
} PASS

# Test /=
tcltest::test 15.26.2-div-1 { test legal identity /= assignments } {
    empty_main T15262div1 {
	byte b = 1;
	b /= 1;
	short s = 1;
	s /= 1;
	char c = 1;
	c /= 1;
	int i = 1;
	i /= 1;
	long l = 1;
	l /= 1;
	float f = 1;
	f /= 1;
	double d = 1;
	d /= 1;
    }
} PASS

tcltest::test 15.26.2-div-2 { test illegal /= assignments: boolean argument } {
    empty_main T15262div2 {
	int i = 1;
	i /= false;
    }
} FAIL

tcltest::test 15.26.2-div-3 { test illegal /= assignments: void argument } {
    empty_class T15262div3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i /= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-div-4 { test illegal /= assignments: Object argument } {
    empty_main T15262div4 {
	int i = 1;
	i /= new Object();
    }
} FAIL

tcltest::test 15.26.2-div-5 { test illegal /= assignments: String argument } {
    empty_main T15262div5 {
	int i = 1;
	i /= "";
    }
} FAIL

tcltest::test 15.26.2-div-6 { test illegal /= assignments: null argument } {
    empty_main T15262div6 {
	int i = 1;
	i /= null;
    }
} FAIL

tcltest::test 15.26.2-div-7 { test illegal /= assignments: Object variable } {
    empty_main T15262div7 {
	Object o = new Object();
	o /= 1;
    }
} FAIL

tcltest::test 15.26.2-div-8 { test illegal /= assignments: String variable } {
    empty_main T15262div8 {
	String s = "";
	s /= 1;
    }
} FAIL

tcltest::test 15.26.2-div-9 { test illegal /= assignments: null variable } {
    empty_main T15262div9 {
	null /= 1;
    }
} FAIL

tcltest::test 15.26.2-div-10 { test illegal /= assignments: boolean variable } {
    empty_main T15262div10 {
	boolean b = true;
	b /= true;
    }
} FAIL

tcltest::test 15.26.2-div-11 { test illegal /= assignments: non-variable } {
    empty_main T15262div11 {
	1 /= 1;
    }
} FAIL

tcltest::test 15.26.2-div-12 { test illegal /= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262div12 {
	int i = 1;
	(i) /= 1;
    }]
} OK

tcltest::test 15.26.2-div-13 { test widening /= assignments } {
    empty_main T15262div13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	s /= b;
	i /= b;
	i /= s;
	i /= c;
	l /= b;
	l /= s;
	l /= c;
	l /= i;
	f /= b;
	f /= s;
	f /= c;
	f /= i;
	f /= l;
	d /= b;
	d /= s;
	d /= c;
	d /= i;
	d /= l;
	d /= f;
    }
} PASS

tcltest::test 15.26.2-div-14 { test narrowing /= assignments } {
    empty_main T15262div14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	b /= s;
	b /= c;
	b /= i;
	b /= l;
	b /= f;
	b /= d;
	s /= c;
	s /= i;
	s /= l;
	s /= f;
	s /= d;
	c /= b;
	c /= s;
	c /= i;
	c /= l;
	c /= f;
	c /= d;
	i /= l;
	i /= f;
	i /= d;
	l /= f;
	l /= d;
	f /= d;
    }
} PASS

tcltest::test 15.26.2-div-zero-1 { test integer /= by integer 0
        (warning recommended) } {
    ok_pass_or_warn [empty_class T15262dz1 {
        int i = 1;
        void foo() {
            i /= 0;
            i /= 0L;
        }
    }]
} OK

tcltest::test 15.26.2-div-zero-2 { test integer /= by integer 0
        (warning recommended) } {
    ok_pass_or_warn [empty_class T15262dz2 {
        long l = 1;
        void foo() {
            l /= 0;
            l /= 0L;
        }
    }]
} OK

tcltest::test 15.26.2-div-zero-3 { test integer /= by floating 0
       (no warning needed) } {
    empty_main T15262dz3 {
	int i = 1;
	long l = 1;
	i /= 0.0f;
	i /= 0.0d;
	l /= 0.0f;
	l /= 0.0d;
    }
} PASS

tcltest::test 15.26.2-div-zero-4 { test floating /= by integer 0
        (no warning needed) } {
    empty_main T15262dz4 {
	float f = 1;
	double d = 1;
	f /= 0;
	f /= 0L;
	d /= 0;
	d /= 0L;
    }
} PASS

tcltest::test 15.26.2-div-zero-5 { test floating /= by floating 0
        (no warning needed) } {
    empty_main T15262dz5 {
	float f = 1;
	double d = 1;
	f /= 0.0f;
	d /= 0.0d;
    }
} PASS

# Test %=
tcltest::test 15.26.2-mod-1 { test legal identity %= assignments } {
    empty_main T15262mod1 {
	byte b = 1;
	b %= 1;
	short s = 1;
	s %= 1;
	char c = 1;
	c %= 1;
	int i = 1;
	i %= 1;
	long l = 1;
	l %= 1;
	float f = 1;
	f %= 1;
	double d = 1;
	d %= 1;
    }
} PASS

tcltest::test 15.26.2-mod-2 { test illegal %= assignments: boolean argument } {
    empty_main T15262mod2 {
	int i = 1;
	i %= false;
    }
} FAIL

tcltest::test 15.26.2-mod-3 { test illegal %= assignments: void argument } {
    empty_class T15262mod3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i %= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-mod-4 { test illegal %= assignments: Object argument } {
    empty_main T15262mod4 {
	int i = 1;
	i %= new Object();
    }
} FAIL

tcltest::test 15.26.2-mod-5 { test illegal %= assignments: String argument } {
    empty_main T15262mod5 {
	int i = 1;
	i %= "";
    }
} FAIL

tcltest::test 15.26.2-mod-6 { test illegal %= assignments: null argument } {
    empty_main T15262mod6 {
	int i = 1;
	i %= null;
    }
} FAIL

tcltest::test 15.26.2-mod-7 { test illegal %= assignments: Object variable } {
    empty_main T15262mod7 {
	Object o = new Object();
	o %= 1;
    }
} FAIL

tcltest::test 15.26.2-mod-8 { test illegal %= assignments: String variable } {
    empty_main T15262mod8 {
	String s = "";
	s %= 1;
    }
} FAIL

tcltest::test 15.26.2-mod-9 { test illegal %= assignments: null variable } {
    empty_main T15262mod9 {
	null %= 1;
    }
} FAIL

tcltest::test 15.26.2-mod-10 { test illegal %= assignments: boolean variable } {
    empty_main T15262mod10 {
	boolean b = true;
	b %= true;
    }
} FAIL

tcltest::test 15.26.2-mod-11 { test illegal %= assignments: non-variable } {
    empty_main T15262mod11 {
	1 %= 1;
    }
} FAIL

tcltest::test 15.26.2-mod-12 { test illegal %= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262mod12 {
	int i = 1;
	(i) %= 1;
    }]
} OK

tcltest::test 15.26.2-mod-13 { test widening %= assignments } {
    empty_main T15262mod13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	s %= b;
	i %= b;
	i %= s;
	i %= c;
	l %= b;
	l %= s;
	l %= c;
	l %= i;
	f %= b;
	f %= s;
	f %= c;
	f %= i;
	f %= l;
	d %= b;
	d %= s;
	d %= c;
	d %= i;
	d %= l;
	d %= f;
    }
} PASS

tcltest::test 15.26.2-mod-14 { test narrowing %= assignments } {
    empty_main T15262mod14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	b %= s;
	b %= c;
	b %= i;
	b %= l;
	b %= f;
	b %= d;
	s %= c;
	s %= i;
	s %= l;
	s %= f;
	s %= d;
	c %= b;
	c %= s;
	c %= i;
	c %= l;
	c %= f;
	c %= d;
	i %= l;
	i %= f;
	i %= d;
	l %= f;
	l %= d;
	f %= d;
    }
} PASS

tcltest::test 15.26.2-mod-zero-1 { test integer %= by integer 0
        (warning recommended) } {
    ok_pass_or_warn [empty_class T15262dz1 {
        int i = 1;
        void foo() {
            i %= 0;
            i %= 0L;
        }
    }]
} OK

tcltest::test 15.26.2-mod-zero-2 { test integer %= by integer 0
        (warning recommended) } {
    ok_pass_or_warn [empty_class T15262dz2 {
        long l = 1;
        void foo() {
            l %= 0;
            l %= 0L;
        }
    }]
} OK

tcltest::test 15.26.2-mod-zero-3 { test integer %= by floating 0
       (no warning needed) } {
    empty_main T15262dz3 {
	int i = 1;
	long l = 1;
	i %= 0.0f;
	i %= 0.0d;
	l %= 0.0f;
	l %= 0.0d;
    }
} PASS

tcltest::test 15.26.2-mod-zero-4 { test floating %= by integer 0
        (no warning needed) } {
    empty_main T15262dz4 {
	float f = 1;
	double d = 1;
	f %= 0;
	f %= 0L;
	d %= 0;
	d %= 0L;
    }
} PASS

tcltest::test 15.26.2-mod-zero-5 { test floating %= by floating 0
        (no warning needed) } {
    empty_main T15262dz5 {
	float f = 1;
	double d = 1;
	f %= 0.0f;
	d %= 0.0d;
    }
} PASS

# Test +=
tcltest::test 15.26.2-add-1 { test legal identity += assignments } {
    empty_main T15262add1 {
	byte b = 1;
	b += 1;
	short s = 1;
	s += 1;
	char c = 1;
	c += 1;
	int i = 1;
	i += 1;
	long l = 1;
	l += 1;
	float f = 1;
	f += 1;
	double d = 1;
	d += 1;
    }
} PASS

tcltest::test 15.26.2-add-2 { test illegal += assignments: boolean argument } {
    empty_main T15262add2 {
	int i = 1;
	i += false;
    }
} FAIL

tcltest::test 15.26.2-add-3 { test illegal += assignments: void argument } {
    empty_class T15262add3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i += foo();
	}
    }
} FAIL

tcltest::test 15.26.2-add-4 { test illegal += assignments: Object argument } {
    empty_main T15262add4 {
	int i = 1;
	i += new Object();
    }
} FAIL

tcltest::test 15.26.2-add-5 { test illegal += assignments: String argument } {
    empty_main T15262add5 {
	int i = 1;
	i += "";
    }
} FAIL

tcltest::test 15.26.2-add-6 { test illegal += assignments: null argument } {
    empty_main T15262add6 {
	int i = 1;
	i += null;
    }
} FAIL

tcltest::test 15.26.2-add-7 { test illegal += assignments: Object variable } {
    empty_main T15262add7 {
	Object o = new Object();
	o += 1;
    }
} FAIL

tcltest::test 15.26.2-add-8 { special case: test legal += assignments:
        String variable } {
    empty_main T15262add8 {
	String s = "";
	s += 1;
    }
} PASS

tcltest::test 15.26.2-add-9 { test illegal += assignments: null variable } {
    empty_main T15262add9 {
	null += 1;
    }
} FAIL

tcltest::test 15.26.2-add-10 { test illegal += assignments: boolean variable } {
    empty_main T15262add10 {
	boolean b = true;
	b += true;
    }
} FAIL

tcltest::test 15.26.2-add-11 { test illegal += assignments: non-variable } {
    empty_main T15262add11 {
	1 += 1;
    }
} FAIL

tcltest::test 15.26.2-add-12 { test illegal += assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262add12 {
	int i = 1;
	(i) += 1;
    }]
} OK

tcltest::test 15.26.2-add-13 { test widening += assignments } {
    empty_main T15262add13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	s += b;
	i += b;
	i += s;
	i += c;
	l += b;
	l += s;
	l += c;
	l += i;
	f += b;
	f += s;
	f += c;
	f += i;
	f += l;
	d += b;
	d += s;
	d += c;
	d += i;
	d += l;
	d += f;
    }
} PASS

tcltest::test 15.26.2-add-14 { test narrowing += assignments } {
    empty_main T15262add14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	b += s;
	b += c;
	b += i;
	b += l;
	b += f;
	b += d;
	s += c;
	s += i;
	s += l;
	s += f;
	s += d;
	c += b;
	c += s;
	c += i;
	c += l;
	c += f;
	c += d;
	i += l;
	i += f;
	i += d;
	l += f;
	l += d;
	f += d;
    }
} PASS

# Test -=
tcltest::test 15.26.2-sub-1 { test legal identity -= assignments } {
    empty_main T15262sub1 {
	byte b = 1;
	b -= 1;
	short s = 1;
	s -= 1;
	char c = 1;
	c -= 1;
	int i = 1;
	i -= 1;
	long l = 1;
	l -= 1;
	float f = 1;
	f -= 1;
	double d = 1;
	d -= 1;
    }
} PASS

tcltest::test 15.26.2-sub-2 { test illegal -= assignments: boolean argument } {
    empty_main T15262sub2 {
	int i = 1;
	i -= false;
    }
} FAIL

tcltest::test 15.26.2-sub-3 { test illegal -= assignments: void argument } {
    empty_class T15262sub3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i -= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-sub-4 { test illegal -= assignments: Object argument } {
    empty_main T15262sub4 {
	int i = 1;
	i -= new Object();
    }
} FAIL

tcltest::test 15.26.2-sub-5 { test illegal -= assignments: String argument } {
    empty_main T15262sub5 {
	int i = 1;
	i -= "";
    }
} FAIL

tcltest::test 15.26.2-sub-6 { test illegal -= assignments: null argument } {
    empty_main T15262sub6 {
	int i = 1;
	i -= null;
    }
} FAIL

tcltest::test 15.26.2-sub-7 { test illegal -= assignments: Object variable } {
    empty_main T15262sub7 {
	Object o = new Object();
	o -= 1;
    }
} FAIL

tcltest::test 15.26.2-sub-8 { test illegal -= assignments: String variable } {
    empty_main T15262sub8 {
	String s = "";
	s -= 1;
    }
} FAIL

tcltest::test 15.26.2-sub-9 { test illegal -= assignments: null variable } {
    empty_main T15262sub9 {
	null -= 1;
    }
} FAIL

tcltest::test 15.26.2-sub-10 { test illegal -= assignments: boolean variable } {
    empty_main T15262sub10 {
	boolean b = true;
	b -= true;
    }
} FAIL

tcltest::test 15.26.2-sub-11 { test illegal -= assignments: non-variable } {
    empty_main T15262sub11 {
	1 -= 1;
    }
} FAIL

tcltest::test 15.26.2-sub-12 { test illegal -= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262sub12 {
	int i = 1;
	(i) -= 1;
    }]
} OK

tcltest::test 15.26.2-sub-13 { test widening -= assignments } {
    empty_main T15262sub13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	s -= b;
	i -= b;
	i -= s;
	i -= c;
	l -= b;
	l -= s;
	l -= c;
	l -= i;
	f -= b;
	f -= s;
	f -= c;
	f -= i;
	f -= l;
	d -= b;
	d -= s;
	d -= c;
	d -= i;
	d -= l;
	d -= f;
    }
} PASS

tcltest::test 15.26.2-sub-14 { test narrowing -= assignments } {
    empty_main T15262sub14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	b -= s;
	b -= c;
	b -= i;
	b -= l;
	b -= f;
	b -= d;
	s -= c;
	s -= i;
	s -= l;
	s -= f;
	s -= d;
	c -= b;
	c -= s;
	c -= i;
	c -= l;
	c -= f;
	c -= d;
	i -= l;
	i -= f;
	i -= d;
	l -= f;
	l -= d;
	f -= d;
    }
} PASS

# Test <<=
tcltest::test 15.26.2-left-shift-1 { test legal identity <<= assignments } {
    empty_main T15262ls1 {
	byte b = 1;
	b <<= 1;
	short s = 1;
	s <<= 1;
	char c = 1;
	c <<= 1;
	int i = 1;
	i <<= 1;
	long l = 1;
	l <<= 1;
    }
} PASS

tcltest::test 15.26.2-left-shift-2 { test illegal <<= assignments: boolean argument } {
    empty_main T15262ls2 {
	int i = 1;
	i <<= false;
    }
} FAIL

tcltest::test 15.26.2-left-shift-3 { test illegal <<= assignments: void argument } {
    empty_class T15262ls3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i <<= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-left-shift-4 { test illegal <<= assignments: Object argument } {
    empty_main T15262ls4 {
	int i = 1;
	i <<= new Object();
    }
} FAIL

tcltest::test 15.26.2-left-shift-5 { test illegal <<= assignments: String argument } {
    empty_main T15262ls5 {
	int i = 1;
	i <<= "";
    }
} FAIL

tcltest::test 15.26.2-left-shift-6 { test illegal <<= assignments: null argument } {
    empty_main T15262ls6 {
	int i = 1;
	i <<= null;
    }
} FAIL

tcltest::test 15.26.2-left-shift-7 { test illegal <<= assignments: Object variable } {
    empty_main T15262ls7 {
	Object o = new Object();
	o <<= 1;
    }
} FAIL

tcltest::test 15.26.2-left-shift-8 { test illegal <<= assignments: String variable } {
    empty_main T15262ls8 {
	String s = "";
	s <<= 1;
    }
} FAIL

tcltest::test 15.26.2-left-shift-9 { test illegal <<= assignments: null variable } {
    empty_main T15262ls9 {
	null <<= 1;
    }
} FAIL

tcltest::test 15.26.2-left-shift-10 { test illegal <<= assignments: boolean variable } {
    empty_main T15262ls10 {
	boolean b = true;
	b <<= true;
    }
} FAIL

tcltest::test 15.26.2-left-shift-11 { test illegal <<= assignments: non-variable } {
    empty_main T15262ls11 {
	1 <<= 1;
    }
} FAIL

tcltest::test 15.26.2-left-shift-12 { test illegal <<= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262ls12 {
	int i = 1;
	(i) <<= 1;
    }]
} OK

tcltest::test 15.26.2-left-shift-13 { test widening <<= assignments } {
    empty_main T15262ls13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s <<= b;
	i <<= b;
	i <<= s;
	i <<= c;
	l <<= b;
	l <<= s;
	l <<= c;
	l <<= i;
    }
} PASS

tcltest::test 15.26.2-left-shift-14 { test narrowing <<= assignments } {
    empty_main T15262ls14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b <<= s;
	b <<= c;
	b <<= i;
	b <<= l;
	s <<= c;
	s <<= i;
	s <<= l;
	c <<= b;
	c <<= s;
	c <<= i;
	c <<= l;
	i <<= l;
    }
} PASS

tcltest::test 15.26.2-left-shift-15 { test illegal <<= assignments: float argument } {
    empty_main T15262ls15 {
	int i = 1;
	i <<= 1.0f;
    }
} FAIL

tcltest::test 15.26.2-left-shift-16 { test illegal <<= assignments: double argument } {
    empty_main T15262ls16 {
	int i = 1;
	i <<= 1.0d;
    }
} FAIL

tcltest::test 15.26.2-left-shift-17 { test illegal <<= assignments: float variable } {
    empty_main T15262ls17 {
	float f = 1;
	f <<= 1;
    }
} FAIL

tcltest::test 15.26.2-left-shift-18 { test illegal <<= assignments: double variable } {
    empty_main T15262ls18 {
	double d = 1;
	d <<= 1;
    }
} FAIL

# Test >>=
tcltest::test 15.26.2-signed-right-shift-1 { test legal identity >>= assignments } {
    empty_main T15262srs1 {
	byte b = 1;
	b >>= 1;
	short s = 1;
	s >>= 1;
	char c = 1;
	c >>= 1;
	int i = 1;
	i >>= 1;
	long l = 1;
	l >>= 1;
    }
} PASS

tcltest::test 15.26.2-signed-right-shift-2 { test illegal >>= assignments: boolean argument } {
    empty_main T15262srs2 {
	int i = 1;
	i >>= false;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-3 { test illegal >>= assignments: void argument } {
    empty_class T15262srs3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i >>= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-4 { test illegal >>= assignments: Object argument } {
    empty_main T15262srs4 {
	int i = 1;
	i >>= new Object();
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-5 { test illegal >>= assignments: String argument } {
    empty_main T15262srs5 {
	int i = 1;
	i >>= "";
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-6 { test illegal >>= assignments: null argument } {
    empty_main T15262srs6 {
	int i = 1;
	i >>= null;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-7 { test illegal >>= assignments: Object variable } {
    empty_main T15262srs7 {
	Object o = new Object();
	o >>= 1;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-8 { test illegal >>= assignments: String variable } {
    empty_main T15262srs8 {
	String s = "";
	s >>= 1;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-9 { test illegal >>= assignments: null variable } {
    empty_main T15262srs9 {
	null >>= 1;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-10 { test illegal >>= assignments: boolean variable } {
    empty_main T15262srs10 {
	boolean b = true;
	b >>= true;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-11 { test illegal >>= assignments: non-variable } {
    empty_main T15262srs11 {
	1 >>= 1;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-12 { test illegal >>= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262srs12 {
	int i = 1;
	(i) >>= 1;
    }]
} OK

tcltest::test 15.26.2-signed-right-shift-13 { test widening >>= assignments } {
    empty_main T15262srs13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s >>= b;
	i >>= b;
	i >>= s;
	i >>= c;
	l >>= b;
	l >>= s;
	l >>= c;
	l >>= i;
    }
} PASS

tcltest::test 15.26.2-signed-right-shift-14 { test narrowing >>= assignments } {
    empty_main T15262srs14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b >>= s;
	b >>= c;
	b >>= i;
	b >>= l;
	s >>= c;
	s >>= i;
	s >>= l;
	c >>= b;
	c >>= s;
	c >>= i;
	c >>= l;
	i >>= l;
    }
} PASS

tcltest::test 15.26.2-signed-right-shift-15 { test illegal >>= assignments: float argument } {
    empty_main T15262srs15 {
	int i = 1;
	i >>= 1.0f;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-16 { test illegal >>= assignments: double argument } {
    empty_main T15262srs16 {
	int i = 1;
	i >>= 1.0d;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-17 { test illegal >>= assignments: float variable } {
    empty_main T15262srs17 {
	float f = 1;
	f >>= 1;
    }
} FAIL

tcltest::test 15.26.2-signed-right-shift-18 { test illegal >>= assignments: double variable } {
    empty_main T15262srs18 {
	double d = 1;
	d >>= 1;
    }
} FAIL

# Test >>>=
tcltest::test 15.26.2-unsigned-right-shift-1 { test legal identity >>>= assignments } {
    empty_main T15262urs1 {
	byte b = 1;
	b >>>= 1;
	short s = 1;
	s >>>= 1;
	char c = 1;
	c >>>= 1;
	int i = 1;
	i >>>= 1;
	long l = 1;
	l >>>= 1;
    }
} PASS

tcltest::test 15.26.2-unsigned-right-shift-2 { test illegal >>>= assignments: boolean argument } {
    empty_main T15262urs2 {
	int i = 1;
	i >>>= false;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-3 { test illegal >>>= assignments: void argument } {
    empty_class T15262urs3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i >>>= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-4 { test illegal >>>= assignments: Object argument } {
    empty_main T15262urs4 {
	int i = 1;
	i >>>= new Object();
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-5 { test illegal >>>= assignments: String argument } {
    empty_main T15262urs5 {
	int i = 1;
	i >>>= "";
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-6 { test illegal >>>= assignments: null argument } {
    empty_main T15262urs6 {
	int i = 1;
	i >>>= null;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-7 { test illegal >>>= assignments: Object variable } {
    empty_main T15262urs7 {
	Object o = new Object();
	o >>>= 1;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-8 { test illegal >>>= assignments: String variable } {
    empty_main T15262urs8 {
	String s = "";
	s >>>= 1;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-9 { test illegal >>>= assignments: null variable } {
    empty_main T15262urs9 {
	null >>>= 1;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-10 { test illegal >>>= assignments: boolean variable } {
    empty_main T15262urs10 {
	boolean b = true;
	b >>>= true;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-11 { test illegal >>>= assignments: non-variable } {
    empty_main T15262urs11 {
	1 >>>= 1;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-12 { test illegal >>>= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262urs12 {
	int i = 1;
	(i) >>>= 1;
    }]
} OK

tcltest::test 15.26.2-unsigned-right-shift-13 { test widening >>>= assignments } {
    empty_main T15262urs13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s >>>= b;
	i >>>= b;
	i >>>= s;
	i >>>= c;
	l >>>= b;
	l >>>= s;
	l >>>= c;
	l >>>= i;
    }
} PASS

tcltest::test 15.26.2-unsigned-right-shift-14 { test narrowing >>>= assignments } {
    empty_main T15262urs14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b >>>= s;
	b >>>= c;
	b >>>= i;
	b >>>= l;
	s >>>= c;
	s >>>= i;
	s >>>= l;
	c >>>= b;
	c >>>= s;
	c >>>= i;
	c >>>= l;
	i >>>= l;
    }
} PASS

tcltest::test 15.26.2-unsigned-right-shift-15 { test illegal >>>= assignments: float argument } {
    empty_main T15262urs15 {
	int i = 1;
	i >>>= 1.0f;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-16 { test illegal >>>= assignments: double argument } {
    empty_main T15262urs16 {
	int i = 1;
	i >>>= 1.0d;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-17 { test illegal >>>= assignments: float variable } {
    empty_main T15262urs17 {
	float f = 1;
	f >>>= 1;
    }
} FAIL

tcltest::test 15.26.2-unsigned-right-shift-18 { test illegal >>>= assignments: double variable } {
    empty_main T15262urs18 {
	double d = 1;
	d >>>= 1;
    }
} FAIL

# Test &=
tcltest::test 15.26.2-and-1 { test legal identity &= assignments } {
    empty_main T15262and1 {
	boolean z = true;
	z &= true;
	byte b = 1;
	b &= 1;
	short s = 1;
	s &= 1;
	char c = 1;
	c &= 1;
	int i = 1;
	i &= 1;
	long l = 1;
	l &= 1;
    }
} PASS

tcltest::test 15.26.2-and-2 { test illegal &= assignments: mix int and boolean } {
    empty_main T15262and2 {
	int i = 1;
	i &= false;
    }
} FAIL

tcltest::test 15.26.2-and-3 { test illegal &= assignments: void argument } {
    empty_class T15262and3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i &= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-and-4 { test illegal &= assignments: Object argument } {
    empty_main T15262and4 {
	int i = 1;
	i &= new Object();
    }
} FAIL

tcltest::test 15.26.2-and-5 { test illegal &= assignments: String argument } {
    empty_main T15262and5 {
	int i = 1;
	i &= "";
    }
} FAIL

tcltest::test 15.26.2-and-6 { test illegal &= assignments: null argument } {
    empty_main T15262and6 {
	int i = 1;
	i &= null;
    }
} FAIL

tcltest::test 15.26.2-and-7 { test illegal &= assignments: Object variable } {
    empty_main T15262and7 {
	Object o = new Object();
	o &= 1;
    }
} FAIL

tcltest::test 15.26.2-and-8 { test illegal &= assignments: String variable } {
    empty_main T15262and8 {
	String s = "";
	s &= 1;
    }
} FAIL

tcltest::test 15.26.2-and-9 { test illegal &= assignments: null variable } {
    empty_main T15262and9 {
	null &= 1;
    }
} FAIL

tcltest::test 15.26.2-and-10 { test illegal &= assignments: mix int and boolean } {
    empty_main T15262and10 {
	boolean b = true;
	b &= 1;
    }
} FAIL

tcltest::test 15.26.2-and-11 { test illegal &= assignments: non-variable } {
    empty_main T15262and11 {
	1 &= 1;
    }
} FAIL

tcltest::test 15.26.2-and-12 { test illegal &= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262and12 {
	int i = 1;
	(i) &= 1;
    }]
} OK

tcltest::test 15.26.2-and-13 { test widening &= assignments } {
    empty_main T15262and13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s &= b;
	i &= b;
	i &= s;
	i &= c;
	l &= b;
	l &= s;
	l &= c;
	l &= i;
    }
} PASS

tcltest::test 15.26.2-and-14 { test narrowing &= assignments } {
    empty_main T15262and14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b &= s;
	b &= c;
	b &= i;
	b &= l;
	s &= c;
	s &= i;
	s &= l;
	c &= b;
	c &= s;
	c &= i;
	c &= l;
	i &= l;
    }
} PASS

tcltest::test 15.26.2-and-15 { test illegal &= assignments: float argument } {
    empty_main T15262and15 {
	int i = 1;
	i &= 1.0f;
    }
} FAIL

tcltest::test 15.26.2-and-16 { test illegal &= assignments: double argument } {
    empty_main T15262and16 {
	int i = 1;
	i &= 1.0d;
    }
} FAIL

tcltest::test 15.26.2-and-17 { test illegal &= assignments: float variable } {
    empty_main T15262and17 {
	float f = 1;
	f &= 1;
    }
} FAIL

tcltest::test 15.26.2-and-18 { test illegal &= assignments: double variable } {
    empty_main T15262and18 {
	double d = 1;
	d &= 1;
    }
} FAIL

# Test ^=
tcltest::test 15.26.2-xor-1 { test legal identity ^= assignments } {
    empty_main T15262xor1 {
	boolean z = true;
	z ^= true;
	byte b = 1;
	b ^= 1;
	short s = 1;
	s ^= 1;
	char c = 1;
	c ^= 1;
	int i = 1;
	i ^= 1;
	long l = 1;
	l ^= 1;
    }
} PASS

tcltest::test 15.26.2-xor-2 { test illegal ^= assignments: mix int and boolean } {
    empty_main T15262xor2 {
	int i = 1;
	i ^= false;
    }
} FAIL

tcltest::test 15.26.2-xor-3 { test illegal ^= assignments: void argument } {
    empty_class T15262xor3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i ^= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-xor-4 { test illegal ^= assignments: Object argument } {
    empty_main T15262xor4 {
	int i = 1;
	i ^= new Object();
    }
} FAIL

tcltest::test 15.26.2-xor-5 { test illegal ^= assignments: String argument } {
    empty_main T15262xor5 {
	int i = 1;
	i ^= "";
    }
} FAIL

tcltest::test 15.26.2-xor-6 { test illegal ^= assignments: null argument } {
    empty_main T15262xor6 {
	int i = 1;
	i ^= null;
    }
} FAIL

tcltest::test 15.26.2-xor-7 { test illegal ^= assignments: Object variable } {
    empty_main T15262xor7 {
	Object o = new Object();
	o ^= 1;
    }
} FAIL

tcltest::test 15.26.2-xor-8 { test illegal ^= assignments: String variable } {
    empty_main T15262xor8 {
	String s = "";
	s ^= 1;
    }
} FAIL

tcltest::test 15.26.2-xor-9 { test illegal ^= assignments: null variable } {
    empty_main T15262xor9 {
	null ^= 1;
    }
} FAIL

tcltest::test 15.26.2-xor-10 { test illegal ^= assignments: mix int and boolean } {
    empty_main T15262xor10 {
	boolean b = true;
	b ^= 1;
    }
} FAIL

tcltest::test 15.26.2-xor-11 { test illegal ^= assignments: non-variable } {
    empty_main T15262xor11 {
	1 ^= 1;
    }
} FAIL

tcltest::test 15.26.2-xor-12 { test illegal ^= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262xor12 {
	int i = 1;
	(i) ^= 1;
    }]
} OK

tcltest::test 15.26.2-xor-13 { test widening ^= assignments } {
    empty_main T15262xor13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s ^= b;
	i ^= b;
	i ^= s;
	i ^= c;
	l ^= b;
	l ^= s;
	l ^= c;
	l ^= i;
    }
} PASS

tcltest::test 15.26.2-xor-14 { test narrowing ^= assignments } {
    empty_main T15262xor14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b ^= s;
	b ^= c;
	b ^= i;
	b ^= l;
	s ^= c;
	s ^= i;
	s ^= l;
	c ^= b;
	c ^= s;
	c ^= i;
	c ^= l;
	i ^= l;
    }
} PASS

tcltest::test 15.26.2-xor-15 { test illegal ^= assignments: float argument } {
    empty_main T15262xor15 {
	int i = 1;
	i ^= 1.0f;
    }
} FAIL

tcltest::test 15.26.2-xor-16 { test illegal ^= assignments: double argument } {
    empty_main T15262xor16 {
	int i = 1;
	i ^= 1.0d;
    }
} FAIL

tcltest::test 15.26.2-xor-17 { test illegal ^= assignments: float variable } {
    empty_main T15262xor17 {
	float f = 1;
	f ^= 1;
    }
} FAIL

tcltest::test 15.26.2-xor-18 { test illegal ^= assignments: double variable } {
    empty_main T15262xor18 {
	double d = 1;
	d ^= 1;
    }
} FAIL

# Test |=
tcltest::test 15.26.2-or-1 { test legal identity |= assignments } {
    empty_main T15262or1 {
	boolean z = true;
	z |= true;
	byte b = 1;
	b |= 1;
	short s = 1;
	s |= 1;
	char c = 1;
	c |= 1;
	int i = 1;
	i |= 1;
	long l = 1;
	l |= 1;
    }
} PASS

tcltest::test 15.26.2-or-2 { test illegal |= assignments: mix int and boolean } {
    empty_main T15262or2 {
	int i = 1;
	i |= false;
    }
} FAIL

tcltest::test 15.26.2-or-3 { test illegal |= assignments: void argument } {
    empty_class T15262or3 {
	void foo() {}
	void bar() {
	    int i = 1;
	    i |= foo();
	}
    }
} FAIL

tcltest::test 15.26.2-or-4 { test illegal |= assignments: Object argument } {
    empty_main T15262or4 {
	int i = 1;
	i |= new Object();
    }
} FAIL

tcltest::test 15.26.2-or-5 { test illegal |= assignments: String argument } {
    empty_main T15262or5 {
	int i = 1;
	i |= "";
    }
} FAIL

tcltest::test 15.26.2-or-6 { test illegal |= assignments: null argument } {
    empty_main T15262or6 {
	int i = 1;
	i |= null;
    }
} FAIL

tcltest::test 15.26.2-or-7 { test illegal |= assignments: Object variable } {
    empty_main T15262or7 {
	Object o = new Object();
	o |= 1;
    }
} FAIL

tcltest::test 15.26.2-or-8 { test illegal |= assignments: String variable } {
    empty_main T15262or8 {
	String s = "";
	s |= 1;
    }
} FAIL

tcltest::test 15.26.2-or-9 { test illegal |= assignments: null variable } {
    empty_main T15262or9 {
	null |= 1;
    }
} FAIL

tcltest::test 15.26.2-or-10 { test illegal |= assignments: mix int and boolean } {
    empty_main T15262or10 {
	boolean b = true;
	b |= 1;
    }
} FAIL

tcltest::test 15.26.2-or-11 { test illegal |= assignments: non-variable } {
    empty_main T15262or11 {
	1 |= 1;
    }
} FAIL

tcltest::test 15.26.2-or-12 { test illegal |= assignments: parenthesized
        expressions can now be variables } {
    ok_pass_or_warn [empty_main T15262or12 {
	int i = 1;
	(i) |= 1;
    }]
} OK

tcltest::test 15.26.2-or-13 { test widening |= assignments } {
    empty_main T15262or13 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s |= b;
	i |= b;
	i |= s;
	i |= c;
	l |= b;
	l |= s;
	l |= c;
	l |= i;
    }
} PASS

tcltest::test 15.26.2-or-14 { test narrowing |= assignments } {
    empty_main T15262or14 {
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b |= s;
	b |= c;
	b |= i;
	b |= l;
	s |= c;
	s |= i;
	s |= l;
	c |= b;
	c |= s;
	c |= i;
	c |= l;
	i |= l;
    }
} PASS

tcltest::test 15.26.2-or-15 { test illegal |= assignments: float argument } {
    empty_main T15262or15 {
	int i = 1;
	i |= 1.0f;
    }
} FAIL

tcltest::test 15.26.2-or-16 { test illegal |= assignments: double argument } {
    empty_main T15262or16 {
	int i = 1;
	i |= 1.0d;
    }
} FAIL

tcltest::test 15.26.2-or-17 { test illegal |= assignments: float variable } {
    empty_main T15262or17 {
	float f = 1;
	f |= 1;
    }
} FAIL

tcltest::test 15.26.2-or-18 { test illegal |= assignments: double variable } {
    empty_main T15262or18 {
	double d = 1;
	d |= 1;
    }
} FAIL

tcltest::test 15.26.2-variable-1 { the left-hand side must be a variable } {
    empty_main T15262var1 {
        int.class += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-2 { the left-hand side must be a variable } {
    empty_main T15262var2 {
        (int.class) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-3 { the left-hand side must be a variable } {
    empty_main T15262var3 {
        this += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-4 { the left-hand side must be a variable } {
    empty_main T15262var4 {
        (this) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-5 { the left-hand side must be a variable } {
    empty_main T15262var5 {
        new String() += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-6 { the left-hand side must be a variable } {
    empty_main T15262var6 {
        (new String()) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-7 { the left-hand side must be a variable } {
    empty_main T15262var7 {
        java.lang.String += "oops";
    }
} FAIL


tcltest::test 15.26.2-variable-8 { the left-hand side must be a variable } {
    empty_main T15262var8 {
        (java.lang.String) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-9 { the left-hand side must be a variable } {
    empty_class T15262var9 {
        void bar() {}
        void foo() {
            bar() += "oops";
        }
    }
} FAIL

tcltest::test 15.26.2-variable-10 { the left-hand side must be a variable } {
    empty_class T15262var10 {
        void bar() {}
        void foo() {
            (bar()) += "oops";
        }
    }
} FAIL

tcltest::test 15.26.2-variable-11 { the left-hand side must be a variable } {
    empty_class T15262var11 {
        String bar() { return ""; }
        void foo() {
            bar() += "oops";
        }
    }
} FAIL

tcltest::test 15.26.2-variable-12 { the left-hand side must be a variable } {
    empty_class T15262var12 {
        String bar() { return ""; }
        void foo() {
            (bar()) += "oops";
        }
    }
} FAIL

tcltest::test 15.26.2-variable-13 { the left-hand side must be a variable } {
    empty_main T15262var13 {
        int i = 1;
        i++ += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-14 { the left-hand side must be a variable } {
    empty_main T15262var14 {
        int i = 1;
        (i++) += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-15 { the left-hand side must be a variable } {
    empty_main T15262var15 {
        int i = 1;
        --i += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-16 { the left-hand side must be a variable } {
    empty_main T15262var16 {
        intn i = 1;
        (--i) += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-17 { the left-hand side must be a variable } {
    empty_main T15262var17 {
        "" instanceof String += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-18 { the left-hand side must be a variable } {
    empty_main T15262var18 {
        ("" instanceof String) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-19 { the left-hand side must be a variable } {
    empty_main T15262var19 {
        int i = 1;
        i + i += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-20 { the left-hand side must be a variable } {
    empty_main T15262var20 {
        int i = 1;
        (i + i) += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-21 { the left-hand side must be a variable } {
    empty_main T15262var21 {
        int i = 1;
        (i += 1) += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-22 { the left-hand side must be a variable } {
    empty_main T15262var22 {
        1 += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-23 { the left-hand side must be a variable } {
    empty_main T15262var23 {
        (1) += 1;
    }
} FAIL

tcltest::test 15.26.2-variable-24 { the left-hand side must be a variable } {
    empty_main T15262var24 {
        null += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-25 { the left-hand side must be a variable } {
    empty_main T15262var25 {
        (null) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-26 { the left-hand side must be a variable } {
    empty_main T15262var26 {
        true += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-27 { the left-hand side must be a variable } {
    empty_main T15262var27 {
        (true) += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-28 { the left-hand side must be a variable } {
    empty_main T15262var28 {
        "" += "oops";
    }
} FAIL

tcltest::test 15.26.2-variable-29 { the left-hand side must be a variable } {
    empty_main T15262var29 {
        ("") += "oops";
    }
} FAIL

tcltest::test 15.26.2-associative-1 { assignment is right-associative } {
    empty_main T15262assoc1 {
        int i = 1;
        i += i += 1;
        i += (i += 1);
    }
} PASS
