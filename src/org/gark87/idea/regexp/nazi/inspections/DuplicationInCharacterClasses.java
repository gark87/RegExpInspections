package org.gark87.idea.regexp.nazi.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.gark87.idea.regexp.nazi.RegExpNaziToolProvider;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: galyash
 * Date: 04.03.12
 * Time: 2:33
 * To change this template use File | Settings | File Templates.
 */
public class DuplicationInCharacterClasses extends LocalInspectionTool {
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
        return "Duplication of characters inside character classes";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "DuplicationInsideClasses";
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        if (file.getClass() != RegExpFile.class)
            return ProblemDescriptor.EMPTY_ARRAY;
        final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
        file.acceptChildren(new RegExpRecursiveElementVisitor() {

            @Override
            public void visitRegExpClass(RegExpClass expClass) {
                final Map<Character, RegExpChar> singleChars = new HashMap<Character, RegExpChar>();
                final Set<CharacterSet> characterSets = new HashSet<CharacterSet>();
                RegExpClassElement[] elements = PsiTreeUtil.getChildrenOfType(expClass, RegExpClassElement.class);

                if (elements == null)
                    return;
                for (RegExpClassElement element : elements) {
                    element.accept(new RegExpElementVisitor() {
                        @Override
                        public void visitRegExpChar(RegExpChar ch) {
                            super.visitRegExpChar(ch);
                            Character value = ch.getValue();
                            if (singleChars.get(value) != null) {
                                ProblemDescriptor problemDescriptor = generateProblemDescriptor(ch, manager, isOnTheFly);
                                result.add(problemDescriptor);
                            } else {
                                singleChars.put(ch.getValue(), ch);
                            }
                        }

                        @Override
                        public void visitRegExpCharRange(RegExpCharRange range) {
                            characterSets.add(new CharRange(range));
                        }

                        @Override
                        public void visitSimpleClass(RegExpSimpleClass simpleClass) {
                            characterSets.add(kind2range[simpleClass.getKind().ordinal()]);
                        }
                    });
                }
                for (Character ch : singleChars.keySet()) {
                    for (CharacterSet set : characterSets) {
                        if (set.containsChar(ch)) {
                            result.add(generateProblemDescriptor(singleChars.get(ch), manager, isOnTheFly));
                        }
                    }
                }
            }
        });
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private ProblemDescriptor generateProblemDescriptor(RegExpChar ch, InspectionManager manager, boolean isOnTheFly) {
        Character value = ch.getValue();
        String msg = "Character class already contains char '" + value + "'";
        return manager.createProblemDescriptor(ch, msg, isOnTheFly, new LocalQuickFix[]{new DeleteCharFix(ch)},
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    private interface CharacterSet {
        boolean containsChar(char ch);
    }

    private static class CharRange implements CharacterSet {
        private Character from;
        private Character to;

        private CharRange(RegExpCharRange charRange) {
            RegExpCharRange.Endpoint fromEndPoint = charRange.getFrom();
            from = convertEndPointToChar(fromEndPoint);
            to = convertEndPointToChar(charRange.getTo());
        }

        private CharRange(char from, char to) {
            this.from = from;
            this.to = to;
        }

        private CharRange(char c) {
            this.from = this.to = c;
        }

        private Character convertEndPointToChar(RegExpCharRange.Endpoint endPoint) {
            if (endPoint instanceof RegExpChar) {
                return ((RegExpChar) endPoint).getValue();
            }
            return null;
        }

        public boolean containsChar(char ch) {
            return !(from == null || to == null) && ch >= from && ch <= to;
        }
    }

    private static final SimpleClassRange[] kind2range;

    static {
        RegExpSimpleClass.Kind[] values = RegExpSimpleClass.Kind.values();
        kind2range = new SimpleClassRange[values.length];
        for (RegExpSimpleClass.Kind kind : values) {
            kind2range[kind.ordinal()] = new SimpleClassRange(generateRanges(kind), negation(kind));
        }
    }

    private static boolean negation(RegExpSimpleClass.Kind kind) {
        switch (kind) {
            case NON_DIGIT:
            case NON_SPACE:
            case NON_WORD:
                return true;
            default:
                return false;
        }
    }

    private static CharRange[] generateRanges(RegExpSimpleClass.Kind kind) {
        switch (kind) {
            case ANY:
                return new CharRange[]{new CharRange((char) 0, Character.MAX_VALUE)};
            case DIGIT:
            case NON_DIGIT:
                return new CharRange[]{new CharRange('0', '9')};
            case SPACE:
            case NON_SPACE:
                return new CharRange[]{new CharRange(' '), new CharRange('\t'), new CharRange('\n'), new CharRange('\f'), new CharRange('\r'), new CharRange('\u000B')};
            case WORD:
            case NON_WORD:
                return new CharRange[]{new CharRange('a', 'z'), new CharRange('A', 'Z'), new CharRange('_'), new CharRange('0', '9')};
            default:
                throw new IllegalStateException("Unknown kind: " + kind);
        }
    }

    private static class SimpleClassRange implements CharacterSet {
        private CharRange[] ranges;
        private boolean negation;

        public SimpleClassRange(CharRange[] ranges, boolean negotion) {
            this.ranges = ranges;
            this.negation = negotion;
        }

        public boolean containsChar(char ch) {
            for (CharRange range : ranges) {
                boolean contains = range.containsChar(ch);
                if (contains && !negation)
                    return true;
                if (!contains && negation)
                    return true;
            }
            return false;
        }
    }

    private class DeleteCharFix implements LocalQuickFix {
        private final RegExpChar ch;

        public DeleteCharFix(RegExpChar ch) {
            this.ch = ch;

        }

        @NotNull
        public String getName() {
            return "Remove duplicate char " + ch.getText();
        }

        @NotNull
        public String getFamilyName() {
            return "Remove duplication";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            ch.delete();
        }
    }
}
