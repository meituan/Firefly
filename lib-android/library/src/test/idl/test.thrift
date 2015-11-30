namespace java com.meituan.firefly.android.test

enum TestEnum {
    A =1, B = 2;
}

struct TestStruct {
    i32 a = 123;
    TestEnum e = TestEnum.B;
    binary bin;
}