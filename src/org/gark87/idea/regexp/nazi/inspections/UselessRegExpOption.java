package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.gark87.idea.regexp.nazi.psi.RegExpClassAnalyzer;
import org.gark87.idea.regexp.nazi.psi.RegExpRecursiveFinder;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.psi.*;
import org.intellij.lang.regexp.psi.impl.RegExpOptionsImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This inspection is about <code>(?i:.\\s\\w*)</code>.
 * When option is used, but has no sense.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class UselessRegExpOption extends LocalInspectionTool {

    private enum Flag {
        i {
            @Override
            public boolean hasProblem(RegExpOptionsImpl options) {
                RegExpRecursiveFinder finder = new RegExpRecursiveFinder() {
                    @Override
                    public void visitRegExpChar(RegExpChar ch) {
                        if (isFound())
                            return;
                        Character value = ch.getValue();
                        if (value == null)
                            return;
                        if ('a' <= value && value <= 'z')
                            found();
                        if ('A' <= value && value <= 'Z')
                            found();
                    }

                    @Override
                    public void visitRegExpClass(RegExpClass expClass) {
                        if (isFound())
                            return;
                        RegExpClassAnalyzer analyzer = new RegExpClassAnalyzer(expClass, false);
                        boolean[] lowerFound = new boolean[26];
                        boolean[] upperFound = new boolean[26];
                        for (Character c : analyzer.getSingleChars().keySet()) {
                            if (c == null)
                                continue;
                            if ('a' <= c && c <= 'z')
                                lowerFound[c - 'a'] = true;
                            if ('A' <= c && c <= 'Z')
                                upperFound[c - 'A'] = true;
                        }
                        for (RegExpClassAnalyzer.CharRange range : analyzer.getCharacterRanges()) {
                            Character from = range.getFrom();
                            Character to = range.getTo();
                            if (from == null || to == null)
                                continue;
                            for (char c = (char) Math.max(from, 'a'); c <= (char) Math.min(to, 'z'); c++)
                                lowerFound[c - 'a'] = true;
                            for (char c = (char) Math.max(from, 'A'); c <= (char) Math.min(to, 'Z'); c++)
                                upperFound[c - 'A'] = true;
                        }
                        for (int i = 0; i < lowerFound.length; i++) {
                            if (upperFound[i] != lowerFound[i]) {
                                found();
                                return;
                            }
                        }
                    }
                };
                options.getParent().getParent().acceptChildren(finder);
                return !finder.isFound();
            }
        },;

        public abstract boolean hasProblem(RegExpOptionsImpl options);

        public char getChar() {
            return name().charAt(0);
        }
    }

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
        return "Useless regexp option";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UselessRegexpOption";
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        if (file.getClass() != RegExpFile.class)
            return ProblemDescriptor.EMPTY_ARRAY;
        final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
        file.acceptChildren(new RegExpRecursiveElementVisitor() {
            @Override
            public void visitRegExpOptions(RegExpOptionsImpl options) {
                super.visitRegExpOptions(options);
                Flag[] flags = Flag.values();
                int length = flags.length;
                for (int i = 0; i < length; i++) {
                    Flag flag = flags[i];
                    char option = flag.getChar();
                    if (options.isSet(option)) {
                        if (flag.hasProblem(options))
                            addProblemDescriptors(result, manager, isOnTheFly, options, flag);
                    }
                }
            }
        });
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private void addProblemDescriptors(List<ProblemDescriptor> result, InspectionManager manager, boolean onTheFly,
                                       RegExpOptionsImpl options, Flag flag) {
        ASTNode node = options.getNode();
        int start = 0;
        char ch = flag.getChar();
        String text = node.getText();
        do {
            int newStart = text.indexOf(ch, start);
            if (newStart < 0)
                break;
            result.add(manager.createProblemDescriptor(options, new TextRange(newStart, newStart),
                    "Useless option " + ch, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, onTheFly, new RemoveUselessRegExpOption(flag)));
            start = newStart + 1;
        } while (true);
    }

    private class RemoveUselessRegExpOption implements LocalQuickFix {
        private final Flag flag;

        public RemoveUselessRegExpOption(Flag flag) {
            this.flag = flag;
        }

        @NotNull
        public String getName() {
            return "Remove useless RegExp option";
        }

        @NotNull
        public String getFamilyName() {
            return "UselessRegExpOption";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            RegExpOptionsImpl options = (RegExpOptionsImpl) problemDescriptor.getPsiElement();
            ASTNode node = options.getNode();
            String target = String.valueOf(flag.getChar());
            for (ASTNode child : node.getChildren(null)) {
                String replacement = child.getText().replace(target, "");
                node.addLeaf(child.getElementType(), replacement, child);
                node.removeChild(child);
            }
        }
    }
}

