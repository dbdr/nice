tcltest::test 6.4.3-1 { interface member reference can not be ambiguous } {
    saveas AmbiguousExtendedInterfaceMember.java \
{
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

interface AmbiguousExtendedInterfaceMember extends One, Two {
  int i = VAL;
}
}

    compile AmbiguousExtendedInterfaceMember.java
} FAIL

tcltest::test 6.4.3-2 { a qualified reference is not ambiguous } {
    saveas QualifiedExtendedInterfaceMember.java \
{
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

interface QualifiedExtendedInterfaceMember extends One, Two {
  int i = Two.VAL;
}
}

    compile QualifiedExtendedInterfaceMember.java
} PASS

# FIXME: How can we test for abstract Object method declarations ???
#
# FROM JLS:
# If an interface has no direct superinterfaces, then the interface
# implicitly declares a public abstract member method m
# with signature s, return type r, and throws clause t corresponding
# to each public instance method m with signature
# s, return type r, and throws clause t declared in Object, unless
# a method with the same signature, same return
# type, and a compatible throws clause is explicitly declared by the interface. 
