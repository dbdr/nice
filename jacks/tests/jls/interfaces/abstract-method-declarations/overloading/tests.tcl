tcltest::test 9.4.2-overload-1 { there are no required relations between
        overloaded methods } {
    empty_class T942o1 {
        interface I {
            void m();
            int m(int i) throws Exception;
        }
    }
} PASS

tcltest::test 9.4.2-overload-2 { there are no required relations between
        overloaded methods } {
    empty_class T942o2 {
        interface I1 {
            void m();
        }
        interface I2 extends I1 {
            int m(int i) throws Exception;
        }
    }
} PASS

