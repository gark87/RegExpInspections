package org.gark87.idea.regexp.nazi;

import com.intellij.codeInspection.InspectionToolProvider;
import org.gark87.idea.regexp.nazi.inspections.*;

/**
 * This is my inspections bundle for RegExps.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpNaziToolProvider implements InspectionToolProvider {
    private static final Class[] INSPECTIONS = {EmptyChoice.class, DuplicationInCharacterClasses.class,
            UselessCharacterClass.class, UselessRegExpOption.class, CapturingZeroWidthAssertion.class,
            ManualDot.class, ManualSimpleClass.class, ExtraSlash.class, RedundantEscapeInCharacterClass.class,
            QuotingText.class};

    public Class[] getInspectionClasses() {
        return INSPECTIONS;
    }
}
