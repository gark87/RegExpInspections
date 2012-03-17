package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.DuplicationInCharacterClasses;
import org.junit.Test;

/**
 * Test for {@link DuplicationInCharacterClasses}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class DuplicationInCharacterClassesTest extends RegExpNaziTest {

    @Test
    public void testSingleVsSingle() throws Exception {
        // positive
        doTestFixes("[abc", "a", "]", "[abc]");
        doTestFixes("[a", "a", "]", "[a]");
        doTestFixes("[\t", "\t", "]", "[\t]");
        doTestFixes("[\\\\", "\\\\", "]", "[\\\\]");
        // negative
        doTestFixes("[^abc", "a", "]", "[^abc]");
        doTestFixes("[^a", "a", "]", "[^a]");
        doTestFixes("[^\t", "\t", "]", "[^\t]");
        doTestFixes("[^\\\\", "\\\\", "]", "[^\\\\]");
    }

    @Test
    public void testSingleVsSimpleClass() throws Exception {
        // positive
        doTestFixes("[\\w", "a", "]", "[\\w]");
        doTestFixes("[\\d", "0", "]", "[\\d]");
        doTestFixes("[\\W", " ", "]", "[\\W]");
        doTestFixes("[\\S", "a", "]", "[\\S]");
        doTestFixes("[\\s", "\\n", "]", "[\\s]");
        doTestFixes("[\\s", "\n", "]", "[\\s]");
        // negative
        doTestFixes("[^\\w", "a", "]", "[^\\w]");
        doTestFixes("[^\\d", "0", "]", "[^\\d]");
        doTestFixes("[^\\W", " ", "]", "[^\\W]");
        doTestFixes("[^\\S", "a", "]", "[^\\S]");
        doTestFixes("[^\\s", "\\n", "]", "[^\\s]");
        doTestFixes("[^\\s", "\n", "]", "[^\\s]");
    }

    @Test
    public void testSingleVsClass() throws Exception {
        // positive
        doTestFixes("[a-g", "a", "]", "[a-g]");
        doTestFixes("[0-8", "5", "]", "[0-8]");
        doTestFixes("[h-p", "p", "]", "[h-p]");
        // negative
        doTestFixes("[^a-g", "a", "]", "[^a-g]");
        doTestFixes("[^0-8", "5", "]", "[^0-8]");
        doTestFixes("[^h-p", "p", "]", "[^h-p]");

    }


    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new DuplicationInCharacterClasses();
    }
}
