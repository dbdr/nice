tcltest::test 15.17-null-1 { null is not a primitive numeric type } {
    empty_class T1517null1 {int i = 2*null;}
} FAIL

tcltest::test 15.17-null-2 { null is not a primitive numeric type } {
    empty_class T1517null2 {int i = 2/null;}
} FAIL

tcltest::test 15.17-null-3 { null is not a primitive numeric type } {
    empty_class T1517null3 {int i = 2%null;}
} FAIL

tcltest::test 15.17-boolean-1 { boolean is not a primitive numeric type } {
    empty_class T1517boolean1 {int i = 2*true;}
} FAIL

tcltest::test 15.17-boolean-2 { boolean is not a primitive numeric type } {
    empty_class T1517boolean2 {int i = 2/true;}
} FAIL

tcltest::test 15.17-boolean-3 { boolean is not a primitive numeric type } {
    empty_class T1517boolean3 {int i = 2%true;}
} FAIL

tcltest::test 15.17-object-1 { Object is not a primitive numeric type } {
    empty_class T1517object1 {int i = 2*(new Object());}
} FAIL

tcltest::test 15.17-object-2 { Object is not a primitive numeric type } {
    empty_class T1517object2 {int i = 2/(new Object());}
} FAIL

tcltest::test 15.17-object-3 { Object is not a primitive numeric type } {
    empty_class T1517object3 {int i = 2%(new Object());}
} FAIL

tcltest::test 15.17-void-1 { void is not a primitive numeric type } {
    empty_class T1517void1 {int i = 2*System.out.println();}
} FAIL

tcltest::test 15.17-void-2 { void is not a primitive numeric type } {
    empty_class T1517void2 {int i = 2/System.out.println();}
} FAIL

tcltest::test 15.17-void-3 { void is not a primitive numeric type } {
    empty_class T1517void3 {int i = 2%System.out.println();}
} FAIL

tcltest::test 15.17-mult-1 { * is a multiplicative operator } {
    constant_expression T1517mult1 {2*2 == 4}
} PASS

tcltest::test 15.17-mult-2 { * is a multiplicative operator } {
    constant_expression T1517mult2 {2L*2L == 4L}
} PASS

tcltest::test 15.17-mult-3 { * is a multiplicative operator } {
    constant_expression T1517mult3 {2.0F * 2.0F == 4.0F}
} PASS

tcltest::test 15.17-mult-4 { * is a multiplicative operator } {
    constant_expression T1517mult4 {2.0D * 2.0D == 4.0D}
} PASS

tcltest::test 15.17-div-1 { / is a multiplicative operator } {
    constant_expression T1517div1 {2/2 == 1}
} PASS

tcltest::test 15.17-div-2 { / is a multiplicative operator } {
    constant_expression T1517div2 {2L/2L == 1L}
} PASS

tcltest::test 15.17-div-3 { / is a multiplicative operator } {
    constant_expression T1517div3 {2.0F / 2.0F == 1.0F}
} PASS

tcltest::test 15.17-div-4 { / is a multiplicative operator } {
    constant_expression T1517div4 {2.0D / 2.0D == 1.0D}
} PASS

tcltest::test 15.17-mod-1 { % is a multiplicative operator } {
    constant_expression T1517mod1 {2%2 == 0}
} PASS

tcltest::test 15.17-mod-2 { % is a multiplicative operator } {
    constant_expression T1517mod2 {2L%2L == 0L}
} PASS

tcltest::test 15.17-mod-3 { % is a multiplicative operator } {
    constant_expression T1517mod3 {2.0F % 2.0F == 0.0F}
} PASS

tcltest::test 15.17-mod-4 { % is a multiplicative operator } {
    constant_expression T1517mod4 {2.0D % 2.0D == 0.0D}
} PASS

# Check simple type promotion for multiplicative operands.

tcltest::test 15.17-type-1 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type1 int {1 * 2L}
} FAIL

tcltest::test 15.17-type-2 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type2 long {1 * 2L}
} PASS

tcltest::test 15.17-type-3 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type3 int {1 / 2L}
} FAIL

tcltest::test 15.17-type-4 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type4 long {1 / 2L}
} PASS

tcltest::test 15.17-type-5 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type5 int {1 % 2L}
} FAIL

tcltest::test 15.17-type-6 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type6 long {1 % 2L}
} PASS

tcltest::test 15.17-type-7 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type7 float {1f * 2D}
} FAIL

tcltest::test 15.17-type-8 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type8 double {1f * 2D}
} PASS

tcltest::test 15.17-type-9 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type9 float {1f / 2D}
} FAIL

tcltest::test 15.17-type-10 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type10 double {1f / 2D}
} PASS

tcltest::test 15.17-type-11 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type11 float {1f % 2D}
} FAIL

tcltest::test 15.17-type-12 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type12 double {1f % 2D}
} PASS

tcltest::test 15.17-type-13 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type13 int {1 * 2D}
} FAIL

tcltest::test 15.17-type-14 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type14 double {1 * 2D}
} PASS

tcltest::test 15.17-type-15 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type15 int {1 / 2D}
} FAIL

tcltest::test 15.17-type-16 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type16 double {1 / 2D}
} PASS

tcltest::test 15.17-type-17 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type17 int {1 % 2D}
} FAIL

tcltest::test 15.17-type-18 { The type of a multiplicative
        expression is the promoted type of its operands. } {
    is_assignable_to T1517type18 double {1 % 2D}
} PASS
