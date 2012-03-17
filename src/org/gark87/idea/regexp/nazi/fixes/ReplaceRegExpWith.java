package org.gark87.idea.regexp.nazi.fixes;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.psi.RegExpPattern;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ReplaceRegExpWith extends RegExpNaziQuickFix {
    private final String name;
    private final String replacementText;

    public ReplaceRegExpWith(String name, String replacementText) {
        this.name = name;
        this.replacementText = replacementText;
    }

    @NotNull
        public String getFamilyName() {
            return "RegExpNazi";
        }

    @NotNull
    public String getName() {
        return name;
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        PsiElement expClass = problemDescriptor.getPsiElement();
        if (replacementText == null) {
            expClass.delete();
            return;
        }
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        PsiFile f = factory.createFileFromText("dummy.regexp", RegExpFileType.INSTANCE, replacementText);
        RegExpPattern replacement = PsiTreeUtil.getChildOfType(f, RegExpPattern.class);
        assert replacement != null;
        expClass.replace(replacement);
    }
}
