tcltest::test 8.3.1.1-valid-modifier-1 { public is a valid
        static FieldModifier } {
    empty_class T8311vm1 {
        public static int i;
    }
} PASS

tcltest::test 8.3.1.1-valid-modifier-2 { protected is a valid
        static FieldModifier } {
    empty_class T8311vm2 {
        protected static int i;
    }
} PASS

tcltest::test 8.3.1.1-valid-modifier-3 { private is a valid
        static FieldModifier } {
    empty_class T8311vm3 {
        private static int i;
    }
} PASS

tcltest::test 8.3.1.1-valid-modifier-4 { final is a valid
        static FieldModifier } {
    empty_class T8311vm4 {
        final static int i = 0;
    }
} PASS

tcltest::test 8.3.1.1-valid-modifier-5 { transient is a valid
        static FieldModifier } {
    empty_class T8311vm5 {
        transient static int i;
    }
} PASS

tcltest::test 8.3.1.1-valid-modifier-6 { volatile is a valid
        static FieldModifier } {
    empty_class T8311vm6 {
        volatile static int i;
    }
} PASS

tcltest::test 8.3.1.1-other-1 { access an initialized final
        static field in another class that has not yet been compiled } {
    saveas T8311o1_1.java {
class T8311o1_1
{
    int i = T8311o1_2.i;
}
    }

    saveas T8311o1_2.java {
class T8311o1_2
{
    final static int i = 0;
}
    }

    compile T8311o1_1.java T8311o1_2.java
} PASS

tcltest::test 8.3.1.1-other-2 { access an uninitialized final
        static field in another class that has not yet been compiled } {
    saveas T8311o2_1.java {
class T8311o2_1
{
    int i = T8311o2_2.i;
}
    }

    saveas T8311o2_2.java {
class T8311o2_2
{
    final static int i;
    static { i = 0; }
}
    }

    compile T8311o2_1.java T8311o2_2.java
} PASS
