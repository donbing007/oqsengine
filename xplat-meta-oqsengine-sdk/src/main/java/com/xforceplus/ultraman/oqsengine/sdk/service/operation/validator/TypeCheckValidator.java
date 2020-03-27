package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import io.vavr.control.Validation;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType.*;

/**
 * check type
 */
public class TypeCheckValidator implements FieldValidator<Object> {


    private static Map<FieldType, Predicate<String>> canParse = new HashMap<>();
    static {
        canParse.put(BOOLEAN, s -> {try {Boolean.parseBoolean(s); return true;} catch(Exception e) {return false;}});
        canParse.put(LONG, s -> {try {Long.parseLong(s); return true;} catch(Exception e) {return false;}});

        canParse.put(DECIMAL, s -> { try { new BigDecimal(s); return true; } catch(Exception e) {return false;}});
        canParse.put(DATETIME, s -> {try {
                                        Instant.ofEpochMilli(Long.parseLong(s));
                                        return true;
                                } catch(Exception e) {return false;}});
        canParse.put(STRING, s -> true);
        canParse.put(ENUM, s -> true );
    };


    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj) {

        if(obj != null) {
            if (field.config().isSplittable() &&
                    !StringUtils.isEmpty(field.config().getDelimiter())) {
                String value = obj.toString();
                String[] terms = value.split(field.config().getDelimiter());

                return Stream.of(terms)
                        .allMatch(canParse.get(field.type())) ?
                        Validation.valid(obj):
                        Validation.invalid(String.format("%s is not satisfied to type %s", obj, field.type()));
            } else {
                return checkType(field.type(), obj) ?
                        Validation.valid(obj):
                        Validation.invalid(String.format("%s is not satisfied to type %s", obj, field.type()));

            }
        }
        return Validation.valid(obj);
    }

    /**
     * this check may be not check with runtime type
     *
     *
     * @param type
     * @param obj
     * @return
     */
    private boolean checkType(FieldType type, Object obj){
        return canParse.get(type).test(obj.toString());
    }
}
