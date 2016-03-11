namespace java com.meituan.firefly.android.test

enum TestEnum {
    A =1111111111, B = 2;
}

struct TestStruct {
    i32 a = 123456789;
	bool b;
	i64 c;
    TestEnum e = TestEnum.B;
    binary bin;
	string str = 'abcde';
	string ss = "fghij";
}
