tcltest::test 8.3-inheritance-1 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    empty_class T83i1 {
	private int i;
	static class Inner extends T83i1 {
	    int j = i; // i not inherited, and enclosing i not available
	}
    }
} FAIL

tcltest::test 8.3-inheritance-2 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    empty_class T83i2 {
	private int i;
	class Inner extends T83i2 {
	    int j = this.i; // i not inherited
	}
    }
} FAIL

tcltest::test 8.3-inheritance-3 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    empty_class T83i3 {
	int i;
	static class One extends T83i3 {
	    private int i; // T83i3.i not inherited...
	}
	static class Two extends One {
	    int j = i; // ...so neither i is inherited; T83i3.i not available
	}
    }
} FAIL

tcltest::test 8.3-inheritance-4 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    empty_class T83i4 {
	int i;
	class One extends T83i4 {
	    private int i; // T83i4.i not inherited...
	}
	class Two extends One {
	    Two() {
		T83i4.this.super();
	    }
	    int j = this.i; // ...so neither i is inherited
	}
    }
} FAIL

tcltest::test 8.3-inheritance-5 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    compile [saveas p1/T83i5a.java {
package p1;
public class T83i5a {
    public int i;
    static class Inner extends p2.T83i5b {
	int j = i; // package i not inherited from b, enclosing i not available
    }
}
    }] [saveas p2/T83i5b.java {
package p2;
public class T83i5b extends p1.T83i5a {
    int i; // hides T83i5a.i
}
    }]
} FAIL

tcltest::test 8.3-inheritance-6 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    compile [saveas p1/T83i6a.java {
package p1;
public class T83i6a {
    public int i;
    class Inner extends p2.T83i6b {
	int j = this.i; // package i not inherited from b
    }
}
    }] [saveas p2/T83i6b.java {
package p2;
public class T83i6b extends p1.T83i6a {
    int i; // hides T83i6a.i
}
    }]
} FAIL

tcltest::test 8.3-inheritance-7 { A class inherits non-private accessible
        fields that it does not hide by redeclaration } {
    empty_class T83i7 {
	class One {
	    int i;
	}
	class Two extends One {
	    {
		i = this.i;
	    }
	}
    }
} PASS

tcltest::test 8.3-inheritance-8 { Multiple fields may be inherited with the
        same name, causing no problems if no ambiguous reference is made } {
    empty_class T83i8 {
	interface I {
	    int i = 1;
	}
	class C {
	    int i;
	}
	class Sub extends C implements I {}
    }
} PASS

tcltest::test 8.3-inheritance-9 { Inheritance along multiple paths is still
        single inheritance } {
    empty_class T83i9 {
	interface Super {
	    int i = 1;
	}
	interface I1 extends Super {}
	interface I2 extends Super {}
	class C implements I1, I2 {
	    int j = i;
	}
    }
} PASS

tcltest::test 8.3-name-1 { Duplicate field names are forbidden } {
    empty_class T83n1 {
	int i, i;
    }
} FAIL

tcltest::test 8.3-name-2 { Field names may match method and type names } {
    empty_class T83n2 {
	int i;
	void i() {}
	class i {}
    }
} PASS

tcltest::test 8.3-hiding-1 { Fields can hide supertype fields regardless of
        accessibility relationships } {
    empty_class T83h1 {
	class One {
	    public int pub;
	    protected int pro;
	    int pack;
	    private int priv;
	}
	class Two extends One {
	    private int pub;
	    private int pro;
	    private int pack;
	    private int priv;
	}
	class Three extends One {
	    int pub;
	    int pro;
	    int pack;
	    int priv;
	}
	class Four extends One {
	    protected int pub;
	    protected int pro;
	    protected int pack;
	    protected int priv;
	}
	class Five extends One {
	    public int pub;
	    public int pro;
	    public int pack;
	    public int priv;
	}
    }
} PASS

tcltest::test 8.3-hiding-2 { Fields can hide supertype fields regardless of
        type relationships } {
    empty_class T83h2 {
	class One {
	    int i;
	}
	class Two extends One {
	    Object i;
	}
    }
} PASS

tcltest::test 8.3-hiding-3 { Fields can hide supertype fields regardless of
        instance relationships } {
    empty_class T83h3 {
	static class One {
	    static int stat;
	    int inst;
	}
	static class Two extends One {
	    int stat;
	    static int inst;
	}
    }
} PASS

tcltest::test 8.3-hiding-4 { Fields can hide supertype fields regardless of
        modifier relationships } {
    empty_class T83h4 {
	class One {
	    final int fin = 1;
	    transient int trans;
	    volatile int vol;
	    int plain;
	}
	class Two extends One {
	    int fin;
	    int trans;
	    int vol;
	    int plain;
	}
	class Three extends One {
	    final int fin = 1;
	    final int trans = 1;
	    final int vol = 1;
	    final int plain = 1;
	}
	class Four extends One {
	    transient int fin;
	    transient int trans;
	    transient int vol;
	    transient int plain;
	}
	class Five extends One {
	    volatile int fin;
	    volatile int trans;
	    volatile int vol;
	    volatile int plain;
	}
    }
} PASS
