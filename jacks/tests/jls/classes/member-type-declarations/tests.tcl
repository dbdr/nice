tcltest::test 8.5-inheritance-1 { A class inherits non-private accessible
        types that it does not hide by redeclaration } {
    empty_class T85i1 {
	private class C {}
	class Inner extends T85i1 {
	    Inner.C c;
	}
    }
} FAIL

tcltest::test 8.5-inheritance-2 { A class inherits non-private accessible
        types that it does not hide by redeclaration } {
    empty_class T85i2 {
	class C {}
	static class One extends T85i2 {
	    private class C {} // T85i2.C not inherited...
	}
	static class Two extends One {
	    Two.C c; // ...so neither C is inherited
	}
    }
} FAIL

tcltest::test 8.5-inheritance-3 { A class inherits non-private accessible
        types that it does not hide by redeclaration } {
    compile [saveas p1/T85i3a.java {
package p1;
public class T85i3a {
    public class C {}
    static class Inner extends p2.T85i3b {
	Inner.C c; // package C not inherited from b
    }
}
    }] [saveas p2/T85i3b.java {
package p2;
public class T85i3b extends p1.T85i3a {
    class C {} // hides T85i3a.C
}
    }]
} FAIL

tcltest::test 8.5-inheritance-4 { A class inherits non-private accessible
        types that it does not hide by redeclaration } {
    empty_class T85i4 {
	class One {
	    class C {}
	}
	class Two extends One {
	    C c = new Two.C();
	}
    }
} PASS

tcltest::test 8.5-inheritance-5 { Multiple types may be inherited with the
        same name, causing no problems if no ambiguous reference is made } {
    empty_class T85i5 {
	interface I {
	    class Dup {}
	}
	class C {
	    class Dup {}
	}
	class Sub extends C implements I {}
    }
} PASS

tcltest::test 8.5-inheritance-6 { Inheritance along multiple paths is still
        single inheritance } {
    empty_class T85i6 {
	interface Super {
	    class C {}
	}
	interface I1 extends Super {}
	interface I2 extends Super {}
	class Sub implements I1, I2 {
	    C c = new Sub.C();
	}
    }
} PASS

tcltest::test 8.5-name-1 { Duplicate type names are forbidden } {
    empty_class T85n1 {
	class Dup {}
	interface Dup {}
    }
} FAIL

tcltest::test 8.5-hiding-1 { Types can hide supertype types regardless of
        accessibility relationships } {
    empty_class T85h1 {
	class One {
	    public class Pub {}
	    protected class Pro {}
	    class Pack {}
	    private class Priv {}
	}
	class Two extends One {
	    private class Pub {}
	    private class Pro {}
	    private class Pack {}
	    private class Priv {}
	}
	class Three extends One {
	    class Pub {}
	    class Pro {}
	    class Pack {}
	    class Priv {}
	}
	class Four extends One {
	    protected class Pub {}
	    protected class Pro {}
	    protected class Pack {}
	    protected class Priv {}
	}
	class Five extends One {
	    public class Pub {}
	    public class Pro {}
	    public class Pack {}
	    public class Priv {}
	}
    }
} PASS

tcltest::test 8.5-hiding-2 { Types can hide supertype types regardless of
        type relationships } {
    empty_class T85h2 {
	static class One {
	    class C {}
	    interface I {}
	}
	static class Two extends One {
	    interface C {}
	    class I {}
	}
    }
} PASS

tcltest::test 8.5-hiding-3 { Types can hide supertype types regardless of
        instance relationships } {
    empty_class T85h3 {
	static class One {
	    static class Stat {}
	    class Inst {}
	}
	static class Two extends One {
	    class Stat {}
	    static class Inst {}
	}
    }
} PASS

tcltest::test 8.5-hiding-4 { Types can hide supertype types regardless of
        modifier relationships } {
    empty_class T85h4 {
	class One {
	    abstract class Abs {}
	    final class Fin {}
	    strictfp class Strict {}
	}
	class Two extends One {
	    final class Abs {}
	    strictfp class Fin {}
	    abstract class Strict {}
	}
    }
} PASS
