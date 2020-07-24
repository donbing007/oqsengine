package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.sdk.vo.DataCollection;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Result helper to extract reuslt from wrapped result such as either or Optional
 */
public class GetResult {

    public static <T> T get(Either<String, T> result){
        return result.getOrElseThrow((Function<String, RuntimeException>) RuntimeException::new);
    }

    public static <T> DataCollection<T> getList(Either<String, Tuple2<Integer, List<T>>> result){
        return result.map(x -> new DataCollection<>(x._1, x._2())).getOrElseThrow((Function<String, RuntimeException>) RuntimeException::new);
    }

    public static <T> T get(Optional<T> result, String message){
        return result
                .orElseThrow(() -> new RuntimeException(message));
    }
}
