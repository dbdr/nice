tcltest::test 15.18.1.1-runtime-1 { "" + char[] should behave the
        same as char[].toString() } {runtime} {
    compile_and_run [saveas T151811r1.java {
class T151811r1 {
    public static void main(String args[]) {
	char[] c = {'w','r','o','n','g'};
	String s = "" + c;
	if (s.equals(c.toString()))
    	    System.out.print("OK");
	else
	    System.out.print("WRONG");
    }
}
    }]
} OK

tcltest::test 15.18.1.1-runtime-2 { "" + char[] should behave the
        same as char[].toString() } {runtime} {
    compile_and_run [saveas T151811r2.java {
class T151811r2 {
    public static void main(String args[]) {
	char[] c = null;
        try {
            String s = "" + c;
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

