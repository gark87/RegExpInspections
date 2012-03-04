package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: galyash
 * Date: 04.03.12
 * Time: 3:56
 * To change this template use File | Settings | File Templates.
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
                if (expClass.isNegated())
                    return;
                RegExpClassElement[] elements = PsiTreeUtil.getChildrenOfType(expClass, RegExpClassElement.class);
                if (elements == null)
                    return;
                if (elements.length == 0) {
                    result.add(createProblemDescriptor(manager, isOnTheFly, expClass, null));
                    return;
                }
                if (elements.length != 1)
                    return;
                RegExpClassElement element = elements[0];
                if (element instanceof RegExpChar) {
                    RegExpChar ch = (RegExpChar) element;
                    Character value = ch.getValue();
                    if (value == null)
                        return;
                    if (value != '.' && value != ']' && value != '[')
                        result.add(createProblemDescriptor(manager, isOnTheFly, expClass, ch.getText()));
                } else if (element instanceof RegExpSimpleClass) {
                    RegExpSimpleClass simpleClass = (RegExpSimpleClass) element;
                    result.add(createProblemDescriptor(manager, isOnTheFly, expClass, simpleClass.getText()));
                }
            }
        });
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private ProblemDescriptor createProblemDescriptor(InspectionManager manager, boolean onTheFly, RegExpClass expClass, @Nullable String replacementText) {
        return manager.createProblemDescriptor(expClass, "Useless character class brackets", onTheFly,
                new LocalQuickFix[]{new RemoveCharacterClass(expClass, replacementText)}, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    private class RemoveCharacterClass implements LocalQuickFix {
        private final RegExpClass expClass;
        private String replacementText;

        public RemoveCharacterClass(RegExpClass expClass, String replacementText) {
            this.expClass = expClass;
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
