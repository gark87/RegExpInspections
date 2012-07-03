package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.ManualDot;
import org.junit.Test;

/**
 * Test for {@link org.gark87.idea.regexp.nazi.inspections.ManualDot}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class ManualDotTest extends RegExpNaziTest {

    @Test
    public void testWithDotAllOption() throws Exception {
        String withDotAll = "(?s:.)";
        doTestAllRegexFix("[\\w\\W]", withDotAll);
        doTestAllRegexFix("[\\s\\S]", withDotAll);
        doTestAllRegexFix("[\\d\\D]", withDotAll);
        doTestAllRegexFix("[0-9\\D]", withDotAll);
        doTestAllRegexFix("[0-4\\D5-9]", withDotAll);
        doTestAllRegexFix("[01234\\D5-9]", withDotAll);
        doTestAllRegexFix("[0123456789\\D5-9]", withDotAll);
        doTestAllRegexFix("[012345\\D5-9]", withDotAll);
        doTestAllRegexFix("[01234\\D5-9]", withDotAll);
        doTestNoProblems("[0123\\D5-9]");
        doTestNoProblems("[^01234\\D5-9]");
        doTestNoProblems("[^\\w\\W]");
    }

    @Test
    public void testWithoutDotAllOption() throws Exception {
        String withDotAll = "(?-s:.)";
        doTestAllRegexFix("[^\\r\\n]", withDotAll);
        doTestAllRegexFix("[\u0000-\u0009\u000B-\u000C\u000E-\uFFFF]", withDotAll);
        doTestAllRegexFix("[\u0000-\u0009\u000B\u000C\u000E-\uFFFF]", withDotAll);
        doTestNoProblems("[^\\r\\na]");
        doTestNoProblems("[\\r\\n]");
        doTestNoProblems("[^\u0000-\u0009\u000B\u000E-\uFFFF]");
        doTestNoProblems("[\u0000-\u0009\u000B\u000E-\uFFFF]");
    }


    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new ManualDot();
    }
}
