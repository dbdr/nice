tcltest::test 15.9.2-inner-member-1 { if C is an inner member, and the
        creation expression is unqualified, the enclosing instance is the
        innermost enclosing instance of the correct type at the point
        of creation } {
    compile [saveas T1592im1.java {
interface T1592im1 {
    class O {
        class C {} 
	O() {
            new C();
        }
    }
}
    }]
} PASS

# TODO: Add tests for all the cases in this section. The JLS is somewhat
# ambiguous or contradictory, care must be taken in writing this section.
