tcltest::test 8.3.2.2-static-1 { Instance field initializers may use static
        members, regardless of their declaration order } {
    empty_class T8322st1 {
        float f = j;
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.2-this-1 { Instance field initializers may refer to this,
        even implicitly } {
    empty_class T8322t1 {
        String s = toString();
    }
} PASS

tcltest::test 8.3.2.2-this-2 { Instance field initializers may refer to this,
        even implicitly } {
    empty_class T8322t2 {
        String s = this.toString();
    }
} PASS

tcltest::test 8.3.2.2-this-3 { Instance field initializers may refer to this,
        even implicitly } {
    empty_class T8322t3 {
        String s = T8322t3.this.toString();
    }
} PASS

tcltest::test 8.3.2.2-super-1 { Instance field initializers may refer to
        super } {
    empty_class T8322s1 {
        String s = super.toString();
    }
} PASS

tcltest::test 8.3.2.2-super-2 { Instance field initializers may refer to
        super } {
    empty_class T8322s2 {
        String s = T8322s2.super.toString();
    }
} PASS
