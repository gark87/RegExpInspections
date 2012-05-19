package org.gark87.idea.regexp.nazi.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpCharRange;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpSimpleClass;

import java.util.regex.Pattern;

/**
 * This class contains IntelliJ util methods.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpUtil {
    private static final Pattern ESCAPED = Pattern.compile("\\\\{1,2}[trn]");

    public static Character convertEndPointToChar(RegExpCharRange.Endpoint endPoint) {
        if (endPoint instanceof RegExpChar)
            return ((RegExpChar) endPoint).getValue();
        return null;
    }

    public static String invertSimpleCharClass(RegExpSimpleClass.Kind kind) {
        switch (kind) {
            case DIGIT:
                return "\\D";
            case NON_DIGIT:
                return "\\d";
            case SPACE:
                return "\\S";
            case NON_SPACE:
                return "\\s";
            case WORD:
                return "\\W";
            case NON_WORD:
                return "\\w";
            default:
                throw new IllegalStateException("Unexpected kind:" + kind);
        }
    }
    
    public static boolean isEscaped(RegExpChar ch) {
        String text = ch.getText();
        if (!text.startsWith("\\"))
            return false;
        return !ESCAPED.matcher(text).matches();

    }

    public static boolean isLookAround(RegExpGroup group) {
        ASTNode node = group.getNode();
        ASTNode firstChildNode = node.getFirstChildNode();
        if (firstChildNode == null)
            return false;
        IElementType elementType = firstChildNode.getElementType();
        return (elementType == RegExpTT.POS_LOOKAHEAD || elementType == RegExpTT.NEG_LOOKAHEAD ||
                elementType == RegExpTT.POS_LOOKBEHIND || elementType == RegExpTT.NEG_LOOKBEHIND);
    }
}

