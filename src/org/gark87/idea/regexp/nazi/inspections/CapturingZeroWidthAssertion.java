package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.gark87.idea.regexp.nazi.fixes.ReplaceRegExpWith;
import org.gark87.idea.regexp.nazi.psi.RegExpRecursiveFinder;
import org.gark87.idea.regexp.nazi.psi.RegExpUtil;
import org.intellij.lang.regexp.RegExpElementType;
import org.intellij.lang.regexp.RegExpElementTypes;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This inspection is about capturing a zero-width assertion
 * For example, {@code (^)} or {@code (\b)}.
 * If capturing needs for quantification - it is ok.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class CapturingZeroWidthAssertion extends RegExpNaziInspection {
    private static final String FIX_NAME = "Remove useless group";

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Useless group";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "CapturingZeroWidthAssertion";
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        if (file.getClass() != RegExpFile.class)
            return ProblemDescriptor.EMPTY_ARRAY;
        final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
        file.acceptChildren(new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpGroup(RegExpGroup group) {
                super.visitRegExpGroup(group);
                if (!group.isCapturing())
                    return;
                PsiElement nextSibling = group.getNextSibling();
                if (nextSibling != null && nextSibling instanceof RegExpQuantifier)
                    return;
                RegExpRecursiveFinder finder = new RegExpRecursiveFinder() {

                    @Override
                    public void visitRegExpGroup(RegExpGroup group) {
                        if (RegExpUtil.isLookAround(group))
                            return;
                        super.visitRegExpGroup(group);
                    }

                    @Override
                    public void visitRegExpChar(RegExpChar ch) {
                        if (isFound())
                            return;
                        found();
                    }

                    @Override
                    public void visitSimpleClass(RegExpSimpleClass simpleClass) {
                        if (isFound())
                            return;
                        found();
                    }

                    @Override
                    public void visitRegExpBackref(RegExpBackref backref) {
                        if (isFound())
                            return;
                        found();
                    }

                    @Override
                    public void visitRegExpCharRange(RegExpCharRange range) {
                        if (isFound())
                            return;
                        found();
                    }

                    @Override
                    public void visitRegExpClass(RegExpClass expClass) {
                        if (isFound())
                            return;
                        found();
                    }
                };
                group.acceptChildren(finder);
                if (finder.isFound())
                    return;
                RegExpPattern pattern = group.getPattern();
                if (pattern == null)
                    return;
                result.add(createProblemDescriptor(manager, isOnTheFly, group, pattern.getText()));
            }
        });
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private ProblemDescriptor createProblemDescriptor(InspectionManager manager, boolean onTheFly, RegExpGroup group,
                                                      @Nullable String replacementText)
    {
        LocalQuickFix[] fixes = {new ReplaceRegExpWith(FIX_NAME, replacementText)};
        return manager.createProblemDescriptor(group, "Useless group", onTheFly, fixes,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      }

}
