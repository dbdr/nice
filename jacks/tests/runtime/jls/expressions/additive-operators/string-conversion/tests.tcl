tcltest::test 15.18.1.1-runtime-1 { "" + char[] should behave the
        same as char[].toString() } {runtime} {
    empty_main T151811r1 {
	char[] c = {'w','r','o','n','g'};
	String s = "" + c;
	if (s.equals(c.toString()))
    	    System.out.print("OK");
	else
	    System.out.print("WRONG");
    } run
} OK

tcltest::test 15.18.1.1-runtime-2 { "" + char[] should behave the
        same as char[].toString() } {runtime} {
    empty_main T151811r2 {
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
    } run
} OK

