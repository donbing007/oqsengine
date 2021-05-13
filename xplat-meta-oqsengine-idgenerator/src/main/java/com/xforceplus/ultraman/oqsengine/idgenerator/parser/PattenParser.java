package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

/**
 * @author leo
 * parser the id patten
 */
public interface PattenParser {
    /**
     * the name of the parser
     * @return
     */
    String getName();

    /**
     * Parse the variable in the ID expression
     * @param patten
     * @param id
     * @return
     */
    String parse(String patten,Long id);


    /**
     * 判断是否可以解析
     * @param patten
     * @return
     */
    boolean  needHandle(String patten);
}
