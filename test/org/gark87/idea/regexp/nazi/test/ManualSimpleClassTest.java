package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.LocalInspectionTool;
import org.gark87.idea.regexp.nazi.inspections.ManualSimpleClass;
import org.junit.Test;

/**
 * Test for {@link ManualSimpleClass}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class ManualSimpleClassTest extends RegExpNaziTest {

    @Test
    public void testDigit() throws Exception {
        doTestFixes("[", "0-9", "]", "[\\\\d]");
        doTestFixes("[", "0", "123456789]", "[\\\\d]");
        doTestFixes("[asd", "0-9", "zxc]", "[asd\\\\dzxc]");
        doTestFixes("[asd", "0", "123456789zxc]", "[asd\\\\dzxc]");
        doTestFixes("[asd", "0", "r123456789zxc]", "[asd\\\\drzxc]");
        doTestFixes("[asd", "0-6", "zxc7-89]", "[asd\\\\dzxc]");
        doTestNoProblems("[012345678]");
        doTestNoProblems("[012345678asdasd]");
        doTestNoProblems("[0-a]");
        doTestNoProblems("[3-a]");
        doTestNoProblems("[3-902\u0000-\uaaaa]");
    }

    @Override
    protected LocalInspectionTool createInspectionToTest() {
        return new ManualSimpleClass();
    }
}
