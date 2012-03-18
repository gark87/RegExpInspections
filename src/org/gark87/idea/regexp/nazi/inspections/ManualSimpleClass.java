package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.gark87.idea.regexp.nazi.fixes.RegExpNaziQuickFix;
import org.gark87.idea.regexp.nazi.psi.RegExpClassAnalyzer;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This inspection is about writing char ranges instead of using predefined character classes
 * For example, {@code [0-9]} instead of {@code \\d}.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class ManualSimpleClass extends RegExpNaziInspection {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Manual predefined character class";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ManualSimpleClass";
    }

    @Override
    protected PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly,
                                              final List<ProblemDescriptor> result)
    {
        return new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpClass(RegExpClass expClass) {
                RegExpClassAnalyzer analyzer = new RegExpClassAnalyzer(expClass, false);
                boolean[] digits = new boolean[10];
                List<RegExpElement> elements = new ArrayList<RegExpElement>();
                for (RegExpClassAnalyzer.CharRange charRange : analyzer.getCharacterRanges()) {
                    Character from = charRange.getFrom();
                    Character to = charRange.getTo();
                    if (from == null || to == null || from > to)
                        continue;
                    if (from > '9' || from < '0')
                        continue;
                    if (to > '9' || to < '0')
                        continue;
                    for (int i = from; i <= to; i++)
                        digits[i - '0'] = true;
                    elements.add(charRange.getPsiElement());
                }
                Map<Character, List<RegExpChar>> singleChars = analyzer.getSingleChars();
                for (char c = '0'; c <= '9'; c++) {
                    List<RegExpChar> regExpChars = singleChars.get(c);
                    if (regExpChars == null || regExpChars.isEmpty())
                        continue;
                    digits[c - '0'] = true;
                    elements.addAll(regExpChars);
                }
                for (boolean digit : digits) {
                    if (!digit)
                        return;
                }
                result.addAll(createProblemDescriptor(manager, isOnTheFly, elements, "\\\\d"));
            }
        };
    }

    private static class ReplaceAllQuickFix extends RegExpNaziQuickFix {
        private final String replacementText;
        private final List<RegExpElement> elements;

        public ReplaceAllQuickFix(String replacementText, List<RegExpElement> elements) {
            this.replacementText = replacementText;
            this.elements = elements;
        }

        @NotNull
        public String getName() {
            return "Replace with " + replacementText;
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            PsiFile f = factory.createFileFromText("dummy.regexp", RegExpFileType.INSTANCE, replacementText);
            RegExpPattern replacement = PsiTreeUtil.getChildOfType(f, RegExpPattern.class);
            assert replacement != null;
            RegExpElement first = elements.remove(0);
            first.replace(replacement);
            for(RegExpElement element : elements)
                element.delete();
        }
    }

    private List<ProblemDescriptor> createProblemDescriptor(InspectionManager manager, boolean onTheFly, List<RegExpElement> elements,
                                                      @Nullable String replacementText) {
        LocalQuickFix[] fixes = {new ReplaceAllQuickFix(replacementText, elements)};
        int size = elements.size();
        List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>(size);
        for(RegExpElement element : elements) {
            result.add(manager.createProblemDescriptor(element, "Better replace with predefined class", onTheFly,
                    fixes, ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
        }
        return result;

    }
}
