package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.gark87.idea.regexp.nazi.fixes.RegExpNaziQuickFix;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpClass;
import org.intellij.lang.regexp.psi.RegExpRecursiveElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author gark87 <arkady.galyash@gmail.com>
 */
public class RedundantEscapeInCharacterClass extends RegExpNaziInspection {
    @Override
    protected PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly,
                                              final List<ProblemDescriptor> result) {
        return new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpChar(RegExpChar ch) {
                super.visitRegExpChar(ch);
                if (!ch.getText().startsWith("\\"))
                    return;
                Character value = ch.getValue();
                if (value == null)
                    return;
                switch (value) {
                    case '\\':
                    case '-':
                    case ']':
                    case '\t':
                    case '\r':
                    case '\n':
                    case '^':
                        return;
                }
                RegExpClass characterClass = PsiTreeUtil.getParentOfType(ch, RegExpClass.class);
                if (characterClass == null)
                    return;
                addProblemDescriptors(result, manager, isOnTheFly, ch);

            }
        };
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Redundant character escape inside character class";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "RedundantEscapeInCharacterClass";
    }

    private void addProblemDescriptors(List<ProblemDescriptor> result, InspectionManager manager, boolean onTheFly,
                                       RegExpChar ch) {
        result.add(manager.createProblemDescriptor(ch, "Redundant character escape inside character class: " + ch,
                new RemoveUselessEscapingInCharacterClass(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, onTheFly));
    }

    private class RemoveUselessEscapingInCharacterClass extends RegExpNaziQuickFix {
        @NotNull
        public String getName() {
            return "Remove redundant escape";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            RegExpChar ch = (RegExpChar) problemDescriptor.getPsiElement();
            ASTNode node = ch.getNode();
            ASTNode parent = node.getTreeParent();
            String text = node.getText();
            String replacement = text.length() == 2 ? text.substring(1) : text.substring(2);
            parent.addLeaf(node.getElementType(), replacement, node);
            parent.removeChild(node);
        }
    }
}
