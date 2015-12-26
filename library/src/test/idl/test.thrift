namespace java com.meituan.firefly.testthrift

struct OrderedStruct{
    1: required i32 id;
}

struct UnorderedStruct{
    1:required i32 id;
}

struct MixStruct{
    1: required i32 id;
    2:required i32 uid;
}

union UnionB{
    1:OrderedStruct os;
    2:UnorderedStruct uos;
    3:MixStruct mos;
}

enum OrderEnum {
    Order = 1;
    UnOrder = 2;
    Mix =3;
}

struct ComplicatedStruct {
    1:i32 a = 12345;
    2:i64 b = 100;
    3:double c = 99.99;
    4:set<i16> shortSet = [1,2,3];
    5:set<i32> intSet = [4,5,6];
    6:list<MixStruct> mixStructlist;
    7:list<i16> shortList;
    8:map<i16, OrderedStruct> orderedStructMap;
    9:map<i16, MixStruct> mixStructMap;
    10:OrderEnum orderEnum;
    11:binary bin;
}

exception TestException {
    1:string message;
}

service TestService{
    oneway void notify(1:required i32 id);
    UnionB get(1:i32 id) throws (1:TestException testException);
    UnionB emptyArgMethod() throws (1:TestException testException);
    list<UnionB> getList(1:list<i16> ids);
    void notifyWithoutOneway(1:required i32 id) throws (1:TestException testException);
}