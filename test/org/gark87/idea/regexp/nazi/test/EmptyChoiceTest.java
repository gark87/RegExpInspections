package org.gark87.idea.regexp.nazi.test;

import org.gark87.idea.regexp.nazi.inspections.EmptyChoice;

/**
 * Test for {@link EmptyChoice}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class EmptyChoiceTest extends RegExpNaziTest {

    public void testNoQuantification() throws Exception {
        doTestFixes("(a", "|", "|b)", "(a|b)?");
        doTestFixes("a", "|", "|b", "(?:a|b)?");
        doTestFixes("a|b", "|", "|", "(?:a|b)?");
        doTestFixes("(a|b", "|", "|)", "(a|b)?");
        doTestFixes("|", "|", "a|b", "(?:a|b)?");
        doTestFixes("(|", "|", "|a|b)", "(a|b)?");
    }

    public void testGreedyQuantification() throws Exception {
        // *
        doTestFixes("(a", "|", "|b)*", "(a|b)*");
        doTestFixes("(a|b", "|", ")*", "(a|b)*");
        doTestFixes("(", "|", "|a|b)*", "(a|b)*");
        // +
        doTestFixes("(a", "|", "|b)+", "(a|b)*");
        doTestFixes("(a|b", "|", ")+", "(a|b)*");
        doTestFixes("(", "|", "|a|b)+", "(a|b)*");
        // ?
        doTestFixes("(a", "|", "|b)?", "(a|b)?");
        doTestFixes("(a|b", "|", ")?", "(a|b)?");
        doTestFixes("(", "|", "|a|b)?", "(a|b)?");
        // {5}
        doTestFixes("(a", "|", "|b){5}", "(a|b){0,5}");
        doTestFixes("(a|b", "|", "){5}", "(a|b){0,5}");
        doTestFixes("(", "|", "|a|b){5}", "(a|b){0,5}");
        // {1}
        doTestFixes("(a", "|", "|b){1}", "(a|b)?");
        doTestFixes("(a|b", "|", "){1}", "(a|b)?");
        doTestFixes("(", "|", "|a|b){1}", "(a|b)?");
        // {1,5}
        doTestFixes("(a", "|", "|b){1,5}", "(a|b){0,5}");
        doTestFixes("(a|b", "|", "){1,5}", "(a|b){0,5}");
        doTestFixes("(", "|", "|a|b){1,5}", "(a|b){0,5}");
        // {0,5}
        doTestFixes("(a", "|", "|b){0,5}", "(a|b){0,5}");
        doTestFixes("(a|b", "|", "){0,5}", "(a|b){0,5}");
        doTestFixes("(", "|", "|a|b){0,5}", "(a|b){0,5}");
        // {0,1}
        doTestFixes("(a", "|", "|b){0,1}", "(a|b)?");
        doTestFixes("(a|b", "|", "){0,1}", "(a|b)?");
        doTestFixes("(", "|", "|a|b){0,1}", "(a|b)?");
        // {0,}
        doTestFixes("(a", "|", "|b){0,}", "(a|b)*");
        doTestFixes("(a|b", "|", "){0,}", "(a|b)*");
        doTestFixes("(", "|", "|a|b){0,}", "(a|b)*");
        // {1,}
        doTestFixes("(a", "|", "|b){1,}", "(a|b)*");
        doTestFixes("(a|b", "|", "){1,}", "(a|b)*");
        doTestFixes("(", "|", "|a|b){1,}", "(a|b)*");
        // {5,}
        doTestFixes("(a", "|", "|b){5,}", "(a|b)*");
        doTestFixes("(a|b", "|", "){5,}", "(a|b)*");
        doTestFixes("(", "|", "|a|b){5,}", "(a|b)*");

    }


    protected EmptyChoice createInspectionToTest() {
        return new EmptyChoice();
    }
}
