package org.exoplatform.services.document.tika;

import org.exoplatform.services.document.AdvancedDocumentReader;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.test.BaseStandaloneTest;

import java.io.InputStream;
import java.io.Reader;

/**
 * Created by The eXo Platform SAS.
 *
 * <br>Date:
 *
 * @author <a href="aboughzela@exoplatform.com">Aymen Boughzela</a>
 * @version $Id: TestMSVisioOnTikaDocumentReader.java
 */
public class TestMSVisioOnTikaDocumentReader  extends BaseStandaloneTest
{
    DocumentReaderService service;

    public void setUp() throws Exception
    {
        super.setUp();
        service = (DocumentReaderService)getComponentInstanceOfType(DocumentReaderService.class);
    }

    public void testGetContentAsStringTemplate() throws Exception
    {
        InputStream is = BaseStandaloneTest.class.getResourceAsStream("/test.vsd");
        String text = service.getDocumentReader("application/vnd.visio").getContentAsText(is);
        String expected = "My first test with visio";
        assertEquals("Wrong string returned", normalizeWhitespaces(expected), normalizeWhitespaces(text));
    }

    public void testGetContentAsReader() throws Exception
    {
        InputStream is = TestPPTOnTikaDocumentReader.class.getResourceAsStream("/test.vsd");
        try
        {
            Reader reader =
                    ((AdvancedDocumentReader)service.getDocumentReader("application/vnd.visio")).getContentAsReader(is);

            //read text
            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = reader.read()) != -1)
            {
                char ch = (char)c;
                buf.append(ch);
            }

            String text = buf.toString();
            String expected = "My first test with visio";

            assertEquals("Wrong string returned", normalizeWhitespaces(expected), normalizeWhitespaces(text));
        }
        finally
        {
            is.close();
        }
    }

}
