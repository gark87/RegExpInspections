package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElementVisitor;
import org.gark87.idea.regexp.nazi.fixes.ReplaceRegExpWith;
import org.gark87.idea.regexp.nazi.psi.RegExpClassAnalyzer;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpClass;
import org.intellij.lang.regexp.psi.RegExpRecursiveElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This inspection is about writing char ranges instead of using dot
 * For example, {@code [\w\W]} instead of {@code (?s:.)} or
 * {@code [^\r\n]} instead of {@code (?-s:.)}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class ManualDot extends RegExpNaziInspection {
    private static final String FIX_NAME = "Replace with dot(.)";
    private static final Set<Character> NEWLINES = new HashSet<Character>(Arrays.asList(new Character[]{'\r', '\n'}));
    private static final Set<Character> EMPTY = Collections.emptySet();
    private static final String WITH_DOT_ALL = "(?s:.)";
    private static final String WITHOUT_DOT_ALL = "(?-s:.)";


    @Override
    protected PsiElementVisitor createVisitor(final InspectionManager manager, final boolean isOnTheFly, final List<ProblemDescriptor> result) {
        return new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpClass(RegExpClass expClass) {
                RegExpClassAnalyzer analyzer = new RegExpClassAnalyzer(expClass, true);
                List<RegExpClassAnalyzer.CharRange> characterRanges = analyzer.getCharacterRanges();
                Map<Character, List<RegExpChar>> singleChars = analyzer.getSingleChars();
                // test for (?s:.)
                if (!analyzer.isNegated()) {
                    if (findFirstMissingChar(characterRanges, singleChars, EMPTY) == Character.MAX_VALUE) {
                        result.add(createProblemDescriptor(manager, isOnTheFly, expClass, WITH_DOT_ALL));
                        return;
                    }
                }
                // test for (?-s:.)
                if (analyzer.isNegated()) {
                    if (characterRanges.isEmpty() && NEWLINES.equals(singleChars.keySet()))
                        result.add(createProblemDescriptor(manager, isOnTheFly, expClass, WITHOUT_DOT_ALL));
                    return;
                }
                if (findFirstMissingChar(characterRanges, singleChars, NEWLINES) == Character.MAX_VALUE)
                    result.add(createProblemDescriptor(manager, isOnTheFly, expClass, WITHOUT_DOT_ALL));
            }

            private char findFirstMissingChar(List<RegExpClassAnalyzer.CharRange> characterRanges, 
                                              Map<Character, List<RegExpChar>> singleChars, Set<Character> skipChars) 
            {
                char ch = Character.MIN_VALUE;
                while (true) {
                    char newChar = ch;
                    for (RegExpClassAnalyzer.CharRange range : characterRanges) {
                        if (range.containsChar(ch)) {
                            Character to = range.getTo();
                            if (to != Character.MAX_VALUE)
                                to++;
                            newChar = (char) Math.max(newChar, to);
                        }
                    }
                    boolean shouldSkip = skipChars.contains(ch);
                    if (newChar != ch) {
                        if (shouldSkip)
                            return ch;
                        ch = newChar;
                        continue;
                    }
                    if (singleChars.containsKey(ch)) {
                        if (shouldSkip)
                            return ch;
                        ch++;
                        continue;
                    }
                    if (shouldSkip) {
                        ch++;
                        continue;
                    }
                    break;
                }
                return ch;
            }
        };
    }

    private ProblemDescriptor createProblemDescriptor(InspectionManager manager, boolean onTheFly,
                                                      RegExpClass regExpClass, @Nullable String replacementText) {
        LocalQuickFix[] fixes = {new ReplaceRegExpWith(FIX_NAME, replacementText)};
        return manager.createProblemDescriptor(regExpClass, "Better replace with dot", onTheFly, fixes,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Manual any symbol";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ManualDot";
    }
}
