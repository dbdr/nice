tcltest::test 14.14-plain-1 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p1 {
        break;
    }
} FAIL

tcltest::test 14.14-plain-2 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p2 {
        for (int i=0; i<10; i++)
            break;
    }
} PASS

tcltest::test 14.14-plain-3 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p3 {
        int i=0;
        while (i++<10)
            break;
    }
} PASS

tcltest::test 14.14-plain-4 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p4 {
        int i=0;
        do break;
        while (i++<10);
    }
} PASS

tcltest::test 14.14-plain-5 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p5 {
        for (int i=0; i<10; i++) {
            break;
        }
    }
} PASS

tcltest::test 14.14-plain-6 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p6 {
        int i=0;
        while (i++<10) {
            break;
        }
    }
} PASS

tcltest::test 14.14-plain-7 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p7 {
        int i=0;
        do {
            break;
        } while (i++<10);
    }
} PASS

tcltest::test 14.14-plain-8 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p8 {
        switch (args.length) {
            case 0: break;
        }
    }
} PASS

tcltest::test 14.14-plain-9 { plain break must occur in a loop
        statement or switch } {
    empty_main T1414p9 {
        a: break;
    }
} FAIL

tcltest::test 14.14-nonlocal-1 { there are no non-local jumps } {
    empty_main T1414nonlocal1 {
        do {
            new Object() {
                {
                    break;
                }
            };
        } while (false);
    }
} FAIL

tcltest::test 14.14-try-1 { the break can be interrupted by
        a finally } {
    empty_main T1414try1 {
        do {
            try {
                try {
                    break;
                } finally { // discard the break
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a break
        } while (false);
    }
} PASS

# labeled breaks
tcltest::test 14.14-label-1 { A label may be
        targeted by break } {
    empty_main T1414l1 {
        a: {
            for (int i=0; i<10; i++)
                break a;
        }
    }
} PASS

tcltest::test 14.14-label-2 { A label may be
        targeted by break } {
    empty_main T1414l2 {
        a: b: for (int i=0; i<10; i++)
            break a;
    }
} PASS

tcltest::test 14.14-label-3 { labeled break must
        occur in that label } {
    empty_main T1414l3 {
        b: break a;
    }
} FAIL

tcltest::test 14.14-label-4 { labeled break must
        occur in that label } {
    empty_main T1414l4 {
        a: break a;
    }
} PASS

tcltest::test 14.14-label-5 { labeled break must
        occur in that label } {
    empty_main T1414l5 {
        a: for (int i=0; i<10; i++)
            break a;
    }
} PASS

tcltest::test 14.14-label-6 { labeled break must
        occur in that label } {
    empty_main T1414l6 {
        int i=0;
        a: while (i++<10)
            break a;
    }
} PASS

tcltest::test 14.14-label-7 { labeled break must
        occur in that label } {
    empty_main T1414l7 {
        int i=0;
        a: do break a;
        while (i++<10);
    }
} PASS

tcltest::test 14.14-label-8 { labeled break must
        occur in that label } {
    empty_main T1414l8 {
        a: for (int i=0; i<10; i++) {
            break a;
        }
    }
} PASS

tcltest::test 14.14-label-9 { labeled break must
        occur in that label } {
    empty_main T1414l9 {
        int i=0;
        a: while (i++<10) {
            break a;
        }
    }
} PASS

tcltest::test 14.14-label-10 { labeled break must
        occur in that label } {
    empty_main T1414l10 {
        int i=0;
        a: do {
            break a;
        } while (i++<10);
    }
} PASS

tcltest::test 14.14-label-11 { labeled break must
        occur in that label } {
    empty_main T1414l11 {
        a: switch (args.length) {
            case 0: break a;
        }
    }
} PASS

tcltest::test 14.14-label-12 { there are no
        non-local jumps } {
    empty_main T1414l12 {
        a: do {
            new Object() {
                {
                    break a;
                }
            };
        } while (false);
    }
} FAIL

tcltest::test 14.14-label-13 { the break can be
        interrupted by a finally } {
    empty_main T1414l13 {
        a: do {
            try {
                try {
                    break a;
                } finally { // discard the break
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a break
        } while (false);
    }
} PASS

# invalid arguments
tcltest::test 14.14-arg-1 { The arg to break
        must be an identifier } {
    empty_main T1414a1 {
        a: do {
            break 1;
        } while (false);
    }
} FAIL

tcltest::test 14.14-arg-2 { The arg to break
        must be an identifier } {
    empty_main T1414a2 {
        a: do {
            break "a";
        } while (false);
    }
} FAIL

tcltest::test 14.14-arg-3 { The arg to break
        must be an identifier } {
    empty_main T1414a3 {
        a: do {
            break (a);
        } while (false);
    }
} FAIL

tcltest::test 14.14-arg-4 { The arg to break
        must be an identifier } {
    empty_main T1414a4 {
        a: do {
            break a.a;
        } while (false);
    }
} FAIL

