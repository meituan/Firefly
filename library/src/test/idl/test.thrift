namespace java com.meituan.firefly.test

struct OrderedStruct{
    1: required i32 id;
}

struct UnorderedStruct{
    required i32 id;
}

struct MixStruct{
    1: required i32 id;
    required i32 uid;
}

union UnionB{
    OrderedStruct os;
    UnorderedStruct uos;
    MixStruct mos;
}

enum OrderEnum {
    Order = 1;
    UnOrder = 2;
    Mix =3;
}

struct ComplicatedStruct {
    i32 a = 12345;
    i64 b = 100;
    double c = 99.99;
    set<i16> shortSet = [1,2,3];
    set<i32> intSet = [4,5,6];
    list<MixStruct> mixStructlist;
    list<i16> shortList;
    map<i16, OrderedStruct> orderedStructMap;
    map<i16, MixStruct> mixStructMap;
    OrderEnum orderEnum;
}

exception TestException {
    string message;
}

service TestService{
    oneway void notify(1:required i32 id);
    UnionB get(i32 id) throws (TestException testException);
    list<UnionB> getList(list<i16> ids);
    UnionB obserableMethod(required i32 id)throws (TestException testException);
}