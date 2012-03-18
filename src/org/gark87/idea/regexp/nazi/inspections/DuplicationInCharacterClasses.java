package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import org.gark87.idea.regexp.nazi.fixes.RegExpNaziQuickFix;
import org.gark87.idea.regexp.nazi.psi.RegExpClassAnalyzer;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This inspection is about <code>[\\s\r\n]</code> or <code>[a\\da]</code>.
 * Char is included in character class that already contain it.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class DuplicationInCharacterClasses extends RegExpNaziInspection {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Duplication of characters inside character classes";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "DuplicationInsideClasses";
    }

    protected PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly,
                                              final List<ProblemDescriptor> result)
    {
        return new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpClass(RegExpClass expClass) {
                RegExpClassAnalyzer analyzer = new RegExpClassAnalyzer(expClass, true);
                Map<Character, List<RegExpChar>> singleChars = analyzer.getSingleChars();
                for (List<RegExpChar> chars : singleChars.values()) {
                    Iterator<RegExpChar> iterator = chars.iterator();
                    if (iterator.hasNext())
                        iterator.next();
                    while (iterator.hasNext())
                        result.add(generateProblemDescriptor(iterator.next(), manager, isOnTheFly));
                }
                for (Character ch : singleChars.keySet()) {
                    for (RegExpClassAnalyzer.CharRange set : analyzer.getCharacterRanges()) {
                        if (!set.containsChar(ch))
                            continue;
                        for (RegExpChar regExpChar : singleChars.get(ch))
                            result.add(generateProblemDescriptor(regExpChar, manager, isOnTheFly));
                    }
                }
            }
        };
    }

    private ProblemDescriptor generateProblemDescriptor(RegExpChar ch, InspectionManager manager, boolean isOnTheFly) {
        Character value = ch.getValue();
        String msg = "Character class already contains char '" + value + "'";
        return manager.createProblemDescriptor(ch, msg, isOnTheFly, new LocalQuickFix[]{new DeleteCharFix(ch)},
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    private class DeleteCharFix extends RegExpNaziQuickFix {
        private final RegExpChar ch;

        public DeleteCharFix(RegExpChar ch) {
            this.ch = ch;

        }

        @NotNull
        public String getName() {
            return "Remove duplicate char " + ch.getText();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            ch.delete();
        }
    }
}
