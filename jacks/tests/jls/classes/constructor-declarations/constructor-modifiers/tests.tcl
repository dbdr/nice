tcltest::test 8.8.3-modifiers-1 { ConstructorModifier can
        be public, protected, or private } {
    empty_class T883m1 "public T883m1() {}"
} PASS

tcltest::test 8.8.3-modifiers-2 { ConstructorModifier can
        be public, protected, or private } {
    empty_class T883m2 "protected T883m2() {}"
} PASS

tcltest::test 8.8.3-modifiers-3 { ConstructorModifier can
        be public, protected, or private } {
    empty_class T883m3 "private T883m3() {}"
} PASS


tcltest::test 8.8.3-duplicate-modifiers-1 { A compile-time
        error occurs if the same modifier appears more than
       once in a constructor declaration } {
    empty_class T883dm1 "public public T883dm1() {}"
} FAIL

tcltest::test 8.8.3-duplicate-modifiers-2 { A compile-time
        error occurs if the same modifier appears more than
       once in a constructor declaration } {
    empty_class T883dm2 "protected protected T883dm2() {}"
} FAIL

tcltest::test 8.8.3-duplicate-modifiers-3 { A compile-time
        error occurs if the same modifier appears more than
       once in a constructor declaration } {
    empty_class T883dm3 "private private T883dm3() {}"
} FAIL


tcltest::test 8.8.3-multiple-modifiers-1 { A compile-time
        error occurs if a constructor declaration has more
        than one of  public, protected, and private } {
    empty_class T883mm1 "public protected T883mm1() {}"
} FAIL

tcltest::test 8.8.3-multiple-modifiers-2 { A compile-time
        error occurs if a constructor declaration has more
        than one of  public, protected, and private } {
    empty_class T883mm2 "public private T883mm2() {}"
} FAIL

tcltest::test 8.8.3-multiple-modifiers-3 { A compile-time
        error occurs if a constructor declaration has more
        than one of  public, protected, and private } {
    empty_class T883mm3 "protected public T883mm3() {}"
} FAIL

tcltest::test 8.8.3-multiple-modifiers-4 { A compile-time
        error occurs if a constructor declaration has more
        than one of  public, protected, and private } {
    empty_class T883mm4 "protected private T883mm4() {}"
} FAIL

tcltest::test 8.8.3-multiple-modifiers-5 { A compile-time
        error occurs if a constructor declaration has more
        than one of  public, protected, and private } {
    empty_class T883mm5 "private public T883mm5() {}"
} FAIL

tcltest::test 8.8.3-multiple-modifiers-6 { A compile-time
        error occurs if a constructor declaration has more
        than one of  public, protected, and private } {
    empty_class T883mm6 "private protected T883mm6() {}"
} FAIL



tcltest::test 8.8.3-invalid-modifiers-1 { A constructor
        cannot be abstract, static, final, native, strictfp,
        or synchronized } {
    empty_class T883im1 "abstract T883im1() {}"
} FAIL

tcltest::test 8.8.3-invalid-modifiers-2 { A constructor
        cannot be abstract, static, final, native, strictfp,
        or synchronized } {
    empty_class T883im2 "static T883im2() {}"
} FAIL

tcltest::test 8.8.3-invalid-modifiers-3 { A constructor
        cannot be abstract, static, final, native, strictfp,
        or synchronized } {
    empty_class T883im3 "final T883im3() {}"
} FAIL

tcltest::test 8.8.3-invalid-modifiers-4 { A constructor
        cannot be abstract, static, final, native, strictfp,
        or synchronized } {
    empty_class T883im4 "native T883im4() {}"
} FAIL

tcltest::test 8.8.3-invalid-modifiers-5 { A constructor
        cannot be abstract, static, final, native, strictfp,
        or synchronized } {
    empty_class T883im5 "strictfp T883im5() {}"
} FAIL

tcltest::test 8.8.3-invalid-modifiers-6 { A constructor
        cannot be abstract, static, final, native, strictfp,
        or synchronized } {
    empty_class T883im6 "synchronized T883im6() {}"
} FAIL


