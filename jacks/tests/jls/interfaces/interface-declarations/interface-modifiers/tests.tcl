# top-level interfaces
tcltest::test 9.1.1-1 {should generate error on synchronized interface} {
    compile [saveas SynchronizedInterface.java \
        "synchronized interface SynchronizedInterface {}"]
} FAIL

tcltest::test 9.1.1-2 {adding the synchronized keyword to an interface declared
        as public triggers a bug in the javac shipped with jdk 1.1} {
    compile [saveas PublicSynchronizedInterface.java \
        "public synchronized interface PublicSynchronizedInterface {}"]
} FAIL

tcltest::test 9.1.1-3 {an interface may be public} {
    compile [saveas PublicInterface.java \
        "public interface PublicInterface {}"]
} PASS

tcltest::test 9.1.1-4 {should generate an error if a modifier is given more than once} {
    compile [saveas PublicPublicInterface.java \
        "public public interface PublicPublicInterface {}"]
} FAIL

tcltest::test 9.1.1-5 {The access modifier protected pertains only to member interfaces} {
    compile [saveas ProtectedInterface.java \
        "protected interface ProtectedInterface {}"]
} FAIL

tcltest::test 9.1.1-6 {The access modifier private pertains only to member interfaces} {
    compile [saveas PrivateInterface.java \
        "private interface PrivateInterface {}"]
} FAIL

tcltest::test 9.1.1-7 {The access modifier static pertains only to member interfaces} {
    compile [saveas StaticInterface.java \
        "static interface StaticInterface {}"]
} FAIL

tcltest::test 9.1.1-8 { An interface may be strictfp } {
    compile [saveas T9118.java "strictfp interface T9118 {}"]
} PASS

tcltest::test 9.1.1-9 { Error declaring strictfp more than once } {
    compile [saveas T9119.java "strictfp strictfp interface T9119 {}"]
} FAIL

tcltest::test 9.1.1-10 { An interface may be redundantly declared abstract } {
    ok_pass_or_warn [compile [saveas T91110.java \
            "abstract interface T91110 {}"]]
} OK

tcltest::test 9.1.1-11 { Error declaring abstract more than once } {
    compile [saveas T91111.java "abstract abstract interface T91111 {}"]
} FAIL

tcltest::test 9.1.1-12 { Error declaring final } {
    compile [saveas T91112.java "final interface T91112 {}"]
} FAIL

tcltest::test 9.1.1-13 { Error declaring native } {
    compile [saveas T91113.java "native interface T91113 {}"]
} FAIL

tcltest::test 9.1.1-14 { Error declaring transient } {
    compile [saveas T91114.java "transient interface T91114 {}"]
} FAIL

tcltest::test 9.1.1-15 { Error declaring volatile } {
    compile [saveas T91115.java "volatile interface T91115 {}"]
} FAIL

tcltest::test 9.1.1-16 { longest legal combination } {
    ok_pass_or_warn [compile [saveas T91116.java {
public abstract strictfp interface T91116 {}
}]]
} OK

tcltest::test 9.1.1-17 { stress test } {
    compile [saveas T91117.java {
public abstract strictfp protected private static final transient synchronized
volatile native public abstract strictfp interface T91117 {}
}]
} FAIL

# interfaces nested in a class
tcltest::test 9.1.1-in-class-1 { may be public } {
    empty_class T911ic1 "public interface I {}"
} PASS

tcltest::test 9.1.1-in-class-2 { may be protected } {
    empty_class T911ic2 "protected interface I {}"
} PASS

tcltest::test 9.1.1-in-class-3 { may be private } {
    empty_class T911ic3 "private interface I {}"
} PASS

tcltest::test 9.1.1-in-class-4 { may not have multiple access modifiers } {
    empty_class T911ic4 "public public interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-5 { may not have multiple access modifiers } {
    empty_class T911ic5 "public protected interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-6 { may not have multiple access modifiers } {
    empty_class T911ic6 "public private interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-7 { may not have multiple access modifiers } {
    empty_class T911ic7 "protected public interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-8 { may not have multiple access modifiers } {
    empty_class T911ic8 "protected protected interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-9 { may not have multiple access modifiers } {
    empty_class T911ic9 "protected public interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-10 { may not have multiple access modifiers } {
    empty_class T911ic10 "private public interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-11 { may not have multiple access modifiers } {
    empty_class T911ic11 "private protected interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-12 { may not have multiple access modifiers } {
    empty_class T911ic12 "private private interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-13 { may be redundantly abstract } {
    ok_pass_or_warn [empty_class T911ic13 "abstract interface I {}"]
} OK

tcltest::test 9.1.1-in-class-14 { may not have abstract twice } {
    empty_class T911ic14 "abstract abstract interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-15 { may be redundantly static } {
    ok_pass_or_warn [empty_class T911ic15 "static interface I {}"]
} OK

tcltest::test 9.1.1-in-class-16 { may not have static twice } {
    empty_class T911ic16 "static static interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-17 { may be strictfp } {
    empty_class T911ic17 "strictfp interface I {}"
} PASS

tcltest::test 9.1.1-in-class-18 { may not have strictfp twice } {
    empty_class T911ic18 "strictfp strictfp interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-19 { may not be final } {
    empty_class T911ic19 "final interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-20 { may not be synchronized } {
    empty_class T911ic20 "synchronized interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-21 { may not be native } {
    empty_class T911ic21 "native interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-22 { may not be transient } {
    empty_class T911ic22 "transient interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-23 { may not be volatile } {
    empty_class T911ic23 "volatile interface I {}"
} FAIL

tcltest::test 9.1.1-in-class-24 { longest legal combination } {
    ok_pass_or_warn [empty_class T911ic24 {
public abstract static strictfp interface I {}
}]
} OK

tcltest::test 9.1.1-in-class-25 { stress test } {
    empty_class T911ic25 {
        public static abstract strictfp protected private final transient
        synchronized volatile native public static abstract strictfp
        interface I {}
    }
} FAIL

# interfaces nested in an interface
tcltest::test 9.1.1-in-interface-1 { may be redundantly public } {
    ok_pass_or_warn [compile [saveas T911ii1.java {
interface T911ii1 {
    public interface I {}
}
}]]
} OK

tcltest::test 9.1.1-in-interface-2 { may not be protected } {
    compile [saveas T911ii2.java {
interface T911ii2 {
    protected interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-3 { may not be private } {
    compile [saveas T911ii3.java {
interface T911ii3 {
    private interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-4 { may not have public twice } {
    compile [saveas T911ii4.java {
interface T911ii4 {
    public public interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-5 { may be redundantly abstract } {
    ok_pass_or_warn [compile [saveas T911ii5.java {
interface T911ii5 {
    abstract interface I {}
}
}]]
} OK

tcltest::test 9.1.1-in-interface-6 { may not have abstract twice } {
    compile [saveas T911ii6.java {
interface T911ii6 {
    abstract abstract interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-7 { may be redundantly static } {
    ok_pass_or_warn [compile [saveas T911ii7.java {
interface T911ii7 {
    static interface I {}
}
}]]
} OK

tcltest::test 9.1.1-in-interface-8 { may not have static twice } {
    compile [saveas T911ii8.java {
interface T911ii8 {
    static static interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-9 { may be strictfp } {
    compile [saveas T911ii9.java {
interface T911ii9 {
    strictfp interface I {}
}
}]
} PASS

tcltest::test 9.1.1-in-interface-10 { may not have strictfp twice } {
    compile [saveas T911ii10.java {
interface T911ii10 {
    strictfp strictfp interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-11 { may not be final } {
    compile [saveas T911ii11.java {
interface T911ii11 {
    final interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-12 { may not be synchronized } {
    compile [saveas T911ii11.java {
interface T911ii11 {
    synchronized interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-13 { may not be native } {
    compile [saveas T911ii11.java {
interface T911ii11 {
    native interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-14 { may not be transient } {
    compile [saveas T911ii11.java {
interface T911ii11 {
    transient interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-15 { may not be volatile } {
    compile [saveas T911ii11.java {
interface T911ii11 {
    volatile interface I {}
}
}]
} FAIL

tcltest::test 9.1.1-in-interface-16 { longest legal combination } {
    ok_pass_or_warn [compile [saveas T911ii16.java {
interface T911ii16 {
    public abstract static strictfp interface I {}
}
}]]
} OK

tcltest::test 9.1.1-in-interface-17 { stress test } {
    compile [saveas T911ii17.java {
interface T911ii17 {
    public static abstract strictfp protected private final transient
    synchronized volatile native public static abstract strictfp
    interface I {}
}
}]
} FAIL

