# byte

tcltest::test 5.2-byte-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52bi1 {
        byte b = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-widening-1 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52bw1 {
        short s = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-widening-2 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52bw2 {
        int i = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-widening-3 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52bw3 {
        long l = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-widening-4 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52bw4 {
        float f = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-widening-5 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52bw5 {
        double d = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-narrowing-1 { Assignment conversion may narrow if
        a constant expression of type byte is assigned to a variable
        of type char } {
    empty_main T52bn1 {
        char c = (byte) 1;
    }
} PASS

tcltest::test 5.2-byte-narrowing-error-1 { Assignment conversion
        may not narrow if the expression is non-constant } {
    empty_main T52bne1 {
        byte b = (byte) 1;
        char c = b;
    }
} FAIL

tcltest::test 5.2-byte-narrowing-error-2 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52bne2 {
        char c = (byte) -1;
    }
} FAIL

# short

tcltest::test 5.2-short-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52si1 {
        short s = (short) 1;
    }
} PASS

tcltest::test 5.2-short-widening-1 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52sw1 {
        int i = (short) 1;
    }
} PASS

tcltest::test 5.2-short-widening-2 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52sw2 {
        long l = (short) 1;
    }
} PASS

tcltest::test 5.2-short-widening-3 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52sw3 {
        float f = (short) 1;
    }
} PASS

tcltest::test 5.2-short-widening-4 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52sw4 {
        double d = (short) 1;
    }
} PASS

tcltest::test 5.2-short-narrowing-1 { Assignment conversion may narrow if
        a constant expression of type short is assigned to a variable
        of type byte } {
    empty_main T52sn1 {
        byte b = (short) 1;
    }
} PASS

tcltest::test 5.2-short-narrowing-2 { Assignment conversion may narrow if
        a constant expression of type short is assigned to a variable
        of type char } {
    empty_main T52sn2 {
        char c = (short) 1;
    }
} PASS

tcltest::test 5.2-short-narrowing-error-1 { Assignment conversion
        may not narrow if the expression is non-constant } {
    empty_main T52sne1 {
        short s = (short) 1;
        char c = s;
    }
} FAIL

tcltest::test 5.2-short-narrowing-error-2 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52sne2 {
        char c = (short) -1;
    }
} FAIL

tcltest::test 5.2-short-narrowing-error-3 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52sne3 {
        byte b = (short) 128;
    }
} FAIL


# char

tcltest::test 5.2-char-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52ci1 {
        char c = '1';
    }
} PASS

tcltest::test 5.2-char-widening-1 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52cw1 {
        int i = '1';
    }
} PASS

tcltest::test 5.2-char-widening-2 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52cw2 {
        long l = '1';
    }
} PASS

tcltest::test 5.2-char-widening-3 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52cw3 {
        float f = '1';
    }
} PASS

tcltest::test 5.2-char-widening-4 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52cw4 {
        double d = '1';
    }
} PASS

tcltest::test 5.2-char-narrowing-1 { Assignment conversion may narrow if
        a constant expression of type char is assigned to a variable
        of type byte } {
    empty_main T52cn1 {
        byte b = '1';
    }
} PASS

tcltest::test 5.2-char-narrowing-2 { Assignment conversion may narrow if
        a constant expression of type char is assigned to a variable
        of type short } {
    empty_main T52cn2 {
        short s = '1';
    }
} PASS

tcltest::test 5.2-char-narrowing-error-1 { Assignment conversion
        may not narrow if the expression is non-constant } {
    empty_main T52cne1 {
        char c = '1';
        byte b = c;
    }
} FAIL

tcltest::test 5.2-char-narrowing-error-2 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52cne2 {
        byte b = '\u0080';
    }
} FAIL

tcltest::test 5.2-char-narrowing-error-3 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52cne3 {
        short s = '\u8000';
    }
} FAIL

# int

tcltest::test 5.2-int-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52ii1 {
        int i = 1;
    }
} PASS

tcltest::test 5.2-int-widening-1 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52iw1 {
        long l = 1;
    }
} PASS

tcltest::test 5.2-int-widening-2 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52iw2 {
        float f = 1;
    }
} PASS

tcltest::test 5.2-int-widening-3 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52iw3 {
        double d = 1;
    }
} PASS

tcltest::test 5.2-int-narrowing-1 { Assignment conversion may narrow if
        a constant expression of type int is assigned to a variable
        of type byte } {
    empty_main T52in1 {
        byte b = 1;
    }
} PASS

tcltest::test 5.2-int-narrowing-2 { Assignment conversion may narrow if
        a constant expression of type int is assigned to a variable
        of type short } {
    empty_main T52in2 {
        short s = 1;
    }
} PASS

tcltest::test 5.2-int-narrowing-3 { Assignment conversion may narrow if
        a constant expression of type int is assigned to a variable
        of type char } {
    empty_main T52in3 {
        char c = 1;
    }
} PASS

tcltest::test 5.2-int-narrowing-error-1 { Assignment conversion
        may not narrow if the expression is non-constant } {
    empty_main T52ine1 {
        int i = 1;
        byte b = i;
    }
} FAIL

tcltest::test 5.2-int-narrowing-error-2 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52ine2 {
        byte b = -129;
    }
} FAIL

tcltest::test 5.2-int-narrowing-error-3 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52ine3 {
        short s = 32768;
    }
} FAIL

tcltest::test 5.2-int-narrowing-error-4 { Assignment conversion
        may not narrow if the expression is not representable in the type
        of the variable } {
    empty_main T52ine4 {
        char c = -1;
    }
} FAIL

# long

tcltest::test 5.2-long-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52li1 {
        long l = 1L;
    }
} PASS

tcltest::test 5.2-long-widening-1 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52lw1 {
        float f = 1L;
    }
} PASS

tcltest::test 5.2-long-widening-2 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52lw2 {
        double d = 1L;
    }
} PASS

tcltest::test 5.2-long-narrowing-error-1 { Assignment conversion may
        not narrow an expression of type long } {
    empty_main T52lne1 {
        byte b = 1L;
    }
} FAIL

tcltest::test 5.2-long-narrowing-error-2 { Assignment conversion may
        not narrow an expression of type long } {
    empty_main T52lne2 {
        short s = 1L;
    }
} FAIL

tcltest::test 5.2-long-narrowing-error-3 { Assignment conversion may
        not narrow an expression of type long } {
    empty_main T52lne3 {
        char c = 1L;
    }
} FAIL

tcltest::test 5.2-long-narrowing-error-4 { Assignment conversion may
        not narrow an expression of type long } {
    empty_main T52lne4 {
        int i = 1L;
    }
} FAIL

# float

tcltest::test 5.2-float-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52fi1 {
        float f = 1.0F;
    }
} PASS

tcltest::test 5.2-float-widening-1 { Assignment contexts allow the use
        of a widening primitive conversion } {
    empty_main T52fw1 {
        double d = 1.0F;
    }
} PASS

tcltest::test 5.2-float-narrowing-error-1 { Assignment conversion may
        not narrow an expression of type float } {
    empty_main T52fne1 {
        byte b = 1.0F;
    }
} FAIL

tcltest::test 5.2-float-narrowing-error-2 { Assignment conversion may
        not narrow an expression of type float } {
    empty_main T52fne2 {
        short s = 1.0F;
    }
} FAIL

tcltest::test 5.2-float-narrowing-error-3 { Assignment conversion may
        not narrow an expression of type float } {
    empty_main T52fne3 {
        char c = 1.0F;
    }
} FAIL

tcltest::test 5.2-float-narrowing-error-4 { Assignment conversion may
        not narrow an expression of type float } {
    empty_main T52fne4 {
        int i = 1.0F;
    }
} FAIL

tcltest::test 5.2-float-narrowing-error-5 { Assignment conversion may
        not narrow an expression of type float } {
    empty_main T52fne5 {
        long l = 1.0F;
    }
} FAIL

# double

tcltest::test 5.2-double-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52di1 {
        double d = 1.0D;
    }
} PASS

tcltest::test 5.2-double-narrowing-error-1 { Assignment conversion may
        not narrow an expression of type double } {
    empty_main T52dne1 {
        byte b = 1.0D;
    }
} FAIL

tcltest::test 5.2-double-narrowing-error-2 { Assignment conversion may
        not narrow an expression of type double } {
    empty_main T52dne2 {
        short s = 1.0D;
    }
} FAIL

tcltest::test 5.2-double-narrowing-error-3 { Assignment conversion may
        not narrow an expression of type double } {
    empty_main T52dne3 {
        char c = 1.0D;
    }
} FAIL

tcltest::test 5.2-double-narrowing-error-4 { Assignment conversion may
        not narrow an expression of type double } {
    empty_main T52dne4 {
        int i = 1.0D;
    }
} FAIL

tcltest::test 5.2-double-narrowing-error-5 { Assignment conversion may
        not narrow an expression of type double } {
    empty_main T52dne5 {
        long l = 1.0D;
    }
} FAIL

tcltest::test 5.2-double-narrowing-error-6 { Assignment conversion may
        not narrow an expression of type double } {
    empty_main T52dne6 {
        float f = 1.0D;
    }
} FAIL

# FIXME: Add checks for overflow in float or double as described in 5.2

# boolean

tcltest::test 5.2-boolean-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52booli1 {
        boolean b = true;
    }
} PASS

tcltest::test 5.2-boolean-error-1 { Assignment conversion cannot
        assign a boolean type to another type } {
    empty_main T52boole1 {
        int i = true;
    }
} FAIL

# reference

tcltest::test 5.2-reference-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_main T52ri1 {
        String s = "";
    }
} PASS

tcltest::test 5.2-reference-widening-1 { Assignment contexts allow the use
        of a widening reference conversion } {
    empty_main T52rw1 {
        Object o = "";
    }
} PASS

tcltest::test 5.2-reference-widening-2 { Assignment contexts allow the use
        of a widening reference conversion } {
    empty_main T52rw2 {
        Object o = null;
    }
} PASS

tcltest::test 5.2-reference-error-1 { Assignment conversion cannot
        assign a primitive type to a reference variable } {
    empty_main T52re1 {
        Object o = 0.0D;
    }
} FAIL

tcltest::test 5.2-reference-error-2 { Assignment conversion cannot narrow } {
    empty_main T52re2 {
        String s = new Object();
    }
} FAIL

tcltest::test 5.2-reference-error-3 { Assignment conversion cannot
        assign a different type to a reference variable } {
    empty_main T52re3 {
        String s = new Integer();
    }
} FAIL

# interface
tcltest::test 5.2-interface-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_class T52ii1 {
        void foo(Cloneable c) {
            Cloneable c1 = c;
        }
    }
} PASS

tcltest::test 5.2-interface-widening-1 { Assignment contexts allow the use
        of a widening reference conversion } {
    empty_class T52iw1 {
        interface I extends Cloneable {}
        void foo(I i) {
            Cloneable c = i;
        }
    }
} PASS

tcltest::test 5.2-interface-widening-2 { Assignment contexts allow the use
        of a widening reference conversion } {
    empty_main T52iw2 {
        Cloneable c = null;
    }
} PASS

tcltest::test 5.2-interface-error-1 { Assignment conversion cannot
        assign a primitive type to a reference variable } {
    empty_main T52ie1 {
        Cloneable c = 0.0D;
    }
} FAIL

tcltest::test 5.2-interface-error-2 { Assignment conversion cannot narrow } {
    empty_main T52ie2 {
        Cloneable c = "";
    }
} FAIL

tcltest::test 5.2-interface-error-3 { Assignment conversion cannot
        assign a different type to a reference variable } {
    empty_main T52ie3 {
        static final class C {}
        Cloneable c = new C();
    }
} FAIL

# array
tcltest::test 5.2-array-identity-1 { Assignment contexts allow the use
        of an identity conversion } {
    empty_class T52ai1 {
        void foo(String[] s) {
            String[] s1 = s;
        }
    }
} PASS

tcltest::test 5.2-array-widening-1 { Assignment contexts allow the use
        of a widening reference conversion } {
    empty_class T52aw1 {
        void foo(String[] s) {
            Object[] o = s;
        }
    }
} PASS

tcltest::test 5.2-array-widening-2 { Assignment contexts allow the use
        of a widening reference conversion } {
    empty_main T52aw2 {
        Object[] o = null;
    }
} PASS

tcltest::test 5.2-array-error-1 { Assignment conversion cannot
        assign a primitive type to a reference variable } {
    empty_main T52ae1 {
        Object[] o = 0.0D;
    }
} FAIL

tcltest::test 5.2-array-error-2 { Assignment conversion cannot narrow } {
    empty_main T52ae2 {
        String[] s = new Object[0];
    }
} FAIL

tcltest::test 5.2-array-error-3 { Assignment conversion cannot
        assign a different type to a reference variable } {
    empty_main T52ae3 {
        String[] s = new Integer[0];
    }
} FAIL

