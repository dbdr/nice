tcltest::test 14.3-1 { tests local class declarations in both static and
        instance methods } {
    empty_class TestLCD {
        static Object static_method() {
            abstract class Foo { }
            return new Foo() { };
        }
        Object instance_method() {
            abstract class Foo { }
            return new Foo() { };
        }
    }
} PASS

tcltest::test 14.3-scope-1 { The scope of a local class includes its
        declaration } {
    empty_main T143s1 {
	class Local extends Local {}
    }
} FAIL

tcltest::test 14.3-scope-2 { The scope of a local class starts at its
        declaration, not before } {
    empty_main T143s2 {
	new Local();
	class Local {}
    }
} FAIL

tcltest::test 14.3-scope-3 { The scope of a local class continues to the end
        of the current block } {
    empty_main T143s3 {
	class Local {
	    { new Local() {}; }
	}
	new Local();
	{
	    new Local();
	}
    }
} PASS

tcltest::test 14.3-scope-4 { The scope of a local class continues to the end
        of the current block } {
    empty_main T143s4 {
	class Local {}
	class Local {}
    }
} FAIL

tcltest::test 14.3-scope-5 { The scope of a local class continues to the end
        of the current block } {
    empty_main T143s5 {
	class Local {}
	{
	    class Local {}
	}
    }
} FAIL

tcltest::test 14.3-scope-6 { The scope of a local class continues to the end
        of the current block, but may be shadowed in other classes } {
    empty_main T143s6 {
	class Local {}
	{
	    new Object() {
		class Local {}
	    };
	}
	new Object() {
	    class Local {}
	};
    }
} PASS

tcltest::test 14.3-scope-7 { The scope of a local class continues to the end
        of the current block } {
    empty_main T143s7 {
	{
	    class Local {}
	}
	class Local {}
    }
} PASS

tcltest::test 14.3-scope-8 { Example of scoping, minus failures } {
    empty_class T143s8 {
	class Cyclic {}
	void foo() {
	    new Cyclic(); // create a T143s8.Cyclic
	    {
		class Local{};
		{
		    class AnotherLocal {
			void bar() {
			    class Local {}; // ok
			}
		    }
		}
	    }
	    class Local{}; // ok, not in scope of prior Local
	}
    }
} PASS

tcltest::test 14.3-scope-9 { The scope of a local class starts at its
        declaration, not before } {
    empty_class T143s9 {
	class Local {
	    static final int i = 1;
	}
	void m(int j) {
	    switch (j) {
		case 0:
		case (1 == Local.i ? 1 : 0):
	    }
	    class Local {
		static final int i = 2;
	    }
	    switch (j) {
		case 0:
		case (2 == Local.i ? 1 : 0):
	    }
	}
    }
} PASS

tcltest::test 14.3-scope-10 { While not well-specified, the scope of a local
        class in a switch statement extends to the end of the switch } {
    empty_class T143s10 {
	void m(int i) {
	    switch (i) {
		case 0:
		class Local {}
		break;
		case 1:
		new Local();
	    }
	}
    }
} PASS

tcltest::test 14.3-scope-11 { While not well-specified, the scope of a local
        class in a switch statement extends to the end of the switch } {
    empty_class T143s11 {
	void m(int i) {
	    switch (i) {
		case 0:
		class Local {}
		break;
		case 1:
		class Local {}
	    }
	}
    }
} FAIL
