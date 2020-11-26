package com.xforceplus.ultraman.oqsengine.devops.rebuild.utils;


import io.vavr.control.Either;

import java.sql.SQLException;

/**
 * desc :
 * name : EitherUtils
 *
 * @author : xujia
 * date : 2020/9/6
 * @since : 1.8
 */
public class EitherUtils {
    public static <R> R eitherRight(Either<SQLException, R> either) throws SQLException {
        if (null == either) {
            throw new SQLException("return is null");
        }

        if (either.isLeft()) {
            throw either.getLeft();
        }

        return either.get();
    }
}
