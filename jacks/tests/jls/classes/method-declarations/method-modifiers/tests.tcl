# test access modifiers
tcltest::test 8.4.3-access-1 { Multiple access modifiers is an error } {
    empty_class T843access1 {
        public public void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-2 { Multiple access modifiers is an error } {
    empty_class T843access2 {
        public protected void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-3 { Multiple access modifiers is an error } {
    empty_class T843access3 {
        public private void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-4 { Multiple access modifiers is an error } {
    empty_class T843access4 {
        protected public void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-5 { Multiple access modifiers is an error } {
    empty_class T843access5 {
        protected protected void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-6 { Multiple access modifiers is an error } {
    empty_class T843access6 {
        protected private void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-7 { Multiple access modifiers is an error } {
    empty_class T843access7 {
        private public void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-8 { Multiple access modifiers is an error } {
    empty_class T843access8 {
        private protected void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-9 { Multiple access modifiers is an error } {
    empty_class T843access9 {
        private private void foo() {}
    }
} FAIL

tcltest::test 8.4.3-access-10 { A single access modifier is legal } {
    empty_class T843access10 {
        public void foo() {}
    }
} PASS

tcltest::test 8.4.3-access-11 { A single access modifier is legal } {
    empty_class T843access11 {
        protected void foo() {}
    }
} PASS

tcltest::test 8.4.3-access-12 { A single access modifier is legal } {
    empty_class T843access12 {
        private void foo() {}
    }
} PASS

tcltest::test 8.4.3-access-13 { No access modifier is legal } {
    empty_class T843access13 {
        void foo() {}
    }
} PASS

# test combinations with the abstract modifier
tcltest::test 8.4.3-abstract-1 { abstract alone is legal } {
    compile [saveas T843abstract1.java {
abstract class T843abstract1 {
    abstract void foo();
}
    }]
} PASS

tcltest::test 8.4.3-abstract-2 { public abstract is legal } {
    compile [saveas T843abstract2.java {
abstract class T843abstract2 {
    public abstract void foo();
}
    }]
} PASS

tcltest::test 8.4.3-abstract-3 { abstract public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [compile [saveas T843abstract3.java {
abstract class T843abstract3 {
    abstract public void foo();
}
    }]]
} OK

tcltest::test 8.4.3-abstract-4 { protected abstract is legal } {
    compile [saveas T843abstract4.java {
abstract class T843abstract4 {
    protected abstract void foo();
}
    }]
} PASS

tcltest::test 8.4.3-abstract-5 { abstract protected is legal, but not in
        the preferred order } {
    ok_pass_or_warn [compile [saveas T843abstract5.java {
abstract class T843abstract5 {
    abstract protected void foo();
}
    }]]
} OK

tcltest::test 8.4.3-abstract-6 { private abstract is illegal } {
    compile [saveas T843abstract6.java {
abstract class T843abstract6 {
    private abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-7 { abstract private is illegal } {
    compile [saveas T843abstract7.java {
abstract class T843abstract7 {
    abstract private void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-8 { abstract abstract is illegal } {
    compile [saveas T843abstract8.java {
abstract class T843abstract8 {
    abstract abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-9 { static abstract is illegal } {
    compile [saveas T843abstract9.java {
abstract class T843abstract9 {
    static abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-10 { abstract static is illegal } {
    compile [saveas T843abstract10.java {
abstract class T843abstract10 {
    abstract static void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-11 { final abstract is illegal } {
    compile [saveas T843abstract11.java {
abstract class T843abstract11 {
    final abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-12 { abstract final is illegal } {
    compile [saveas T843abstract12.java {
abstract class T843abstract12 {
    abstract final void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-13 { synchronized abstract is illegal } {
    compile [saveas T843abstract13.java {
abstract class T843abstract13 {
    synchronized abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-14 { abstract synchronized is illegal } {
    compile [saveas T843abstract14.java {
abstract class T843abstract14 {
    abstract synchronized void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-15 { native abstract is illegal } {
    compile [saveas T843abstract15.java {
abstract class T843abstract15 {
    native abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-16 { abstract native is illegal } {
    compile [saveas T843abstract16.java {
abstract class T843abstract16 {
    abstract native void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-17 { strictfp abstract is illegal } {
    compile [saveas T843abstract17.java {
abstract class T843abstract17 {
    strictfp abstract void foo();
}
    }]
} FAIL

tcltest::test 8.4.3-abstract-18 { abstract strictfp is illegal } {
    compile [saveas T843abstract18.java {
abstract class T843abstract18 {
    abstract strictfp void foo();
}
    }]
} FAIL

# test combinations with static
#(static/abstract already tested)
tcltest::test 8.4.3-static-1 { static alone is legal } {
    empty_class T843static1 {
        static void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-2 { public static is legal } {
    empty_class T843static2 {
        public static void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-3 { static public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static3 {
        static public void foo() {}
    }]
} OK

tcltest::test 8.4.3-static-4 { protected static is legal } {
    empty_class T843static4 {
        protected static void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-5 { static protected is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static5 {
        static protected void foo() {}
    }]
} OK

tcltest::test 8.4.3-static-6 { private static is legal } {
    empty_class T843static6 {
        private static void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-7 { static private is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static7 {
        static private void foo() {}
    }]
} OK

tcltest::test 8.4.3-static-8 { static static is illegal } {
    empty_class T843static8 {
        static static void foo() {}
    }
} FAIL

tcltest::test 8.4.3-static-9 { static final is legal } {
    empty_class T843static9 {
        static final void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-10 { final static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static10 {
        final static void foo() {}
    }]
} OK

tcltest::test 8.4.3-static-11 { static synchronized is legal } {
    empty_class T843static11 {
        static synchronized void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-12 { synchronized static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static12 {
        synchronized static void foo() {}
    }]
} OK

tcltest::test 8.4.3-static-13 { static native is legal } {
    empty_class T843static13 {
        static native void foo();
    }
} PASS

tcltest::test 8.4.3-static-14 { native static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static14 {
        native static void foo();
    }]
} OK

tcltest::test 8.4.3-static-15 { static strictfp is legal } {
    empty_class T843static15 {
        static strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-static-16 { strictfp static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843static16 {
        strictfp static void foo() {}
    }]
} OK

# test combinations with final
#(final/abstract already tested)
#(final/static already tested)
tcltest::test 8.4.3-final-1 { final alone is legal } {
    empty_class T843final1 {
        final void foo() {}
    }
} PASS

tcltest::test 8.4.3-final-2 { public final is legal } {
    empty_class T843final2 {
        public final void foo() {}
    }
} PASS

tcltest::test 8.4.3-final-3 { final public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843final3 {
        final public void foo() {}
    }]
} OK

tcltest::test 8.4.3-final-4 { protected final is legal } {
    empty_class T843final4 {
        protected final void foo() {}
    }
} PASS

tcltest::test 8.4.3-final-5 { final protected is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843final5 {
        final protected void foo() {}
    }]
} OK

tcltest::test 8.4.3-final-6 { private final is legal, but redundant } {
    ok_pass_or_warn [empty_class T843final6 {
        private final void foo() {}
    }]
} OK

tcltest::test 8.4.3-final-7 { final private is legal, but redundant, and
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843final7 {
        final private void foo() {}
    }]
} OK

tcltest::test 8.4.3-final-8 { final final is illegal } {
    empty_class T843final8 {
        final final void foo() {}
    }
} FAIL

tcltest::test 8.4.3-final-9 { final synchronized is legal } {
    empty_class T843final9 {
        final synchronized void foo() {}
    }
} PASS

tcltest::test 8.4.3-final-10 { synchronized final is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843final10 {
        synchronized final void foo() {}
    }]
} OK

tcltest::test 8.4.3-final-11 { final native is legal } {
    empty_class T843final11 {
        final native void foo();
    }
} PASS

tcltest::test 8.4.3-final-12 { native final is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843final12 {
        native final void foo();
    }]
} OK

tcltest::test 8.4.3-final-13 { final strictfp is legal } {
    empty_class T843final13 {
        final strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-final-14 { strictfp final is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843final14 {
        strictfp final void foo() {}
    }]
} OK

tcltest::test 8.4.3-final-15 { final in a final class is legal, but
        redundant } {
    ok_pass_or_warn [compile [saveas T843final15.java {
final class T843final15 {
    final void foo() {}
}
    }]]
} OK

# test combinations with synchronized
#(synchronized/abstract already tested)
#(synchronized/static already tested)
#(synchronized/final already tested)
tcltest::test 8.4.3-synchronized-1 { synchronized alone is legal } {
    empty_class T843synchronized1 {
        synchronized void foo() {}
    }
} PASS

tcltest::test 8.4.3-synchronized-2 { public synchronized is legal } {
    empty_class T843synchronized2 {
        public synchronized void foo() {}
    }
} PASS

tcltest::test 8.4.3-synchronized-3 { synchronized public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843synchronized3 {
        synchronized public void foo() {}
    }]
} OK

tcltest::test 8.4.3-synchronized-4 { protected synchronized is legal } {
    empty_class T843synchronized4 {
        protected synchronized void foo() {}
    }
} PASS

tcltest::test 8.4.3-synchronized-5 { synchronized protected is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843synchronized5 {
        synchronized protected void foo() {}
    }]
} OK

tcltest::test 8.4.3-synchronized-6 { private synchronized is legal } {
    empty_class T843synchronized6 {
        private synchronized void foo() {}
    }
} PASS

tcltest::test 8.4.3-synchronized-7 { synchronized private is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843synchronized7 {
        synchronized private void foo() {}
    }]
} OK

tcltest::test 8.4.3-synchronized-8 { synchronized synchronized is illegal } {
    empty_class T843synchronized8 {
        synchronized synchronized void foo() {}
    }
} FAIL

tcltest::test 8.4.3-synchronized-9 { synchronized native is legal } {
    empty_class T843synchronized9 {
        synchronized native void foo();
    }
} PASS

tcltest::test 8.4.3-synchronized-10 { native synchronized is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843synchronized10 {
        native synchronized void foo();
    }]
} OK

tcltest::test 8.4.3-synchronized-11 { synchronized strictfp is legal } {
    empty_class T843synchronized11 {
        synchronized strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-synchronized-12 { strictfp synchronized is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843synchronized12 {
        strictfp synchronized void foo() {}
    }]
} OK

# test combinations with native
#(native/abstract already tested)
#(native/static already tested)
#(native/final already tested)
#(native/synchronized already tested)
tcltest::test 8.4.3-native-1 { native alone is legal } {
    empty_class T843native1 {
        native void foo();
    }
} PASS

tcltest::test 8.4.3-native-2 { public native is legal } {
    empty_class T843native2 {
        public native void foo();
    }
} PASS

tcltest::test 8.4.3-native-3 { native public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843native3 {
        native public void foo();
    }]
} OK

tcltest::test 8.4.3-native-4 { protected native is legal } {
    empty_class T843native4 {
        protected native void foo();
    }
} PASS

tcltest::test 8.4.3-native-5 { native protected is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843native5 {
        native protected void foo();
    }]
} OK

tcltest::test 8.4.3-native-6 { private native is legal } {
    empty_class T843native6 {
        private native void foo();
    }
} PASS

tcltest::test 8.4.3-native-7 { native private is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843native7 {
        native private void foo();
    }]
} OK

tcltest::test 8.4.3-native-8 { native native is illegal } {
    empty_class T843native8 {
        native native void foo();
    }
} FAIL

tcltest::test 8.4.3-native-9 { native strictfp is illegal } {
    empty_class T843native9 {
        native strictfp void foo();
    }
} FAIL

tcltest::test 8.4.3-native-10 { strictfp native is illegal } {
    empty_class T843native10 {
        strictfp native void foo();
    }
} FAIL

# test combinations with strictfp
#(strictfp/abstract already tested)
#(strictfp/static already tested)
#(strictfp/final already tested)
#(strictfp/synchronized already tested)
tcltest::test 8.4.3-strictfp-1 { strictfp alone is legal } {
    empty_class T843strictfp1 {
        strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-strictfp-2 { public strictfp is legal } {
    empty_class T843strictfp2 {
        public strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-strictfp-3 { strictfp public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T843strictfp3 {
        strictfp public void foo() {}
    }]
} OK

tcltest::test 8.4.3-strictfp-4 { protected strictfp is legal } {
    empty_class T843strictfp4 {
        protected strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-strictfp-5 { strictfp protected is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843strictfp5 {
        strictfp protected void foo() {}
    }]
} OK

tcltest::test 8.4.3-strictfp-6 { private strictfp is legal } {
    empty_class T843strictfp6 {
        private strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-strictfp-7 { strictfp private is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T843strictfp7 {
        strictfp private void foo() {}
    }]
} OK

tcltest::test 8.4.3-strictfp-8 { strictfp strictfp is illegal } {
    empty_class T843strictfp8 {
        strictfp strictfp void foo() {}
    }
} FAIL

tcltest::test 8.4.3-strictfp-9 { strictfp in a strictfp class is legal, but
        redundant } {
    ok_pass_or_warn [compile [saveas T843strictfp9.java {
strictfp class T843strictfp9 {
    strictfp void foo() {}
}
    }]]
} OK

# Because we can...
tcltest::test 8.4.3-big-1 { largest number of modifiers allowed } {
    empty_class T843big1 {
        public static final synchronized strictfp void foo() {}
    }
} PASS

tcltest::test 8.4.3-big-2 { largest number of modifiers allowed } {
    empty_class T843big2 {
        protected static final synchronized native void foo();
    }
} PASS

tcltest::test 8.4.3-big-3 { modifiers stress test } {
    empty_class T843big3 {
        public protected private static abstract final synchronized
        native strictfp public protected private static abstract
        final synchronized native strictfp void foo();
    }
} FAIL

# These never modify methods
tcltest::test 8.4.3-bad-1 { transient methods don't exist } {
    empty_class T843bad1 {
        transient void foo() {}
    }
} FAIL

tcltest::test 8.4.3-bad-2 { volatile methods don't exist } {
    empty_class T843bad2 {
        volatile void foo() {}
    }
} FAIL
