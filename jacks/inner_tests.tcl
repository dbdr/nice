# This file is not longer used, the individual test are being converted over


# Warn only applies in jikes!!!

#tcltest::test AbstractInnerInterface_InInnerface {should warn about implicit abstract keyword} {
#  compile AbstractInnerInterface_InInnerface.java
#} WARN


# For results that depend on compiler, should we use something like this?
# [compiler_results WARN {javac PASS} {gcj PASS}]

#tcltest::test AbstractInnerInterface {warn on abstract keyword on inner interface} {
#    compile AbstractInnerInterface.java
#} WARN








# bunch of tests that should fail ( Depends on new JLS spec )

tcltest::test FinalInnerInterface {final keyword not allowed on inner interface} {
  compile FinalInnerInterface.java
} FAIL

tcltest::test InnerInterfaceIsStatic {access crosses static boundry} {
  compile InnerInterfaceIsStatic.java
} FAIL



tcltest::test PrivateInnerInterface {extended interface is private} {
  compile PrivateInnerInterface.java
} FAIL




# Add all of my goofy method test cases, not just
# these two !!!!!!!!!!!

tcltest::test MethodFailure2 {method invocation is ambiguous} {
  compile MethodFailure2.java
} FAIL

tcltest::test MethodInvoker5 {we really should try to run this test!} {
  compile MethodInvoker5.java
} PASS







# Bunch of tests that should pass

#tcltest::test OneTwo {circular dependence} {
#    compile One.java Two.java
#} PASS

#tcltest::test AbstractMethodAbstractClass {} {
#  compile AbstractMethodAbstractClass.java
#} PASS

tcltest::test AbstractMethodInnerInterface {} {
  compile AbstractMethodInnerInterface.java
} PASS

tcltest::test AbstractMethodInnerInterface_InInterface {} {
  compile AbstractMethodInnerInterface_InInterface.java
} PASS

tcltest::test AbstractMethodInterface {} {
  compile AbstractMethodInterface.java
} PASS

tcltest::test ExtendInnerInterface {} {
  compile ExtendInnerInterface.java
} PASS

tcltest::test InnerClass {} {
  compile InnerClass.java
} PASS

tcltest::test InnerInterface {} {
  compile InnerInterface.java
} PASS

tcltest::test InnerInterface2 {} {
  compile InnerInterface2.java
} PASS

tcltest::test ProtectedInnerInterface {} {
  compile ProtectedInnerInterface.java
} PASS

tcltest::test ProtectedInnerInterface_Test1 {} {
  compile ProtectedInnerInterface_Test1.java
} PASS

tcltest::test ProtectedInnerInterface_Test2 {} {
  compile ProtectedInnerInterface_Test2.java
} PASS

tcltest::test ProtectedInnerInterface_Test3 {} {
  compile ProtectedInnerInterface_Test3.java
} PASS

tcltest::test PublicInnerClass {} {
  compile PublicInnerClass.java
} PASS

tcltest::test PublicInnerInterface {} {
  compile PublicInnerInterface.java
} PASS

tcltest::test PublicInnerInterface_InInterface {} {
  compile PublicInnerInterface_InInterface.java
} PASS

tcltest::test PublicMethodInnerInterface {} {
  compile PublicMethodInnerInterface.java
} PASS

# FIXME : should this warn?

tcltest::test PublicMethodInterface {} {
  compile PublicMethodInterface.java
} PASS

# Checked in as tests/gcj/PR198.java
#tcltest::test Regexp {} {
#  compile Regexp.java
#} PASS

tcltest::test DefaultStaticInnerClass {} {
  compile DefaultStaticInnerClass.java
} PASS

tcltest::test PublicStaticInnerClass {} {
  compile PublicStaticInnerClass.java
} PASS

tcltest::test StaticInnerInterface {} {
  compile StaticInnerInterface.java
} PASS

tcltest::test DefaultStaticInnerClass_InPackage {} {
  compile DefaultStaticInnerClass_InPackage.java
} PASS

tcltest::test DefaultStaticInnerClass_InPackage_ExternalTest {} {
  compile DefaultStaticInnerClass_InPackage.java DefaultStaticInnerClass_InPackage_ExternalTest.java
} PASS

tcltest::test InterfaceWithInnerClass {Known bug in Sun's javac} {
  compile InterfaceWithInnerClass.java
} PASS

tcltest::test ClassInInterface {check default access on an inner class defined inside of an interface} {
  compile ClassInInterface.java ClassInInterface_OtherPkgTest.java
} PASS




# It is really not clear what should happen in this case. One
# could make a good argument that protected inner classes
# are like protected members of a class, and are therefore
# accessable from inside the same package (global in this case)

tcltest::test ProtectedInnerInterface {see if protected keyword on inner interface
disallows access to others while still allowing access to those that derive} {

  list \
  [compile ProtectedInnerInterface.java ProtectedInnerInterface_Test1.java] \
  [compile ProtectedInnerInterface.java ProtectedInnerInterface_Test2.java] \
  [compile ProtectedInnerInterface.java ProtectedInnerInterface_Test3.java]

} {PASS FAIL FAIL}




tcltest::test ProtectedInnerInterface_OtherPackage {see if protected keyword
on inner interface disallows access to classes in other packages while
still allowing access to classes that derive} {

  list \
  [compile ProtectedInnerInterface.java ProtectedInnerInterface_OtherPkgTest1.java] \
  [compile ProtectedInnerInterface.java ProtectedInnerInterface_OtherPkgTest2.java] \
  [compile ProtectedInnerInterface.java ProtectedInnerInterface_OtherPkgTest3.java]

} {PASS FAIL FAIL}

