tcltest::test 14.15-plain-1 { continue must occur in a loop statement } {
    empty_main T1415p1 {
        continue;
    }
} FAIL

tcltest::test 14.15-plain-2 { continue must occur in a loop statement } {
    empty_main T1415p2 {
        for (int i=0; i<10; i++)
            continue;
    }
} PASS

tcltest::test 14.15-plain-3 { continue must occur in a loop statement } {
    empty_main T1415p3 {
        int i=0;
        while (i++<10)
            continue;
    }
} PASS

tcltest::test 14.15-plain-4 { continue must occur in a loop statement } {
    empty_main T1415p4 {
        int i=0;
        do continue;
        while (i++<10);
    }
} PASS

tcltest::test 14.15-plain-5 { continue must occur in a loop statement } {
    empty_main T1415p5 {
        for (int i=0; i<10; i++) {
            continue;
        }
    }
} PASS

tcltest::test 14.15-plain-6 { continue must occur in a loop statement } {
    empty_main T1415p6 {
        int i=0;
        while (i++<10) {
            continue;
        }
    }
} PASS

tcltest::test 14.15-plain-7 { continue must occur in a loop statement } {
    empty_main T1415p7 {
        int i=0;
        do {
            continue;
        } while (i++<10);
    }
} PASS

tcltest::test 14.15-plain-8 { continue must occur in a loop statement } {
    empty_main T1415p8 {
        switch (args.length) {
            case 0: continue;
        }
    }
} FAIL

tcltest::test 14.15-nonlocal-1 { there are no non-local jumps } {
    empty_main T1415nonlocal1 {
        do {
            new Object() {
                {
                    continue;
                }
            };
        } while (false);
    }
} FAIL

tcltest::test 14.15-try-1 { the continue can be interrupted by a finally } {
    empty_main T1415try1 {
        do {
            try {
                try {
                    continue;
                } finally { // discard the continue
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a continue
        } while (false);
    }
} PASS

# labeled continues

tcltest::test 14.15-label-1 { A label targeted by continue
        must have a loop as its statment } {
    empty_main T1415l1 {
        a: {
            for (int i=0; i<10; i++)
                continue a;
        }
    }
} FAIL

tcltest::test 14.15-label-2 { A label targeted by continue
        must have a loop as its statment } {
    empty_main T1415l2 {
        a: b: for (int i=0; i<10; i++)
            continue a;
    }
} FAIL

tcltest::test 14.15-label-3 { labeled continue must occur in that label } {
    empty_main T1415l3 {
        b: continue a;
    }
} FAIL

tcltest::test 14.15-label-4 { continue must occur in a loop statement } {
    empty_main T1415l4 {
        a: continue a;
    }
} FAIL

tcltest::test 14.15-label-5 { continue must occur in a loop statement } {
    empty_main T1415l5 {
        a: for (int i=0; i<10; i++)
            continue a;
    }
} PASS

tcltest::test 14.15-label-6 { continue must occur in a loop statement } {
    empty_main T1415l6 {
        int i=0;
        a: while (i++<10)
            continue a;
    }
} PASS

tcltest::test 14.15-label-7 { continue must occur in a loop statement } {
    empty_main T1415l7 {
        int i=0;
        a: do continue a;
        while (i++<10);
    }
} PASS

tcltest::test 14.15-label-8 { continue must occur in a loop statement } {
    empty_main T1415l8 {
        a: for (int i=0; i<10; i++) {
            continue a;
        }
    }
} PASS

tcltest::test 14.15-label-9 { continue must occur in a loop statement } {
    empty_main T1415l9 {
        int i=0;
        a: while (i++<10) {
            continue a;
        }
    }
} PASS

tcltest::test 14.15-label-10 { continue must occur in a loop statement } {
    empty_main T1415l10 {
        int i=0;
        a: do {
            continue a;
        } while (i++<10);
    }
} PASS

tcltest::test 14.15-label-11 { continue must occur in a loop statement } {
    empty_main T1415l11 {
        a: switch (args.length) {
            case 0: continue a;
        }
    }
} FAIL

tcltest::test 14.15-nonlocal-2 { there are no non-local jumps } {
    empty_main T1415nonlocal2 {
        a: do {
            new Object() {
                {
                    continue a;
                }
            };
        } while (false);
    }
} FAIL

tcltest::test 14.15-try-2 { the continue can be interrupted by a finally } {
    empty_main T1415try2 {
        a: do {
            try {
                try {
                    continue a;
                } finally { // discard the continue
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a continue
        } while (false);
    }
} PASS

# invalid arguments
tcltest::test 14.15-arg-1 { The arg to continue must be an identifier } {
    empty_main T1415a1 {
        a: do {
            continue 1;
        } while (false);
    }
} FAIL

tcltest::test 14.15-arg-2 { The arg to continue must be an identifier } {
    empty_main T1415a2 {
        a: do {
            continue "a";
        } while (false);
    }
} FAIL

tcltest::test 14.15-arg-3 { The arg to continue must be an identifier } {
    empty_main T1415a3 {
        a: do {
            continue (a);
        } while (false);
    }
} FAIL

tcltest::test 14.15-arg-4 { The arg to continue must be an identifier } {
    empty_main T1415a4 {
        a: do {
            continue a.a;
        } while (false);
    }
} FAIL

