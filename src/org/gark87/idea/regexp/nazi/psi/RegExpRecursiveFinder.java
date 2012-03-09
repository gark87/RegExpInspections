package org.gark87.idea.regexp.nazi.psi;

import org.intellij.lang.regexp.psi.RegExpElement;
import org.intellij.lang.regexp.psi.RegExpRecursiveElementVisitor;

/**
 * This is visitor that can stop when he finds what he is looking for.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpRecursiveFinder extends RegExpRecursiveElementVisitor {
    private boolean found = false;

    public void visitRegExpElement(RegExpElement element) {
        if (found)
            return;
        super.visitRegExpElement(element);
    }

    protected void found() {
        this.found = true;
    }

    public boolean isFound() {
        return found;
    }
}
