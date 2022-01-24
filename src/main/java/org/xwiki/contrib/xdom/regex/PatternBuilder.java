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
package org.xwiki.contrib.xdom.regex;

import org.xwiki.contrib.xdom.regex.internal.BlockPattern;
import org.xwiki.contrib.xdom.regex.internal.SpaceBlockPattern;
import org.xwiki.contrib.xdom.regex.internal.SpecialSymbolBlockPattern;
import org.xwiki.contrib.xdom.regex.internal.WordBlockPattern;

/**
 * Builder for {@link Pattern}.
 *
 * @version $Id$
 * @since 1.0
 */
public class PatternBuilder
{
    private int position;

    private boolean isEscaping;

    private boolean willEscape;

    private BlockPattern<?> currentBlock;

    private Pattern expression;

    private String pattern;

    /**
     * Build a new pattern.
     *
     * @param pattern the string pattern to use
     * @return the new Pattern created form the string pattern
     */
    public Pattern build(String pattern)
    {
        this.pattern = pattern;
        this.expression = new Pattern();

        buildInternal();

        return expression;
    }

    private void buildInternal()
    {
        willEscape = false;

        char currentChar = pattern.charAt(position);
        if (currentBlock == null) {
            handleNoCurrentBlock(currentChar);
        } else if (currentBlock instanceof WordBlockPattern) {
            handleWordPatternBlock(currentChar);
        }

        isEscaping = willEscape;

        if (++position < pattern.length()) {
            buildInternal();
        } else if (currentBlock instanceof WordBlockPattern) {
            ((WordBlockPattern) currentBlock).buildPattern();
        }
    }

    private void handleNoCurrentBlock(char currentChar)
    {
        if (isEscaping) {
            if (currentChar == ' ') {
                expression.addPatternBlock(new SpaceBlockPattern());
            } else {
                expression.addPatternBlock(new SpecialSymbolBlockPattern(currentChar));
            }
        } else {
            switch (currentChar) {
                case '^':
                    currentBlock = new WordBlockPattern();
                    ((WordBlockPattern) currentBlock).addChar(currentChar);
                    expression.addPatternBlock(currentBlock);
                    break;
                case '\\':
                    willEscape = true;
                    break;
                case '?':
                    expression.addPatternBlock(new SpecialSymbolBlockPattern());
                    break;
                case ' ':
                    expression.addPatternBlock(new SpaceBlockPattern());
                    break;
                default:
                    expression.addPatternBlock(new SpecialSymbolBlockPattern(currentChar));
                    break;
            }
        }
    }

    private void handleWordPatternBlock(char currentChar)
    {
        WordBlockPattern currentWordBlock = (WordBlockPattern) currentBlock;
        if (isEscaping) {
            currentWordBlock.addChar(currentChar);
        } else {
            switch (currentChar) {
                case '\\':
                    willEscape = true;
                    break;
                case '$':
                    currentWordBlock.addChar(currentChar);
                    currentWordBlock.buildPattern();
                    currentBlock = null;
                    break;
                default:
                    currentWordBlock.addChar(currentChar);
                    break;
            }
        }
    }
}
