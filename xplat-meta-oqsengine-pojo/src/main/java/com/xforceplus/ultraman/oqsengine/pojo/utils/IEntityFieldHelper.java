package com.xforceplus.ultraman.oqsengine.pojo.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.LoggerUtils.typeConverterError;

/**
 * make field mutable for basic info
 */
public class IEntityFieldHelper {

    private Logger logger = LoggerFactory.getLogger(IEntityClassHelper.class);

    private List<IValue> toTypedValue(IEntityField entityField, String value) {
        try {
            Objects.requireNonNull(value, "value值不能为空");
            Objects.requireNonNull(entityField, "field值不能为空");
            List<IValue> iValues = new LinkedList<>();
            switch (entityField.type()) {
                case LONG:
                    iValues.add(new LongValue(entityField, Long.parseLong(value)));
                    break;
                case DATETIME:
                    //DATETIME is a timestamp
                    Instant instant = Instant.ofEpochMilli(Long.parseLong(value));
                    iValues.add(new DateTimeValue(entityField, LocalDateTime.ofInstant(instant, DateTimeValue.zoneId)));
                    break;
                case ENUM:
                    iValues.add(new EnumValue(entityField, value));
                    break;
                case BOOLEAN:
                    iValues.add(new BooleanValue(entityField, Boolean.parseBoolean(value)));
                    break;
                case DECIMAL:
                    //min is 1
                    int precision = Optional.ofNullable(entityField.config()).map(FieldConfig::getPrecision).filter(x -> x > 0).orElse(1);
                    iValues.add(new DecimalValue(entityField, new BigDecimal(value).setScale(precision, RoundingMode.HALF_UP)));
                    break;
                case STRINGS:
//                    Stream.of(value.split(",")).map(x ->
//                            new StringsValue(entityField, new String[]{x})).forEach(iValues::add);
                    iValues.add(new StringsValue(entityField, value));
                    break;
                default:
                    iValues.add(new StringValue(entityField, value));
            }
            return iValues;
        } catch (Exception ex) {
            logger.error("{}", ex);
            throw new RuntimeException(typeConverterError(entityField, value, ex));
        }
    }

    private List<IValue> toTypedValue(IEntityClass entityClass, Long id, String value) {
        try {
            Objects.requireNonNull(value, "值不能为空");
            Optional<Tuple2<IEntityClass, IEntityField>> fieldTuple = IEntityClassHelper.findFieldByIdInAll(entityClass, id);

            Optional<IEntityField> fieldOp = fieldTuple.map(Tuple2::_2);

            if (entityClass.extendEntityClass() != null && !fieldOp.isPresent()) {
                fieldOp = entityClass.extendEntityClass().field(id);
            }
            if (fieldOp.isPresent()) {
                return toTypedValue(fieldOp.get(), value);
            } else {
                logger.error("不存在对应的field id:{}", id);
                return null;
            }
        } catch (Exception ex) {
            logger.error("{}", ex);
            throw new RuntimeException("类型转换失败 " + ex.getMessage());
        }
    }
}
