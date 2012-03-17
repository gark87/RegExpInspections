package org.gark87.idea.regexp.nazi.psi;

import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpCharRange;
import org.intellij.lang.regexp.psi.RegExpSimpleClass;

/**
 * This class contains IntelliJ util methods.
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public class RegExpUtil {
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
}

