package org.gark87.idea.regexp.nazi;

import com.intellij.codeInspection.InspectionToolProvider;
import org.gark87.idea.regexp.nazi.inspections.EmptyChoice;
import org.gark87.idea.regexp.nazi.inspections.DuplicationInCharacterClasses;
import org.gark87.idea.regexp.nazi.inspections.UselessCharacterClass;

/**
 * This is my inspections bundle for RegExps.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpNaziToolProvider implements InspectionToolProvider {
    public static final String GROUP_NAME = "RegExpNazi";
    private static final Class[] INSPECTIONS = new Class[]{EmptyChoice.class, DuplicationInCharacterClasses.class, UselessCharacterClass.class};

    public Class[] getInspectionClasses() {
        return INSPECTIONS;
    }
}
