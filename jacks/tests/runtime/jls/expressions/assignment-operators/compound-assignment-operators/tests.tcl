tcltest::test 15.26.2-runtime-1 { String += char[] should do
        same as s += char[].toString() } {runtime} {
    compile_and_run [saveas T15262r1.java {
class T15262r1 {
    public static void main(String args[]) {
	String s = "";
	char[] c = {'w','r','o','n','g'};
	s += c;
	if (s.equals(c.toString()))
	    System.out.print("OK");
	else
	    System.out.print("WRONG");
    }
}
    }]
} OK

tcltest::test 15.26.2-runtime-2 { String += char[] should do
        same as s += char[].toString() } {runtime} {
    compile_and_run [saveas T15262r2.java {
class T15262r2 {
    public static void main(String args[]) {
        String s = "";
	char[] c = null;
        try {
            s += c;
            if (s.equals("null"))
    	        System.out.print("OK");
            else
	        System.out.print("WRONG string");
        } catch (NullPointerException e) {
            System.out.print("WRONG method");
        }
    }
}
    }]
} OK

