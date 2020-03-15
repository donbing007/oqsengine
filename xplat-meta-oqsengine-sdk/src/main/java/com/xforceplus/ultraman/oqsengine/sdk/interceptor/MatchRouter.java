package com.xforceplus.ultraman.oqsengine.sdk.interceptor;

import io.vavr.API;
import io.vavr.API.Match.Case;
import io.vavr.control.Option;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.vavr.API.*;

public class MatchRouter<T, R> {

    List<Case<T, R>> caseList = new LinkedList<>();

    Function<T, Option<R>> fun;

    public MatchRouter<T, R> addRouter(T key, Function<T,R> router){
        caseList.add(Case($(key), router));
        return this;
    }

    public MatchRouter<T, R> build(){
        Case<T,R>[] caseArray = caseList.toArray(new Case[]{});
        fun = x -> Match(x).option(caseArray);

        return this;
    }

    public Optional<R> route(T key){
        if(fun != null){
            return fun.apply(key).toJavaOptional();
        }else{
            return Optional.empty();
        }
    }
}
