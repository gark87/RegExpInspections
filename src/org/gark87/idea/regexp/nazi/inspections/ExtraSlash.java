package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.gark87.idea.regexp.nazi.fixes.RegExpNaziQuickFix;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpRecursiveElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This inspection is about writing extra slash before special character(like `\\n', `\\r' or `\\t').
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class ExtraSlash extends RegExpNaziInspection {
    private static final Pattern EXTRA_SLASH = Pattern.compile("\\\\(\\\\[ntrf])");

    @Override
    protected PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly,
                                              final List<ProblemDescriptor> result)
    {
        return new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpChar(RegExpChar ch) {
                super.visitRegExpChar(ch);
                String text = ch.getText();
                if (text == null)
                    return;
                Matcher matcher = EXTRA_SLASH.matcher(text);
                if (matcher.matches()) {
                        result.add(createProblemDescriptor(manager, isOnTheFly, ch, matcher.group(1)));
                }
            }
        };
    }

     private ProblemDescriptor createProblemDescriptor(InspectionManager manager, boolean onTheFly, RegExpChar expChar,
                                                      String replacement)
     {
        LocalQuickFix[] fixes = {new ReplaceRegExp(replacement)};
        return manager.createProblemDescriptor(expChar, "Unneeded second slash", onTheFly, fixes,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Extra slash in special characters";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ExtraSlash";
    }

    private class ReplaceRegExp extends RegExpNaziQuickFix {
        private String replacement;

        public ReplaceRegExp(String replacement) {
            this.replacement = replacement;
        }

        @NotNull
        public String getName() {
            return "Delete second slash";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            RegExpChar slash = (RegExpChar) problemDescriptor.getPsiElement();
            if (slash == null)
                return;
            com.intellij.lang.ASTNode node = slash.getNode();
            com.intellij.lang.ASTNode parent = node.getTreeParent();
            parent.addLeaf(RegExpTT.CHARACTER, replacement, node);
            parent.removeChild(node);
        }
    }
}
