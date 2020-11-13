package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class LongTest {

    private int bitLength(String longStr){
        int bitLength = longStr.length();

        /**
         * omit all tail zeros
         */
        for(int i = longStr.length() - 1 ; i > 0 ; i -- ){
            char c = longStr.charAt(i);
            if(c == '0'){
                bitLength --;
            } else {
                break;
            }
        }

        return bitLength;
    }

    private String paddingZero(String longStr, int padding){

        StringBuffer sb = new StringBuffer();

        sb.append(longStr);

        while(padding --> 0){
            sb.append(0);
        }
        return sb.toString();
    }

    @Test
    public void longTest(){

        String longA = "30.020392300";
        String longB = "30.203923000";

        long o = 20392300000000000L;
        long p = 203923000000000000L;

        //extract
        int fixed = 18;

        String smallPart = longA.split("\\.")[1];

        int i = bitLength(smallPart);
        long a = Long.parseLong(paddingZero(smallPart.substring(0, i), fixed - i));

        String smallPartB = longB.split("\\.")[1];
        int i2 = bitLength(smallPartB);
        long b = Long.parseLong(paddingZero(smallPartB.substring(0, i2), fixed - i2));

        System.out.println(a);
        System.out.println(b);
    }


    @Test
    public void stringOrder(){
        Arrays.asList("a", "z", "b").stream().sorted(String::compareTo).forEach(System.out::println);
    }

    @Test
    public void test(){

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // create random string builder
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();

        // specify length of random string
        int length = 7;

        for(int i = 0; i < length; i++) {

            // generate random index number
            int index = random.nextInt(alphabet.length());

            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);

            // append the character to string builder
            sb.append(randomChar);
        }

        System.out.println(sb.toString());
    }
}