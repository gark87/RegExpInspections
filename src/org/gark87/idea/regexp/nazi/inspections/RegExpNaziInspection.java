package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.intellij.lang.regexp.RegExpFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    protected abstract PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly,
                                                       final List<ProblemDescriptor> result);

    @Override
    public final ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager,
                                         final boolean isOnTheFly)
    {
        if (file.getClass() != RegExpFile.class)
            return ProblemDescriptor.EMPTY_ARRAY;
        final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
        file.acceptChildren(createVisitor(manager, isOnTheFly, result));
        return result.toArray(new ProblemDescriptor[result.size()]);
    }
}
