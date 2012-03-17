package org.gark87.idea.regexp.nazi.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import org.jetbrains.annotations.NotNull;

/**
 * This is base class for all {@link LocalQuickFix}-es of RegExpNazi.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public abstract class RegExpNaziQuickFix implements LocalQuickFix {
    @NotNull
    public String getFamilyName() {
        return "RegExpNazi";
    }

}
