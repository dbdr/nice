tcltest::test 15.21.3-runtime-1 { Test (non)equality } {runtime} {
    compile_and_run [saveas T15213r1.java {
class T15213r1 {
    public boolean equals(Object o) {
	return this == o;
    }
    boolean not(Object o) {
	return this != o;
    }
    public static void main(String[] args) {
	T15213r1 t = new T15213r1();
	System.out.print(t.equals("") + " " + t.not("") + " ");
	System.out.print(t.equals(t) + " " + t.not(t));
    }
}
    }]
} {false true true false}

tcltest::test 15.21.3-runtime-2 { Strings are == only if they are same
        reference, and not because they have same contents } {runtime} {
    compile_and_run [saveas T15213r2.java {
class T15213r2 {
    public static void main(String[] args) {
	System.out.print(new String("") == "");
    }
}
    }]
} false
