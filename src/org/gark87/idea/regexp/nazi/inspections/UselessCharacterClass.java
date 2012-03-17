package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiFile;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.gark87.idea.regexp.nazi.fixes.ReplaceRegExpWith;
import org.gark87.idea.regexp.nazi.psi.RegExpClassAnalyzer;
import org.gark87.idea.regexp.nazi.psi.RegExpUtil;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This inspection is about <code>[\\s]</code> or <code>[;]</code>.
 * It these cases we do not need character class.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class UselessCharacterClass extends RegExpNaziInspection {
    private static final String FIX_NAME = "Remove character class brackets";

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
                if (analyzer.isNegated()) {
                    if (!analyzer.getSingleChars().isEmpty())
                        return;
                    Set<RegExpSimpleClass> simpleClasses = analyzer.getSimpleClasses();
                    if (simpleClasses.size() != 1)
                        return;
                    RegExpSimpleClass simpleClass = simpleClasses.iterator().next();
                    String replacement = RegExpUtil.invertSimpleCharClass(simpleClass.getKind());
                    result.add(createProblemDescriptor(manager, isOnTheFly, expClass, replacement));
                }
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

    private ProblemDescriptor createProblemDescriptor(InspectionManager manager, boolean onTheFly, RegExpClass expClass,
                                                      @Nullable String replacementText)
    {
        LocalQuickFix[] fixes = {new ReplaceRegExpWith(FIX_NAME, replacementText)};
        return manager.createProblemDescriptor(expClass, "Useless character class brackets", onTheFly, fixes,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

}
