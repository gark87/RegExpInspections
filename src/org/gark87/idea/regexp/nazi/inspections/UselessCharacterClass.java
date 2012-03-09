package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.gark87.idea.regexp.nazi.psi.RegExpClassAnalyzer;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This inspection is about <code>[\\s]</code> or <code>[;]</code>.
 * It these cases we do not need character class.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class UselessCharacterClass extends LocalInspectionTool {

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
        return "Useless character class brackets";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UselessCharacterClass";
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        if (file.getClass() != RegExpFile.class)
            return ProblemDescriptor.EMPTY_ARRAY;
        final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
        file.acceptChildren(new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpClass(RegExpClass expClass) {
                RegExpClassAnalyzer analyzer = new RegExpClassAnalyzer(expClass, false);
                if (analyzer.isNegated())
                    return;
                RegExpClassElement only = null;
                for (List<RegExpChar> chars : analyzer.getSingleChars().values()) {
                    int size = chars.size();
                    if (size == 0)
                        continue;
                    if (only != null)
                        return;
                    RegExpChar regExpChar = chars.get(0);
                    Character ch = regExpChar.getValue();
                    if (ch == null || ch == '.' || ch == ']' || ch =='[')
                        continue;
                    only = regExpChar;
                }
                for (RegExpSimpleClass simpleClass : analyzer.getSimpleClasses()) {
                    if (only != null)
                        return;
                    only = simpleClass;
                }
                if (only != null)
                    result.add(createProblemDescriptor(manager, isOnTheFly, expClass, only.getText()));
            }
        });
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private ProblemDescriptor createProblemDescriptor(InspectionManager manager, boolean onTheFly, RegExpClass expClass, @Nullable String replacementText) {
        return manager.createProblemDescriptor(expClass, "Useless character class brackets", onTheFly,
                new LocalQuickFix[]{new RemoveCharacterClass(replacementText)}, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    private class RemoveCharacterClass implements LocalQuickFix {
        private String replacementText;

        public RemoveCharacterClass(String replacementText) {
            this.replacementText = replacementText;
        }

        @NotNull
        public String getName() {
            return "Remove character class brackets";
        }

        @NotNull
        public String getFamilyName() {
            return "RegExpNazi";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            RegExpClass expClass = (RegExpClass) problemDescriptor.getPsiElement();
            if (replacementText == null) {
                expClass.delete();
                return;
            }
            final PsiFileFactory factory = PsiFileFactory.getInstance(project);
            final PsiFile f = factory.createFileFromText("dummy.regexp", RegExpFileType.INSTANCE, replacementText);
            RegExpPattern replacement = PsiTreeUtil.getChildOfType(f, RegExpPattern.class);
            assert replacement != null;
            expClass.replace(replacement);
        }

    }
}
