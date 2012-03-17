package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.*;
import org.intellij.lang.regexp.psi.impl.RegExpPatternImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This inspection is about empty choice.
 * I'm pretty sure that using quantification in this case safer.
 * Some people still confuse {@code |} and {@code ||}. And use the second one in regexps.
 * This inspections is for them.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class EmptyChoice extends LocalInspectionTool {

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return RegExpNaziToolProvider.GROUP_NAME;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Empty choice instead of quantification";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "EmptyChoice";
    }


    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        if (file.getClass() != RegExpFile.class)
            return ProblemDescriptor.EMPTY_ARRAY;
        final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
        file.acceptChildren(new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpBranch(RegExpBranch branch) {
                super.visitRegExpBranch(branch);
                if (branch.getAtoms().length == 0) {
                    final PsiElement nextSibling = branch.getNextSibling();
                    RegExpPatternImpl patternParent = (RegExpPatternImpl) branch.getParent();
                    if (nextSibling != null && nextSibling.getNode().getElementType() == RegExpTT.UNION)
                        result.add(createProblemDesc(nextSibling, patternParent, isOnTheFly, manager, "Left choice is empty"));
                    final PsiElement prevSibling = branch.getPrevSibling();
                    if (prevSibling != null && prevSibling.getNode().getElementType() == RegExpTT.UNION)
                        result.add(createProblemDesc(prevSibling, patternParent, isOnTheFly,  manager, "Right choice is empty"));
                }
            }
        });
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private ProblemDescriptor createProblemDesc(PsiElement element, RegExpPatternImpl pattern, boolean isOnTheFly,
                                                InspectionManager manager, String text)
    {
        return manager.createProblemDescriptor(element, text, isOnTheFly,
                new LocalQuickFix[]{new ReplaceEmptyChoice(pattern)}, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    private class ReplaceEmptyChoice implements LocalQuickFix {
        private final RegExpPatternImpl pattern;

        @NotNull
        public String getName() {
            return "Replace with quantification";
        }

        @NotNull
        public String getFamilyName() {
            return "RegExpNazi";
        }

        public ReplaceEmptyChoice(RegExpPatternImpl pattern) {
            this.pattern = pattern;
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            StringBuilder newPattern = new StringBuilder();
            for(PsiElement element: pattern.getChildren()) {
                if (element instanceof RegExpBranch) {
                    RegExpBranch branch = (RegExpBranch)element;
                    if (branch.getAtoms().length == 0)
                        continue;
                    newPattern.append(branch.getText()).append('|');
                }
            }
            if (newPattern.length() == 0) {
                pattern.delete();
                return;
            }
            newPattern.deleteCharAt(newPattern.length() - 1);
            PsiElement replace = pattern;

            PsiElement parent = pattern.getParent();
            if (parent != null && parent instanceof RegExpGroup) {
                RegExpGroup group = (RegExpGroup) parent;
                replace = group;
                PsiElement open = group.getFirstChild();
                assert open != null;
                PsiElement close = group.getLastChild();
                assert close != null;
                newPattern.insert(0, open.getText()).append(close.getText());
                PsiElement uncle = group.getNextSibling();
                if (uncle != null && uncle instanceof RegExpQuantifier) {
                    RegExpQuantifier quantifier = (RegExpQuantifier)uncle;
                    RegExpQuantifier.Count count = quantifier.getCount();
                    int max = count.getMax();
                    if (max == Integer.MAX_VALUE) {
                        newPattern.append("*");
                    } else if (count == RegExpQuantifier.SimpleCount.ONE_OR_MORE) {
                        newPattern.append("?");
                    } else if (max == 1) {
                        newPattern.append("?");
                    } else {
                        newPattern.append("{0,").append(max).append('}');
                    }
                    newPattern.append(quantifier.getType().getToken());
                    uncle.delete();
                } else {
                    newPattern.append('?');
                }
            } else {
                newPattern.insert(0, "(?:").append(")?");
            }

            final PsiFileFactory factory = PsiFileFactory.getInstance(project);
            final PsiFile f = factory.createFileFromText("dummy.regexp", RegExpFileType.INSTANCE, newPattern);
            RegExpPattern replacement = PsiTreeUtil.getChildOfType(f, RegExpPattern.class);
            assert replacement != null;
            replace.replace(replacement);


        }
    }
}
