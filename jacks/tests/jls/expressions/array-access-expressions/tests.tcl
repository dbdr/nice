tcltest::test 15.13-anon-1 { According to the grammar in Chapter 18, it
        is also legal to acess the element of an anonymous array } {
    empty_main T1513a1 {
        int i = new int[]{1}[0];
    }
} PASS

# FIXME: Add more tests of array access

        
