package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * This is base class for all {@link LocalInspectionTool}-s of RegExpNazi.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public abstract class RegExpNaziInspection extends LocalInspectionTool {

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "RegExpNazi";
    }
}
