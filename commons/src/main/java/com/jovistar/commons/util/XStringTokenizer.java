/*
 * Copyright (C) Alerteyes, 2007
 *
 * Author grants you a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source
 * and binary code form, provided that this copyright notice and
 * license appear on all copies of the derived software source code.
 *
 * This software is provided "AS IS", without a warranty of any kind.
 * Use at your own risk. Be advised that running network server
 * software like this can severely damage the security of your system.
 *
 */
package com.jovistar.commons.util;

import java.util.Enumeration;

public class XStringTokenizer implements Enumeration {

    /**
     * The position in the str, where we currently are.
     */
    private int pos;
    /**
     * The string that should be split into tokens.
     */
    private final String str;
    /**
     * The length of the string.
     */
    private final int len;
    /**
     * The string containing the delimiter characters.
     */
    private String delim;
    /**
     * Tells, if we should return the delimiters.
     */
    private final boolean retDelims;

    public XStringTokenizer(String str, String delim, boolean returnDelims) {
        len = str.length();
        this.str = str;
        // The toString() hack causes the NullPointerException.
        this.delim = delim;
        this.retDelims = returnDelims;
        this.pos = 0;
    }

    public XStringTokenizer(String tok, String str) {
        this(tok, str, false);
    }

    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    public Object nextElement() {
        return nextToken();
    }

    boolean hasMoreTokens() {
        if (!retDelims) {
            while (pos < len && delim.indexOf(str.charAt(pos)) >= 0) {
                pos++;
            }
        }
        return pos < len;
    }

    String nextToken() {
        if (pos < len && delim.indexOf(str.charAt(pos)) >= 0) {
            if (retDelims) {
                return str.substring(pos, ++pos);
            }
            while (++pos < len && delim.indexOf(str.charAt(pos)) >= 0);
        }
        if (pos < len) {
            int start = pos;
            while (++pos < len && delim.indexOf(str.charAt(pos)) < 0);

            return str.substring(start, pos);
        }
        throw new XNoSuchElementException();
    }

    public int countTokens() {
        int count = 0;
        int delimiterCount = 0;
        boolean tokenFound = false; // Set when a non-delimiter is found
        int tmpPos = pos;

        // Note for efficiency, we count up the delimiters rather than check
        // retDelims every time we encounter one.  That way, we can
        // just do the conditional once at the end of the method
        while (tmpPos < len) {
            if (delim.indexOf(str.charAt(tmpPos++)) >= 0) {
                if (tokenFound) {
                    // 	Got to the end of a token
                    count++;
                    tokenFound = false;
                }
                delimiterCount++; // Increment for this delimiter
            } else {
                tokenFound = true;
                // Get to the end of the token
                while (tmpPos < len && delim.indexOf(str.charAt(tmpPos)) < 0) {
                    ++tmpPos;
                }
            }
        }

        // Make sure to count the last token
        if (tokenFound) {
            count++;        // if counting delmiters add them into the token count
        }
        return retDelims ? count + delimiterCount : count;
    }
}
