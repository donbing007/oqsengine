package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import org.junit.Test;

public class PageTest {

    @Test
    public void test(){
        Page page = new Page(1,10);
        page.setVisibleTotalCount(1);



        page.setTotalCount(10);

        PageScope nextPage = page.getNextPage();

        long startLine = nextPage.getStartLine();
        long endLine = nextPage.getEndLine();

        System.out.println(startLine);
        System.out.println(endLine);


        nextPage = page.getNextPage();

        startLine = nextPage.getStartLine();
        endLine = nextPage.getEndLine();

        System.out.println(startLine);
        System.out.println(endLine);
    }

}
