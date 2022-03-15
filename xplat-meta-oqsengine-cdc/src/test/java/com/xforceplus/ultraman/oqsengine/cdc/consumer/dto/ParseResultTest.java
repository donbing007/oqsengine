package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.Tuple3;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ParseResultTest {

    @Test
    public void finishOneTest() {
        ParseResult old = new ParseResult();

        long expectedKey = Long.MAX_VALUE - 100;

        OriginalEntity expectedOriginal = new OriginalEntity();
        old.getFinishEntries().put(expectedKey, expectedOriginal);
        old.finishOne(expectedKey);

        old.clean();

        Assertions.assertEquals(0, old.getFinishEntries().size());
    }

    @Test
    public void addErrorTest() {
        ParseResult parseResult = new ParseResult();

        ErrorCase.errorCases.forEach(
            e -> {
                parseResult.addError(e._1(), e._2(), e._3());
            }
        );

        AtomicInteger i = new AtomicInteger(0);

        parseResult.getErrors().forEach(
            (k, v) -> {
                Assertions.assertEquals(k, v.keyGenerate());

                Tuple3<Long, Long, String> errorCase = ErrorCase.errorCases.get(i.getAndIncrement());
                Assertions.assertEquals(errorCase._1(), v.getId());
                Assertions.assertEquals(errorCase._2(), v.getCommitId());
                Assertions.assertEquals(errorCase._3(), v.getMessage());
            }
        );
    }
}
