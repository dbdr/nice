proc keyword { name keyword } {
    set data "class $name {int ${keyword};}\n"
    return [compile [saveas $name.java $data] ]
}


tcltest::test 3.9-1 { abstract is a keyword } {
    keyword T391 abstract
} FAIL

tcltest::test 3.9-2 { boolean is a keyword } {
    keyword T392 boolean
} FAIL

tcltest::test 3.9-3 { break is a keyword } {
    keyword T393 break
} FAIL

tcltest::test 3.9-4 { byte is a keyword } {
    keyword T394 byte
} FAIL

tcltest::test 3.9-5 { case is a keyword } {
    keyword T395 case
} FAIL

tcltest::test 3.9-6 { catch is a keyword } {
    keyword T396 catch
} FAIL

tcltest::test 3.9-7 { char is a keyword } {
    keyword T397 char
} FAIL

tcltest::test 3.9-8 { class is a keyword } {
    keyword T398 class
} FAIL

tcltest::test 3.9-9 { const is a keyword } {
    keyword T399 const
} FAIL

tcltest::test 3.9-10 { continue is a keyword } {
    keyword T3910 continue
} FAIL

tcltest::test 3.9-11 { default is a keyword } {
    keyword T3911 default
} FAIL

tcltest::test 3.9-12 { do is a keyword } {
    keyword T3912 do
} FAIL

tcltest::test 3.9-13 { double is a keyword } {
    keyword T3913 double
} FAIL

tcltest::test 3.9-14 { else is a keyword } {
    keyword T3914 else
} FAIL

tcltest::test 3.9-15 { extends is a keyword } {
    keyword T3915 extends
} FAIL

tcltest::test 3.9-16 { final is a keyword } {
    keyword T3916 final
} FAIL

tcltest::test 3.9-17 { finally is a keyword } {
    keyword T3917 finally
} FAIL

tcltest::test 3.9-18 { float is a keyword } {
    keyword T3918 float
} FAIL

tcltest::test 3.9-19 { for is a keyword } {
    keyword T3919 for
} FAIL

tcltest::test 3.9-20 { goto is a keyword } {
    keyword T3920 goto
} FAIL

tcltest::test 3.9-21 { if is a keyword } {
    keyword T3921 if
} FAIL

tcltest::test 3.9-22 { implements is a keyword } {
    keyword T3922 implements
} FAIL

tcltest::test 3.9-23 { import is a keyword } {
    keyword T3923 import
} FAIL

tcltest::test 3.9-24 { instanceof is a keyword } {
    keyword T3924 instanceof
} FAIL

tcltest::test 3.9-25 { int is a keyword } {
    keyword T3925 int
} FAIL

tcltest::test 3.9-26 { interface is a keyword } {
    keyword T3926 interface
} FAIL

tcltest::test 3.9-27 { long is a keyword } {
    keyword T3927 long
} FAIL

tcltest::test 3.9-28 { native is a keyword } {
    keyword T3928 native
} FAIL

tcltest::test 3.9-29 { new is a keyword } {
    keyword T3929 new
} FAIL

tcltest::test 3.9-30 { package is a keyword } {
    keyword T3930 package
} FAIL

tcltest::test 3.9-31 { private is a keyword } {
    keyword T3931 private
} FAIL

tcltest::test 3.9-32 { protected is a keyword } {
    keyword T3932 protected
} FAIL

tcltest::test 3.9-33 { public is a keyword } {
    keyword T3933 public
} FAIL

tcltest::test 3.9-34 { return is a keyword } {
    keyword T3934 return
} FAIL

tcltest::test 3.9-35 { short is a keyword } {
    keyword T3935 short
} FAIL

tcltest::test 3.9-36 { static is a keyword } {
    keyword T3936 static
} FAIL

tcltest::test 3.9-37 { strictfp is a keyword,
        it is new to version 2 of the JLS spec } {
    keyword T3937 strictfp
} FAIL

tcltest::test 3.9-38 { super is a keyword } {
    keyword T3938 super
} FAIL

tcltest::test 3.9-39 { switch is a keyword } {
    keyword T3939 switch
} FAIL

tcltest::test 3.9-40 { synchronized is a keyword } {
    keyword T3940 synchronized
} FAIL

tcltest::test 3.9-41 { this is a keyword } {
    keyword T3941 this
} FAIL

tcltest::test 3.9-42 { throw is a keyword } {
    keyword T3942 throw
} FAIL

tcltest::test 3.9-43 { throws is a keyword } {
    keyword T3943 throws
} FAIL

tcltest::test 3.9-44 { transient is a keyword } {
    keyword T3944 transient
} FAIL

tcltest::test 3.9-45 { try is a keyword } {
    keyword T3945 try
} FAIL

tcltest::test 3.9-46 { void is a keyword } {
    keyword T3946 void
} FAIL

tcltest::test 3.9-47 { volatile is a keyword } {
    keyword T3947 volatile
} FAIL

tcltest::test 3.9-48 { while is a keyword } {
    keyword T3948 while
} FAIL

# These may be C or C++ keywords, but they are legal in Java!

tcltest::test 3.9-valid-1 { asm is not a keyword } {
    keyword T39v1 asm
} PASS

tcltest::test 3.9-valid-2 { auto is not a keyword } {
    keyword T39v2 auto
} PASS

tcltest::test 3.9-valid-3 { bool is not a keyword } {
    keyword T39v3 bool
} PASS

tcltest::test 3.9-valid-4 { const_cast is not a keyword } {
    keyword T39v4 const_cast
} PASS

tcltest::test 3.9-valid-5 { delete is not a keyword } {
    keyword T39v5 delete
} PASS

tcltest::test 3.9-valid-6 { dynamic_cast is not a keyword } {
    keyword T39v6 dynamic_cast
} PASS

tcltest::test 3.9-valid-7 { enum is not a keyword } {
    keyword T39v7 enum
} PASS

tcltest::test 3.9-valid-8 { explicit is not a keyword } {
    keyword T39v8 explicit
} PASS

tcltest::test 3.9-valid-9 { extern is not a keyword } {
    keyword T39v9 extern
} PASS

tcltest::test 3.9-valid-10 { friend is not a keyword } {
    keyword T39v10 friend
} PASS

tcltest::test 3.9-valid-11 { inline is not a keyword } {
    keyword T39v11 inline
} PASS

tcltest::test 3.9-valid-12 { mutable is not a keyword } {
    keyword T39v12 mutable
} PASS

tcltest::test 3.9-valid-13 { namespace is not a keyword } {
    keyword T39v13 namespace
} PASS

tcltest::test 3.9-valid-14 { operator is not a keyword } {
    keyword T39v14 operator
} PASS

tcltest::test 3.9-valid-15 { register is not a keyword } {
    keyword T39v15 register
} PASS

tcltest::test 3.9-valid-16 { reinterpret_cast is not a keyword } {
    keyword T39v16 reinterpret_cast
} PASS

tcltest::test 3.9-valid-17 { signed is not a keyword } {
    keyword T39v17 signed
} PASS

tcltest::test 3.9-valid-18 { sizeof is not a keyword } {
    keyword T39v18 sizeof
} PASS

tcltest::test 3.9-valid-19 { static_cast is not a keyword } {
    keyword T39v19 static_cast
} PASS

tcltest::test 3.9-valid-20 { struct is not a keyword } {
    keyword T39v20 struct
} PASS

tcltest::test 3.9-valid-21 { template is not a keyword } {
    keyword T39v21 template
} PASS

tcltest::test 3.9-valid-22 { typedef is not a keyword } {
    keyword T39v22 typedef
} PASS

tcltest::test 3.9-valid-23 { typeid is not a keyword } {
    keyword T39v23 typeid
} PASS

tcltest::test 3.9-valid-24 { typename is not a keyword } {
    keyword T39v24 typename
} PASS

tcltest::test 3.9-valid-25 { union is not a keyword } {
    keyword T39v25 union
} PASS

tcltest::test 3.9-valid-26 { unsigned is not a keyword } {
    keyword T39v26 unsigned
} PASS

tcltest::test 3.9-valid-27 { using is not a keyword } {
    keyword T39v27 using
} PASS

tcltest::test 3.9-valid-28 { virtual is not a keyword } {
    keyword T39v28 virtual
} PASS

tcltest::test 3.9-valid-29 { wchar_t is not a keyword } {
    keyword T39v29 wchar_t
} PASS

