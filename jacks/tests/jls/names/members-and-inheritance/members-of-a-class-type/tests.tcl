tcltest::test 6.4.2-1 { interface member reference can not be ambiguous } {
    saveas AmbiguousInterfaceMember.java \
{
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

class AmbiguousInterfaceMember implements One, Two {
  int i = VAL;
}
}

    compile AmbiguousInterfaceMember.java
} FAIL

tcltest::test 6.4.2-2 { a qualified reference is not ambiguous } {
    saveas QualifiedInterfaceMember.java \
{
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

class QualifiedInterfaceMember implements One, Two {
  int i = One.VAL;
}
}

    compile QualifiedInterfaceMember.java
} PASS
