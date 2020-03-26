package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * regx validator
 */
public class RegxValidator implements FieldValidator<Object> {

    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj) {

        if(obj != null && StringUtils.isEmpty(field.config().getValidateRegexString())){
            Pattern pattern = Pattern.compile(field.config().getValidateRegexString());
            if(isSplittable(field)){
                String[] term = obj.toString().split(field.config().getValidateRegexString());
                return Stream.of(term)
                      .allMatch(x -> checkRegex(pattern, x)) ?
                         Validation.valid(obj) :
                         Validation.invalid(String.format("%s is not satisfied with regex %s"
                                 , obj
                                 , field.config().getValidateRegexString()));
            }else{
                return checkRegex(pattern, obj.toString()) ?
                        Validation.valid(obj) :
                        Validation.invalid(String.format("%s is not satisfied with regex %s"
                                , obj
                                , field.config().getValidateRegexString()));
            }
        }

        return Validation.valid(obj);
    }

    private boolean checkRegex(Pattern pattern, String obj){
        return pattern.matcher(obj).matches();
    }
}
