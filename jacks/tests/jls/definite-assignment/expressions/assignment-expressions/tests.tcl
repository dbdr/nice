tcltest::test 16.1.7-compound-assign-1 { final variables cannot be assigned
        with compound assignment } {
    empty_main T1617ca1 {
	final int i;
	i += 1;
    }
} FAIL

tcltest::test 16.1.7-compound-assign-2 { final variables cannot be assigned
        with compound assignment } {
    empty_main T1617ca2 {
	final int i;
	i += 1L;
    }
} FAIL

tcltest::test 16.1.7-compound-assign-3 { final variables cannot be assigned
        with compound assignment } {
    empty_main T1617ca3 {
	final String s = "";
	s += "";
    }
} FAIL

tcltest::test 16.1.7-compound-assign-4 { final variables cannot be assigned
        with compound assignment } {
    empty_main T1617ca4 {
	final String s = "";
	s += 1;
    }
} FAIL

# tests of simple assignment expressions, cases which are illegal
# because x is not definitely assigned before use
tcltest::test 16.1.7-simple-definite-assignment-fail-1 { V is DA after
        a=b iff either a is V or V is DA after b } {
    empty_main T1617sdaf1 {
	int i, x;
	i = 1;
	x++;
    }
} FAIL;

tcltest::test 16.1.7-simple-definite-assignment-fail-2 { V is DA after
        a=b iff either a is V or V is DA after b } {
    empty_main T1617sdaf2 {
	int i, x;
	boolean b = true;
	i = (b ? x = 1 : 1);
	x++;
    }
} FAIL;

tcltest::test 16.1.7-simple-definite-assignment-fail-3 { V is DA before
        the subexpressions of a iff V is DA before a=b } {
    empty_main T1617sdaf3 {
	int ia[] = {0}, i, x;
	ia[i = x]++;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-assignment-fail-4 { V is DA before
        the subexpressions of a iff V is DA before a=b } {
    empty_class T1617sdaf4 {
        T1617sdaf4 t;
        void foo() {
            T1617sdaf4 x;
	    x.t = new T1617sdaf4();
	}
    }
} FAIL

tcltest::test 16.1.7-simple-definite-assignment-fail-5 { V is DA before
        b iff V is DA after the subexpressions of a in a=b } {
    empty_main T1617sdaf5 {
	int i, x;
	i = x;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-assignment-fail-6 { V is DA before
        b iff V is DA after the subexpressions of a in a=b } {
    empty_main T1617sdaf6 {
	int ia[] = {0}, x;
	boolean b = true;
	ia[b ? x = 0 : 0] = x;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-assignment-fail-7 { V is DA before
        b iff V is DA after the subexpressions of a in a=b } {
    empty_class T1617sdaf7 {
        T1617sdaf7 t;
	void foo() {
            T1617sdaf7 x;
	    boolean b = true;
	    (b ? x = new T1617sdaf7() : null).t = x;
	}
    }
} FAIL

tcltest::test 16.1.7-simple-definite-assignment-fail-8 { V is DA after
        boolean a=b when true iff V is DA after a=b } {
    empty_main T1617sdaf8 {
	boolean b;
	int x;
	if (b = false)
	    x++;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-assignment-fail-9 { V is DA after
        boolean a=b when false iff V is DA after a=b } {
    empty_main T1617sdaf9 {
	boolean b;
	int x;
	if (b = true);
	else x++;
    }
} FAIL

# tests of compound assignment expressions, cases which are illegal
# because x is not definitely assigned before use
tcltest::test 16.1.7-compound-definite-assignment-fail-1 { V is DA after
        a<op>=b iff either a is V or V is DA after b } {
    empty_main T1617cdaf1 {
	int i = 0, x;
	i += 1;
	x++;
    }
} FAIL;

tcltest::test 16.1.7-compound-definite-assignment-fail-2 { V is DA after
        a<op>=b iff either a is V or V is DA after b } {
    empty_main T1617cdaf2 {
	int i = 0, x;
	boolean b = true;
	i += (b ? x = 1 : 1);
	x++;
    }
} FAIL;

tcltest::test 16.1.7-compound-definite-assignment-fail-3 { V is DA before
        the subexpressions of a iff V is DA before a<op>=b } {
    empty_main T1617cdaf3 {
	int ia[] = {0}, i = 0, x;
	ia[i += x]++;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-4 { V is DA before
        the subexpressions of a iff V is DA before a<op>=b } {
    empty_class T1617cdaf4 {
	String s;
	void foo() {
            T1617cdaf4 x;
	    x.s += x = new T1617cdaf4();
	}
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-5 { V is DA before
        b iff V is DA after the subexpressions of a in a<op>=b } {
    empty_main T1617cdaf5 {
	int i = 0, x;
	i += x;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-6 { V is DA before
        b iff V is DA after the subexpressions of a in a<op>=b } {
    empty_main T1617cdaf6 {
	int ia[] = {0}, x;
	boolean b = true;
	ia[b ? x = 0 : 0] += x;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-7 { V is DA before
        b iff V is DA after the subexpressions of a in a<op>=b } {
    empty_class T1617cdaf7 {
	String s;
	void foo() {
            T1617cdaf7 x;
	    boolean b = true;
	    (b ? x = new T1617cdaf7() : null).s += x;
	}
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-8 { V is DA after
        boolean a<op>=b when true iff V is DA after a<op>=b } {
    empty_main T1617cdaf8 {
	boolean b = false;
	int x;
	if (b &= false)
	    x++;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-9 { V is DA after
        boolean a<op>=b when false iff V is DA after a<op>=b } {
    empty_main T1617cdaf9 {
	boolean b = true;
	int x;
	if (b |= true);
	else x++;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-assignment-fail-10 { V must be DA
        before a if V is a in a<op>=b } {
    empty_main T1617cdaf10 {
	int x;
	x += 0;
    }
} FAIL
    
tcltest::test 16.1.7-compound-definite-assignment-fail-11 { V must be DA
        before a if V is a in a<op>=b } {
    empty_main T1617cdaf11 {
	int x;
	x += x = 0;
    }
} FAIL
    
# tests of simple assignment expressions, cases which are legal
# because x is definitely assigned before use
tcltest::test 16.1.7-simple-definite-assignment-pass-1 { V is DA after
        a=b iff either a is V or V is DA after b } {
    empty_main T1617sdap1 {
	int x;
	x = 1;
	x++;
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-2 { V is DA after
        a=b iff either a is V or V is DA after b } {
    empty_main T1617sdap2 {
	int i, x;
	i = x = 1;
	x++;
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-3 { V is DA before
        the subexpressions of a iff V is DA before a=b } {
    empty_main T1617sdap3 {
	int ia[] = {0}, i, x = 0;
	ia[i = x]++;
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-4 { V is DA before
        the subexpressions of a iff V is DA before a=b } {
    empty_class T1617sdap4 {
	T1617sdap4 t;
        void foo() {
            T1617sdap4 x = null;
	    x.t = x;
	}
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-5 { V is DA before
        b iff V is DA after the subexpressions of a in a=b } {
    empty_main T1617sdap5 {
	int i, x = 0;
	i = x;
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-6 { V is DA before
        b iff V is DA after the subexpressions of a in a=b } {
    empty_main T1617sdap6 {
	int ia[] = {0}, x;
	ia[true ? x = 0 : 0] = x;
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-7 { V is DA before
        b iff V is DA after the subexpressions of a in a=b } {
    empty_class T1617sdap7 {
        T1617sdap7 t;
        void foo() {
            T1617sdap7 x;
	    (true ? x = new T1617sdap7() : null).t = x;
	}
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-8 { V is DA after
        boolean a=b when true iff V is DA after a=b } {
    empty_main T1617sdap8 {
	boolean b;
	int x;
	if ((b = false) && false)
	    x++;
    }
} PASS

tcltest::test 16.1.7-simple-definite-assignment-pass-9 { V is DA after
        boolean a=b when false iff V is DA after a=b } {
    empty_main T1617sdap9 {
	boolean b;
	int x;
	if ((b = true) || true);
	else x++;
    }
} PASS

# tests of compound assignment expressions, cases which are legal
# because x is definitely assigned before use
tcltest::test 16.1.7-compound-definite-assignment-pass-1 { V is DA after
        a<op>=b iff either a is V or V is DA after b } {
    empty_main T1617cdap1 {
	int x = 0;
	x += 1;
	x++;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-2 { V is DA after
        a<op>=b iff either a is V or V is DA after b } {
    empty_main T1617cdap2 {
	int i = 0, x;
	i += x = 1;
	x++;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-3 { V is DA before
        the subexpressions of a iff V is DA before a<op>=b } {
    empty_main T1617cdap3 {
	int ia[] = {0}, i = 0, x = 0;
	ia[i += x]++;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-4 { V is DA before
        the subexpressions of a iff V is DA before a<op>=b } {
    empty_class T1617cdap4 {
	String s;
	void foo() {
            T1617cdap4 x = null;
	    x.s += x;
	}
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-5 { V is DA before
        b iff V is DA after the subexpressions of a in a<op>=b } {
    empty_main T1617cdap5 {
	int i = 0, x = 0;
	i += x;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-6 { V is DA before
        b iff V is DA after the subexpressions of a in a<op>=b } {
    empty_main T1617cdap6 {
	int ia[] = {0}, x;
	ia[true ? x = 0 : 0] += x;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-7 { V is DA before
        b iff V is DA after the subexpressions of a in a<op>=b } {
    empty_class T1617cdap7 {
	String s;
	void foo() {
            T1617cdap7 x;
	    (true ? x = new T1617cdap7() : null).s += x;
	}
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-8 { V is DA after
        boolean a<op>=b when true iff V is DA after a<op>=b } {
    empty_main T1617cdap8 {
	boolean b = false;
	int x;
	if ((b &= false) && false)
	    x++;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-9 { V is DA after
        boolean a<op>=b when false iff V is DA after a<op>=b } {
    empty_main T1617cdap9 {
	boolean b = true;
	int x;
	if ((b |= true) || true);
	else x++;
    }
} PASS

tcltest::test 16.1.7-compound-definite-assignment-pass-10 { V must be DA
        before a if V is a in a<op>=b } {
    empty_main T1617cdap10 {
	int x = 0;
	x += 0;
    }
} PASS
    
# tests of simple assignment expressions, cases which are illegal
# because x is not definitely unassigned before assignment
tcltest::test 16.1.7-simple-definite-unassignment-fail-1 { V is DU after
        a=b iff both a is not V and V is DU after b } {
    empty_main T1617sduf1 {
	if (false) {
	    final int x;
	    x = 1;
	    x = 1;
	}
    }
} FAIL;

tcltest::test 16.1.7-simple-definite-unassignment-fail-2 { V is DU after
        a=b iff both a is not V and V is DU after b } {
    empty_main T1617sduf2 {
	final int x;
	int i;
	i = x = 1;
	x = 1;
    }
} FAIL;

tcltest::test 16.1.7-simple-definite-unassignment-fail-3 { V is DU before
        the subexpressions of a iff V is DU before a=b } {
    empty_main T1617sduf3 {
	final int x;
	int ia[] = {0}, i = x = 0;
	ia[x = i]++;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-4 { V is DU before
        the subexpressions of a iff V is DU before a=b } {
    empty_class T1617sduf4 {
	final T1617sduf4 x;
	int i;
	{
	    x = null;
	    (x = new T1617sduf4()).i++;
	}
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-5 { V is DU before
        b iff V is DU after the subexpressions of a in a=b } {
    empty_main T1617sduf5 {
	final int x;
	int i = x = 0;
	i = x = 1;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-6 { V is DU before
        b iff V is DU after the subexpressions of a in a=b } {
    empty_main T1617sduf6 {
	final int x;
	int ia[] = {0};
	boolean b = false;
	ia[b ? x = 0 : 0] = x = 0;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-7 { V is DU before
        b iff V is DU after the subexpressions of a in a=b } {
    empty_class T1617sduf7 {
	final T1617sduf7 x;
        T1617sduf7 t;
	{
	    boolean b = false;
	    (b ? x = new T1617sduf7() : null).t = x = null;
	}
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-8 { V is DU after
        boolean a=b when true iff V is DU after a=b } {
    empty_main T1617sduf8 {
	boolean b;
	final int x;
	if (b = false)
	    x++;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-9 { V is DU after
        boolean a=b when false iff V is DU after a=b } {
    empty_main T1617sduf9 {
	boolean b;
	final int x;
	if (b = true);
	else x++;
    }
} FAIL

tcltest::test 16.1.7-simple-definite-unassignment-fail-10 { V must be DU
        after b if V is a blank final and V is a in a=b } {
    empty_main T1617sduf10 {
	final int x;
	x = x = 10;
    }
} FAIL

# Also see tests for chapter 16 in general - a blank final field may not be
# assigned except by simple name or by this.name

# tests of compound assignment expressions, cases which are illegal
# because x is not definitely unassigned before assignment
tcltest::test 16.1.7-compound-definite-unassignment-fail-1 { V is DU after
        a<op>=b iff both a is not V and V is DU after b } {
    empty_main T1617cduf1 {
	final int x;
	if (false) {
	    x += 1;
	    x = 1;
	}
    }
} FAIL;

tcltest::test 16.1.7-compound-definite-unassignment-fail-2 { V is DU after
        a<op>=b iff both a is not V and V is DU after b } {
    empty_main T1617cduf2 {
	final int x;
	int i;
	i += x = 1;
	x = 1;
    }
} FAIL;

tcltest::test 16.1.7-compound-definite-unassignment-fail-3 { V is DU before
        the subexpressions of a iff V is DU before a<op>=b } {
    empty_main T1617cduf3 {
	final int x;
	int ia[] = {0}, i = x = 0;
	ia[x = i] += 1;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-4 { V is DU before
        the subexpressions of a iff V is DU before a<op>=b } {
    empty_class T1617cduf4 {
	final T1617cduf4 x;
	String s;
	{
	    x = null;
	    (x = new T1617cduf4()).s += "";
	}
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-5 { V is DU before
        b iff V is DU after the subexpressions of a in a<op>=b } {
    empty_main T1617cduf5 {
	final int x;
	int i = x = 0;
	i += x = 1;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-6 { V is DU before
        b iff V is DU after the subexpressions of a in a<op>=b } {
    empty_main T1617cduf6 {
	final int x;
	int ia[] = {0};
	ia[false ? x = 0 : 0] += x = 0;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-7 { V is DU before
        b iff V is DU after the subexpressions of a in a<op>=b } {
    empty_class T1617cduf7 {
	final T1617cduf7 x;
	String s;
	{
	    (false ? x = new T1617cduf7() : null).s += x = null;
	}
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-8 { V is DU after
        boolean a<op>=b when true iff V is DU after a<op>=b } {
    empty_main T1617cduf8 {
	boolean b = false;
	final int x;
	x = 0;
	if (b &= false)
	    x = 1;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-9 { V is DU after
        boolean a<op>=b when false iff V is DU after a<op>=b } {
    empty_main T1617cduf9 {
	boolean b = true;
	final int x;
	x = 0;
	if (b |= true);
	else x = 1;
    }
} FAIL

tcltest::test 16.1.7-compound-definite-unassignment-fail-10 { V must be DU
        after b if V is a blank final and V is a in a<op>=b } {
    empty_main T1617cduf10 {
	final int x;
	x += x = 10;
    }
} FAIL

# tests of simple assignment expressions, cases which are legal
# because x is definitely unassigned before assignment
tcltest::test 16.1.7-simple-definite-unassignment-pass-1 { a blank final
        V must be DU before a=b if V is a } {
    empty_main T1617sdup1 {
	final int x;
	x = 1;
	if (false) {
	    x = 1;
	}
    }
} PASS;

tcltest::test 16.1.7-simple-definite-unassignment-pass-2 { V is DU after
        a=b iff both a is not V and V is DU after b } {
    empty_main T1617sdup2 {
	final int x;
	int i;
	i = 1;
	x = 1;
    }
} PASS;

tcltest::test 16.1.7-simple-definite-unassignment-pass-3 { V is DU before
        the subexpressions of a iff V is DU before a=b } {
    empty_main T1617sdup3 {
	final int x;
	int ia[] = {0}, i = 0;
	ia[x = i]++;
    }
} PASS

tcltest::test 16.1.7-simple-definite-unassignment-pass-4 { V is DU before
        the subexpressions of a iff V is DU before a=b } {
    empty_class T1617sdup4 {
	final T1617sdup4 x;
	int i;
	{
	    (x = new T1617sdup4()).i++;
	}
    }
} PASS

tcltest::test 16.1.7-simple-definite-unassignment-pass-5 { V is DU before
        b iff V is DU after the subexpressions of a in a=b } {
    empty_main T1617sdup5 {
	final int x;
	int i = 0;
	i = x = 1;
    }
} PASS

tcltest::test 16.1.7-simple-definite-unassignment-pass-6 { V is DU after
        boolean a=b when true iff V is DU after a=b } {
    empty_main T1617sdup6 {
	boolean b;
	final int x;
	x = 0;
	if ((b = false) && false)
	    x = 1;
    }
} PASS

tcltest::test 16.1.7-simple-definite-unassignment-pass-7 { V is DU after
        boolean a=b when false iff V is DU after a=b } {
    empty_main T1617sdup7 {
	boolean b;
	final int x;
	x = 0;
	if ((b = true) || true);
	else x = 1;
    }
} PASS

tcltest::test 16.1.7-simple-definite-unassignment-pass-8 { V must be DU
        after b if V is a blank final and V is a in a=b } {
    empty_main T1617sdup8 {
	final int x;
	int i;
	x = i = 10;
    }
} PASS

# tests of compound assignment expressions, cases which are legal
# because x is definitely unassigned before assignment
tcltest::test 16.1.7-compound-definite-unassignment-pass-1 { a blank final
        V must be DU before a<op>=b if V is a } {
    empty_main T1617cdup1 {
	final int x;
	if (false) {
	    x += 1;
	}
    }
} PASS;

tcltest::test 16.1.7-compound-definite-unassignment-pass-2 { V is DU after
        a<op>=b iff both a is not V and V is DU after b } {
    empty_main T1617cdup2 {
	final int x;
	int i = 0;
	i += 1;
	x = 1;
    }
} PASS;

tcltest::test 16.1.7-compound-definite-unassignment-pass-3 { V is DU before
        the subexpressions of a iff V is DU before a<op>=b } {
    empty_main T1617cdup3 {
	final int x;
	int ia[] = {0}, i = 0;
	ia[x = i] += 1;
    }
} PASS

tcltest::test 16.1.7-compound-definite-unassignment-pass-4 { V is DU before
        the subexpressions of a iff V is DU before a<op>=b } {
    empty_class T1617cdup4 {
	final T1617cdup4 x;
	int i;
	{
	    (x = new T1617cdup4()).i += 1;
	}
    }
} PASS

tcltest::test 16.1.7-compound-definite-unassignment-pass-5 { V is DU before
        b iff V is DU after the subexpressions of a in a<op>=b } {
    empty_main T1617cdup5 {
	final int x;
	int i = 0;
	i += x = 1;
    }
} PASS

tcltest::test 16.1.7-compound-definite-unassignment-pass-6 { V is DU after
        boolean a<op>=b when true iff V is DU after a<op>=b } {
    empty_main T1617cdup6 {
	boolean b = false;
	final int x;
	x = 0;
	if ((b &= false) && false)
	    x = 1;
    }
} PASS

tcltest::test 16.1.7-compound-definite-unassignment-pass-7 { V is DU after
        boolean a<op>=b when false iff V is DU after a<op>=b } {
    empty_main T1617cdup7 {
	boolean b = true;
	final int x;
	x = 0;
	if ((b |= true) || true);
	else x = 1;
    }
} PASS

tcltest::test 16.1.7-compound-definite-unassignment-pass-8 { V must be DU
        after b if V is a blank final and V is a in a<op>=b } {
    empty_main T1617cdup8 {
	final int x;
	int i = 0;
	x = i += 1;
    }
} PASS

