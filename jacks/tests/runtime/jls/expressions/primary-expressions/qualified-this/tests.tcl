tcltest::test 15.8.4-runtime-1 { Qualified this refers to the lexically
        enclosing instance } {runtime} {
    compile_and_run [saveas T1584r1.java {
class T1584r1 {
    String member;
    T1584r1(String value) { member = value; }
    T1584r1() { }
    class Inner extends T1584r1 {
        Inner() {
            this.member = T1584r1.this.member; // refers to two distinct fields
        }
    }
    public static void main(String[] args) {
        T1584r1 t = new T1584r1("value");
        System.out.print(t.member + " ");
        System.out.print(t.new Inner().member);
    }
}
    }]
} {value value}

