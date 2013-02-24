/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mymam.server.test;

import junit.framework.TestCase;
import net.mymam.controller.TimeFrameConverter;
import org.junit.Test;

/**
 * @author fstab
 */
public class TimeFrameConverterUnitTest extends TestCase {

    public void execTest(String input) {
        TimeFrameConverter conv = new TimeFrameConverter();
        Object o = conv.getAsObject(null, null, input);
        String s = conv.getAsString(null, null, o);
        assertEquals(s, input);
    }

    public void execTest(Long input) {
        TimeFrameConverter conv = new TimeFrameConverter();
        String s = conv.getAsString(null, null, input);
        System.out.println("s: " + s);
        Object o = conv.getAsObject(null, null, s);
        assertEquals(o, input);
    }

    @Test
    public void testWithStringInput1() {
        execTest("101:03:20.123");
    }

    @Test
    public void testWithStringInput2() {
        execTest("00:00:00.000");
    }

    @Test
    public void testWithLongInput1() {
        execTest(0L);
    }

    @Test
    public void testWithLongInput2() {
        execTest(2L * 61L * 61L * 61L * 1001L);
    }

}
