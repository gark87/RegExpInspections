package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.UselessCharacterClass;
import org.junit.Test;

/**
 * Test for {@link UselessCharacterClass}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class UselessCharacterClassTest extends RegExpNaziTest {

    @Test
    public void testRegularChar() throws Exception {
        doTestAllRegexFix("[c]", "c");
        doTestAllRegexFix("[1]", "1");
        doTestAllRegexFix("[A]", "A");
        doTestAllRegexFix("[;]", ";");
        doTestNoProblems("[^c]");
        doTestNoProblems("[^1]");
        doTestNoProblems("[^A]");
    }


    @Test
    public void testSpecialChars() throws Exception {
        doTestNoProblems("[.]");
        doTestNoProblems("[]]");
        doTestNoProblems("[\\[]");
        doTestAllRegexFix("[-]", "-");
    }

    @Test
    public void testSimpleCharacterClasses() throws Exception {
        doTestAllRegexFix("[\\s]", "\\s");
        doTestFixes("", "[\\s]", "*", "\\s*");
        doTestAllRegexFix("[\\S]", "\\S");
        doTestAllRegexFix("[\\D]", "\\D");
        doTestAllRegexFix("[\\d]", "\\d");
        doTestAllRegexFix("[\\w]", "\\w");
        doTestAllRegexFix("[\\W]", "\\W");
    }

    @Test
    public void testNegatedSimpleCharacterClasses() throws Exception {
        doTestAllRegexFix("[^\\s]", "\\S");
        doTestAllRegexFix("[^\\S]", "\\s");
        doTestAllRegexFix("[^\\D]", "\\d");
        doTestAllRegexFix("[^\\d]", "\\D");
        doTestAllRegexFix("[^\\w]", "\\W");
        doTestAllRegexFix("[^\\W]", "\\w");
    }

    @Test
    public void testNegatedSimple() throws Exception {
        doTestNoProblems("[^.]");
        doTestNoProblems("[^a]");
        doTestNoProblems("[^\\t]");
    }

    @Test
    public void testSlashedChars() throws Exception {
        doTestAllRegexFix("[\\\\]", "\\\\");
        doTestAllRegexFix("[\0]", "\0");
        doTestAllRegexFix("[\02]", "\02");
        doTestAllRegexFix("[\001]", "\001");
        doTestAllRegexFix("[\u1234]", "\u1234");
        doTestAllRegexFix("[\t]", "\t");
        doTestAllRegexFix("[\n]", "\n");
        doTestAllRegexFix("[\f]", "\f");
        doTestAllRegexFix("[\\a]", "\\a");
        doTestAllRegexFix("[\\e]", "\\e");
    }

    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new UselessCharacterClass();
    }
}
