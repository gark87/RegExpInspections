package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.UselessRegExpOption;
import org.junit.Test;

/**
 * Test for {@link UselessRegExpOptionTest}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class UselessRegExpOptionTest extends RegExpNaziTest {

    @Test
    public void test_i() throws Exception {
        // \t
        doTestFixes("(?", "i", ")\t", "(?)\t");
        doTestFixes("(?", "-i", ")\t", "(?-)\t");
        doTestFixes("(?", "i", ":\t)", "(?:\t)");
        doTestFixes("(?", "-i", ":\t)", "(?-:\t)");
        // \\s
        doTestFixes("(?", "i", ")\\s", "(?)\\s");
        doTestFixes("(?", "-i", ")\\s", "(?-)\\s");
        doTestFixes("(?", "i", ":\\s)", "(?:\\s)");
        doTestFixes("(?", "-i", ":\\s)", "(?-:\\s)");
        // ok
        doTestNoProblems("(?i)\\si");
        doTestNoProblems("(?-i)\\si");
        doTestNoProblems("(?i:\\si)");
        doTestNoProblems("(?-i:\\si)");
    }

    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new UselessRegExpOption();
    }
}
