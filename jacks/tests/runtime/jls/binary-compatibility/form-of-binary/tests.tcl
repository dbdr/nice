tcltest::test 13.1-runtime-constant-1 { A constant variable must never
        appear to have it's initial value, even when accessed in a
        non-constant expression } {runtime} {
    compile_and_run [saveas T131c1.java {
class T131c1 {
    int m(T131c1 t) { return t.i; }
    int j = m(this);
    final int i = 1;
    public static void main(String[] args) {
	System.out.print(new T131c1().j);
    }
}
    }]
} 1

tcltest::test 13.1-runtime-constant-2 { A constant variable must never
        appear to have it's initial value, even when accessed in a
        non-constant expression } {runtime} {
    compile_and_run [saveas T131c2.java {
class T131c2 {
    static int m(T131c2 t) { return t.i; }
    static int j = m(new T131c2());
    static final int i = 1;
    public static void main(String[] args) {
	System.out.print(j);
    }
}
    }]
} 1

tcltest::test 13.1-runtime-constant-3 { A constant variable must be inlined
        without reference to the original class; hence, it can be deleted from
        the original class and the code still works } {runtime} {
    compile [saveas T131rc3a.java {
class T131rc3a {
    static final int i = 1;
    final int j = 2;
}
class T131rc3b {
    static void m(T131rc3a t) {
	System.out.print(t.i + t.j);
    }
}
    }]
    compile_and_run -classpath . [saveas T131rc3a.java {
class T131rc3a {
    public static void main(String[] args) {
	T131rc3b.m(new T131rc3a());
    }
    // Notice that i and j no longer exist
}
    }]
} 3

tcltest::test 13.1-runtime-field-1 { A field reference must include the
        (implicit) qualifier of the expression it was in } {runtime} {
    compile [saveas T131rc3a.java {
class T131rf1a {
    static int i = "a".length();
}
interface T131rf1b {
    int j = "ab".length();
}
class T131rf1c extends T131rf1a implements T131rf1b {
    static void m() {
	System.out.print(i + " " + j + " " + T131rf1c.i + " " + T131rf1c.j);
    }
}
    }]
    compile_and_run -classpath . [saveas T131rf1a.java {
class T131rf1a {
    public static void main(String[] args) {
	T131rf1c.m();
    }
    // Notice that i and j have swapped locations and values in supertypes of c
    static int j = "abc".length();
}
interface T131rf1b {
    int i = "abcd".length();
}
    }]
} {4 3 4 3}
