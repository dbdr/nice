tcltest::test 16-static-1 { A blank final field may only be assigned by
        simple name } {
    empty_class T16s1 {
        static final int i;
        static {
            T16s1.i = 1;
        }
    }
} FAIL

tcltest::test 16-static-2 { A blank final field may only be assigned by
        simple name } {
    empty_class T16s2 {
        static final int i;
        static {
            new T16s2().i = 1;
        }
    }
} FAIL

tcltest::test 16-static-3 { A blank final field may only be assigned by
        simple name } {
    empty_class T16s3 {
        static final int i;
        static {
            i = 1;
        }
    }
} PASS

tcltest::test 16-instance-1 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i1 {
        final int i;
        {
            new T16i1().i = 1;
        }
    }
} FAIL

tcltest::test 16-instance-2 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i2 {
        final T16i2 t, x;
        {
            (true ? x = new T16i2() : null).t = null;
        }
    }
} FAIL

tcltest::test 16-instance-3 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i3 {
        final int i;
        {
            i = 1;
        }
    }
} PASS

tcltest::test 16-instance-4 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i4 {
        final int i;
        {
            this.i = 1;
        }
    }
} PASS

tcltest::test 16-instance-5 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i5 {
        final int i;
        {
            T16i5.this.i = 1;
        }
    }
} FAIL

tcltest::test 16-instance-6 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i6 {
        final int i;
        {
            (this).i = 1;
        }
    }
} FAIL

tcltest::test 16-instance-7 { A blank final field may only be assigned by
        simple name or this.name } {
    empty_class T16i7 {
        final int i;
        {
            (T16i7.this).i = 1;
        }
    }
} FAIL

