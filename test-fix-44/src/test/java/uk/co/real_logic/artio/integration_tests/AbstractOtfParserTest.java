/*
 * Copyright 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.artio.integration_tests;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import uk.co.real_logic.artio.dictionary.IntDictionary;
import uk.co.real_logic.artio.otf.OtfMessageAcceptor;
import uk.co.real_logic.artio.otf.OtfParser;
import uk.co.real_logic.artio.util.AsciiBuffer;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public abstract class AbstractOtfParserTest
{
    protected static final int SESSION_ID = 0;

    protected final MutableAsciiBuffer buffer = new MutableAsciiBuffer(new byte[16 * 1024]);
    protected final OtfMessageAcceptor acceptor = mock(OtfMessageAcceptor.class);
    protected final OtfParser parser = new OtfParser(acceptor, new IntDictionary());

    protected void verifyField(final InOrder inOrder, final int tag, final String expectedValue)
    {
        final ArgumentCaptor<Integer> offset = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Integer> length = ArgumentCaptor.forClass(Integer.class);
        once(inOrder).onField(eq(tag), anyBuffer(), offset.capture(), length.capture());

        final String value = buffer.getAscii(offset.getValue(), length.getValue());
        assertEquals(expectedValue, value);
    }

    protected void verifyField(final InOrder inOrder, final int tag)
    {
        once(inOrder).onField(eq(tag), anyBuffer(), anyInt(), anyInt());
    }

    protected void verifyNext(final InOrder inOrder)
    {
        once(inOrder).onNext();
    }

    protected void verifyComplete(final InOrder inOrder)
    {
        once(inOrder).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    protected OtfMessageAcceptor once(final InOrder inOrder)
    {
        return inOrder.verify(acceptor, times(1));
    }

    protected AsciiBuffer anyBuffer()
    {
        return any(AsciiBuffer.class);
    }

    protected void parseTestRequest(final int offset, final int length)
    {
        parser.onMessage(buffer, offset, length);

        final InOrder inOrder = inOrder(acceptor);
        verifyNext(inOrder);
        verifyField(inOrder, 35);
        verifyField(inOrder, 49);
        verifyField(inOrder, 56);
        verifyField(inOrder, 112);
        verifyComplete(inOrder);
    }
}
