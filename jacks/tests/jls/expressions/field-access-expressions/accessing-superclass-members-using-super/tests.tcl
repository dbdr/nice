tcltest::test 15.11.2-syntax-1 { The type in the qualified super is not
        obscured } {
    compile [saveas T15112s1a.java {
class T15112s1a {
    int i;
}
class T15112s1b extends T15112s1a {
    int T15112s1b; // obscure the class name from normal expressions
    int j = T15112s1b.super.i;
}
}]
} PASS

tcltest::test 15.11.2-syntax-2 { The type in qualified super may not be a
        primitive type } {
    compile [saveas T15112s2a.java {
class T15112s2a {
    int i;
}
class T15112s2b extends T15112s2a {
    Object o = int.super.i;
}
}]
} FAIL

tcltest::test 15.11.2-syntax-3 { The type in qualified super may not be an
        array type } {
    compile [saveas T15112s3a.java {
class T15112s3a {
    int i;
}
class T15112s3b extends T15112s3a {
    Object o = int[].super.i;
}
}]
} FAIL

tcltest::test 15.11.2-syntax-4 { The type in qualified super may not be an
        array type } {
    compile [saveas T15112s4a.java {
class T15112s4a {
    int i;
}
class T15112s4b extends T15112s4a {
    Object o = T15112s4a[].super.i;
}
}]
} FAIL

tcltest::test 15.11.2-syntax-5 { The type in qualified super may not be
        null } {
    compile [saveas T15112s5a.java {
class T15112s5a {
    int i;
}
class T15112s5b extends T15112s5a {
    Object o = null.super.i;
}
}]
} FAIL

tcltest::test 15.11.2-syntax-6 { The type in qualified super may not be
        void } {
    compile [saveas T15112s6a.java {
class T15112s6a {
    int i;
}
class T15112s6b extends T15112s6a {
    Object o = void.super.i;
}
}]
} FAIL

tcltest::test 15.11.2-explicit-constructor-1 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T15112ec1 {
	int i;
	T15112ec1(int i) {}
	class Sub extends T15112ec1 {
	    Sub() {
		// calling superclass version of inherited i is wrong
		super(super.i);
	    }
	}
    }
} FAIL

tcltest::test 15.11.2-explicit-constructor-2 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T15112ec2 {
	class One {
	    int i;
	}
	class Two extends One {
	    class Inner {
		Inner(int i) {}
		Inner() {
		    // calling super field of enclosing class is legal
		    this(Two.super.i);
		}
	    }
	}
    }
} PASS

#TODO: Add tests for remainder of rules.
