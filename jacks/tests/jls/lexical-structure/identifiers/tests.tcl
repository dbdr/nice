proc identifier { name identifier } {
    set data "class $name {int ${identifier} = 0;}\n"
    return [compile [saveas $name.java $data]]
}

tcltest::test 3.8-invalid-1 { A keyword cannot be an identifier } {
    identifier T38i1 int
} FAIL

tcltest::test 3.8-invalid-2 { A keyword cannot be an identifier } {
    identifier T38i2 {\u0069\u006e\u0074}
} FAIL

tcltest::test 3.8-invalid-3 { A boolean literal cannot be an identifier } {
    identifier T38i3 true
} FAIL

tcltest::test 3.8-invalid-4 { A boolean literal cannot be an identifier } {
    identifier T38i4 {\u0074\u0072\u0075\u0065}
} FAIL

tcltest::test 3.8-invalid-5 { A null literal cannot be an identifier } {
    identifier T38i5 null
} FAIL

tcltest::test 3.8-invalid-6 { A unicode null literal cannot be an identifier } {
    identifier T38i6 {\u006e\u0075\u006c\u006c}
} FAIL

tcltest::test 3.8-invalid-7 { The first character of an identifier
        must satisfy Character.isJavaIdentifierStart } {
    identifier T38i7 1
} FAIL

tcltest::test 3.8-invalid-8 { Trailing characters in an identifier must
        satisfy Character.isJavaIdentifierPart } {
    identifier T38i8 i\\uffff
} FAIL

tcltest::test 3.8-invalid-9 { Invalid, as \0 is not short for \u0000 } {
    identifier T38i9 i\\0
} FAIL

# Valid identifiers

tcltest::test 3.8-valid-1 { example identifier } {
    identifier T38v1 String
} PASS

tcltest::test 3.8-valid-2 { example identifier } {
    identifier T38v2 i3
} PASS

tcltest::test 3.8-valid-3 { example identifier } {
    identifier T38v3 MAX_VALUE
} PASS

tcltest::test 3.8-valid-4 { example identifier } {
    identifier T38v4 isLetterOrDigit
} PASS

tcltest::test 3.8-valid-5 { valid identifier } {
    identifier T38v5 _
} PASS

tcltest::test 3.8-valid-6 { valid identifier, although discouraged } {
    ok_pass_or_warn [identifier T38v6 \$] 
} OK

tcltest::test 3.8-valid-7 { valid identifier, although discouraged } {
    ok_pass_or_warn [identifier T38v7 a_\$1] 
} OK

tcltest::test 3.8-valid-8 { valid identifier, as \ufeff satisfies
        Character.isJavaIdentifierPart } {
    identifier T38v8 i\\ufeff
} PASS

tcltest::test 3.8-valid-9 { valid identifier, as \u0000 satisfies
        Character.isJavaIdentifierPart } {
    identifier T38v9 i\\u0000
} PASS

tcltest::test 3.8-valid-10 { example of unique identifiers } {
    compile [saveas T38v10.java {
class T38v10 {
    int \u0041; // LATIN CAPITAL A
    int \u0061; // LATIN SMALL A
    int \u0391; // GREEK CAPITAL ALPHA
    int \u0480; // CYRILLIC SMALL A
}
    }]
} PASS

tcltest::test 3.8-valid-11 { example of unique identifiers } {
    compile [saveas T38v11.java {
class T38v11 {
    int \u00c1; // LATIN CAPITAL A ACUTE
    int \u0041\u0301; // LATIN CAPITAL A, followed by NON-SPACING ACUTE
}
    }]
} PASS

tcltest::test 3.8-valid-12 { valid identifier, as \u001a satisfies
        Character.isJavaIdentifierPart } {
    identifier T38v12 i\\u001a
} PASS

tcltest::test 3.8-ignorable-1 { JCL docs refer to 200a, but this is a typo } {
    compile [saveas T38ignoreable1.java {
class T38ignoreable1 {
    int i\u200a;
}
    }]
} FAIL

tcltest::test 3.8-ignorable-2 { JCL docs refer to 200a, but real value is 202a } {
    compile [saveas T38ignoreable2.java {
class T38ignoreable2 {
    int i\u202a;
}
    }]
} PASS
