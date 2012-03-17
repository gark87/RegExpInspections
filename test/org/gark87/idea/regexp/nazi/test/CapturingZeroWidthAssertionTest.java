package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.CapturingZeroWidthAssertion;
import org.junit.Test;

/**
 * Test for {@link CapturingZeroWidthAssertion}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class CapturingZeroWidthAssertionTest extends RegExpNaziTest {

    @Test
    public void testSimple() throws Exception {
        doTestAllRegexFix("(^)", "^");
        doTestAllRegexFix("(^|$|\\z|\\Z)", "^|$|\\z|\\Z");
        doTestAllRegexFix("(\\G*\\b+\\z?)", "\\G*\\b+\\z?");
        doTestNoProblems("((?i:a)$)");
        doTestNoProblems("(((?i)a)$)");
    }

    @Test
    public void testLookAround() throws Exception {
        doTestAllRegexFix("((?=a)$)", "(?=a)$");
        doTestAllRegexFix("((?=a))", "(?=a)");
        doTestAllRegexFix("((?!a)\\B)", "(?!a)\\B");
        doTestAllRegexFix("((?<=X)^|$|\\z|\\Z)", "(?<=X)^|$|\\z|\\Z");
        doTestAllRegexFix("((?<!X)\\G*\\b+\\z?)", "(?<!X)\\G*\\b+\\z?");
    }

    @Test
    public void testQuantification() throws Exception {
        doTestNoProblems("(^)?");
        doTestNoProblems("(^|$|\\z|\\Z)*");
        doTestNoProblems("(\\G*\\b+\\z?){91}");
        doTestNoProblems("((?i:a)$){2,}");
        doTestNoProblems("(((?i)a)$){2,3}");
        doTestNoProblems("((?=a)$)*?");
        doTestNoProblems("((?=a))+?");
        doTestNoProblems("((?!a)\\B)??");
        doTestNoProblems("((?<=X)^|$|\\z|\\Z)++");
        doTestNoProblems("((?<!X)\\G*\\b+\\z?){5,6}+");
    }

    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new CapturingZeroWidthAssertion();
    }
}
