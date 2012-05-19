package org.gark87.idea.regexp.nazi.test;

import org.gark87.idea.regexp.nazi.inspections.QuotingText;

/**
 * Test for {@link QuotingText}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class QuotingTextTest extends RegExpNaziTest {

    public void testSimple() throws Exception {
        doTestFixes("", "\\(\\(\\(\\(\\(\\(", "", "\\\\Q((((((\\\\E");
        doTestFixes("", ":\\(\\(\\(\\(\\(\\(", "", "\\\\Q:((((((\\\\E");
        doTestFixes("", "foobar\\(\\(\\(\\(\\(\\(", "", "\\\\Qfoobar((((((\\\\E");
        doTestFixes("\\d", "\\(\\(\\(\\(\\(\\(", "", "\\d\\\\Q((((((\\\\E");
        doTestFixes("", "\\(\\(\\(\\(\\(\\(", "\\d", "\\\\Q((((((\\\\E\\d");
        doTestFixes("", "\\(\\(\\(\\(\\(\\(", "\\d\\(\\(\\(\\(\\(\\(", "\\\\Q((((((\\\\E\\d\\(\\(\\(\\(\\(\\(");
    }

    public void testSpecial() throws Exception {
        doTestNoProblems("\\r\\n\\t\\r\\n\\r\\t\\n");
        doTestFixes("", "\\r\\n\\t\\r\\n\\r\\t\\n\\(\\(\\(\\(\\(\\(", "",
                        "\\\\Q\\r\\n\\t\\r\\n\\r\\t\\n((((((\\\\E");
    }

    protected QuotingText createInspectionToTest() {
        return new QuotingText();
    }
}
