tcltest::test 8.2-accessibility-inherited-member-1 { members
        declared public are accessible in a subclass } {
    saveas T82aim1.java {
class T82aim1 {
    public int foo;
}

class T82aim1_Test extends T82aim1 {
    void bar() { foo = 0; }
}
    }

    compile T82aim1.java
} PASS

tcltest::test 8.2-accessibility-inherited-member-2 { members
        declared public are accessible in a subclass } {
    saveas T82aim2.java {
class T82aim2 {
    public void foo() {}
}

class T82aim2_Test extends T82aim2 {
    void bar() { foo(); }
}
    }

    compile T82aim2.java
} PASS

tcltest::test 8.2-accessibility-inherited-member-3 { members
        declared protected are accessible in a subclass } {
    saveas T82aim3.java {
class T82aim3 {
    protected int foo;
}

class T82aim3_Test extends T82aim3 {
    void bar() { foo = 0; }
}
    }

    compile T82aim3.java
} PASS

tcltest::test 8.2-accessibility-inherited-member-4 { members
        declared protected are accessible in a subclass } {
    saveas T82aim4.java {
class T82aim4 {
    protected void foo() {}
}

class T82aim4_Test extends T82aim4 {
    void bar() { foo(); }
}
    }

    compile T82aim4.java
} PASS

tcltest::test 8.2-accessibility-inherited-member-5 { members
        declared private are not accessible in a subclass } {
    saveas T82aim5.java {
class T82aim5 {
    private int foo;
}

class T82aim5_Test extends T82aim5 {
    void bar() { foo = 0; }
}
    }

    compile T82aim5.java
} FAIL

tcltest::test 8.2-accessibility-inherited-member-6 { members
        declared private are not accessible in a subclass } {
    saveas T82aim6.java {
class T82aim6 {
    private void foo() {}
}

class T82aim6_Test extends T82aim6 {
    void bar() { foo(); }
}
    }

    compile T82aim6.java
} FAIL


tcltest::test 8.2-accessibility-inherited-member-7 { members
        with default protection are accessible in a subclass } {
    saveas T82aim7.java {
class T82aim7 {
    int foo;
}

class T82aim7_Test extends T82aim7 {
    void bar() { foo = 0; }
}
    }

    compile T82aim7.java
} PASS

tcltest::test 8.2-accessibility-inherited-member-8 { members
        with default protection are accessible in a subclass } {
    saveas T82aim8.java {
class T82aim8 {
    void foo() {}
}

class T82aim8_Test extends T82aim8 {
    void bar() { foo(); }
}
    }

    compile T82aim8.java
} PASS

tcltest::test 8.2-constructor-not-inherited-1 { A constructor
        is not a class member and is therefore not inherited } {
    saveas T82cni1.java {
class T82cni1 {
    public T82cni1() {}
    public T82cni1(int value) {}
}

class T82cni1_Test extends T82cni1 {
    Object o = new T82cni1_Test(0);
}
    }

    compile T82cni1.java
} FAIL
