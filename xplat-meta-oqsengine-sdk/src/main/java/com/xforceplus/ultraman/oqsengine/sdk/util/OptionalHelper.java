package com.xforceplus.ultraman.oqsengine.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by luye on 2016/6/20.
 */
public class OptionalHelper {

    static Logger logger = LoggerFactory.getLogger(OptionalHelper.class);

    public static <T, R> R keepNull(T r, Function<T, R> func ){
        return tryGet(r, func).orElse(null);
    }

    public static <T> Optional<T> ofEmpty(T r, Predicate<T> emptyPredicate){
        return emptyPredicate.negate().test(r) ? Optional.ofNullable(r) : Optional.empty();
    }

    public static Optional<String> ofEmptyStr(Object r){
        return ofEmpty((String)r, StringUtils::isEmpty);
    }

    public static <T> Boolean orFalse(T r, Function<T, Boolean> func){
        return tryGet(r, func).orElse(false);
    }

    public static <T> Optional<T> seqGet(T...seqValue){
        return Stream.of(seqValue).filter(v ->  v != null).findFirst();
    }

    /**
     * make sure this t is consist
     * @param candidates
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> Optional<T> combine(Optional<T> ...candidates){
        return Stream.of(candidates).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    static public <T,U> Optional<T> tryGet(U input, Function<U,T> mapper){
        try{
            return Optional.ofNullable(input).map(mapper);
        }catch(Exception ex){
            logger.error("{}", ex);
            return Optional.empty();
        }
    }

}
