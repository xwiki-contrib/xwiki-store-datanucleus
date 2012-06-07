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

import org.junit.Assert;
import org.junit.Test;

public class JavaIdentifierEscaperTest
{
    @Test
    public void testEscape()
    {
        Assert.assertEquals("testX20space", JavaIdentifierEscaper.escape("test space"));
        Assert.assertEquals("testX5820xcode", JavaIdentifierEscaper.escape("testX20xcode"));
        Assert.assertEquals("testX2codeNotQuite", JavaIdentifierEscaper.escape("testX2codeNotQuite"));
        Assert.assertEquals("testX582CisEscaped", JavaIdentifierEscaper.escape("testX2CisEscaped"));
        Assert.assertEquals("null_", JavaIdentifierEscaper.escape("null"));
        Assert.assertEquals("notReserved__", JavaIdentifierEscaper.escape("notReserved_"));
    }

    @Test
    public void testUnescape()
    {
        Assert.assertEquals("test space", JavaIdentifierEscaper.unescape("testX20space_"));
        Assert.assertEquals("testX20xcode", JavaIdentifierEscaper.unescape("testX5820xcode"));
        Assert.assertEquals("testX2codeNotQuite", JavaIdentifierEscaper.unescape("testX2codeNotQuite"));
        Assert.assertEquals("testX2CisEscaped", JavaIdentifierEscaper.unescape("testX582CisEscaped"));
        Assert.assertEquals("null", JavaIdentifierEscaper.unescape("null_"));
        Assert.assertEquals("notReserved_", JavaIdentifierEscaper.unescape("notReserved__"));
    }
}
