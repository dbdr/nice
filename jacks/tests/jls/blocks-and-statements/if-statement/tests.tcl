tcltest::test 14.9-invalid-syntax-1 { Expression have () } {
    empty_main T149is1 {
        if true
            ;
    }
} FAIL

tcltest::test 14.9-invalid-syntax-2 { Expression must be of type boolean } {
    empty_main T149is2 {
        if (0)
            ;
    }
} FAIL

tcltest::test 14.9-invalid-syntax-3 { else without Statement is invalid } {
    empty_main T149is3 {
        if (true)
            ;
        else
    }
} FAIL

tcltest::test 14.9-invalid-syntax-4 { Multiple if Statement are invalid } {
    empty_main T149is4 {
        if (true)
            ;
            ;
        else
            ;
    }
} FAIL



tcltest::test 14.9-expression-evaluation-1 {constant expression} {
    empty_main T149ee1 {
        if (true)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-2 {constant expression} {
    empty_main T149ee2 {
        if (false == true)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-3 {non-constant expression} {
    empty_main T149ee3 {
        final boolean aconst = false;
        boolean anonconst = true;

        if (aconst == anonconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-4 {constant expression} {
    empty_main T149ee4 {
        if ((byte) 0 == (byte) 1)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-5 {non-constant expression} {
    empty_main T149ee5 {
        final byte aconst = 0;
        byte anonconst = 1;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-6 {constant expression} {
    empty_main T149ee6 {
        if ((short) 0 == (short) 1)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-7 {non-constant expression} {
    empty_main T149ee7 {
        final short aconst = 0;
        short anonconst = 1;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-8 {constant expression} {
    empty_main T149ee8 {
        if ((char) 0 == (char) 1)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-9 {non-constant expression} {
    empty_main T149ee9 {
        final char aconst = 0;
        char anonconst = 1;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-10 {constant expression} {
    empty_main T149ee10 {
        if (0 == 1)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-11 {non-constant expression} {
    empty_main T149ee11 {
        final int aconst = 0;
        int anonconst = 1;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-12 {constant expression} {
    empty_main T149ee12 {
        if (0L == 1L)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-13 {non-constant expression} {
    empty_main T149ee13 {
        final long aconst = 0L;
        long anonconst = 1L;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-14 {constant expression} {
    empty_main T149ee14 {
        if (0F == 1F)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-15 {non-constant expression} {
    empty_main T149ee15 {
        final float aconst = 0F;
        float anonconst = 1F;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-16 {constant expression} {
    empty_main T149ee16 {
        if (0D == 1D)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-17 {non-constant expression} {
    empty_main T149ee17 {
        final double aconst = 0D;
        double anonconst = 1D;

        if (anonconst == aconst)
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-18 {constant expression} {
    empty_main T149ee18 {
        if ("hi" == "there")
            ;
    }
} PASS

tcltest::test 14.9-expression-evaluation-19 {non-constant expression} {
    empty_main T149ee19 {
        final String aconst = "hi";
        String anonconst = "there";

        if (anonconst == aconst)
            ;
    }
} PASS
