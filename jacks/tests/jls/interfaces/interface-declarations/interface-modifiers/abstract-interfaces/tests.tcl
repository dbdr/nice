tcltest::test 9.1.1.1-1 { The compiler may generate a warning when
        abstract modifier is applied to an implicitly abstract interface } {
    saveas AbstractInterface.java "abstract interface AbstractInterface {}"
    ok_pass_or_warn [compile AbstractInterface.java]
} OK

tcltest::test 9.1.1.1-2 { The compiler may generate a warning when
        abstract modifier is applied to an implicitly abstract inner interface } {
    saveas AbstractInnerInterface.java \
{
public class AbstractInnerInterface {
    abstract interface Inter {}
}
}
    ok_pass_or_warn [compile AbstractInnerInterface.java]
} OK

tcltest::test 9.1.1.1-3 { The compiler may generate a warning when
        abstract modifier is applied to an implicitly abstract inner interface
        that is defined inside another interface } {
    saveas AbstractInnerInterface_InInnerface.java \
{
public interface AbstractInnerInterface_InInnerface {
    abstract interface Inter {}
}
}
    ok_pass_or_warn [compile AbstractInnerInterface_InInnerface.java]
} OK
