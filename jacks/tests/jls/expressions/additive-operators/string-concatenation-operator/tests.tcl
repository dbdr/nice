tcltest::test 15.18.1-1 { String + requires a String argument } {
    # Sun seems to have relaxed this so that null is used as type String implicitly
    is_assignable_to T151811 String {null + 1} String {1 + null}
} FAIL

tcltest::test 15.18.1-2 { (String)null denotes String concatenation } {
    is_assignable_to T151812 String {(String)null + 1} String {1 + (String)null}
} PASS

tcltest::test 15.18.1-3 { (String)null + null denotes String concatenation } {
    is_assignable_to T151813 \
        String {null + (String)null} \
        String {(String)null + null} \
        String {(String)null + (String)null}
} PASS

tcltest::test 15.18.1-4 { void + String is invalid } {
    is_assignable_to T151814 \
        String {System.out.println() + ""} \
        String {"" + System.out.println()}
} FAIL

tcltest::test 15.18.1-5 { + operator should be left-associative } {
    constant_expression T151815 \
        {1 + 2 + " fiddlers" == "3 fiddlers"} \
        {"fiddlers " + 1 + 2 == "fiddlers 12"}
} PASS

tcltest::test 15.18.1-6 { + operator and integer constants } {
    constant_expression T151816 \
        {"-12300000" == "" + -12300000} \
        {"-12300000,-50000000" ==
            -12300000 + "," + -50000000}
} PASS

tcltest::test 15.18.1-7 { + operator and long constants } {
    constant_expression T151817 \
        {"-12192592592745" == "" + -12192592592745L} \
        {"-12192592592745,-12192592592745" ==
            -12192592592745L + "," + -12192592592745L}
} PASS

tcltest::test 15.18.1-float-1 { convert denormalized float to String } {
    constant_expression T15181f1 {"" + 1e-44f == "9.8E-45"}
} PASS

tcltest::test 15.18.1-float-2 { convert NaN float to String } {
    constant_expression T15181f2 {"" + Float.NaN == "NaN"} {"" + (-Float.NaN) == "NaN"}
} PASS

tcltest::test 15.18.1-float-3 { convert Infinity float to String } {
    constant_expression T15181f3 {"" + Float.NEGATIVE_INFINITY == "-Infinity"} \
                                 {"" + Float.POSITIVE_INFINITY == "Infinity"}
} PASS

tcltest::test 15.18.1-float-4 { convert min float to String } {
    constant_expression T15181f4 {"" +  Float.MIN_VALUE == "1.4E-45"}
} PASS

tcltest::test 15.18.1-float-5 { convert max float to String } {
    constant_expression T15181f5 {"" +  Float.MAX_VALUE == "3.4028235E38"}
} PASS

tcltest::test 15.18.1-float-6 { conversion of float to String must use as few
        digits after decimal as produce the same float again by rounding } {
    constant_expression T15181f6 \
            {"" + 123456768f == "1.2345677E8"} \
            {"" + 123456776f == "1.23456776E8"} \
            {"" + 123456784f == "1.2345678E8"} \
            {"" + 123456792f == "1.2345679E8"} \
            {"" + 123456800f == "1.234568E8"}
} PASS

tcltest::test 15.18.1-float-7 { conversion of float to String switches from
        plain to exponent at 1e7 } {
    constant_expression T15181f7 \
            {"" + 9999999f == "9999999.0"} \
            {"" + 10000000f == "1.0E7"}
} PASS

tcltest::test 15.18.1-float-8 { conversion of float to String switches from
        plain to exponent at 1e-3. Trailing 0's are not permitted. } {
    constant_expression T15181f8 \
            {"" + .001f == "0.001"} \
            {"" + .000999998f == "9.99998E-4"}
} PASS

# Note - this test follows the spirit, not the letter, of the specs in
# java.lang.Float.toString(), by checking the shortest string.  The
# algorithm, as listed, expects 9.9999998E22.
tcltest::test 15.18.1-float-9 { conversion of float to String produces the
        shortest string that parses back to the same float } {
    constant_expression T15181f9 {"" + 9.9999998e22f == "1.0E23"}
} PASS

tcltest::test 15.18.1-float-10 { One of the longest possible Strings
        possible when converting floats } {
    constant_expression T15181f10 \
            {"" + -1.00000126e-10f == "-1.00000126E-10"}
} PASS

tcltest::test 15.18.1-float-11 { conversion of float to String prints the
        closest decimal string to the exact value, even on denorms } {
    constant_expression T15181f11 \
            {"" + 1e-43f == "9.9E-44"}
} PASS

tcltest::test 15.18.1-double-1 { convert denormalized double to String } {
    constant_expression T15181d1 {"" + 1e-315 == "1.0E-315"}
} PASS

tcltest::test 15.18.1-double-2 { convert Nan double to String } {
    constant_expression T15181d2 {"" + Double.NaN == "NaN"} {"" + (-Double.NaN) == "NaN"}
} PASS

tcltest::test 15.18.1-double-3 { convert Infinity double to String } {
    constant_expression T15181d3 {"" + Double.NEGATIVE_INFINITY == "-Infinity"} \
                                 {"" + Double.POSITIVE_INFINITY == "Infinity"}
} PASS

tcltest::test 15.18.1-double-4 { convert min double to String } {
    constant_expression T15181d4 {"" + Double.MIN_VALUE == "4.9E-324"}
} PASS

tcltest::test 15.18.1-double-5 { convert max double to String } {
    constant_expression T15181d5 {"" + Double.MAX_VALUE == "1.7976931348623157E308"}
} PASS

tcltest::test 15.18.1-double-6 { NaN != 12.0 } {
    constant_expression T15181d6 {"" + Double.NaN == "NaN"} \
                                 {"" + 12.0 == "12.0"}
} PASS

tcltest::test 15.18.1-double-7 { NaN != 12.0 } {
    constant_expression T15181d7 {"" + 12.0 == "12.0"} \
                                 {"" + Double.NaN == "NaN"}
} PASS

tcltest::test 15.18.1-double-8 { conversion of double to String must use as few
        digits after decimal as produce the same double again by rounding } {
    constant_expression T15181d8 \
            {"" + 40000000000000000. == "4.0E16"} \
            {"" + 40000000000000008. == "4.000000000000001E16"} \
            {"" + 40000000000000016. == "4.000000000000002E16"} \
            {"" + 40000000000000024. == "4.0000000000000024E16"} \
            {"" + 40000000000000032. == "4.000000000000003E16"} \
            {"" + 40000000000000040. == "4.000000000000004E16"}
} PASS

tcltest::test 15.18.1-double-9 { conversion of double to String switches from
        plain to exponent at 1e7 } {
    constant_expression T15181d9 \
            {"" + 9999999.999999998 == "9999999.999999998"} \
            {"" + 10000000.0 == "1.0E7"}
} PASS

tcltest::test 15.18.1-double-10 { conversion of double to String switches from
        plain to exponent at 1e-3. Trailing 0's are not permitted. } {
    constant_expression T15181d10 \
            {"" + .001 == "0.001"} \
            {"" + .0009999999999999998 == "9.999999999999998E-4"}
} PASS

# Note - this test follows the spirit, not the letter, of the specs in
# java.lang.Double.toString(), by checking the shortest string.  The
# algorithm, as listed, expects 9.999999999999999E22.
tcltest::test 15.18.1-double-11 { conversion of double to String produces the
        shortest string that parses back to the same double } {
    constant_expression T15181d11 \
            {"" + 9.999999999999999e22 == "1.0E23"}
} PASS

tcltest::test 15.18.1-double-12 { One of the longest possible Strings
        possible when converting doubles } {
    constant_expression T15181d12 \
            {"" + -1.2345678912345678e-200 == "-1.2345678912345678E-200"}
} PASS

tcltest::test 15.18.1-double-13 { conversion of double to String prints the
        closest decimal string to the exact value, even on denorms } {
    constant_expression T15181d13 \
            {"" + 1e-323 == "9.9E-324"} \
            {"" + 1e-322 == "9.9E-323"}
} PASS

tcltest::test 15.18.1-boolean-1 { conversion of boolean to String } {
    constant_expression T15181b1 \
            {"" + true == "true"} \
            {"" + false == "false"}
} PASS

tcltest::test 15.18.1-boolean-2 { conversion of boolean to String } {
    empty_main T15181b2 {
        String s1 = "" + true;
        String s2 = "" + false;
    }
} PASS

tcltest::test 15.18.1-valid-1 { String + null type is valid } {
    empty_main T15181v1 {
        boolean b = true;
        String s = "" + (b ? null : null);
    }
} PASS

tcltest::test 15.18.1-valid-2 { String + primitive is valid } {
    empty_main T15181v2 {
        boolean b = true;
        String s1 = "" + b;
        String s2 = b + "";
    }
} PASS

tcltest::test 15.18.1-valid-3 { String + primitive is valid } {
    empty_main T15181v3 {
        byte b = 1;
        String s1 = "" + b;
        String s2 = b + "";
    }
} PASS

tcltest::test 15.18.1-valid-4 { String + primitive is valid } {
    empty_main T15181v4 {
        short s = 1;
        String s1 = "" + s;
        String s2 = s + "";
    }
} PASS

tcltest::test 15.18.1-valid-5 { String + primitive is valid } {
    empty_main T15181v5 {
        char c = 1;
        String s1 = "" + c;
        String s2 = c + "";
    }
} PASS

tcltest::test 15.18.1-valid-6 { String + primitive is valid } {
    empty_main T15181v6 {
        int i = 1;
        String s1 = "" + i;
        String s2 = i + "";
    }
} PASS

tcltest::test 15.18.1-valid-7 { String + primitive is valid } {
    empty_main T15181v7 {
        long l = 1;
        String s1 = "" + l;
        String s2 = l + "";
    }
} PASS

tcltest::test 15.18.1-valid-8 { String + primitive is valid } {
    empty_main T15181v8 {
        float f = 1;
        String s1 = "" + f;
        String s2 = f + "";
    }
} PASS

tcltest::test 15.18.1-valid-9 { String + primitive is valid } {
    empty_main T15181v9 {
        double d = 1;
        String s1 = "" + d;
        String s2 = d + "";
    }
} PASS

tcltest::test 15.18.1-non-const-1 { runtime concatenation } {
    empty_main T15181nc1 {
byte n = 0;
String s = "s" + n;
    }
} PASS

tcltest::test 15.18.1-non-const-2 { runtime concatenation } {
    empty_main T15181nc2 {
        String s1 = "C";
        String s2 = "A" + "B" + s1;
    }
} PASS

tcltest::test 15.18.1-non-const-3 { runtime concatenation } {
    empty_main T15181nc3 {
        String s1 = "C";
        String s2 = "A" + ("B" + s1);
    }
} PASS

tcltest::test 15.18.1-non-const-4 { runtime concatenation } {
    empty_main T15181nc4 {
        String s1 = "C";
        int n = 10;
        String s2 = "A" + n + s1;
    }
} PASS

tcltest::test 15.18.1-non-const-5 { runtime concatenation } {
    empty_main T15181nc5 {
        String s1 = "C";
        int n = 10;
        String s2 = "A" + (n + s1);
    }
} PASS

tcltest::test 15.18.1-non-const-6 { runtime concatenation } {
    empty_main T15181nc6 {
        String s1 = "C";
        int n = 10;
        String s2 = n + ("B" + s1);
    }
} PASS

tcltest::test 15.18.1-non-const-7 { concatenation of empty string } {
    empty_main T15181nc7 {
        String s1 = "C";
        String s2 = "" + s1;
    }
} PASS

tcltest::test 15.18.1-non-const-8 { concatenation of empty string } {
    empty_main T15181nc8 {
        int i = 10;
        String s2 = "" + i;
    }
} PASS

tcltest::test 15.18.1-non-const-9 { concatenation of empty string } {
    empty_main T15181nc9 {
        int i = 10;
        String s2 = "" + i + "";
    }
} PASS

tcltest::test 15.18.1-non-const-10 { jikes had an optimization of string appends
        which resulted in an assertion failure. Bug id 2919. } {
    empty_class T15181nc10 {
	private final static String s = val() + 'a' + "bcde";
	static String val () { return null; }
    }
} PASS
