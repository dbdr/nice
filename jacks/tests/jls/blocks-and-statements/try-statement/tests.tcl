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

tcltest::test 14.19-syntax-1 { The try block must be a block } {
    empty_main T1419syn1 {
	try ;
	catch (Exception e) {}
    }
} FAIL

tcltest::test 14.19-syntax-2 { The try block must be a block } {
    empty_main T1419syn2 {
	try int i;
	catch (Exception e) {}
    }
} FAIL

tcltest::test 14.19-syntax-3 { The try block must be a block } {
    empty_main T1419syn3 {
	int i;
	try i++;
	catch (Exception e) {}
    }
} FAIL

tcltest::test 14.19-syntax-4 { The try block must be a block } {
    empty_main T1419syn4 {
	try class C {}
	catch (Exception e) {}
    }
} FAIL

tcltest::test 14.19-syntax-5 { The try block must be a block } {
    empty_main T1419syn5 {
	try label: {}
	catch (Exception e) {}
    }
} FAIL

tcltest::test 14.19-syntax-6 { The catch block must be a block } {
    empty_main T1419syn6 {
	try {}
	catch (Exception e) ;
    }
} FAIL

tcltest::test 14.19-syntax-7 { The catch block must be a block } {
    empty_main T1419syn7 {
	try {}
	catch (Exception e) int i;
    }
} FAIL

tcltest::test 14.19-syntax-8 { The catch block must be a block } {
    empty_main T1419syn8 {
	int i;
	try {}
	catch (Exception e) i++;
    }
} FAIL

tcltest::test 14.19-syntax-9 { The catch block must be a block } {
    empty_main T1419syn9 {
	try {}
	catch (Exception e) class C {}
    }
} FAIL

tcltest::test 14.19-syntax-10 { The catch block must be a block } {
    empty_main T1419syn10 {
	try {}
	catch (Exception e) label: {}
    }
} FAIL

tcltest::test 14.19-syntax-11 { The finally block must be a block } {
    empty_main T1419syn11 {
	try {}
	finally ;
    }
} FAIL

tcltest::test 14.19-syntax-12 { The finally block must be a block } {
    empty_main T1419syn12 {
	try {}
	finally int i;
    }
} FAIL

tcltest::test 14.19-syntax-13 { The finally block must be a block } {
    empty_main T1419syn13 {
	int i;
	try {}
	finally i++;
    }
} FAIL

tcltest::test 14.19-syntax-14 { The finally block must be a block } {
    empty_main T1419syn14 {
	try {}
	finally class C {}
    }
} FAIL

tcltest::test 14.19-syntax-15 { The finally block must be a block } {
    empty_main T1419syn15 {
	try {}
	finally label: {}
    }
} FAIL

tcltest::test 14.19-syntax-16 { The finally block must be a block } {
    empty_main T1419syn16 {
	try {}
	catch (Exception e) {}
	finally ;
    }
} FAIL

tcltest::test 14.19-syntax-17 { The finally block must be a block } {
    empty_main T1419syn17 {
	try {}
	catch (Exception e) {}
	finally int i;
    }
} FAIL

tcltest::test 14.19-syntax-18 { The finally block must be a block } {
    empty_main T1419syn18 {
	int i;
	try {}
	catch (Exception e) {}
	finally i++;
    }
} FAIL

tcltest::test 14.19-syntax-19 { The finally block must be a block } {
    empty_main T1419syn19 {
	try {}
	catch (Exception e) {}
	finally class C {}
    }
} FAIL

tcltest::test 14.19-syntax-20 { The finally block must be a block } {
    empty_main T1419syn20 {
	try {}
	catch (Exception e) {}
	finally label: {}
    }
} FAIL

tcltest::test 14.19.exception-1 { checked exceptions must be caught or
        declared } {
    empty_class T1419e1 {
	void m() {
	    try {
		throw new Exception();
	    } catch (RuntimeException e) {
	    }
	}
    }
} FAIL

tcltest::test 14.19.exception-2 { checked exceptions must be caught or
        declared } {
    empty_class T1419e2 {
	void m() throws Exception {
	    try {
		throw new Exception();
	    } catch (RuntimeException e) {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-3 { checked exceptions must be caught or
        declared } {
    empty_class T1419e3 {
	void m() {
	    try {
		throw new Exception();
	    } finally {
	    }
	}
    }
} FAIL

tcltest::test 14.19.exception-4 { checked exceptions must be caught or
        declared } {
    empty_class T1419e4 {
	void m() throws Exception {
	    try {
		throw new Exception();
	    } finally {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-5 { checked exceptions must be caught or
        declared } {
    empty_class T1419e5 {
	void m() {
	    try {
		throw new Exception();
	    } catch (Exception e) {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-6 { checked exceptions must be caught or
        declared } {
    empty_class T1419e6 {
	void m() {
	    try {
		throw new Exception();
	    } catch (Exception e) {
	    } finally {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-7 { checked exceptions must be caught or
        declared } {
    empty_class T1419e7 {
	void m() {
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    }
	}
    }
} FAIL

tcltest::test 14.19.exception-8 { checked exceptions must be caught or
        declared } {
    empty_class T1419e8 {
	void m() throws Exception {
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-9 { checked exceptions must be caught or
        declared } {
    empty_class T1419e9 {
	void m() {
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    } finally {
	    }
	}
    }
} FAIL

tcltest::test 14.19.exception-10 { checked exceptions must be caught or
        declared } {
    empty_class T1419e10 {
	void m() throws Exception {
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    } finally {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-11 { checked exceptions must be caught or
        declared } {
    empty_class T1419e11 {
	void m() {
	    try {
	    } finally {
		throw new Exception();
	    }
	}
    }
} FAIL

tcltest::test 14.19.exception-12 { checked exceptions must be caught or
        declared } {
    empty_class T1419e12 {
	void m() throws Exception {
	    try {
	    } finally {
		throw new Exception();
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-13 { checked exceptions must be caught or
        declared } {
    empty_class T1419e13 {
	void m() {
	    try {
		throw new Exception();
	    } finally {
		return;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-14 { checked exceptions must be caught or
        declared } {
    empty_class T1419e14 {
	void m() {
	    a:
	    try {
		throw new Exception();
	    } finally {
		break a;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-15 { checked exceptions must be caught or
        declared } {
    empty_class T1419e15 {
	void m() {
	    a: do
	    try {
		throw new Exception();
	    } finally {
		continue a;
	    }
	    while (false);
	}
    }
} PASS

tcltest::test 14.19.exception-16 { checked exceptions must be caught or
        declared } {
    empty_class T1419e16 {
	void m() {
	    try {
		try {
		    throw new Exception();
		} finally {
		    throw new RuntimeException();
		}
	    } catch (RuntimeException e) {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-17 { checked exceptions must be caught or
        declared } {
    empty_class T1419e17 {
	void m() {
	    try {
		throw new Exception();
	    } catch (Exception e) {
	    } finally {
		return;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-18 { checked exceptions must be caught or
        declared } {
    empty_class T1419e18 {
	void m() {
	    a:
	    try {
		throw new Exception();
	    } catch (Exception e) {
	    } finally {
		break a;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-19 { checked exceptions must be caught or
        declared } {
    empty_class T1419e19 {
	void m() {
	    a: do
	    try {
		throw new Exception();
	    } catch (Exception e) {
	    } finally {
		continue a;
	    }
	    while (false);
	}
    }
} PASS

tcltest::test 14.19.exception-20 { checked exceptions must be caught or
        declared } {
    empty_class T1419e20 {
	void m() {
	    try {
		try {
		    throw new Exception();
		} catch (Exception e) {
		} finally {
		    throw new RuntimeException();
		}
	    } catch (RuntimeException e) {
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-21 { checked exceptions must be caught or
        declared } {
    empty_class T1419e21 {
	void m() {
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    } finally {
		return;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-22 { checked exceptions must be caught or
        declared } {
    empty_class T1419e22 {
	void m() {
	    a:
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    } finally {
		break a;
	    }
	}
    }
} PASS

tcltest::test 14.19.exception-23 { checked exceptions must be caught or
        declared } {
    empty_class T1419e23 {
	void m() {
	    a: do
	    try {
		throw new Exception();
	    } catch (Exception e) {
		throw e;
	    } finally {
		continue a;
	    }
	    while (false);
	}
    }
} PASS

tcltest::test 14.19.exception-24 { checked exceptions must be caught or
        declared } {
    empty_class T1419e24 {
	void m() {
	    try {
		try {
		    throw new Exception();
		} catch (Exception e) {
		    throw e;
		} finally {
		    throw new RuntimeException();
		}
	    } catch (RuntimeException e) {
	    }
	}
    }
} PASS


# TODO: test more on try-catch(-finally) statements
