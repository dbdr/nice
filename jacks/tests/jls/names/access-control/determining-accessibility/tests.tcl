# public access on toplevel class or interface

tcltest::test 6.6.1-1 {A class declared public is accessible from outside
        the package} {
    saveas AccessPublicClassInPackage.java {
public class AccessPublicClassInPackage {
    pkg.PublicClass ref;
}
}

    saveas pkg/PublicClass.java {
package pkg;

public class PublicClass {}
}

    compile AccessPublicClassInPackage.java pkg/PublicClass.java
} PASS

tcltest::test 6.6.1-2 {A class declared public is accessible from outside
        the package when the .java source is loaded from the CLASSPATH} {
    delete pkg/PublicClass.class
    saveas AccessPublicClassInPackage.java {
public class AccessPublicClassInPackage {
    pkg.PublicClass ref;
}
}

    compile -classpath . AccessPublicClassInPackage.java
} PASS

tcltest::test 6.6.1-3 {A class declared public is accessible from outside
        the package when the .class file is loaded from the CLASSPATH} {
    saveas AccessPublicClassInPackage.java {
public class AccessPublicClassInPackage {
    pkg.PublicClass ref;
}
}

    saveas pkg/PublicClass.java {
package pkg;

public class PublicClass {}
}

    list \
        [compile pkg/PublicClass.java] \
        [compile -classpath . AccessPublicClassInPackage.java]
} {PASS PASS}

tcltest::test 6.6.1-4 {An interface declared public is accessible from outside
        the package} {
    saveas AccessPublicInterfaceInPackage.java {
public class AccessPublicInterfaceInPackage {
    pkg.PublicInterface ref;
}
}

    saveas pkg/PublicInterface.java {
package pkg;

public interface PublicInterface {}
}

    compile AccessPublicInterfaceInPackage.java pkg/PublicInterface.java
} PASS

tcltest::test 6.6.1-5 {An interface declared public is accessible from outside
        the package when the .java source is loaded from the CLASSPATH} {
    delete pkg/PublicInterface.class
    compile -classpath . AccessPublicInterfaceInPackage.java
} PASS

tcltest::test 6.6.1-6 {An interface declared public is accessible from outside
        the package when the .class file is loaded from the CLASSPATH} {
    delete pkg/PublicInterface.class
    list \
        [compile pkg/PublicInterface.java] \
        [compile -classpath . AccessPublicInterfaceInPackage.java]
} {PASS PASS}

tcltest::test 6.6.1-7 {Access a public class from inside the package} {
    saveas pkg/AccessPublicClass.java {
package pkg;

public class AccessPublicClass {
    PublicClass ref;
}
}

    compile pkg/AccessPublicClass.java pkg/PublicClass.java
} PASS


# Default access on toplevel class or interface

tcltest::test 6.6.1-8 {A class declared with default access is inaccessible
        from outside the package} {
    saveas AccessDefaultClassInPackage.java {
public class AccessDefaultClassInPackage {
    pkg.DefaultClass ref;
}
}

    saveas pkg/DefaultClass.java {
package pkg;

class DefaultClass {}
}

    compile AccessDefaultClassInPackage.java pkg/DefaultClass.java
} FAIL

tcltest::test 6.6.1-9 {A class declared with default access is inaccessible
        from outside the package when the .java source is loaded from the
        CLASSPATH} {
    delete pkg/DefaultClass.class
    compile -classpath . AccessDefaultClassInPackage.java
} FAIL

tcltest::test 6.6.1-10 {A class declared with default access is inaccessible
        from outside the package when the .class file is loaded from the
        CLASSPATH} {
    delete pkg/DefaultClass.class
    list \
        [compile pkg/DefaultClass.java] \
        [compile -classpath . AccessDefaultClassInPackage.java]
} {PASS FAIL}

tcltest::test 6.6.1-11 {An interface declared with default access is
        inaccessible from outside the package} {
    saveas AccessDefaultInterfaceInPackage.java {
public class AccessDefaultInterfaceInPackage {
    pkg.DefaultInterface ref;
}
}

    saveas pkg/DefaultInterface.java {
package pkg;

interface DefaultInterface {}
}

    compile AccessDefaultInterfaceInPackage.java pkg/DefaultInterface.java
} FAIL

tcltest::test 6.6.1-12 {An interface declared with default access is
        inaccessible from outside the package when the .java source is
        loaded from the CLASSPATH} {
    delete pkg/DefaultInterface.class
    compile -classpath . AccessDefaultInterfaceInPackage.java
} FAIL

tcltest::test 6.6.1-13 {An interface declared with default access is
        inaccessible from outside the package when the .class file is
        loaded from the CLASSPATH} {
    delete pkg/DefaultInterface.class
    list \
        [compile pkg/DefaultInterface.java] \
        [compile -classpath . AccessDefaultInterfaceInPackage.java]
} {PASS FAIL}

tcltest::test 6.6.1-14 {Access a default class from inside the package} {
    saveas pkg/AccessDefaultClass.java {
package pkg;

public class AccessDefaultClass {
    DefaultClass ref;
}
}

    compile pkg/AccessDefaultClass.java pkg/DefaultClass.java
} PASS

# protected access should be covered in section 6.6.2

# Access of array types
tcltest::test 6.6.1-array-1 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a1.java {
class T661a1 {
    pkg.Array1[] ref;
}
}
    saveas pkg/Array1.java {
package pkg;
public class Array1 {}
}
    compile T661a1.java pkg/Array1.java
} PASS

tcltest::test 6.6.1-array-2 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a2.java {
class T661a2 {
    pkg.Array2[] ref;
}
}
    saveas pkg/Array2.java {
package pkg;
class Array2 {}
}
    compile T661a2.java pkg/Array2.java
} FAIL

tcltest::test 6.6.1-array-3 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a3.java {
class T661a3 {
    pkg.Array3.A[] ref;
}
}
    saveas pkg/Array3.java {
package pkg;
public class Array3 {
    protected static class A {}
}
}
    compile T661a3.java pkg/Array3.java
} FAIL

tcltest::test 6.6.1-array-4 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a4.java {
class T661a4 extends pkg.Array4 {
    pkg.Array4.A[] ref;
}
}
    saveas pkg/Array4.java {
package pkg;
public class Array4 {
    protected static class A {}
}
}
    compile T661a4.java pkg/Array4.java
} PASS

tcltest::test 6.6.1-array-5 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a5.java {
class T661a5 extends pkg.Array5 {
    pkg.Array5.A[] ref;
}
}
    saveas pkg/Array5.java {
package pkg;
public class Array5 {
    private static class A {}
}
}
    compile T661a5.java pkg/Array5.java
} FAIL

tcltest::test 6.6.1-array-6 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a6.java {
class T661a6 {
    Array6.A[] ref;
}
class Array6 {
    protected static class A {}
}
}
    compile T661a6.java
} PASS

tcltest::test 6.6.1-array-7 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a7.java {
class T661a7 {
    Array7.A[] ref;
}
class Array7 {
    static class A {}
}
}
    compile T661a7.java
} PASS

tcltest::test 6.6.1-array-8 { Arrays are exactly as accessible as their
        component type } {
    saveas T661a8.java {
class T661a8 {
    Array8.A[] ref;
}
class Array8 {
    private static class A {}
}
}
    compile T661a8.java
} FAIL

tcltest::test 6.6.1-array-9 { Arrays are exactly as accessible as their
        component type } {
    empty_class T661a9 {
        class Inner {
            Array[] ref;
        }
        private static class Array {}
    }
} PASS

tcltest::test 6.6.1-array-10 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a10.java {
class T661a10 {
    int i = pkg.Array10.array.length;
}
}
    saveas pkg/Array10.java {
package pkg;
public class Array10 {
    public static A[] array;
    public static class A {}
}
}
    compile T661a10.java pkg/Array10.java
} PASS

tcltest::test 6.6.1-array-11 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a11.java {
class T661a11 {
    int i = pkg.Array11.array.length;
}
}
    saveas pkg/Array11.java {
package pkg;
public class Array11 {
    public static A[] array;
    protected static class A {}
}
}
    compile T661a11.java pkg/Array11.java
} FAIL

tcltest::test 6.6.1-array-12 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a12.java {
class T661a12 extends pkg.Array12 {
    int i = pkg.Array12.array.length;
}
}
    saveas pkg/Array12.java {
package pkg;
public class Array12 {
    public static A[] array;
    protected static class A {}
}
}
    compile T661a12.java pkg/Array12.java
} PASS

tcltest::test 6.6.1-array-13 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a13.java {
class T661a13 {
    int i = pkg.Array13.array.length;
}
}
    saveas pkg/Array13.java {
package pkg;
public class Array13 {
    public static A[] array;
    static class A {}
}
}
    compile T661a13.java pkg/Array13.java
} FAIL

tcltest::test 6.6.1-array-14 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a14.java {
class T661a14 {
    int i = pkg.Array14.array.length;
}
}
    saveas pkg/Array14.java {
package pkg;
public class Array14 {
    public static A[] array;
    private static class A {}
}
}
    compile T661a14.java pkg/Array14.java
} FAIL

tcltest::test 6.6.1-array-15 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a15.java {
class T661a15 {
    int i = Array15.array.length;
}
class Array15 {
    static A[] array;
    protected static class A {}
}
}
    compile T661a15.java
} PASS

tcltest::test 6.6.1-array-16 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a16.java {
class T661a16 {
    int i = Array16.array.length;
}
class Array16 {
    static A[] array;
    static class A {}
}
}
    compile T661a16.java
} PASS

tcltest::test 6.6.1-array-17 { The public length field of an array is
        accessible only if the array class is accessible } {
    saveas T661a17.java {
class T661a17 {
    int i = Array17.array.length;
}
class Array17 {
    static A[] array;
    private static class A {}
}
}
    compile T661a17.java
} FAIL

tcltest::test 6.6.1-array-18 { The public length field of an array is
        accessible only if the array class is accessible } {
    empty_class T661a18 {
        class Inner {
            int i = Array18.array.length;
        }
        static class Array18 {
            static A[] array;
            private static class A {}
        }
    }
} PASS

tcltest::test 6.6.1-array-19 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a19.java {
class T661a19 {
    Object o = pkg.Array19.array.toString();
}
}
    saveas pkg/Array19.java {
package pkg;
public class Array19 {
    public static A[] array;
    public static class A {}
}
}
    compile T661a19.java pkg/Array19.java
} PASS

tcltest::test 6.6.1-array-20 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a20.java {
class T661a20 {
    Object o = pkg.Array20.array.toString();
}
}
    saveas pkg/Array20.java {
package pkg;
public class Array20 {
    public static A[] array;
    protected static class A {}
}
}
    compile T661a20.java pkg/Array20.java
} FAIL

tcltest::test 6.6.1-array-21 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a21.java {
class T661a21 extends pkg.Array21 {
    Object o = pkg.Array21.array.toString();
}
}
    saveas pkg/Array21.java {
package pkg;
public class Array21 {
    public static A[] array;
    protected static class A {}
}
}
    compile T661a21.java pkg/Array21.java
} PASS

tcltest::test 6.6.1-array-22 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a22.java {
class T661a22 {
    Object o = pkg.Array22.array.toString();
}
}
    saveas pkg/Array22.java {
package pkg;
public class Array22 {
    public static A[] array;
    static class A {}
}
}
    compile T661a22.java pkg/Array22.java
} FAIL

tcltest::test 6.6.1-array-23 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a23.java {
class T661a23 {
    Object o = pkg.Array23.array.toString();
}
}
    saveas pkg/Array23.java {
package pkg;
public class Array23 {
    public static A[] array;
    private static class A {}
}
}
    compile T661a23.java pkg/Array23.java
} FAIL

tcltest::test 6.6.1-array-24 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a24.java {
class T661a24 {
    Object o = Array24.array.toString();
}
class Array24 {
    static A[] array;
    protected static class A {}
}
}
    compile T661a24.java
} PASS

tcltest::test 6.6.1-array-25 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a25.java {
class T661a25 {
    Object o = Array25.array.toString();
}
class Array25 {
    static A[] array;
    static class A {}
}
}
    compile T661a25.java
} PASS

tcltest::test 6.6.1-array-26 { A public member method of an array is
        accessible only if the array class is accessible } {
    saveas T661a26.java {
class T661a26 {
    Object o = Array26.array.toString();
}
class Array26 {
    static A[] array;
    private static class A {}
}
}
    compile T661a26.java
} FAIL

tcltest::test 6.6.1-array-27 { A public member method of an array is
        accessible only if the array class is accessible } {
    empty_class T661a27 {
        class Inner {
            Object o = Array27.array.toString();
        }
        static class Array27 {
            static A[] array;
            private static class A {}
        }
    }
} PASS

tcltest::test 6.6.1-array-28 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a28.java {
class T661a28 {
    Object o = pkg.Array28.array.clone();
}
}
    saveas pkg/Array28.java {
package pkg;
public class Array28 {
    public static A[] array;
    public static class A {}
}
}
    compile T661a28.java pkg/Array28.java
} PASS

tcltest::test 6.6.1-array-29 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a29.java {
class T661a29 {
    Object o = pkg.Array29.array.clone();
}
}
    saveas pkg/Array29.java {
package pkg;
public class Array29 {
    public static A[] array;
    protected static class A {}
}
}
    compile T661a29.java pkg/Array29.java
} FAIL

tcltest::test 6.6.1-array-30 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a30.java {
class T661a30 extends pkg.Array30 {
    Object o = pkg.Array30.array.clone();
}
}
    saveas pkg/Array30.java {
package pkg;
public class Array30 {
    public static A[] array;
    protected static class A {}
}
}
    compile T661a30.java pkg/Array30.java
} PASS

tcltest::test 6.6.1-array-31 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a31.java {
class T661a31 {
    Object o = pkg.Array31.array.clone();
}
}
    saveas pkg/Array31.java {
package pkg;
public class Array31 {
    public static A[] array;
    static class A {}
}
}
    compile T661a31.java pkg/Array31.java
} FAIL

tcltest::test 6.6.1-array-32 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a32.java {
class T661a32 {
    Object o = pkg.Array32.array.clone();
}
}
    saveas pkg/Array32.java {
package pkg;
public class Array32 {
    public static A[] array;
    private static class A {}
}
}
    compile T661a32.java pkg/Array32.java
} FAIL

tcltest::test 6.6.1-array-33 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a33.java {
class T661a33 {
    Object o = Array33.array.clone();
}
class Array33 {
    static A[] array;
    protected static class A {}
}
}
    compile T661a33.java
} PASS

tcltest::test 6.6.1-array-34 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a34.java {
class T661a34 {
    Object o = Array34.array.clone();
}
class Array34 {
    static A[] array;
    static class A {}
}
}
    compile T661a34.java
} PASS

tcltest::test 6.6.1-array-35 { The public clone method of an array is
        accessible only if the array class is accessible } {
    saveas T661a35.java {
class T661a35 {
    Object o = Array35.array.clone();
}
class Array35 {
    static A[] array;
    private static class A {}
}
}
    compile T661a35.java
} FAIL

tcltest::test 6.6.1-array-36 { The public clone method of an array is
        accessible only if the array class is accessible } {
    empty_class T661a36 {
        class Inner {
            Object o = Array36.array.clone();
        }
        static class Array36 {
            static A[] array;
            private static class A {}
        }
    }
} PASS
