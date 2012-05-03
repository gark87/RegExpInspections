package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.RedundantEscapeInCharacterClass;
import org.junit.Test;

/**
 * Test for {@link org.gark87.idea.regexp.nazi.inspections.RedundantEscapeInCharacterClass}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RedundantEscapeInCharacterClassTest extends RegExpNaziTest {

    @Test
    public void testRegularCharClass() throws Exception {
        doTestFixes("[", "\\}", "]", "[}]");
        doTestFixes("[\\s", "\\}", "]", "[\\s}]");
        doTestFixes("[", "\\(", "]", "[(]");
        doTestFixes("[", "\\)", "]", "[)]");
        doTestNoProblems("[c]");
        doTestNoProblems("[1]");
        doTestNoProblems("[A]");
        doTestNoProblems("[\\\\]");
    }

    @Test
    public void testNegationCharacterClass() throws Exception {
        doTestFixes("[^", "\\}", "]", "[^}]");
        doTestFixes("[^\\s", "\\}", "]", "[^\\s}]");
        doTestFixes("[^", "\\(", "]", "[^(]");
        doTestFixes("[^", "\\)", "]", "[^)]");
        doTestNoProblems("[^c]");
        doTestNoProblems("[^1]");
        doTestNoProblems("[^A]");
        doTestNoProblems("[^\\\\]");
        doTestNoProblems("[asd\\^qwe]");
        doTestNoProblems("[\\^qwe]");
        doTestNoProblems("[asd\\^]");
    }

    @Test
    public void testClassRanges() throws Exception {
        doTestFixes("[", "\\{", "-}]", "[{-}]");
        doTestFixes("[^", "\\{", "-}]", "[^{-}]");
        doTestFixes("[{-", "\\}", "]", "[{-}]");
        doTestFixes("[^{-", "\\}", "]", "[^{-}]");
        doTestNoProblems("[^a\\-b]");
    }

    @Test
    public void testSpecialChars() throws Exception {
        doTestNoProblems("[\\r]");
        doTestNoProblems("[\\t]");
        doTestNoProblems("[\\n]");
        doTestNoProblems("[^\\r]");
        doTestNoProblems("[^\\t]");
        doTestNoProblems("[^\\n]");
        doTestNoProblems("[\\\\r]");
        doTestNoProblems("[\\\\t]");
        doTestNoProblems("[\\\\n]");
        doTestNoProblems("[\r]");
        doTestNoProblems("[\t]");
        doTestNoProblems("[\n]");
    }

    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new RedundantEscapeInCharacterClass();
    }
}
