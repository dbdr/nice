# test access modifiers
tcltest::test 8.3.1-access-1 { Multiple access modifiers is an error } {
    empty_class T831access1 {
        public public int i;
    }
} FAIL

tcltest::test 8.3.1-access-2 { Multiple access modifiers is an error } {
    empty_class T831access2 {
        public protected int i;
    }
} FAIL

tcltest::test 8.3.1-access-3 { Multiple access modifiers is an error } {
    empty_class T831access3 {
        public private int i;
    }
} FAIL

tcltest::test 8.3.1-access-4 { Multiple access modifiers is an error } {
    empty_class T831access4 {
        protected public int i;
    }
} FAIL

tcltest::test 8.3.1-access-5 { Multiple access modifiers is an error } {
    empty_class T831access5 {
        protected protected int i;
    }
} FAIL

tcltest::test 8.3.1-access-6 { Multiple access modifiers is an error } {
    empty_class T831access6 {
        protected private int i;
    }
} FAIL

tcltest::test 8.3.1-access-7 { Multiple access modifiers is an error } {
    empty_class T831access7 {
        private public int i;
    }
} FAIL

tcltest::test 8.3.1-access-8 { Multiple access modifiers is an error } {
    empty_class T831access8 {
        private protected int i;
    }
} FAIL

tcltest::test 8.3.1-access-9 { Multiple access modifiers is an error } {
    empty_class T831access9 {
        private private int i;
    }
} FAIL

tcltest::test 8.3.1-access-10 { A single access modifier is legal } {
    empty_class T831access10 {
        public int i;
    }
} PASS

tcltest::test 8.3.1-access-11 { A single access modifier is legal } {
    empty_class T831access11 {
        protected int i;
    }
} PASS

tcltest::test 8.3.1-access-12 { A single access modifier is legal } {
    empty_class T831access12 {
        private int i;
    }
} PASS

tcltest::test 8.3.1-access-13 { No access modifier is legal } {
    empty_class T831access13 {
        int i;
    }
} PASS

# test combinations with static
tcltest::test 8.3.1-static-1 { static alone is legal } {
    empty_class T831static1 {
        static int i;
    }
} PASS

tcltest::test 8.3.1-static-2 { public static is legal } {
    empty_class T831static2 {
        public static int i;
    }
} PASS

tcltest::test 8.3.1-static-3 { static public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831static3 {
        static public int i;
    }]
} OK

tcltest::test 8.3.1-static-4 { protected static is legal } {
    empty_class T831static4 {
        protected static int i;
    }
} PASS

tcltest::test 8.3.1-static-5 { static protected is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831static5 {
        static protected int i;
    }]
} OK

tcltest::test 8.3.1-static-6 { private static is legal } {
    empty_class T831static6 {
        private static int i;
    }
} PASS

tcltest::test 8.3.1-static-7 { static private is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831static7 {
        static private int i;
    }]
} OK

tcltest::test 8.3.1-static-8 { static static is illegal } {
    empty_class T831static8 {
        static static int i;
    }
} FAIL

tcltest::test 8.3.1-static-9 { static final is legal } {
    empty_class T831static9 {
        static final int i = 1;
    }
} PASS

tcltest::test 8.3.1-static-10 { final static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831static10 {
        final static int i = 1;
    }]
} OK

tcltest::test 8.3.1-static-11 { static transient is legal } {
    empty_class T831static11 {
        static transient int i;
    }
} PASS

tcltest::test 8.3.1-static-12 { transient static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831static12 {
        transient static int i;
    }]
} OK

tcltest::test 8.3.1-static-13 { static volatile is legal } {
    empty_class T831static13 {
        static volatile int i;
    }
} PASS

tcltest::test 8.3.1-static-14 { volatile static is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831static14 {
	volatile static int i;
    }]
} OK

# test combinations with final
#(final/static already tested)
tcltest::test 8.3.1-final-1 { final alone is legal } {
    empty_class T831final1 {
        final int i = 1;
    }
} PASS

tcltest::test 8.3.1-final-2 { public final is legal } {
    empty_class T831final2 {
        public final int i = 1;
    }
} PASS

tcltest::test 8.3.1-final-3 { final public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831final3 {
        final public int i = 1;
    }]
} OK

tcltest::test 8.3.1-final-4 { protected final is legal } {
    empty_class T831final4 {
        protected final int i = 1;
    }
} PASS

tcltest::test 8.3.1-final-5 { final protected is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831final5 {
        final protected int i = 1;
    }]
} OK

tcltest::test 8.3.1-final-6 { private final is legal, but redundant } {
    ok_pass_or_warn [empty_class T831final6 {
        private final int i = 1;
    }]
} OK

tcltest::test 8.3.1-final-7 { final private is legal, but redundant, and
        not in the preferred order } {
    ok_pass_or_warn [empty_class T831final7 {
        final private int i = 1;
    }]
} OK

tcltest::test 8.3.1-final-8 { final final is illegal } {
    empty_class T831final8 {
        final final int i = 1;
    }
} FAIL

tcltest::test 8.3.1-final-9 { final transient is legal } {
    empty_class T831final9 {
        final transient int i = 1;
    }
} PASS

tcltest::test 8.3.1-final-10 { transient final is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831final10 {
	transient final int i = 1;
    }]
} OK

tcltest::test 8.3.1-final-11 { final volatile is illegal } {
    empty_class T831final11 {
        final volatile int i = 1;
    }
} FAIL

tcltest::test 8.3.1-final-12 { volatile final is illegal } {
    empty_class T831final12 {
        volatile final int i = 1;
    }
} FAIL

# test combinations with transient
#(transient/static already tested)
#(transient/final already tested)
tcltest::test 8.3.1-transient-1 { transient alone is legal } {
    empty_class T831transient1 {
        transient int i;
    }
} PASS

tcltest::test 8.3.1-transient-2 { public transient is legal } {
    empty_class T831transient2 {
        public transient int i;
    }
} PASS

tcltest::test 8.3.1-transient-3 { transient public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831transient3 {
        transient public int i;
    }]
} OK

tcltest::test 8.3.1-transient-4 { protected transient is legal } {
    empty_class T831transient4 {
        protected transient int i;
    }
} PASS

tcltest::test 8.3.1-transient-5 { transient protected is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T831transient5 {
        transient protected int i;
    }]
} OK

tcltest::test 8.3.1-transient-6 { private transient is legal } {
    empty_class T831transient6 {
        private transient int i;
    }
} PASS

tcltest::test 8.3.1-transient-7 { transient private is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T831transient7 {
        transient private int i;
    }]
} OK

tcltest::test 8.3.1-transient-8 { transient transient is illegal } {
    empty_class T831transient8 {
        transient transient int i;
    }
} FAIL

tcltest::test 8.3.1-transient-9 { transient volatile is legal } {
    empty_class T831transient9 {
        transient volatile int i;
    }
} PASS

tcltest::test 8.3.1-transient-10 { volatile transient is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831transient10 {
        volatile transient int i;
    }]
} OK

# test combinations with volatile
#(volatile/static already tested)
#(volatile/final already tested)
#(volatile/synchronized already tested)
tcltest::test 8.3.1-volatile-1 { volatile alone is legal } {
    empty_class T831volatile1 {
        volatile int i;
    }
} PASS

tcltest::test 8.3.1-volatile-2 { public volatile is legal } {
    empty_class T831volatile2 {
        public volatile int i;
    }
} PASS

tcltest::test 8.3.1-volatile-3 { volatile public is legal, but not in
        the preferred order } {
    ok_pass_or_warn [empty_class T831volatile3 {
        volatile public int i;
    }]
} OK

tcltest::test 8.3.1-volatile-4 { protected volatile is legal } {
    empty_class T831volatile4 {
        protected volatile int i;
    }
} PASS

tcltest::test 8.3.1-volatile-5 { volatile protected is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T831volatile5 {
        volatile protected int i;
    }]
} OK

tcltest::test 8.3.1-volatile-6 { private volatile is legal } {
    empty_class T831volatile6 {
        private volatile int i;
    }
} PASS

tcltest::test 8.3.1-volatile-7 { volatile private is legal, but
        not in the preferred order } {
    ok_pass_or_warn [empty_class T831volatile7 {
        volatile private int i;
    }]
} OK

tcltest::test 8.3.1-volatile-8 { volatile volatile is illegal } {
    empty_class T831volatile8 {
        volatile volatile int i;
    }
} FAIL

# Because we can...
tcltest::test 8.3.1-big-1 { largest number of modifiers allowed } {
    empty_class T831big1 {
        public static transient volatile int i;
    }
} PASS

tcltest::test 8.3.1-big-2 { largest number of modifiers allowed } {
    empty_class T831big2 {
        protected static final transient int i = 1;
    }
} PASS

tcltest::test 8.3.1-big-3 { modifiers stress test } {
    empty_class T831big3 {
	public static final protected private abstract transient volatile
	strictfp native synchronized public static final int i = 1;
    }
} FAIL

# These never modify methods
tcltest::test 8.3.1-bad-1 { abstract fields don't exist } {
    empty_class T831bad1 {
        abstract int i;
    }
} FAIL

tcltest::test 8.3.1-bad-2 { synchronized fields don't exist } {
    empty_class T831bad2 {
	synchronized int i;
    }
} FAIL

tcltest::test 8.3.1-bad-3 { native fields don't exist } {
    empty_class T831bad3 {
	native int i;
    }
} FAIL

tcltest::test 8.3.1-bad-4 { strictfp fields don't exist } {
    empty_class T831bad4 {
	strictfp int i;
    }
} FAIL
