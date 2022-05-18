package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import io.vavr.Tuple3;
import java.util.Arrays;
import java.util.List;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ErrorCase {
    public static final List<Tuple3<Long, Long, String>> errorCases = Arrays.asList(
        new Tuple3<>(1L, 1L, "error1"),
        new Tuple3<>(1L, 2L, "error2"),
        new Tuple3<>(1L, 3L, "error3"),
        new Tuple3<>(2L, 11L, "error11"),
        new Tuple3<>(2L, 12L, "error12"),
        new Tuple3<>(2L, 13L, "error13")
    );
}
