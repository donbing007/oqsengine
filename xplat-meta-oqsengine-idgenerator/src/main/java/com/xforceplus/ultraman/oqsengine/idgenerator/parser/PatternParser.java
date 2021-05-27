package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

/**
 * parser the id patten.
 *
 * @author leo
 */
public interface PatternParser {
    /**
     * the name of the parser.
     *
     * @return parser name
     */
    String getName();

    /**
     * Parse the variable in the ID expression.
     *
     * @param patten pattern
     * @param id id
     *
     * @return target id
     */
    String parse(String patten, Long id);


    /**
     * 判断是否可以解析.
     *
     * @param patten pattern
     *
     * @return boolean
     */
    boolean needHandle(String patten);
}
