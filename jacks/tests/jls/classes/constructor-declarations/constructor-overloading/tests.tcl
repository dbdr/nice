tcltest::test 8.8.6-overloaded-1 { An overloaded constructor
        is resolved at compiler time as described in 15.9.3 } {
    empty_class T886o1 {
T886o1(Object o) {}
T886o1(String s) {}
void foo() {
    Object obj = new T886o1("");
}
    }
} PASS

# FIXME: Add more overloaded constructor examples
