tcltest::test 5.1.1-primitive-1 { boolean identity conversion } {
    empty_class T511p1 {
        boolean b1 = (boolean) true;
        boolean b2 = (boolean) b1;
        boolean b3 = (boolean) (false != false);
        boolean b4 = (boolean) Boolean.TRUE.booleanValue();
    }
} PASS

tcltest::test 5.1.1-primitive-2 { byte identity conversion } {
    empty_class T511p2 {
        static final byte b1 = 1;
        byte b2 = (byte) b1;
        byte b3 = (byte) (new Byte(Byte.MAX_VALUE).byteValue());
    }
} PASS

tcltest::test 5.1.1-primitive-3 { short identity conversion } {
    empty_class T511p3 {
        static final short s1 = 1;
        short s2 = (short) s1;
        short s3 = (short) (new Short(Short.MAX_VALUE).shortValue());
    }
} PASS

tcltest::test 5.1.1-primitive-4 { char identity conversion } {
    empty_class T511p4 {
        char c1 = (char) '1';
        char c2 = (char) c1;
        char c3 = (char) (new Character(Character.MAX_VALUE).charValue());
    }
} PASS

tcltest::test 5.1.1-primitive-5 { int identity conversion } {
    empty_class T511p5 {
        int i1 = (int) 1;
        int i2 = (int) i1;
        int i3 = (int) (1 + 2);
        int i4 = (int) (new Integer(Integer.MAX_VALUE).intValue());
    }
} PASS

tcltest::test 5.1.1-primitive-6 { long identity conversion } {
    empty_class T511p6 {
        long l1 = (long) 1L;
        long l2 = (long) l1;
        long l3 = (long) (1L + 2L);
        long l4 = (long) (new Long(Long.MAX_VALUE).longValue());
    }
} PASS

tcltest::test 5.1.1-primitive-7 { float identity conversion } {
    empty_class T511p7 {
        float f1 = (float) 1.0f;
        float f2 = (float) f1;
        float f3 = (float) (1f + 2f);
        float f4 = (float) (new Float(Float.MAX_VALUE).floatValue());
    }
} PASS

tcltest::test 5.1.1-primitive-8 { double identity conversion } {
    empty_class T511p8 {
        double d1 = (double) 1.0;
        double d2 = (double) d1;
        double d3 = (double) (1.0 + 2.0);
        double d4 = (double) (new Double(Double.MAX_VALUE).doubleValue());
    }
} PASS

tcltest::test 5.1.1-reference-1 { reference identity conversion: Object } {
    empty_class T511r1 {
        Object o1 = (Object) new Object();
        Object o2 = (Object) null;
        Object o3 = (Object) o1;
        Object o4 = (Object) o2;
        Object foo() { return new Object(); }
        Object o5 = (Object) foo();
    }
} PASS

tcltest::test 5.1.1-reference-2 { reference identity conversion: String } {
    empty_class T511r2 {
        String s1 = (String) "";
        String s2 = (String) null;
        String s3 = (String) s1;
        String s4 = (String) s2;
        String s5 = (String) ("1" + "2");
        String s6 = (String) "1".concat("2");
    }
} PASS

tcltest::test 5.1.1-reference-3 { reference identity conversion: this class } {
    empty_class T511r3 {
        T511r3 t1 = (T511r3) new T511r3();
        T511r3 t2 = (T511r3) null;
        T511r3 t3 = (T511r3) t1;
        T511r3 t4 = (T511r3) t2;
    }
} PASS

tcltest::test 5.1.1-reference-4 { reference identity conversion: primitive array } {
    empty_class T511r4 {
        int[] ia1 = {1, 2};
        int[] ia2 = (int[]) null;
        int[] ia3 = (int[]) ia1;
        int[] ia4 = (int[]) ia2;
        int[] foo() { return new int[0]; }
        int[] ia5 = (int[]) foo();
    }
} PASS

tcltest::test 5.1.1-reference-5 { reference identity conversion: Object array } {
    empty_class T511r5 {
        Object[] oa1 = {new Object[1]};
        Object[] oa2 = (Object[]) null;
        Object[] oa3 = (Object[]) oa1;
        Object[] oa4 = (Object[]) oa2;
        Object[] foo() { return new Object[0]; }
        Object[] oa5 = (Object[]) foo();
    }
} PASS

tcltest::test 5.1.1-null-1 { there is no such thing as null identity conversion } {
    empty_class T511n1 {
        Object n = (null) null;
    }
} FAIL

