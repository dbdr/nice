tcltest::test 8.4.6-miranda-1 { Abstract classes no longer need Miranda
        methods (generated place-holders in classes which inherit an abstract
        method from an interface without overriding it) } {
    compile [saveas T846m1a.java {
interface T846m1a { void m(); }
}] [saveas T846m1b.java {
// If a compiler still inserts Miranda methods, then class b will have a
// bogus declaration "abstract void m();"
abstract class T846m1b implements T846m1a {}
}]
# Now, leave b in .class form only, and recompile a to lose m()
    delete T846m1b.java
    compile [saveas T846m1a.java {
interface T846m1a {}
}]
# The real test: if c inherits b.m(), the compiler goofed in compiling b
    compile [saveas T846m1c.java {
abstract class T846m1c extends T846m1b {
    void foo() { m(); }
}
}]
} FAIL
