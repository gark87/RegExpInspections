package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import org.gark87.idea.regexp.nazi.fixes.RegExpNaziQuickFix;
import org.gark87.idea.regexp.nazi.psi.RegExpUtil;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpBranch;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpRecursiveElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gark87 <arkady.galyash@gmail.com>
 */
public class QuotingText extends RegExpNaziInspection {
    public long threshold = 3;

    @Override
    public JComponent createOptionsPanel() {
        JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT));
        result.add(new JLabel("Escapes threshold:"));
        JFormattedTextField textField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        textField.setValue(threshold);
        textField.setColumns(5);
        result.add(textField);
        textField.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                threshold = (Long)evt.getNewValue();
            }
        });
        return result;
    }

    private final static LocalQuickFix FIX = new RegExpNaziQuickFix() {
        @NotNull
        public String getName() {
            return "Quote text";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            PsiElement startElement = problemDescriptor.getStartElement();
            PsiElement endElement = problemDescriptor.getEndElement();
            ASTNode startNode = startElement.getNode();
            startNode.getTreeParent().addChild(new PsiWhiteSpaceImpl("\\\\Q"), startNode);
            ASTNode endNode = endElement.getNode();
            ASTNode treeNext = endNode.getTreeNext();
            endNode.getTreeParent().addChild(new PsiWhiteSpaceImpl("\\\\E"), treeNext);
            List<RegExpChar> chars = new ArrayList<RegExpChar>();
            for (PsiElement elem = startElement; elem != null && elem.getPrevSibling() != endElement; elem = elem.getNextSibling()) {
                if (!(elem instanceof RegExpChar))
                    continue;
                chars.add((RegExpChar)elem);
            }
            for(RegExpChar ch : chars) {
                if (!RegExpUtil.isEscaped(ch))
                    continue;
                ASTNode node = ch.getNode();
                String text = node.getText();
                String replacement = text.length() == 2 ? text.substring(1) : text.substring(2);
                ASTNode parent = node.getTreeParent();
                parent.addLeaf(RegExpTT.CHARACTER, replacement, node);
                parent.removeChild(node);
            }
        }
    };

    @Override
    protected PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly, final List<ProblemDescriptor> result) {
        return new RegExpRecursiveElementVisitor() {

            @Override
            public void visitRegExpBranch(RegExpBranch branch) {
                int escaped = 0, startIndex = 0;
                PsiElement[] children = branch.getChildren();
                int length = children.length;
                for (int i = 0; i < length; i++) {
                    PsiElement child = children[i];
                    if (!(child instanceof RegExpChar)) {
                        if (escaped >= threshold)
                            result.add(createProblemDescriptor(manager, isOnTheFly, children[startIndex], children[i - 1]));
                        escaped = 0;
                        startIndex = -1;
                        continue;
                    }
                    RegExpChar ch = (RegExpChar) child;
                    if (startIndex < 0)
                        startIndex = i;
                    ASTNode node = ch.getNode();
                    ASTNode firstChildNode = node.getFirstChildNode();
                    if (!(firstChildNode instanceof  LeafPsiElement))
                        return;
                    LeafPsiElement leaf = (LeafPsiElement) firstChildNode;
                    if (leaf.getElementType() == RegExpTT.ESC_CHARACTER)
                        escaped++;
                }
                if (escaped >= threshold)
                    result.add(createProblemDescriptor(manager, isOnTheFly, children[startIndex], children[length - 1]));
            }
        };
    }

    private static ProblemDescriptor createProblemDescriptor(final InspectionManager manager, final boolean isOnTheFly, PsiElement start, PsiElement end) {
        return manager.createProblemDescriptor(start, end, "\\Q...\\E could be used",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly, FIX);
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Quoting text";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "QuotingText";
    }
}
