package org.gark87.idea.regexp.nazi.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.*;

import java.util.*;

/**
 * Because i cannot find proper API in {@link RegExpClass}, all logic about accumulating single characters and
 * char ranges are accumulating here.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpClassAnalyzer {
    private final Map<Character, List<RegExpChar>> singleChars = new HashMap<Character, List<RegExpChar>>();
    private final List<CharRange> characterSets = new ArrayList<CharRange>();
    private final Set<RegExpSimpleClass> simpleClasses = new HashSet<RegExpSimpleClass>();
    private final boolean negated;

    public RegExpClassAnalyzer(RegExpClass expClass, final boolean treatClassesAsRange) {
        PsiElement firstChild = expClass.getFirstChild();
        if (firstChild != null) {
            PsiElement secondChild = firstChild.getNextSibling();
            negated = secondChild == null || secondChild.getNode().getElementType() == RegExpTT.CARET;
        } else {
            negated = false;
        }
        RegExpClassElement[] elements = PsiTreeUtil.getChildrenOfType(expClass, RegExpClassElement.class);
        if (elements == null)
            return;
        for (RegExpClassElement element : elements) {
            element.accept(new RegExpElementVisitor() {
                @Override
                public void visitRegExpChar(RegExpChar ch) {
                    super.visitRegExpChar(ch);
                    Character value = ch.getValue();
                    List<RegExpChar> regExpChars = singleChars.get(value);
                    if (regExpChars == null)
                        singleChars.put(value, regExpChars = new ArrayList<RegExpChar>(3));
                    regExpChars.add(ch);
                }

                @Override
                public void visitRegExpCharRange(RegExpCharRange range) {
                    characterSets.add(new CharRange(range));
                }

                @Override
                public void visitSimpleClass(RegExpSimpleClass simpleClass) {
                    if (treatClassesAsRange)
                        characterSets.addAll(Arrays.asList(kind2range[simpleClass.getKind().ordinal()]));
                    else
                        simpleClasses.add(simpleClass);
                }
            });
        }
    }

    public Map<Character, List<RegExpChar>> getSingleChars() {
        return singleChars;
    }

    public List<CharRange> getCharacterRanges() {
        return characterSets;
    }

    public boolean isNegated() {
        return negated;
    }

    public Set<RegExpSimpleClass> getSimpleClasses() {
        return simpleClasses;
    }

    public static class CharRange {
        private final Character from;
        private final Character to;
        private final RegExpCharRange psiElement;

        private CharRange(RegExpCharRange charRange) {
            psiElement = charRange;
            RegExpCharRange.Endpoint fromEndPoint = charRange.getFrom();
            from = RegExpUtil.convertEndPointToChar(fromEndPoint);
            to = RegExpUtil.convertEndPointToChar(charRange.getTo());
        }

        private CharRange(char from, char to) {
            this.from = from;
            this.to = to;
            psiElement = null;
        }

        private CharRange(char c) {
            this.from = this.to = c;
            psiElement = null;
        }

        public Character getFrom() {
            return from;
        }

        public Character getTo() {
            return to;
        }

        public boolean containsChar(Character ch) {
            return from <= ch && ch <= to;
        }

        public RegExpCharRange getPsiElement() {
            return psiElement;
        }
    }

    private static final CharRange[][] kind2range;

    static {
        RegExpSimpleClass.Kind[] values = RegExpSimpleClass.Kind.values();
        kind2range = new CharRange[values.length][];
        for (RegExpSimpleClass.Kind kind : values) {
            kind2range[kind.ordinal()] = generateRanges(kind);
        }
    }

    private static char prev(char c) {
        return --c;
    }

    private static char next(char c) {
        return ++c;
    }

    private static CharRange[] generateRanges(RegExpSimpleClass.Kind kind) {
        char start = Character.MIN_VALUE;
        char end = Character.MAX_VALUE;
        switch (kind) {
            case ANY:
                return new CharRange[]{new CharRange(start, Character.MAX_VALUE)};
            case DIGIT:
                return new CharRange[]{new CharRange('0', '9')};
            case NON_DIGIT:
                return new CharRange[]{new CharRange(start, prev('0')), new CharRange(next('9'), end)};
            case SPACE:
                return new CharRange[]{new CharRange(' '), new CharRange('\t', '\r')};
            case NON_SPACE:
                return new CharRange[] {
                        new CharRange(start, prev('\t')), new CharRange(next('\r'), prev(' ')),
                        new CharRange(next(' '), end)
                };
            case WORD:
                return new CharRange[]{
                        new CharRange('a', 'z'), new CharRange('A', 'Z'), new CharRange('_'), new CharRange('0', '9')
                };
            case NON_WORD:
                return new CharRange[]{
                        new CharRange(start, prev('0')), new CharRange(next('9'), prev('A')),
                        new CharRange(next('Z'), prev('_')), new CharRange(next('_'), prev('a')),
                        new CharRange(next('z'), end)
                };
            default:
                throw new IllegalStateException("Unknown kind: " + kind);
        }
    }
}
