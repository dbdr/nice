tcltest::test 8.3.2.1-this-1 { Class field initializers cannot refer to this,
        not even implicitly } {
    empty_class T8321t1 {
        static String s = toString();
    }
} FAIL

tcltest::test 8.3.2.1-this-2 { Class field initializers cannot refer to this,
        not even implicitly } {
    empty_class T8321t2 {
        static String s = this.toString();
    }
} FAIL

tcltest::test 8.3.2.1-this-3 { Class field initializers cannot refer to this,
        not even implicitly } {
    empty_class T8321t3 {
        static String s = T8321t3.this.toString();
    }
} FAIL

tcltest::test 8.3.2.1-super-1 { Class field initializers cannot refer to
        super } {
    empty_class T8321s1 {
        static String s = super.toString();
    }
} FAIL

tcltest::test 8.3.2.1-super-2 { Class field initializers cannot refer to
        super } {
    empty_class T8321s2 {
        static String s = T8321s2.super.toString();
    }
} FAIL
