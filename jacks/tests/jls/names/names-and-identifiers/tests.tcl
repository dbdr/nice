tcltest::test 6.2-identifier-1 { Declarations expect an identifier, not a
        qualified name } {
    empty_class T62i1 {
        T62i1 qualified.name;
    }
} FAIL

tcltest::test 6.2-identifier-2 { Declarations expect an identifier, not a
        qualified name } {
    empty_class T62i2 {
        class Qualified.Name {}
    }
} FAIL

tcltest::test 6.2-identifier-3 { Declarations expect an identifier, not a
        qualified name } {
    empty_class T62i3 {
        interface Qualified.Name {}
    }
} FAIL

tcltest::test 6.2-identifier-4 { Declarations expect an identifier, not a
        qualified name } {
    empty_class T62i4 {
        void qualified.name() {}
    }
} FAIL

tcltest::test 6.2-identifier-5 { Declarations expect an identifier, not a
        qualified name } {
    empty_class T62i5 {
        void foo(int qualified.name) {}
    }
} FAIL

tcltest::test 6.2-identifier-6 { Declarations expect an identifier, not a
        qualified name } {
    empty_main T62i6 {
        try {
            throw new Exception();
        } catch (Exception qualified.name) {
        }
    }
} FAIL

tcltest::test 6.2-identifier-7 { Declarations expect an identifier, not a
        qualified name } {
    empty_main T62i7 {
        int qualified.name;
    }
} FAIL

tcltest::test 6.2-identifier-8 { Declarations expect an identifier, not a
        qualified name } {
    empty_main T62i8 {
        for (int qualified.name; ; );
    }
} FAIL

tcltest::test 6.2-identifier-9 { Labels expect an identifier, not a
        qualified name } {
    empty_main T62i9 {
        qualified.label: toString();
    }
} FAIL

tcltest::test 6.2-identifier-10 { Labels expect an identifier, not a
        qualified name } {
    empty_main T62i10 {
        qualified.label: break qualified.label;
    }
} FAIL

tcltest::test 6.2-identifier-11 { Qualified class instance creation expects
        an identifier, not a qualified name } {
    empty_class T62i11 {
        class Inner {}
        Object o = new T62i11().new T62i11.Inner();
    }
} FAIL

tcltest::test 6.2-example-1 { First example from the specs } {
    compile [saveas T62e1.java {
// T62e1 is identifier
class T62e1 {
    // main, args are identifier; String is name
    public static void main(String[] args) {
        // c is identifier; Class, System.out.getClass are names
        Class c = System.out.getClass();
        // length is identifier; System.out.println, c are names
        System.out.println(c.toString().length() + 
        // first length is identifier; args, args.length are names
            args[0].length() + args.length);
    }
}
}]
} PASS

tcltest::test 6.2-example-2 { Second example from the specs: labels are
        independent from other uses of names } {
    empty_class T62e2 {
        char[] value;
        int offset, count;
        int indexOf(T62e2 str, int fromIndex) {
            char[] v1 = value, v2 = str.value;
            int max = offset + (count - str.count);
            int start = offset + ((fromIndex < 0) ? 0 : fromIndex);
        i:
            for (int i = start; i <= max; i++) {
                int n = str.count, j = i, k = str.offset;
                while (n-- != 0) {
                    if (v1[j++] != v2[k++])
                        continue i;
                }
                return i - offset;
            }
            return -1;
        }
    }
} PASS
