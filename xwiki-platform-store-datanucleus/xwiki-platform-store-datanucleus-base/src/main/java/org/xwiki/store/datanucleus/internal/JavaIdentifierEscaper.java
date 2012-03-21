/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.store.datanucleus.internal;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.UnsupportedEncodingException;

public class JavaIdentifierEscaper
{
    private static final String[] JAVA_RESERVED_WORDS = new String[] {
        "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package",
        "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements",
        "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof",
        "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface",
        "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native",
        "super", "while", "null", "true", "false"
    };

    /**
     * Escape a string so that it is a valid java class/memeber name.
     * XWiki supports names with characters which are not allowed in java fields so we must do escaping.
     * This escaping scheme is more strict than what Java allows, Java allows many non ASCII characters
     * to be used in identifiers but this escape function only outputs [a-zA-Z0-9_]*
     * the escaping scheme converts the String to a byte array then uses capital 'X' followed by 2
     * hexidecimal letters/numbers (also capital) to represent the code point for a byte which cannot
     * be represented normally. The letter 'X' in normal use will be escaped if and only if it is followed
     * by two characters which are decimal digit or capital letter between 'A' and 'F' inclusive.
     * If the first character is not a letter ([a-zA-Z]) then it must be escaped as well.
     *
     * @param toEscape a String which may contain unallowable characters.
     * @return a String which may be used for a Java class, method, or field.
     */
    public static String escape(final String toEscape)
    {
        // Pass #1, escape any occurences of X[0-9A-F][0-9A-F]
        // At this point it is not necessary to convert the String from UTF-16 because it will be serialized
        // as UTF-8 and in that format, X, 0-9 and A-F are never used to represent anything but themselves.
        final String pass1 = toEscape.replaceAll("X([0-9A-F][0-9A-F])", "X58$1");

        final byte[] pass1Bin;
        try {
            pass1Bin = pass1.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8, this java vm is not standards compliant!");
        }

        // Convert all of the non [a-zA-Z0-9_] chars.
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pass1Bin.length; i++) {
            // if ((pass1Bin[i] < '0' || pass1Bin[i] > '9')
            //     && (pass1Bin[i] < 'a' || pass1Bin[i] > 'z')
            //     && (pass1Bin[i] < 'A' || pass1Bin[i] > 'Z')
            //     && pass1Bin[i] != '_'
            if ((pass1Bin[i] < 0x30 || pass1Bin[i] > 0x39)
                && (pass1Bin[i] < 0x61 || pass1Bin[i] > 0x7a)
                && (pass1Bin[i] < 0x41 || pass1Bin[i] > 0x5a)
                && pass1Bin[i] != 0x5F)
            {
                escapeChar(pass1Bin[i], sb);
            } else {
                sb.append((char) pass1Bin[i]);
            }
        }
        final String pass2 = sb.toString();

        // The last bit of uglyness is caused by using java keywords or literals.
        for (final String reserved : JAVA_RESERVED_WORDS) {
            // In order to simplify decoding, we will escape the last letter even though it's valid.
            if (pass2.equals(reserved)) {
                // This works because all java reserved words require no escaping.
                sb.deleteCharAt(sb.length() - 1);
                escapeChar(pass1Bin[pass1Bin.length - 1], sb);
                return sb.toString();
            }
        }

        return pass2;
    }

    private static void escapeChar(final byte toEscape, final StringBuilder writeTo)
    {
        writeTo.append('X');
        final String hexString = Integer.toHexString(toEscape).toUpperCase();
        if (hexString.length() == 1) {
            writeTo.append('0');
        }
        writeTo.append(hexString);
    }

    public static String unescape(final String escaped)
    {
        final Pattern p = Pattern.compile("X([0-9A-F][0-9A-F])");
        final Matcher m = p.matcher(escaped);
        final StringBuffer out = new StringBuffer();

        try {
            while (m.find()) {
                m.appendReplacement(out, new String(new byte[] {Byte.parseByte(m.group(1), 16)}, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8, this java vm is not standards compliant!");
        }
        m.appendTail(out);

        return out.toString();
    }
}
