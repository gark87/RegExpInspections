package org.gark87.idea.regexp.nazi.psi;

import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpCharRange;

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
}

