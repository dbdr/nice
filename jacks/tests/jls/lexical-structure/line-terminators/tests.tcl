tcltest::test 3.4-1 { CR (\r) is a LineTerminator } {
    set S \r
    compile [saveas T341.java "class T341 {$S // $S}"]
} PASS

tcltest::test 3.4-2 { CR (\r) is a LineTerminator } {
    set S \r
    compile [saveas T342.java "class T342 { // $S}"]
} PASS

tcltest::test 3.4-3 { LF (\n) is a LineTerminator } {
    set S \n
    compile [saveas T343.java "class T343 {$S // $S}"]
} PASS

tcltest::test 3.4-4 { LF (\n) is a LineTerminator } {
    set S \n
    compile [saveas T344.java "class T344 { // $S}"]
} PASS

tcltest::test 3.4-5 { CR + LF (\r\n) is a LineTerminator } {
    set S \r\n
    compile [saveas T345.java "class T345 {$S // $S}"]
} PASS

tcltest::test 3.4-6 { CR + LF (\r\n) is a LineTerminator } {
    set S \r\n
    compile [saveas T346.java "class T346 { // $S}"]
} PASS

tcltest::test 3.4-7 { CR (\u000d) is a LineTerminator } {
    set S \\u000d
    compile [saveas T347.java "class T347 {$S // $S}"]
} PASS

tcltest::test 3.4-8 { CR (\u000d) is a LineTerminator } {
    set S \\u000d
    compile [saveas T348.java "class T348 { // $S}"]
} PASS

tcltest::test 3.4-9 { LF (\u000a) is a LineTerminator } {
    set S \\u000a
    compile [saveas T349.java "class T349 {$S // $S}"]
} PASS

tcltest::test 3.4-10 { LF (\u000a) is a LineTerminator } {
    set S \\u000a
    compile [saveas T3410.java "class T3410 { // $S}"]
} PASS

tcltest::test 3.4-11 { CR + LF (\u000d\u000a) is a LineTerminator } {
    set S \\u000d\\u000a
    compile [saveas T3411.java "class T3411 {$S // $S}"]
} PASS

tcltest::test 3.4-12 { CR + LF (\u000d\u000a) is a LineTerminator } {
    set S \\u000d\\u000a
    compile [saveas T3412.java "class T3412 { // $S}"]
} PASS

tcltest::test 3.4-13 { CR + CR (\u000d\u000d) is two LineTerminators } {
    set S \\u000d\\u000d
    compile [saveas T3413.java "class T3413 {$S // $S}"]
} PASS

tcltest::test 3.4-14 { CR + CR (\u000d\u000d) is two LineTerminators } {
    set S \\u000d\\u000d
    compile [saveas T3414.java "class T3414 { // $S}"]
} PASS

tcltest::test 3.4-15 { LF + LF (\u000a\u000a) is two LineTerminators } {
    set S \\u000a\\u000a
    compile [saveas T3415.java "class T3415 {$S // $S}"]
} PASS

tcltest::test 3.4-16 { LF + LF (\u000a\u000a) is two LineTerminators } {
    set S \\u000a\\u000a
    compile [saveas T3416.java "class T3416 { // $S}"]
} PASS

tcltest::test 3.4-17 { CR + CR (\u000d\r) is two LineTerminators } {
    set S \\u000d\r
    compile [saveas T3417.java "${S}class T3417 {}"]
} PASS

tcltest::test 3.4-18 { LF + LF (\u000a\n) is two LineTerminators } {
    set S \\u000a\n
    compile [saveas T3418.java "${S}class T3418 {}"]
} PASS

tcltest::test 3.4-19 { CR + LF (\u000d\n) is one LineTerminator } {
    set S \\u000d\n
    compile [saveas T3419.java "${S}class T3419 {}"]
} PASS

tcltest::test 3.4-20 { LF + CR (\u000a\r) is two LineTerminators } {
    set S \\u000a\r
    compile [saveas T3420.java "${S}class T3420 {}"]
} PASS

tcltest::test 3.4-21 { LF + CR + LF (\u000a\r\u000a) is two LineTerminators } {
    set S \\u000a\r\u000a
    compile [saveas T3421.java "${S}class T3421 {}"]
} PASS

tcltest::test 3.4-22 { CR + LF + LF (\u000d\u000a\n) is two LineTerminators } {
    set S \\u000d\\u000a\n
    compile [saveas T3422.java "${S}class T3422 {}"]
} PASS

tcltest::test 3.4-23 { CR + CR + LF + LF (\u000d\u000d\u000a\n) is three LineTerminators } {
    set S \\u000d\\u000d\\u000a\n
    compile [saveas T3423.java "${S}class T3423 {}"]
} PASS

tcltest::test 3.4-24 { CR + CR + CR + LF (\u000d\u000d\u000d\n) is three LineTerminators } {
    set S \\u000d\\u000d\\u000d\n
    compile [saveas T3424.java "${S}class T3424 {}"]
} PASS

tcltest::test 3.4-25 { LF (\uu000a) is a LineTerminator } {
    set S \\uu000a
    compile [saveas T3425.java "${S}class T3425 {}"]
} PASS

tcltest::test 3.4-26 { LF (\uu000a) is a LineTerminator } {
    set S \\uu000a
    compile [saveas T3426.java "class T3426 { // ${S}}"]
} PASS

tcltest::test 3.4-27 { CR + LF (\uu000d\n) is a LineTerminator } {
    set S \\uu000d\n
    compile [saveas T3427.java "${S}class T3427 {}"]
} PASS

tcltest::test 3.4-28 { CR + CR + LF + LF (\u000d\u000d\uu000a\n) is three LineTerminators } {
    set S \\u000d\\u000d\\uu000a\n
    compile [saveas T3428.java "${S}class T3428 {}"]
} PASS
