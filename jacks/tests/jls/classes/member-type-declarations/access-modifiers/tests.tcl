# See 8.1.1 for more class access modifier tests

tcltest::test 8.5.1-valid-modifier-1 { public is a
        valid ClassModifier for a member class } {
    empty_class T851vm1 {
    public class Inner {}
    }
} PASS

tcltest::test 8.5.1-valid-modifier-2 { protected
        is a valid ClassModifier for a member class } {
    empty_class T851vm2 {
    protected class Inner {}
    }
} PASS

tcltest::test 8.5.1-valid-modifier-3 { private
        is a valid ClassModifier for a member class } {
    empty_class T851vm3 {
    protected class Inner {}
    }
} PASS

tcltest::test 8.5.1-valid-modifier-4 { static
        is a valid ClassModifier for a member class } {
    empty_class T851vm4 {
    static class Member {}
    }
} PASS

tcltest::test 8.5.1-valid-modifier-5 { strictfp
        is a valid ClassModifier for a member class } {
    empty_class T851vm5 {
    strictfp class Member {}
    }
} PASS

# duplicate or conflicting modifiers

tcltest::test 8.5.1-duplicate-modifier-1 { compile time
        error to specify a class modifier twice } {
    empty_class T851dm1 {
    static static class Member {}
    }
} FAIL

tcltest::test 8.5.1-duplicate-modifier-2 { compile time
        error to specify a class modifier twice } {
    empty_class T851dm2 {
    protected protected class Inner {}
    }
} FAIL

tcltest::test 8.5.1-duplicate-modifier-3 { compile time
        error to specify a class modifier twice } {
    empty_class T851dm3 {
    private private class Inner {}
    }
} FAIL


tcltest::test 8.5.1-conflicting-modifier-1 { conflicting
        modifiers must generate a compile time error } {
    empty_class T851cm1 {
    public private class Inner {}
    }
} FAIL

tcltest::test 8.5.1-conflicting-modifier-2 { conflicting
        modifiers must generate a compile time error } {
    empty_class T851cm2 {
    public protected class Inner {}
    }
} FAIL

tcltest::test 8.5.1-conflicting-modifier-3 { conflicting
        modifiers must generate a compile time error } {
    empty_class T851cm3 {
    protected public class Inner {}
    }
} FAIL

tcltest::test 8.5.1-conflicting-modifier-4 { conflicting
        modifiers must generate a compile time error } {
    empty_class T851cm4 {
    protected private class Inner {}
    }
} FAIL

tcltest::test 8.5.1-conflicting-modifier-5 { conflicting
        modifiers must generate a compile time error } {
    empty_class T851cm5 {
    private public class Inner {}
    }
} FAIL

tcltest::test 8.5.1-conflicting-modifier-6 { conflicting
        modifiers must generate a compile time error } {
    empty_class T851cm6 {
    private protected class Inner {}
    }
} FAIL

