package com.xforceplus.ultraman.oqsengine.sdk;

import com.github.benmanes.caffeine.cache.*;
//import com.github.jknack.handlebars.Handlebars;
//import com.github.jknack.handlebars.Helper;
//import com.github.jknack.handlebars.Options;
//import com.github.jknack.handlebars.Template;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.sdk.util.ConditionQueryRequestHelper;
import com.xforceplus.ultraman.oqsengine.sdk.util.flow.QueueFlow;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.stringtemplate.v4.*;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class UtilsTest {

    @Test
    public void testTemplate(){

        String template = "<a>$name$</a>";
        ST st = new ST(template,'$','$');
        st.add("name", "World");
        assertEquals("<a>World</a>", st.render());
    }

//    @Test
//    public void testHandle(){
//        Handlebars handlebars = new Handlebars();
//
//        Template template = null;
//        try {
//            template = handlebars.compileInline("Hello {{this}}!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            System.out.println(template.apply("Handlebars.java"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void testVelocity(){
//        String template =  "  #set( $current =  $ldt.now().format($dtf.ofPattern('YYYY'))) \n " +
//                "<html>\n" +
//                "  <body>\n" +
//
//                "  Hello $fileName.split('-')[0] World!\n" +
//                "  Hello $current World!\n" +
//                "  </body>\n" +
//                "</html>";

        String template = "#set( $currentFileName = $fileName + '-' + $ldt.now().format($dtf.ofPattern('YYYYMMddHHmmSS')) ) \n" +
                "<a href=$downloadUrl.split('\\?')[0]?filename=$currentFileName>$currentFileName</a>";

        Velocity.init();

        VelocityContext context = new VelocityContext();
        context.put("downloadUrl", "/dowm?fileName=aaaa-dsdsd");
        context.put("fileName", "哈哈哈-12312312312");
        context.put("ldt", LocalDateTime.class);
        context.put("dtf", DateTimeFormatter.class);
        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "info", template);
        System.out.println(writer.toString());
    }

    class Format{
        DateTimeFormatter format(String formatter){
            return DateTimeFormatter.ofPattern(formatter);
        }
    }

//    @Test
//    public void testHandleToMessage(){
//        Handlebars handlebars = new Handlebars();
//        Template template = null;
//        try {
//            handlebars.registerHelper("formatDate", new Helper<LocalDateTime>(){
//
//                @Override
//                public Object apply(LocalDateTime localDateTime, Options options) throws IOException {
//                    String formatPattern = options.param(0);
//                    return localDateTime.format(DateTimeFormatter.ofPattern(formatPattern));
//                }
//            });
//
//            handlebars.registerHelper("now", new Helper<Void>(){
//
//                @Override
//                public LocalDateTime apply(Void nothing, Options options) throws IOException {
//                   return LocalDateTime.now();
//                }
//            });
//            template = handlebars.compileInline("Hello {{ now | formatDate 'YYYY'}}!");
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            System.out.println(template.apply(LocalDateTime.now()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    @Test
    public void testHelper(){

        ConditionQueryRequest conditionQueryRequestA = new ConditionQueryRequest();

        conditionQueryRequestA.setPageNo(1);
        conditionQueryRequestA.setPageSize(10);
        Conditions conditions = new Conditions();
        SubFieldCondition subFieldCondition = new SubFieldCondition();
        FieldCondition fieldCondition = new FieldCondition();
        conditions.setEntities(Arrays.asList(subFieldCondition));
        conditions.setFields(Arrays.asList(fieldCondition));
        conditionQueryRequestA.setConditions(conditions);
        EntityItem item = new EntityItem();
        item.setFields(Arrays.asList("ssss"));
        SubEntityItem subItem = new SubEntityItem();
        item.setEntities(Arrays.asList(subItem));
        conditionQueryRequestA.setEntity(item);
        NameMapping nameMapping = new NameMapping();
        conditionQueryRequestA.setMapping(Arrays.asList(nameMapping));
        FieldSort fieldSort = new FieldSort();
        conditionQueryRequestA.setSort(Arrays.asList(fieldSort));

        ConditionQueryRequest conditionQueryRequestB = ConditionQueryRequestHelper.copyOrNew(conditionQueryRequestA);

        //assertEquals(conditionQueryRequestA, conditionQueryRequestB);

    }


}
