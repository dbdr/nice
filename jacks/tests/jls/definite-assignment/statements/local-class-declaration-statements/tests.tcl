tcltest::test 16.2.3-switch-1 { The JLS is missing a rule. Although 16.2.2
        claims v is DA before the initializer in Local since it is DA before
        the declaration of Local, this is not true for a local class in a
        switch statement, if an instance is created where v is not DA } {
    empty_class T1623s1 {
	void m(int i) {
	    switch (i) {
            case 0:
		final int j;
		j = 1;
		class Local {
		    int k = j;
		}
		break;
            case 1:
		// Javac 1.4 compiles this, and causes a VerifyError
		int k = new Local().k;
	    }
	}
    }
} FAIL
