# FIXME: it might be a good idea to revisit the constant_expression
# macro to see if we could incorporate some optional class body
# or method body code that would be inserted before the actual
# switch body.

# byte to short
tcltest::test 5.1.2-bts-1 { byte to short conversion never loses information } {
    empty_main T512bts1 {
        final byte b = 72;
        final short s = 72;
        switch (args.length) {
            case 0:
            case (((short)b == s) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-bts-2 { byte to short boundary case } {
    empty_main T512bts2 {
        final byte b = Byte.MAX_VALUE;
        final short s = 127;
        switch (args.length) {
            case 0:
            case (((short)b == s) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-bts-3 { byte to short boundary case } {
    empty_main T512bts3 {
        final byte b = 0;
        final short s = 0;
        switch (args.length) {
            case 0:
            case (((short)b == s) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-bts-4 { byte to short boundary case } {
    empty_main T512bts4 {
        final byte b = Byte.MIN_VALUE;
        final short s = -128;
        switch (args.length) {
            case 0:
            case (((short)b == s) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-bts-5 { byte to short boundary case } {
    empty_main T512bts5 {
        final byte b = -1;
        final short s = -1;
        switch (args.length) {
            case 0:
            case (((short)b == s) ? 1 : 0):
        }
    }
} PASS

# byte to int
tcltest::test 5.1.2-bti-1 { byte to int conversion never loses information } {
    empty_main T512bti1 {
        final byte b = 72;
        switch (args.length) {
            case 0:
            case (((int)b == 72) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-bti-2 { byte to int boundary case } {
    constant_expression T512bti2 {(int)Byte.MAX_VALUE == 127}
} PASS

tcltest::test 5.1.2-bti-3 { byte to int boundary case } {
    empty_main T512bti3 {
        final byte b = 0;
        switch (args.length) {
            case 0:
            case (((int)b == 0) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-bti-4 { byte to int boundary case } {
    constant_expression T512bti4 {(int)Byte.MIN_VALUE == -128}
} PASS

tcltest::test 5.1.2-bti-5 { byte to int boundary case } {
    empty_main T512bti5 {
        final byte b = -1;
        switch (args.length) {
            case 0:
            case (((int)b == -1) ? 1 : 0):
        }
    }
} PASS

# byte to long
tcltest::test 5.1.2-btl-1 { byte to long conversion never loses information } {
    empty_main T512btl1 {
        final byte b = 72;
        switch (args.length) {
            case 0:
            case (((long)b == 72L) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-btl-2 { byte to long boundary case } {
    constant_expression T512btl2 {(long)Byte.MAX_VALUE == 127L}
} PASS

tcltest::test 5.1.2-btl-3 { byte to long boundary case } {
    empty_main T512btl3 {
        final byte b = 0;
        switch (args.length) {
            case 0:
            case (((long)b == 0L) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-btl-4 { byte to long boundary case } {
    constant_expression T512btl4 {(long)Byte.MIN_VALUE == -128L}
} PASS

tcltest::test 5.1.2-btl-5 { byte to long boundary case } {
    empty_main T512btl5 {
        final byte b = -1;
        switch (args.length) {
            case 0:
            case (((long)b == -1L) ? 1 : 0):
        }
    }
} PASS

# byte to float
tcltest::test 5.1.2-btf-1 { byte to float conversion never loses information } {
    empty_main T512btf1 {
        final byte b = 72;
        switch (args.length) {
            case 0:
            case (((float)b == 72.0f) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-btf-2 { byte to float boundary case } {
    constant_expression T512btf2 {(float)Byte.MAX_VALUE == 127.0f}
} PASS

tcltest::test 5.1.2-btf-3 { byte to float boundary case } {
    empty_main T512btf3 {
        final byte b = 0;
        switch (args.length) {
            case 0:
            case (((float)b == 0.0f) ? 1 : 0):
            case ((1/(float)b == Float.POSITIVE_INFINITY) ? 2 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-btf-4 { byte to float boundary case } {
    constant_expression T512btf4 {(float)Byte.MIN_VALUE == -128.0f}
} PASS

tcltest::test 5.1.2-btf-5 { byte to float boundary case } {
    empty_main T512btf5 {
        final byte b = -1;
        switch (args.length) {
            case 0:
            case (((float)b == -1.0f) ? 1 : 0):
        }
    }
} PASS

# byte to double
tcltest::test 5.1.2-btd-1 { byte to double conversion never loses information } {
    empty_main T512btd1 {
        final byte b = 72;
        switch (args.length) {
            case 0:
            case (((double)b == 72.0) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-btd-2 { byte to double boundary case } {
    constant_expression T512btd2 {(double)Byte.MAX_VALUE == 127.0}
} PASS

tcltest::test 5.1.2-btd-3 { byte to double boundary case } {
    empty_main T512btd3 {
        final byte b = 0;
        switch (args.length) {
            case 0:
            case (((double)b == 0.0) ? 1 : 0):
            case ((1/(double)b == Double.POSITIVE_INFINITY) ? 2 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-btd-4 { byte to double boundary case } {
    constant_expression T512btd4 {(double)Byte.MIN_VALUE == -128.0}
} PASS

tcltest::test 5.1.2-btd-5 { byte to double boundary case } {
    empty_main T512btd5 {
        final byte b = -1;
        switch (args.length) {
            case 0:
            case (((double)b == -1.0) ? 1 : 0):
        }
    }
} PASS

# short to int
tcltest::test 5.1.2-sti-1 { short to int conversion never loses information } {
    empty_main T512sti1 {
        final short s = 12345;
        switch (args.length) {
            case 0:
            case (((int)s == 12345) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-sti-2 { short to int boundary case } {
    constant_expression T512sti2 {(int)Short.MAX_VALUE == 32767}
} PASS

tcltest::test 5.1.2-sti-3 { short to int boundary case } {
    empty_main T512sti3 {
        final short s = 0;
        switch (args.length) {
            case 0:
            case (((int)s == 0) ? 1 : 0):
        }
    }
    constant_expression T512sti3 {(int)0 == 0L}
} PASS

tcltest::test 5.1.2-sti-4 { short to int boundary case } {
    constant_expression T512sti4 {(int)Short.MIN_VALUE == -32768}
} PASS

tcltest::test 5.1.2-sti-5 { short to int boundary case } {
    empty_main T512sti5 {
        final short s = -1;
        switch (args.length) {
            case 0:
            case (((int)s == -1) ? 1 : 0):
        }
    }
} PASS

# short to long
tcltest::test 5.1.2-stl-1 { short to long conversion never loses information } {
    empty_main T512stl1 {
        final short s = 12345;
        switch (args.length) {
            case 0:
            case (((long)s == 12345L) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-stl-2 { short to long boundary case } {
    constant_expression T512stl2 {(long)Short.MAX_VALUE == 32767L}
} PASS

tcltest::test 5.1.2-stl-3 { short to long boundary case } {
    empty_main T512stl3 {
        final short s = 0;
        switch (args.length) {
            case 0:
            case (((long)s == 0L) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-stl-4 { short to long boundary case } {
    constant_expression T512stl4 {(long)Short.MIN_VALUE == -32768L}
} PASS

tcltest::test 5.1.2-stl-5 { short to long boundary case } {
    empty_main T512stl5 {
        final short s = -1;
        switch (args.length) {
            case 0:
            case (((long)s == -1L) ? 1 : 0):
        }
    }
} PASS

# short to float
tcltest::test 5.1.2-stf-1 { short to float conversion never loses information } {
    empty_main T512stf3 {
        final short s = 12345;
        switch (args.length) {
            case 0:
            case (((float)s == 12345.0f) ? 1 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-stf-2 { short to float boundary case } {
    constant_expression T512stf2 {(float)Short.MAX_VALUE == 32767.0f}
} PASS

tcltest::test 5.1.2-stf-3 { short to float boundary case } {
    empty_main T512stf3 {
        final short s = 0;
        switch (args.length) {
            case 0:
            case (((float)s == 0.0f) ? 1 : 0):
            case ((1/(float)s == Float.POSITIVE_INFINITY) ? 2 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-stf-4 { short to float boundary case } {
    constant_expression T512stf4 {(float)Short.MIN_VALUE == -32768.0f}
} PASS

tcltest::test 5.1.2-stf-5 { short to float boundary case } {
    empty_main T512stf5 {
        final short s = -1;
        switch (args.length) {
            case 0:
            case (((float)s == -1.0f) ? 1 : 0):
        }
    }
} PASS

# short to double
tcltest::test 5.1.2-std-1 { short to double conversion never loses information } {
    empty_main T512std1 {
        final short s = 12345;
        switch (args.length) {
            case 0:
            case (((double)s == 12345.0) ? 1 : 0):
        }
    }
    constant_expression T512std1 {(double)12345678 == 12345678L}
} PASS

tcltest::test 5.1.2-std-2 { short to double boundary case } {
    constant_expression T512std2 {(double)Short.MAX_VALUE == 32767.0}
} PASS

tcltest::test 5.1.2-std-3 { short to double boundary case } {
    empty_main T512std3 {
        final short s = 0;
        switch (args.length) {
            case 0:
            case (((double)s == 0.0) ? 1 : 0):
            case ((1/(double)s == Double.POSITIVE_INFINITY) ? 2 : 0):
        }
    }
} PASS

tcltest::test 5.1.2-std-4 { short to double boundary case } {
    constant_expression T512std4 {(double)Short.MIN_VALUE == -32768.0}
} PASS

tcltest::test 5.1.2-std-5 { short to double boundary case } {
    empty_main T512std5 {
        final short s = -1;
        switch (args.length) {
            case 0:
            case (((double)s == -1.0) ? 1 : 0):
        }
    }
} PASS

# char to int
tcltest::test 5.1.2-cti-1 { char to int conversion never loses information } {
    constant_expression T512cti1 {(int)'\u789a' == 0x789a}
} PASS

tcltest::test 5.1.2-cti-2 { char to int boundary case } {
    constant_expression T512cti2 {(int)'\uffff' == 0xffff}
} PASS

tcltest::test 5.1.2-cti-3 { char to int boundary case } {
    constant_expression T512cti3 {(int)'\0' == 0}
} PASS

tcltest::test 5.1.2-cti-4 { char to int boundary case } {
    constant_expression T512cti4 {(int)'\u7fff' == 0x7fff}
} PASS

tcltest::test 5.1.2-cti-5 { char to int boundary case } {
    constant_expression T512cti5 {(int)'\u8000' == 0x8000}
} PASS

tcltest::test 5.1.2-cti-6 { char to int boundary case } {
    constant_expression T512cti6 {(int)'\177' == 0x7f}
} PASS

tcltest::test 5.1.2-cti-7 { char to int boundary case } {
    constant_expression T512cti7 {(int)'\200' == 0x80}
} PASS

tcltest::test 5.1.2-cti-8 { char to int boundary case } {
    constant_expression T512cti8 {(int)'\377' == 0xff}
} PASS

tcltest::test 5.1.2-cti-9 { char to int boundary case } {
    constant_expression T512cti9 {(int)'\u0100' == 0x100}
} PASS

# char to long
tcltest::test 5.1.2-ctl-1 { char to long conversion never loses information } {
    constant_expression T512ctl1 {(long)'\u789a' == 0x789aL}
} PASS

tcltest::test 5.1.2-ctl-2 { char to long boundary case } {
    constant_expression T512ctl2 {(long)'\uffff' == 0xffffL}
} PASS

tcltest::test 5.1.2-ctl-3 { char to long boundary case } {
    constant_expression T512ctl3 {(long)'\0' == 0L}
} PASS

tcltest::test 5.1.2-ctl-4 { char to long boundary case } {
    constant_expression T512ctl4 {(long)'\u7fff' == 0x7fffL}
} PASS

tcltest::test 5.1.2-ctl-5 { char to long boundary case } {
    constant_expression T512ctl5 {(long)'\u8000' == 0x8000L}
} PASS

tcltest::test 5.1.2-ctl-6 { char to long boundary case } {
    constant_expression T512ctl6 {(long)'\177' == 0x7fL}
} PASS

tcltest::test 5.1.2-ctl-7 { char to long boundary case } {
    constant_expression T512ctl7 {(long)'\200' == 0x80L}
} PASS

tcltest::test 5.1.2-ctl-8 { char to long boundary case } {
    constant_expression T512ctl8 {(long)'\377' == 0xffL}
} PASS

tcltest::test 5.1.2-ctl-9 { char to long boundary case } {
    constant_expression T512ctl9 {(int)'\u0100' == 0x100L}
} PASS

# char to float
tcltest::test 5.1.2-ctf-1 { char to float conversion never loses information } {
    constant_expression T512ctf1 {(float)'\u789a' == 30874.0f}
} PASS

tcltest::test 5.1.2-ctf-2 { char to float boundary case } {
    constant_expression T512ctf2 {(float)'\uffff' == 65535.0f}
} PASS

tcltest::test 5.1.2-ctf-3 { char to float boundary case } {
    constant_expression T512ctf3 {(float)'\0' == 0.0f} \
            {1/(float)'\0' == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ctf-4 { char to float boundary case } {
    constant_expression T512ctf4 {(float)'\u7fff' == 32767.0f}
} PASS

tcltest::test 5.1.2-ctf-5 { char to float boundary case } {
    constant_expression T512ctf5 {(float)'\u8000' == 32768.0f}
} PASS

tcltest::test 5.1.2-ctf-6 { char to float boundary case } {
    constant_expression T512ctf6 {(float)'\177' == 127.0f}
} PASS

tcltest::test 5.1.2-ctf-7 { char to float boundary case } {
    constant_expression T512ctf7 {(float)'\200' == 128.0f}
} PASS

tcltest::test 5.1.2-ctf-8 { char to float boundary case } {
    constant_expression T512ctf8 {(float)'\377' == 255.0f}
} PASS

tcltest::test 5.1.2-ctf-9 { char to float boundary case } {
    constant_expression T512ctf9 {(float)'\u0100' == 256.0f}
} PASS

# char to double
tcltest::test 5.1.2-ctd-1 { char to double conversion never loses information } {
    constant_expression T512ctd1 {(double)'\u789a' == 30874.0}
} PASS

tcltest::test 5.1.2-ctd-2 { char to double boundary case } {
    constant_expression T512ctd2 {(double)'\uffff' == 65535.0}
} PASS

tcltest::test 5.1.2-ctd-3 { char to double boundary case } {
    constant_expression T512ctd3 {(double)'\0' == 0.0} \
            {1/(double)'\0' == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ctd-4 { char to double boundary case } {
    constant_expression T512ctd4 {(double)'\u7fff' == 32767.0}
} PASS

tcltest::test 5.1.2-ctd-5 { char to double boundary case } {
    constant_expression T512ctd5 {(double)'\u8000' == 32768.0}
} PASS

tcltest::test 5.1.2-ctd-6 { char to double boundary case } {
    constant_expression T512ctd6 {(double)'\177' == 127.0}
} PASS

tcltest::test 5.1.2-ctd-7 { char to double boundary case } {
    constant_expression T512ctd7 {(double)'\200' == 128.0}
} PASS

tcltest::test 5.1.2-ctd-8 { char to double boundary case } {
    constant_expression T512ctd8 {(double)'\377' == 255.0}
} PASS

tcltest::test 5.1.2-ctd-9 { char to double boundary case } {
    constant_expression T512ctd9 {(double)'\u0100' == 256.0}
} PASS

# int to long
tcltest::test 5.1.2-itl-1 { int to long conversion never loses information } {
    constant_expression T512itl1 {(long)12345678 == 12345678L}
} PASS

tcltest::test 5.1.2-itl-2 { int to long boundary case } {
    constant_expression T512itl2 {(long)Integer.MAX_VALUE == 0x7fffffffL}
} PASS

tcltest::test 5.1.2-itl-3 { int to long boundary case } {
    constant_expression T512itl3 {(long)0 == 0L}
} PASS

tcltest::test 5.1.2-itl-4 { int to long boundary case } {
    constant_expression T512itl4 {(long)Integer.MIN_VALUE == 0xffffffff80000000L}
} PASS

tcltest::test 5.1.2-itl-5 { int to long boundary case } {
    constant_expression T512itl5 {(long)-1 == 0xffffffffffffffffL}
} PASS

# int to float
tcltest::test 5.1.2-itf-1 { int to float conversion may lose precision } {
    constant_expression T512itf1 {1.23456792e8f == (float)123456789}
} PASS

tcltest::test 5.1.2-itf-2 { int to float conversion rounds down to nearest
        if remainder < .5} {
    constant_expression T512itf2 {(float)0x7fffff3f == (float)0x7fffff00}
} PASS

tcltest::test 5.1.2-itf-3 { int to float conversion rounds up to nearest
        if remainder > .5} {
    constant_expression T512itf3 {(float)0x70ffffc1 == (float)0x71000000}
} PASS

tcltest::test 5.1.2-itf-4 { int to float conversion rounds to least
        significant bit 0 if remainder = .5} {
    constant_expression T512itf4 {(float)0x7fffff40 == (float)0x7fffff00}
} PASS

tcltest::test 5.1.2-itf-5 { int to float conversion rounds to least
        significant bit 0 if remainder = .5} {
    constant_expression T512itf5 {(float)0x70ffffc0 == (float)0x71000000}
} PASS

tcltest::test 5.1.2-itf-6 { int to float boundary case } {
    constant_expression T512itf6 {(float)Integer.MAX_VALUE == 2.14748365E9f}
} PASS

tcltest::test 5.1.2-itf-7 { int to float boundary case } {
    constant_expression T512itf7 {(float)0 == 0f} {1/(float)0 == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-itf-8 { int to float boundary case } {
    constant_expression T512itf8 {(float)Integer.MIN_VALUE == -2.14748365E9f}
} PASS

tcltest::test 5.1.2-itf-9 { int to float boundary case } {
    constant_expression T512itf9 {(float)-1 == -1.0f}
} PASS

# int to double
tcltest::test 5.1.2-itd-1 { int to double conversion never loses precision } {
    constant_expression T512itd1 {123456789.0 == (double)123456789}
} PASS

tcltest::test 5.1.2-itd-2 { int to double boundary case } {
    constant_expression T512itd2 {(double)Integer.MAX_VALUE == 2.147483647E9}
} PASS

tcltest::test 5.1.2-itd-3 { int to double boundary case } {
    constant_expression T512itd3 {(double)0 == 0.0} {1/(double)0 == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-itd-4 { int to double boundary case } {
    constant_expression T512itd4 {(double)Integer.MIN_VALUE == -2.147483648e9}
} PASS

tcltest::test 5.1.2-itd-5 { int to double boundary case } {
    constant_expression T512itd5 {(double)-1 == -1.0}
} PASS

# long to float
tcltest::test 5.1.2-ltf-1 { long to float conversion may lose precision } {
    constant_expression T512ltf1 {1.23456792e8f == (float)123456789L}
} PASS

tcltest::test 5.1.2-ltf-2 { long to float conversion rounds down to nearest
        if remainder < .5} {
    constant_expression T512ltf2 {(float)0x7fffff3fL == (float)0x7fffff00L}
} PASS

tcltest::test 5.1.2-ltf-3 { long to float conversion rounds up to nearest if
        remainder > .5} {
    constant_expression T512ltf3 {(float)0x7fffffc1L == (float)0x80000000L}
} PASS

tcltest::test 5.1.2-ltf-4 { long to float conversion rounds to least
        significant bit 0 if remainder = .5} {
    constant_expression T512ltf4 {(float)0x7fffff40L == (float)0x7fffff00L}
} PASS

tcltest::test 5.1.2-ltf-5 { long to float conversion rounds to least
        significant bit 0 if remainder = .5} {
    constant_expression T512ltf5 {(float)0x7fffffc0L == (float)0x80000000L}
} PASS

tcltest::test 5.1.2-ltf-6 { long to float conversion rounds down to
        nearest if remainder < .5} {
    constant_expression T512ltf6 \
        {(float)0xffffff7ffffffbfL == (float)0xffffff000000000L}
} PASS

tcltest::test 5.1.2-ltf-7 { long to float conversion rounds down to
        nearest if remainder < .5. Hardware FP  that rounds to 64-bit
        double precision before rounding to 32-bit single precision
        will fail for this test case } {
    constant_expression T512ltf7 \
        {(float)0xffffff7ffffffffL == (float)0xffffff000000000L}
} PASS

tcltest::test 5.1.2-ltf-8 { long to float boundary case } {
    constant_expression T512ltf8 {(float)Long.MAX_VALUE == 9.223372e18f}
} PASS

tcltest::test 5.1.2-ltf-9 { long to float boundary case } {
    constant_expression T512ltf9 {(float)0L == 0f} {1/(float)0L == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ltf-10 { long to float boundary case } {
    constant_expression T512ltf10 {(float)Long.MIN_VALUE == -9.223372e18f}
} PASS

tcltest::test 5.1.2-ltf-11 { long to float boundary case } {
    constant_expression T512ltf11 {(float)-1L == -1.0f}
} PASS

# long to double
tcltest::test 5.1.2-ltd-1 { long to double conversion may lose precision } {
    constant_expression T512ltd1 {0x123456789abcde0L == (double)0x123456789abcde7L}
} PASS

tcltest::test 5.1.2-ltd-2 { long to double conversion rounds down to nearest
        if remainder < .5} {
    constant_expression T512ltd2 {(double)0xffffff7ffffffbfL == 0xffffff7ffffff80L}
} PASS

tcltest::test 5.1.2-ltd-3 { long to double conversion rounds up to nearest
        if remainder > .5} {
    constant_expression T512ltd3 {(double)0xffffff7ffffffc1L == 0xffffff800000000L}
} PASS

tcltest::test 5.1.2-ltd-4 { long to double conversion rounds to least
        significant bit 0 if remainder = .5} {
    constant_expression T512ltd4 {(double)0xffffff7ffffff40L == 0xffffff7ffffff00L}
} PASS

tcltest::test 5.1.2-ltd-5 { long to double conversion rounds to least
        significant bit 0 if remainder = .5} {
    constant_expression T512ltd5 {(double)0xffffff7ffffffc0L == 0xffffff800000000L}
} PASS

tcltest::test 5.1.2-ltd-6 { long to double boundary case } {
    constant_expression T512ltd6 \
            {(double)Long.MAX_VALUE == 9.223372036854776e18}
} PASS

tcltest::test 5.1.2-ltd-7 { long to double boundary case } {
    constant_expression T512ltd7 {(double)0L == 0f} {1/(double)0L == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ltd-8 { long to float boundary case } {
    constant_expression T512ltd8 \
            {(double)Long.MIN_VALUE == -9.223372036854776e18}
} PASS

tcltest::test 5.1.2-ltd-9 { long to float boundary case } {
    constant_expression T512ltd9 {(double)-1L == -1.0}
} PASS

# float to double
tcltest::test 5.1.2-ftd-1 { float to double conversion never loses precision } {
    constant_expression T512ftd1 {(double)1234567.75f == 1234567.75}
} PASS

tcltest::test 5.1.2-ftd-2 { float to double boundary case } {
    constant_expression T512ftd2 \
            {(double)Float.MAX_VALUE == 3.4028234663852886e38}
} PASS

tcltest::test 5.1.2-ftd-3 { float to double boundary case } {
    constant_expression T512ftd3 \
            {(double)Float.MIN_VALUE == 1.401298464324817e-45}
} PASS

tcltest::test 5.1.2-ftd-4 { float to double boundary case } {
    constant_expression T512ftd4 {(double)0f == 0.0} \
            {1/(double)0f == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ftd-5 { float to double boundary case } {
    constant_expression T512ftd5 {(double)-0f == -0.0} \
            {1/(double)-0f == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ftd-6 { float to double boundary case } {
    constant_expression T512ftd6 \
            {(double)Float.POSITIVE_INFINITY == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ftd-7 { float to double boundary case } {
    constant_expression T512ftd7 \
            {(double)Float.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 5.1.2-ftd-8 { float to double boundary case } {
    constant_expression T512ftd8 {(double)Float.NaN != Double.NaN} \
            {(double)Float.NaN != (double)Float.NaN}
} PASS

tcltest::test 5.1.2-ftd-9 { float to double on a negative } {
    constant_expression T512ftd9 {(double)-12345.678f == -12345.677734375}
} PASS
