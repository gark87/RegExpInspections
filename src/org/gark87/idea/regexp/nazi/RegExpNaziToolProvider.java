package org.gark87.idea.regexp.nazi;

import com.intellij.codeInspection.InspectionToolProvider;
import org.gark87.idea.regexp.nazi.inspections.EmptyChoice;
import org.gark87.idea.regexp.nazi.inspections.DuplicationInCharacterClasses;
import org.gark87.idea.regexp.nazi.inspections.UselessCharacterClass;
import org.gark87.idea.regexp.nazi.inspections.UselessRegExpOption;

/**
 * This is my inspections bundle for RegExps.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpNaziToolProvider implements InspectionToolProvider {
    public static final String GROUP_NAME = "RegExpNazi";
    private static final Class[] INSPECTIONS = {EmptyChoice.class, DuplicationInCharacterClasses.class,
            UselessCharacterClass.class, UselessRegExpOption.class};

    public Class[] getInspectionClasses() {
        return INSPECTIONS;
    }
}
