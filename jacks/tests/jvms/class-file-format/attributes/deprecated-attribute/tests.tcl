# Some of the rules about the @deprecated tag can be found at
# http://java.sun.com/j2se/1.4/docs/tooldocs/win32/javadoc.html#comments
# These tests were verified using javadoc, as javac 1.4.1 goofs severely.

# test class deprecation
tcltest::test 4.7.10-jvms-class-1 { Test that a class can be deprecated } {
    compile [saveas T4710jc1a.java {
// most compact form, need not start line
import java.lang.*;/**@deprecated*/ class T4710jc1a {}
    }]
    delete T4710jc1a.java
    list [compile -classpath . -deprecation [saveas T4710jc1b.java {
class T4710jc1b extends T4710jc1a {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-2 { Test that a class can be deprecated } {
    compile [saveas T4710jc2a.java {
// one-liner form
/** @deprecated */
class T4710jc2a {}
    }]
    delete T4710jc2a.java
    list [compile -classpath . -deprecation [saveas T4710jc2b.java {
class T4710jc2b extends T4710jc2a {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-3 { Test that a class can be deprecated } {
    compile [saveas T4710jc3a.java {
/**
 * all leading whitespace and * are ignored
 	* ** 	 @deprecated
 **/
class T4710jc3a {}
    }]
    delete T4710jc3a.java
    list [compile -classpath . -deprecation [saveas T4710jc3b.java {
class T4710jc3b extends T4710jc3a {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-4 { Test that a class can be deprecated } {
    compile [saveas T4710jc4a.java {
/**
 leading * is optional
 @deprecated
 */
class T4710jc4a {}
    }]
    delete T4710jc4a.java
    list [compile -classpath . -deprecation [saveas T4710jc4b.java {
class T4710jc4b extends T4710jc4a {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-5 { Test that a class can be deprecated } {
    compile [saveas T4710jc5a.java {
/**
 @deprecated
 */ /* */ // additional comments don't matter
class T4710jc5a {}
    }]
    delete T4710jc5a.java
    list [compile -classpath . -deprecation [saveas T4710jc5b.java {
class T4710jc5b extends T4710jc5a {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-6 { Test that a class can be deprecated } {
    compile [saveas T4710jc6a.java {
/**
 @deprecated
 */
; // oops, must be directly before class
class T4710jc6a {}
    }]
    delete T4710jc6a.java
    list [compile -classpath . -deprecation [saveas T4710jc6b.java {
class T4710jc6b extends T4710jc6a {}
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-class-7 { Test that a class can be deprecated } {
    compile [saveas T4710jc7a.java {
/**
 @deprecated
 */
import java.lang.*; // oops, must be directly before class
class T4710jc7a {}
    }]
    delete T4710jc7a.java
    list [compile -classpath . -deprecation [saveas T4710jc7b.java {
class T4710jc7b extends T4710jc7a {}
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-class-8 { Test that a class can be deprecated } {
    compile [saveas T4710jc8a.java {
public
/** oops - too late
 * @deprecated */
       class T4710jc8a {}
    }]
    delete T4710jc8a.java
    list [compile -classpath . -deprecation [saveas T4710jc8b.java {
class T4710jc8b extends T4710jc8a {}
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-class-9 { Test that a class can be deprecated } {
    compile [saveas T4710jc9a.java {
/** characters besides whitespace or * before @ deactivate tag @deprecated */
class T4710jc9a {}
    }]
    delete T4710jc9a.java
    list [compile -classpath . -deprecation [saveas T4710jc9b.java {
class T4710jc9b extends T4710jc9a {}
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-class-10 { Test that a class can be deprecated } {
    compile [saveas T4710jc10a.java {
/** @deprecated */
class T4710jc10a {}
    }]
    delete T4710jc10a.java
    list [compile -classpath . -deprecation [saveas T4710jc10b.java {
class T4710jc10b {
    Object o = new T4710jc10a();
}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-11 { Test that a class can be deprecated } {
    compile [saveas T4710jc11a.java {
/** @deprecated */
class T4710jc11a {
    static int i;
}
    }]
    delete T4710jc11a.java
    list [compile -classpath . -deprecation [saveas T4710jc11b.java {
class T4710jc11b {
    int i = T4710jc11a.i;
}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-12 { Test that a class can be deprecated } {
    compile [saveas T4710jc12a.java {
/** @deprecated */
class T4710jc12a {}
    }]
    delete T4710jc12a.java
    list [compile -classpath . -deprecation [saveas T4710jc12b.java {
class T4710jc12b {
    Object o;
    boolean b = o instanceof T4710jc12a;
}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-13 { Test that a class can be deprecated } {
    compile [saveas T4710jc13a.java {
/** @deprecated */
class T4710jc13a {}
    }]
    delete T4710jc13a.java
    list [compile -classpath . -deprecation [saveas T4710jc13b.java {
class T4710jc13b {
    T4710jc13a t;
}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-14 { Test that a class can be deprecated } {
    compile [saveas T4710jc14a.java {
/** @deprecated */
class T4710jc14a {}
    }]
    delete T4710jc14a.java
    list [compile -classpath . -deprecation [saveas T4710jc14b.java {
class T4710jc14b {
    T4710jc14a[] t;
}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-15 { Test that a class can be deprecated } {
    compile [saveas T4710jc15a.java {
/** @deprecated */
/** Only the most recent doc comment applies */
class T4710jc15a {}
    }]
    delete T4710jc15a.java
    list [compile -classpath . -deprecation [saveas T4710jc15b.java {
class T4710jc15b extends T4710jc15a {}
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-class-16 { Test that a class can be deprecated } {
    compile [saveas T4710jc16a.java {
/** @deprecated */
/**/ // degenerate doc comment; only most recent one applies
class T4710jc16a {}
    }]
    delete T4710jc16a.java
    list [compile -classpath . -deprecation [saveas T4710jc16b.java {
class T4710jc16b extends T4710jc16a {}
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-class-17 { Test that a class can be deprecated } {
    compile [saveas p1/T4710jc17a.java {
package p1;
/** @deprecated */
public class T4710jc17a {}
    }]
    delete p1/T4710jc17a.java
    list [compile -classpath . -deprecation [saveas T4710jc17b.java {
import p1.T4710jc17a;
class T4710jc17b {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-18 { Test that a class can be deprecated } {
    compile [saveas p1/T4710jc18a.java {
package p1;
/** @deprecated */
public class T4710jc18a {
    public static class Inner {}
}
    }]
    delete p1/T4710jc18a.java
    list [compile -classpath . -deprecation [saveas T4710jc18b.java {
import p1.T4710jc18a.*;
class T4710jc18b extends Inner {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-class-19 { Test that a class can be deprecated } {
    compile [saveas T4710jc19a.java {
public class T4710jc19a {
    /** @deprecated */
    public static class Inner {}
}
    }]
    delete T4710jc19a.java
    list [compile -classpath . -deprecation [saveas T4710jc19b.java {
class T4710jc19b extends T4710jc19a.Inner {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

# test method deprecation
tcltest::test 4.7.10-jvms-method-1 { Test that a method can be deprecated } {
    empty_class T4710jm1a {
	// most compact form, need not start line
	int i;/**@deprecated*/ static int m() { return 1; }
    }
    delete T4710jm1a.java
    list [compile -classpath . -deprecation [saveas T4710jm1b.java {
class T4710jm1b { int i = T4710jm1a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-method-2 { Test that a method can be deprecated } {
    empty_class T4710jm2a {
	// one-liner form
	/** @deprecated */
	static int m() { return 1; }
    }
    delete T4710jm2a.java
    list [compile -classpath . -deprecation [saveas T4710jm2b.java {
class T4710jm2b { int i = T4710jm2a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-method-3 { Test that a method can be deprecated } {
    empty_class T4710jm3a {
	/**
          * all leading whitespace and * are ignored
	 	* ** 	 @deprecated
	 **/
	static int m() { return 1; }
    }
    delete T4710jm3a.java
    list [compile -classpath . -deprecation [saveas T4710jm3b.java {
class T4710jm3b { int i = T4710jm3a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-method-4 { Test that a method can be deprecated } {
    empty_class T4710jm4a {
	/**
	  leading * is optional
	  @deprecated
	 */
	static int m() { return 1; }
    }
    delete T4710jm4a.java
    list [compile -classpath . -deprecation [saveas T4710jm4b.java {
class T4710jm4b { int i = T4710jm4a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-method-5 { Test that a method can be deprecated } {
    empty_class T4710jm5a {
	/**
	  @deprecated
	 */ /* */ // additional comments don't matter
	static int m() { return 1; }
    }
    delete T4710jm5a.java
    list [compile -classpath . -deprecation [saveas T4710jm5b.java {
class T4710jm5b { int i = T4710jm5a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-method-6 { Test that a method can be deprecated } {
    empty_class T4710jm6a {
	/**
	  @deprecated
	 */
	; // oops, must be directly before method
	static int m() { return 1; }
    }
    delete T4710jm6a.java
    list [compile -classpath . -deprecation [saveas T4710jm6b.java {
class T4710jm6b { int i = T4710jm6a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-method-7 { Test that a method can be deprecated } {
    empty_class T4710jm7a {
	/**
	  @deprecated
	 */
	int j; // oops, must be directly before method
	static int m() { return 1; }
    }
    delete T4710jm7a.java
    list [compile -classpath . -deprecation [saveas T4710jm7b.java {
class T4710jm7b { int i = T4710jm7a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-method-8 { Test that a method can be deprecated } {
    empty_class T4710jm8a {
	public
	  /** oops - too late
	   * @deprecated */
               static int m() { return 1; }
    }
    delete T4710jm8a.java
    list [compile -classpath . -deprecation [saveas T4710jm8b.java {
class T4710jm8b { int i = T4710jm8a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-method-9 { Test that a method can be deprecated } {
    empty_class T4710jm9a {
/** characters besides whitespace or * before @ deactivate tag @deprecated */
        static int m() { return 1; }
    }
    delete T4710jm9a.java
    list [compile -classpath . -deprecation [saveas T4710jm9b.java {
class T4710jm9b { int i = T4710jm9a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-method-10 { Test that a method can be deprecated } {
    empty_class T4710jm10a {
	/** @deprecated */
        static int m() { return 1; }
    }
    delete T4710jm10a.java
    list [compile -classpath . -deprecation [saveas T4710jm10b.java {
class T4710jm10b extends T4710jm10a { int j = m(); }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-method-11 { Test that a method can be deprecated } {
    empty_class T4710jm11a {
	/** @deprecated */
	/** Only the most recent doc comment applies */
	static int m() { return 1; }
    }
    delete T4710jm11a.java
    list [compile -classpath . -deprecation [saveas T4710jm11b.java {
class T4710jm11b { int i = T4710jm11a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-method-12 { Test that a method can be deprecated } {
    empty_class T4710jm12a {
	/** @deprecated */
	/**/ // degenerate doc comment; only most recent one applies
	static int m() { return 1; }
    }
    delete T4710jm12a.java
    list [compile -classpath . -deprecation [saveas T4710jm12b.java {
class T4710jm12b { int i = T4710jm12a.m(); }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

# test constructor deprecation
tcltest::test 4.7.10-jvms-constructor-1 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon1a {
	// most compact form, need not start line
	int i;/**@deprecated*/ T4710jcon1a() {}
    }
    delete T4710jcon1a.java
    list [compile -classpath . -deprecation [saveas T4710jcon1b.java {
class T4710jcon1b { { new T4710jcon1a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-2 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon2a {
	// one-liner form
	/** @deprecated */
	T4710jcon2a() {}
    }
    delete T4710jcon2a.java
    list [compile -classpath . -deprecation [saveas T4710jcon2b.java {
class T4710jcon2b { { new T4710jcon2a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-3 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon3a {
	/**
          * all leading whitespace and * are ignored
	 	* ** 	 @deprecated
	 **/
	T4710jcon3a() {}
    }
    delete T4710jcon3a.java
    list [compile -classpath . -deprecation [saveas T4710jcon3b.java {
class T4710jcon3b { { new T4710jcon3a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-4 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon4a {
	/**
	  leading * is optional
	  @deprecated
	 */
	T4710jcon4a() {}
    }
    delete T4710jcon4a.java
    list [compile -classpath . -deprecation [saveas T4710jcon4b.java {
class T4710jcon4b { { new T4710jcon4a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-5 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon5a {
	/**
	  @deprecated
	 */ /* */ // additional comments don't matter
	T4710jcon5a() {}
    }
    delete T4710jcon5a.java
    list [compile -classpath . -deprecation [saveas T4710jcon5b.java {
class T4710jcon5b { { new T4710jcon5a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-6 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon6a {
	/**
	  @deprecated
	 */
	; // oops, must be directly before constructor
	T4710jcon6a() {}
    }
    delete T4710jcon6a.java
    list [compile -classpath . -deprecation [saveas T4710jcon6b.java {
class T4710jcon6b { { new T4710jcon6a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-constructor-7 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon7a {
	/**
	  @deprecated
	 */
	int i; // oops, must be directly before constructor
	T4710jcon7a() {}
    }
    delete T4710jcon7a.java
    list [compile -classpath . -deprecation [saveas T4710jcon7b.java {
class T4710jcon7b { { new T4710jcon7a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-constructor-8 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon8a {
	public
	  /** oops - too late
	   * @deprecated */
               T4710jcon8a() {}
    }
    delete T4710jcon8a.java
    list [compile -classpath . -deprecation [saveas T4710jcon8b.java {
class T4710jcon8b { { new T4710jcon8a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-constructor-9 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon9a {
/** characters besides whitespace or * before @ deactivate tag @deprecated */
        T4710jcon9a() {}
    }
    delete T4710jcon9a.java
    list [compile -classpath . -deprecation [saveas T4710jcon9b.java {
class T4710jcon9b { { new T4710jcon9a(); } }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-constructor-10 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon10a {
	/** @deprecated */
        T4710jcon10a() {}
    }
    delete T4710jcon10a.java
    list [compile -classpath . -deprecation [saveas T4710jcon10b.java {
class T4710jcon10b extends T4710jcon10a {}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-11 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon11a {
	/** @deprecated */
        T4710jcon11a() {}
    }
    delete T4710jcon11a.java
    list [compile -classpath . -deprecation [saveas T4710jcon11b.java {
class T4710jcon11b extends T4710jcon11a {
    T4710jcon11b() {
	super();
    }
}
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-12 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon12a {
	/** @deprecated */
        T4710jcon12a() {}
    }
    delete T4710jcon12a.java
    list [compile -classpath . -deprecation [saveas T4710jcon12b.java {
class T4710jcon12b { { new T4710jcon12a() {}; } }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-constructor-13 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon13a {
	/** @deprecated */
	/** Only the most recent doc comment applies */
	T4710jcon13a() {}
    }
    delete T4710jcon13a.java
    list [compile -classpath . -deprecation [saveas T4710jcon13b.java {
class T4710jcon13b { { new T4710jcon13a() {}; } }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-constructor-14 { Test that a constructor can be
        deprecated } {
    empty_class T4710jcon14a {
	/** @deprecated */
	/**/ // degenerate doc comment; only most recent one applies
	T4710jcon14a() {}
    }
    delete T4710jcon14a.java
    list [compile -classpath . -deprecation [saveas T4710jcon14b.java {
class T4710jcon14b { { new T4710jcon14a() {}; } }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

# test field deprecation
tcltest::test 4.7.10-jvms-field-1 { Test that a field can be deprecated } {
    empty_class T4710jf1a {
	// most compact form, need not start line
	int j;/**@deprecated*/ static int i;
    }
    delete T4710jf1a.java
    list [compile -classpath . -deprecation [saveas T4710jf1b.java {
class T4710jf1b { int i = T4710jf1a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-2 { Test that a field can be deprecated } {
    empty_class T4710jf2a {
	// one-liner form
	/** @deprecated */
	static int i;
    }
    delete T4710jf2a.java
    list [compile -classpath . -deprecation [saveas T4710jf2b.java {
class T4710jf2b { int i = T4710jf2a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-3 { Test that a field can be deprecated } {
    empty_class T4710jf3a {
	/**
          * all leading whitespace and * are ignored
	 	* ** 	 @deprecated
	 **/
	static int i;
    }
    delete T4710jf3a.java
    list [compile -classpath . -deprecation [saveas T4710jf3b.java {
class T4710jf3b { int i = T4710jf3a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-4 { Test that a field can be deprecated } {
    empty_class T4710jf4a {
	/**
	  leading * is optional
	  @deprecated
	 */
	static int i;
    }
    delete T4710jf4a.java
    list [compile -classpath . -deprecation [saveas T4710jf4b.java {
class T4710jf4b { int i = T4710jf4a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-5 { Test that a field can be deprecated } {
    empty_class T4710jf5a {
	/**
	  @deprecated
	 */ /* */ // additional comments don't matter
	static int i;
    }
    delete T4710jf5a.java
    list [compile -classpath . -deprecation [saveas T4710jf5b.java {
class T4710jf5b { int i = T4710jf5a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-6 { Test that a field can be deprecated } {
    empty_class T4710jf6a {
	/**
	  @deprecated
	 */
	; // oops, must be directly before field
	static int i;
    }
    delete T4710jf6a.java
    list [compile -classpath . -deprecation [saveas T4710jf6b.java {
class T4710jf6b { int i = T4710jf6a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-field-7 { Test that a field can be deprecated } {
    empty_class T4710jf7a {
	/**
	  @deprecated
	 */
	int j; // oops, must be directly before field
	static int i;
    }
    delete T4710jf7a.java
    list [compile -classpath . -deprecation [saveas T4710jf7b.java {
class T4710jf7b { int i = T4710jf7a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-field-8 { Test that a field can be deprecated } {
    empty_class T4710jf8a {
	public
	  /** oops - too late
	   * @deprecated */
               static int i;
    }
    delete T4710jf8a.java
    list [compile -classpath . -deprecation [saveas T4710jf8b.java {
class T4710jf8b { int i = T4710jf8a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-field-9 { Test that a field can be deprecated } {
    empty_class T4710jf9a {
/** characters besides whitespace or * before @ deactivate tag @deprecated */
        static int i;
    }
    delete T4710jf9a.java
    list [compile -classpath . -deprecation [saveas T4710jf9b.java {
class T4710jf9b { int i = T4710jf9a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-field-10 { Test that a field can be deprecated } {
    empty_class T4710jf10a {
	/** @deprecated */
        static int i;
    }
    delete T4710jf10a.java
    list [compile -classpath . -deprecation [saveas T4710jf10b.java {
class T4710jf10b extends T4710jf10a { int j = i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-11 { Test that a field can be deprecated } {
    empty_class T4710jf11a {
	/**
	 * multiple fields in the same declaration are all deprecated
	 * @deprecated
	 */
        static int j, i;
    }
    delete T4710jf11a.java
    list [compile -classpath . -deprecation [saveas T4710jf11b.java {
class T4710jf11b { int i = T4710jf11a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-field-12 { Test that a field can be deprecated } {
    empty_class T4710jf12a {
	/** @deprecated */
	/** Only the most recent doc comment applies */
	static int i;
    }
    delete T4710jf12a.java
    list [compile -classpath . -deprecation [saveas T4710jf12b.java {
class T4710jf12b { int i = T4710jf12a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-field-13 { Test that a field can be deprecated } {
    empty_class T4710jf13a {
	/** @deprecated */
	/**/ // degenerate doc comment; only most recent one applies
	static int i;
    }
    delete T4710jf13a.java
    list [compile -classpath . -deprecation [saveas T4710jf13b.java {
class T4710jf13b { int i = T4710jf13a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

# Test the lexing of the deprecated symbol. Unlike the rest of the class file,
# @deprecated must be followed by '*' or java.lang.Character.isWhitespace().
tcltest::test 4.7.10-jvms-lex-1 { Test lexing of deprecated } {
    empty_class T4710jl1a {
	/** / before tag disables tag (except starting /**)
	/* @deprecated */
	static int i;
	/**
	/** @deprecated */
	static int j;
    }
    delete T4710j1a.java
    list [compile -classpath . -deprecation [saveas T4710jl1b.java {
class T4710jl1b { int i = T4710jl1a.i + T4710jl1a.j; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-lex-2 { Test lexing of deprecated } {
    empty_class T4710jl2a {
	/** whitespace excludes < \u0009, > \u000d
	* @deprecated\u0008 */
	static int i;
	/** @deprecated\u000e */
	static int j;
    }
    delete T4710j2a.java
    list [compile -classpath . -deprecation [saveas T4710jl2b.java {
class T4710jl2b { int i = T4710jl2a.i + T4710jl2a.j; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-lex-3 { Test lexing of deprecated } {
    empty_class T4710jl3a {
	/** whitespace excludes < \u001c, > \u0020
	* @deprecated\u001b */
	static int i;
	/** @deprecated\u0021 */
	static int j;
    }
    delete T4710j3a.java
    list [compile -classpath . -deprecation [saveas T4710jl3b.java {
class T4710jl3b { int i = T4710jl3a.i + T4710jl3a.j; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-lex-4 { Test lexing of deprecated } {
    empty_class T4710jl4a {
	/** whitespace excludes \u00a0, \u2007, \u202f
	* @deprecated\u00a0 */
	static int i;
	/** @deprecated\u2007 */
	static int j;
	/** @deprecated\u202f */
	static int k;
    }
    delete T4710j4a.java
    list [compile -classpath . -deprecation [saveas T4710jl4b.java {
class T4710jl4b { int i = T4710jl4a.i + T4710jl4a.j + T4710jl4a.k; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-lex-5 { Test lexing of deprecated } {
    empty_class T4710jl5a {
	/** only Character.isSpace() may appear before @ sign
	* \u000b @deprecated */
	static int i;
	/** \u001e @deprecated */
	static int j;
	/** \u2028 @deprecated */
	static int k;
    }
    delete T4710j5a.java
    list [compile -classpath . -deprecation [saveas T4710jl5b.java {
class T4710jl5b { int i = T4710jl5a.i + T4710jl5a.j + T4710jl5a.k; }
    }]] [match_err_or_warn {*depreca*}]
} {PASS 0}

tcltest::test 4.7.10-jvms-lex-6 { Test lexing of deprecated } {
    empty_class T4710jl6a {
	/** whitespace includes \v, even though the rest of the source doesn't
	* @deprecated\u000b */
	static int i;
    }
    delete T4710j6a.java
    list [compile -classpath . -deprecation [saveas T4710jl6b.java {
class T4710jl6b { int i = T4710jl6a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-lex-7 { Test lexing of deprecated } {
    empty_class T4710jl7a {
	/** whitespace includes the ASCII separators (\u001c - \u001f),
	* even though the rest of the source doesn't
	* @deprecated\u001d */
	static int i;
    }
    delete T4710j7a.java
    list [compile -classpath . -deprecation [saveas T4710jl7b.java {
class T4710jl7b { int i = T4710jl7a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-lex-8 { Test lexing of deprecated } {
    empty_class T4710jl8a {
	/** whitespace includes Unicode space characters,
	* even though the rest of the source doesn't
	* @deprecated\u2002 */
	static int i;
    }
    delete T4710j8a.java
    list [compile -classpath . -deprecation [saveas T4710jl8b.java {
class T4710jl8b { int i = T4710jl8a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-lex-9 { Test lexing of deprecated } {
    empty_class T4710jl9a {
	/** whitespace includes Unicode line separator characters,
	* even though the rest of the source doesn't
	* @deprecated\u2028 */
	static int i;
    }
    delete T4710j9a.java
    list [compile -classpath . -deprecation [saveas T4710jl9b.java {
class T4710jl9b { int i = T4710jl9a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

tcltest::test 4.7.10-jvms-lex-10 { Test lexing of deprecated } {
    empty_class T4710jl10a {
	/** whitespace includes Unicode paragraph separator characters,
	* even though the rest of the source doesn't
	* @deprecated\u2029 */
	static int i;
    }
    delete T4710j10a.java
    list [compile -classpath . -deprecation [saveas T4710jl10b.java {
class T4710jl10b { int i = T4710jl10a.i; }
    }]] [match_err_or_warn {*depreca*}]
} {WARN 1}

