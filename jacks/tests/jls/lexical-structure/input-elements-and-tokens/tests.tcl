tcltest::test 3.5-1 { The input sequence CRCR (0x0d0d) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \r\r
    compile [saveas T351.java "class T351 {$S$S}"]
} PASS

tcltest::test 3.5-2 { The input sequence LFLF (0x0a0a) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \n\n
    compile [saveas T352.java "class T352 {$S$S}"]
} PASS

tcltest::test 3.5-3 {  The input sequence LFCR (0x0a0d) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \n\r
    compile [saveas T353.java "class T353 {$S$S}"]
} PASS

tcltest::test 3.5-4 {  The input sequence CRLFCRLF (0x0d0a0d0a) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \r\n\r\n
    compile [saveas T354.java "class T354 {$S$S}"]
} PASS

tcltest::test 3.5-5 {  The input sequence CRCRLF (0x0d0d0a) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \r\r\n
    compile [saveas T355.java "class T355 {$S$S}"]
} PASS

tcltest::test 3.5-6 {  The input sequence LFCRLF (0x0a0d0a) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \n\r\n
    compile [saveas T356.java "class T356 {$S$S}"]
} PASS

tcltest::test 3.5-7 {  The input sequence CRLFCR (0x0d0a0d) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \r\n\r
    compile [saveas T357.java "class T357 {$S$S}"]
} PASS

tcltest::test 3.5-8 {  The input sequence CRLFLF (0x0d0a0a) should be treated as two
        line terminators with an empty collection of input elements between them.} {
    set S \r\n\n
    compile [saveas T358.java "class T358 {$S$S}"]
} PASS

tcltest::test 3.5-9 {  The ASCII SUB character (0x1a) should be ignored if it is
        the last character in the escaped stream. } {
    set S \x1a
    compile [saveas T359.java "class T359 {  }$S"]
} PASS

tcltest::test 3.5-10 {  The ASCII SUB character (0x1a) should be ignored if it is
        the last character in the escaped stream. } {
    set S \\u001a
    compile [saveas T3510.java "class T3510 {  }$S"]
} PASS

tcltest::test 3.5-11 {  The ASCII SUB character (0x1a) should be ignored if it is
        the last character in the escaped stream. } {
    set S \x1a
    compile [saveas T3511.java "class T3511 { $S }"]
} FAIL
