tcltest::test 14.19-shadow-1 { A catch clause parameter may shadow a member } {
    empty_class T1419s1 {
        int i;
        void foo() {
            try {
                i = 1;
                throw new Exception();
            } catch (Exception i) {
            }
        }
    }
} PASS

tcltest::test 14.19-shadow-2 { A catch clause parameter may shadow a member } {
    empty_class T1419s2 {
        static int i;
        void foo() {
            try {
                i = 1;
                throw new Exception();
            } catch (Exception i) {
            }
        }
    }
} PASS

tcltest::test 14.19-shadow-3 { A catch clause parameter may shadow a member } {
    empty_class T1419s3 {
        static int i;
        static void foo() {
            try {
                i = 1;
                throw new Exception();
            } catch (Exception i) {
            }
        }
    }
} PASS

tcltest::test 14.19-shadow-4 { A catch clause parameter may shadow a local } {
    empty_class T1419s4 {
        void foo(final int i) {
            new Object() {
                {
                    try {
                        int j = i;
                        throw new Exception();
                    } catch (Exception i) {
                    }
                }
            };
        }
    }
} PASS

tcltest::test 14.19-shadow-5 { A catch clause parameter may shadow a local } {
    empty_main T1419s5 {
        final int i = 1;
        new Object() {
            {
                try {
                    int j = i;
                    throw new Exception();
                } catch (Exception i) {
                }
            }
        };
    }
} PASS

tcltest::test 14.19-shadow-6 { A catch clause parameter may shadow a local } {
    empty_main T1419s6 {
        int j = 1;
        for (final int i = 1; j < 1; )
            new Object() {
                {
                    try {
                        int j = i;
                        throw new Exception();
                    } catch (Exception i) {
                    }
                }
            };
    }
} PASS

tcltest::test 14.19-shadow-7 { A catch clause parameter may shadow a local } {
    empty_main T1419s7 {
        try {
            throw new ArithmeticException();
        } catch (final ArithmeticException i) {
            new Object() {
                {
                    try {
                        ArithmeticException ae = i;
                        throw new NullPointerException();
                    } catch (NullPointerException i) {
                    }
                }
            };
        }
    }
} PASS

tcltest::test 14.19-shadow-8 { Catch clauses may repeat a parameter name } {
    empty_main T1419s8 {
        try {
            throw new Exception();
        } catch (NullPointerException e) {
        } catch (RuntimeException e) {
        } catch (Throwable e) {
        }
    }
} PASS

tcltest::test 14.19-shadow-9 { Local variables may not shadow a catch clause
        parameter } {
    empty_main T1419s9 {
        try {
            throw new Exception();
        } catch (Exception e) {
            int e;
        }
    }
} FAIL

tcltest::test 14.19-shadow-10 { Local variables may not shadow a catch clause
        parameter } {
    empty_main T1419s10 {
        try {
            throw new Exception();
        } catch (Exception e) {
            try {
                throw new Exception();
            } catch (Exception e) {
            }
        }
    }
} FAIL

tcltest::test 14.19-shadow-11 { A catch clause parameter may be shadowed } {
    empty_main T1419s11 {
        try {
            throw new Exception();
        } catch (Exception e) {
            new Object() {
                int e;
            };
        }
    }
} PASS

tcltest::test 14.19-shadow-12 { A catch clause parameter may be shadowed } {
    empty_main T1419s12 {
        try {
            throw new Exception();
        } catch (final Exception e) {
            new Object() {
                Object o = e;
                void foo(int e) {}
            };
        }
    }
} PASS

tcltest::test 14.19-shadow-13 { A catch clause parameter may be shadowed } {
    empty_main T1419s13 {
        try {
            throw new Exception();
        } catch (final Exception e) {
            new Object() {
                {
                    Object o = e;
                    int e;
                    e = 1;
                }
            };
        }
    }
} PASS

tcltest::test 14.19-shadow-14 { A catch clause parameter may be shadowed } {
    empty_main T1419s14 {
        try {
            throw new Exception();
        } catch (final Exception e) {
            new Object() {
                {
                    Object o = e;
                    for (int e = 1; e < 1; );
                    o = e;
                }
            };
        }
    }
} PASS

tcltest::test 14.19-shadow-15 { A catch clause parameter may be shadowed } {
    empty_main T1419s15 {
        try {
            throw new ArithmeticException();
        } catch (final ArithmeticException e) {
            new Object() {
                {
                    try {
                        ArithmeticException a = e;
                        throw new NullPointerException();
                    } catch (NullPointerException e) {
                    }
                }
            };
        }
    }
} PASS

# TODO: test the remainder of the syntax of try-catch(-finally) statements
