tcltest::test 15.21-assoc-1 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a1 {1 == 2 == 0}
} FAIL

tcltest::test 15.21-assoc-2 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a2 {1 != 2 != 0}
} FAIL

tcltest::test 15.21-assoc-3 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a3 {1 == 2 == false} {1 != 2 == true} \
	    {1 == 2 != true} {1 != 2 != false}
} PASS

tcltest::test 15.21-assoc-4 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a4 {(1 == 2) == false} {(1 != 2) == true} \
	    {(1 == 2) != true} {(1 != 2) != false}
} PASS

tcltest::test 15.21-assoc-5 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a5 {true == false == false} \
	    {true != false == true} \
	    {true == false != true} \
	    {true != false != false}
} PASS

tcltest::test 15.21-assoc-6 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a6 {(true == false) == false} \
	    {(true != false) == true} \
	    {(true == false) != true} \
	    {(true != false) != false}
} PASS

tcltest::test 15.21-assoc-7 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a7 {"1" == "2" == false} {"1" != "2" == true} \
	    {"1" == "2" != true} {"1" != "2" != false}
} PASS

tcltest::test 15.21-assoc-8 { a==b==c is (a==b)==c, and hence c must be
        boolean } {
    constant_expression T1521a8 {("1" == "2") == false} {("1" != "2") == true} \
	    {("1" == "2") != true} {("1" != "2") != false}
} PASS

tcltest::test 15.21-assoc-9 { a==b==c is (a==b)==c } {
    constant_expression T1521a9 {1 == (0 == false)}
} FAIL

tcltest::test 15.21-precedence-1 { == is lower than < } {
    constant_expression T1521p1 {1 < 2 == 2 < 3} {(1 < 2) == (2 < 3)}
} PASS

tcltest::test 15.21-precedence-2 { == is lower than < } {
    constant_expression T1521p2 {1 < (2 == 2) < 3}
} FAIL

tcltest::test 15.21-precedence-3 { == is lower than < } {
    constant_expression T1521p3 {((1 < 2) == 2) < 3}
} FAIL

tcltest::test 15.21-equal-1 { a!=b is !(a==b) } {
    constant_expression T1521e1 {(1!=2) == (!(1==2))} {(1!=1) == (!(1==1))}
} PASS

tcltest::test 15.21-equal-2 { a!=b is !(a==b) } {
    constant_expression T1521e2 {(true!=true) == (!(true==true))} \
	    {(true!=false) == (!(true==false))}
} PASS

tcltest::test 15.21-equal-3 { a!=b is !(a==b) } {
    constant_expression T1521e3 {("1"!="1") == (!("1"=="1"))} \
	    {("1"!="2") == (!("1"=="2"))}
} PASS

tcltest::test 15.21-type-1 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t1 {
	boolean b = true == 1;
    }
} FAIL

tcltest::test 15.21-type-2 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t2 {
	boolean b = true == 1L;
    }
} FAIL

tcltest::test 15.21-type-3 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t3 {
	boolean b = true == '1';
    }
} FAIL

tcltest::test 15.21-type-4 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t4 {
	boolean b = true == 1f;
    }
} FAIL

tcltest::test 15.21-type-5 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t5 {
	boolean b = true == 1.;
    }
} FAIL

tcltest::test 15.21-type-6 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t6 {
	boolean b = true == "1";
    }
} FAIL

tcltest::test 15.21-type-7 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t7 {
	boolean b = true == null;
    }
} FAIL

tcltest::test 15.21-type-8 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t8 {
	boolean b = true == System.out.println();
    }
} FAIL

tcltest::test 15.21-type-9 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t9 {
	boolean b = true == new Boolean(true);
    }
} FAIL

tcltest::test 15.21-type-10 { in a==b, if a is boolean, b must be too } {
    empty_main T1521t10 {
	boolean b = true == false;
    }
} PASS

tcltest::test 15.21-type-11 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t11 {
	boolean b = 1 == 1;
    }
} PASS

tcltest::test 15.21-type-12 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t12 {
	boolean b = 1 == 1L;
    }
} PASS

tcltest::test 15.21-type-13 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t13 {
	boolean b = 1 == '1';
    }
} PASS

tcltest::test 15.21-type-14 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t14 {
	boolean b = 1 == 1f;
    }
} PASS

tcltest::test 15.21-type-15 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t15 {
	boolean b = 1 == 1.;
    }
} PASS

tcltest::test 15.21-type-16 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t16 {
	boolean b = 1 == "1";
    }
} FAIL

tcltest::test 15.21-type-17 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t17 {
	boolean b = 1 == null;
    }
} FAIL

tcltest::test 15.21-type-18 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t18 {
	boolean b = 1 == System.out.println();
    }
} FAIL

tcltest::test 15.21-type-19 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t19 {
	boolean b = 1 == new Integer(1);
    }
} FAIL

tcltest::test 15.21-type-20 { in a==b, if a is numeric, b must be too } {
    empty_main T1521t20 {
	boolean b = 1 == true;
    }
} FAIL

tcltest::test 15.21-type-21 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t21 {
	boolean b = null == 1;
    }
} FAIL

tcltest::test 15.21-type-22 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t22 {
	boolean b = null == 1L;
    }
} FAIL

tcltest::test 15.21-type-23 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t23 {
	boolean b = null == '1';
    }
} FAIL

tcltest::test 15.21-type-24 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t24 {
	boolean b = null == 1f;
    }
} FAIL

tcltest::test 15.21-type-25 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t25 {
	boolean b = null == 1.;
    }
} FAIL

tcltest::test 15.21-type-26 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t26 {
	boolean b = null == "1";
    }
} PASS

tcltest::test 15.21-type-27 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t27 {
	boolean b = null == null;
    }
} PASS

tcltest::test 15.21-type-28 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t28 {
	boolean b = null == System.out.println();
    }
} FAIL

tcltest::test 15.21-type-29 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t29 {
	boolean b = null == new Boolean(true);
    }
} PASS

tcltest::test 15.21-type-30 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t30 {
	boolean b = null == false;
    }
} FAIL

tcltest::test 15.21-type-31 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t31 {
	boolean b = "1" == 1;
    }
} FAIL

tcltest::test 15.21-type-32 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t32 {
	boolean b = "1" == 1L;
    }
} FAIL

tcltest::test 15.21-type-33 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t33 {
	boolean b = "1" == '1';
    }
} FAIL

tcltest::test 15.21-type-34 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t34 {
	boolean b = "1.0" == 1f;
    }
} FAIL

tcltest::test 15.21-type-35 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t35 {
	boolean b = "1.0" == 1.;
    }
} FAIL

tcltest::test 15.21-type-36 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t36 {
	boolean b = "1" == "1";
    }
} PASS

tcltest::test 15.21-type-37 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t37 {
	boolean b = "null" == null;
    }
} PASS

tcltest::test 15.21-type-38 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t38 {
	boolean b = "" == System.out.println();
    }
} FAIL

tcltest::test 15.21-type-39 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t39 {
	boolean b = new Object() == new Integer(1);
    }
} PASS

tcltest::test 15.21-type-40 { in a==b, if a is a reference, b must be too } {
    empty_main T1521t40 {
	boolean b = "true" == true;
    }
} FAIL

tcltest::test 15.21-type-41 { void==void is not valid } {
    empty_main T1521t41 {
	boolean b = System.out.println() == System.out.println();
    }
} FAIL
