package org.gark87.idea.regexp.nazi;

import com.intellij.codeInspection.InspectionToolProvider;
import inspections.EmptyChoice;

/**
 * This is my inspections bundle for RegExps.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpNaziToolProvider implements InspectionToolProvider {
    private static final Class[] INSPECTIONS = new Class[]{EmptyChoice.class};

    public Class[] getInspectionClasses() {
        return INSPECTIONS;
    }
}
