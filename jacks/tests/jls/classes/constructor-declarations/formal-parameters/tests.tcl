tcltest::test 8.8.1-formal-parameters-1 { See 8.4.1 } {
    empty_class T881fp1 "T881fp1() {}"
} PASS

tcltest::test 8.8.1-formal-parameters-2 { See 8.4.1 } {
    empty_class T881fp2 "T881fp2(int i) {}"
} PASS

tcltest::test 8.8.1-formal-parameters-3 { See 8.4.1 } {
    empty_class T881fp3 "T881fp3(int i, Object j) {}"
} PASS

tcltest::test 8.8.1-formal-parameters-4 { See 8.4.1 } {
    empty_class T881fp4 "T881fp4(int i, final Object j) {}"
} PASS

tcltest::test 8.8.1-formal-parameters-5 { See 8.4.1 } {
    empty_class T881fp5 {T881fp5(int[] arr, Object j) {}}
} PASS

tcltest::test 8.8.1-formal-parameters-6 { See 8.4.1 } {
    empty_class T881fp6 {T881fp6(final int[] arr) {}}
} PASS


tcltest::test 8.8.1-invalid-formal-parameters-1 { See 8.4.1 } {
    empty_class T881ifp1 "T881ifp1(int i, ) {}"
} FAIL

tcltest::test 8.8.1-invalid-formal-parameters-2 { See 8.4.1 } {
    empty_class T881ifp2 "T881ifp2(int i, foo) {}"
} FAIL

tcltest::test 8.8.1-invalid-formal-parameters-3 { See 8.4.1 } {
    empty_class T881ifp3 "T881ifp3(int i, int) {}"
} FAIL

tcltest::test 8.8.1-invalid-formal-parameters-4 { See 8.4.1 } {
    empty_class T881ifp4 "T881ifp4(int i, final int) {}"
} FAIL

# FIXME: Implement more tests mentioned in 8.4.1

