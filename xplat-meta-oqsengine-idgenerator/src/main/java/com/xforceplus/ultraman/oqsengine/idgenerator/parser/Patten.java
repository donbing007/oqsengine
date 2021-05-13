package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leo
 * patten
 */
public interface Patten {
    /**
     * year {yyyy}
     */
    String YEAR = "{yyyy}";

    /**
     * month  {MM}
     */
    String MONTH = "{MM}";

    /**
     * day {dd}
     */
    String DAY = "{dd}";

    /**
     * number
     */
    String NUMBER = "{0}";

//
//    public static void main(String[] args) {
//        String s = "the quick brown fox jumps over the lazy dog.";
//        Pattern p = Pattern.compile("\\wo\\w");
//        Matcher m = p.matcher(s);
//        while (m.find()) {
//            String sub = s.substring(m.start(), m.end());
//            System.out.println(sub);
//        }
//
//    }


    public static void main(String[] args) {
        String s = "The     quick\t\t brown   fox  jumps   over the  lazy dog.";
        String r = s.replaceAll("\\s+", " ");
        System.out.println(r); // "The quick brown fox jumps over the lazy dog."
    }

}
