package ru.javaops.masterjava.xml;

import org.junit.Test;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import src.main.resources.ObjectFactory;

/**
 * @author dkorolev
 * Date: 7/9/2019
 * Time: 12:32 PM
 */
public class MainXmlTest {

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    private MainXml mainXml = new MainXml();


    @Test
    public void extractParticipantsListFromProject() {
         mainXml.extractParticipantsListFromProject("masterjava","payload.xml",JAXB_PARSER);
    }
}