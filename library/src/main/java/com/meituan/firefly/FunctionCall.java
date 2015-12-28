package com.meituan.firefly;

import com.meituan.firefly.annotations.Field;
import com.meituan.firefly.annotations.Func;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

/**
 * Executes real function call.
 */
class FunctionCall {
    private final List<FieldSpec> requestTypeList = new ArrayList<>();
    private final HashMap<Short, FieldSpec> responseExceptionTypeMap = new HashMap<>();
    private FieldSpec responseSuccessType;
    private final String methodName;
    private final boolean oneway;
    private final TStruct argsStruct;
    private final boolean isObservable;
    private boolean isVoid = false;

    FunctionCall(Method method, Thrift thrift) {
        methodName = method.getName();
        Func func = method.getAnnotation(Func.class);
        if (func == null) {
            throw new IllegalArgumentException("method " + methodName + " should be annotated with @Func");
        }
        oneway = func.oneway();
        isObservable = getIsObservable(method);
        parseRequest(method, thrift);
        parseResponse(method, func, thrift);
        argsStruct = new TStruct(methodName + "_args");
    }

    void parseRequest(Method method, Thrift thrift) {
        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0, n = parametersAnnotations.length; i < n; i++) {
            Field paramField = null;
            Annotation[] parameterAnnotations = parametersAnnotations[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof Field) {
                    paramField = (Field) annotation;
                    break;
                }
            }
            if (paramField == null) {
                throw new IllegalArgumentException("parameter" + " of method " + methodName + " is not annotated with @Field");
            }
            TypeAdapter typeAdapter = thrift.getAdapter(parameterTypes[i]);
            requestTypeList.add(new FieldSpec(paramField.id(), paramField.required(), paramField.name(), typeAdapter));
        }
    }

    void parseResponse(Method method, Func func, Thrift thrift) {
        Type type = getMethodReturnType(method);
        //if return type is Void,we don't need to find adapter
        if (Void.class.equals(type) || void.class.equals(type)) {
            isVoid = true;
        } else {
            TypeAdapter returnTypeAdapter = thrift.getAdapter(type);
            responseSuccessType = new FieldSpec((short) 0, false, "success", returnTypeAdapter); //success
        }
        Field[] exceptionFields = func.value();
        Class<?>[] exceptions = method.getExceptionTypes();
        if (exceptionFields != null) {
            for (int i = 0, n = exceptionFields.length; i < n; i++) {
                Field exceptionField = exceptionFields[i];
                TypeAdapter exceptionTypeAdapter = thrift.getAdapter(exceptions[i]);
                responseExceptionTypeMap.put(exceptionField.id(), new FieldSpec(exceptionField.id(), false, exceptionField.name(), exceptionTypeAdapter));
            }
        }
    }

    private Type getMethodReturnType(Method method) {
        Type type = method.getGenericReturnType();
        if (isObservable) {
            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                type = Object.class;
            }
        }
        return type;
    }


    Object apply(Object[] args, TProtocol protocol, int seqid) throws Exception {
        return apply(args, protocol, seqid, null);
    }

    Object apply(final Object[] args, final TProtocol protocol, final int seqid, Scheduler subscribScheduler) throws Exception {
        if (isObservable) {
            Observable observable = Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    try {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        subscriber.onNext(FunctionCall.this.sendAndRecv(args, protocol, seqid));
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            });
            if (null != subscribScheduler)
                return observable.subscribeOn(subscribScheduler);
            else
                return observable;
        }
        return sendAndRecv(args, protocol, seqid);
    }

    Object sendAndRecv(Object[] args, TProtocol protocol, int seqid) throws Exception {
        send(args, protocol, seqid);
        if (!oneway) {
            return recv(protocol, seqid);
        }
        return null;
    }

    void send(Object[] args, TProtocol protocol, int seqid) throws TException {
        protocol.writeMessageBegin(new TMessage(methodName, TMessageType.CALL, seqid));
        protocol.writeStructBegin(argsStruct);
        //method with no parameters
        if (null != args) {
            for (int i = 0, n = args.length; i < n; i++) {
                FieldSpec fieldSpec = requestTypeList.get(i);
                Object value = args[i];
                if (value == null) {
                    if (fieldSpec.required) {
                        throw new TProtocolException("Required field '" + fieldSpec.name + "' was not present! Struct: " + argsStruct.name);
                    }
                } else {
                    protocol.writeFieldBegin(fieldSpec.tField);
                    fieldSpec.typeAdapter.write(value, protocol);
                    protocol.writeFieldEnd();
                }
            }
        }
        protocol.writeFieldStop();
        protocol.writeStructEnd();
        protocol.writeMessageEnd();
        protocol.getTransport().flush();
    }

    Object recv(TProtocol protocol, int seqid) throws Exception {
        TMessage msg = protocol.readMessageBegin();
        if (msg.type == TMessageType.EXCEPTION) {
            TApplicationException applicationException = TApplicationException.read(protocol);
            protocol.readMessageEnd();
            throw applicationException;
        }
        if (msg.seqid != seqid) {
            throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, methodName + " failed: out of sequence response");
        }
        protocol.readStructBegin();
        Object success = null;
        Exception exception = null;
        while (true) {
            TField tField = protocol.readFieldBegin();
            if (tField.type == TType.STOP) {
                break;
            }
            FieldSpec fieldSpec = null;
            if (tField.id == 0) {
                fieldSpec = responseSuccessType;
            } else {
                fieldSpec = responseExceptionTypeMap.get(tField.id);
            }
            if (fieldSpec == null || fieldSpec.typeAdapter.getTType() != tField.type) {
                TProtocolUtil.skip(protocol, tField.type);
            } else {
                Object value = fieldSpec.typeAdapter.read(protocol);
                if (tField.id == 0) {
                    success = value;
                } else {
                    exception = (Exception) value;
                }
            }
            protocol.readFieldEnd();
        }
        protocol.readStructEnd();
        protocol.readMessageEnd();
        if (exception != null) {
            throw exception;
        }
        if (success != null) {
            return success;
        }
        if (isVoid) {
            return null;
        }
        throw new TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, methodName + " failed: unknown result");
    }

    public boolean getIsObservable(Method method) {
        return Observable.class.isAssignableFrom(Types.getRawType(method.getGenericReturnType()));
    }

    static class FieldSpec {
        final short id;
        final boolean required;
        final String name;
        final TypeAdapter typeAdapter;
        final TField tField;

        public FieldSpec(short id, boolean required, String name, TypeAdapter typeAdapter) {
            this.id = id;
            this.required = required;
            this.name = name;
            this.typeAdapter = typeAdapter;
            this.tField = new TField(name, typeAdapter.getTType(), id);
        }
    }
}
