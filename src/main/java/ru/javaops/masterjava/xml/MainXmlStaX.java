package ru.javaops.masterjava.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

/**
 * @author dkorolev
 * Date: 7/9/2019
 * Time: 1:17 PM
 */
public class MainXmlStaX {

    public void extractParticipantsEmailAndNameListFromProject(String projectName, String fileName) {
        String elementValue;
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource(fileName).openStream())) {
            //get groupId
            elementValue = processor.getElementValueForAttribute(projectName);
            System.out.println(elementValue);

            try (StaxStreamProcessor processor2 =
                         new StaxStreamProcessor(Resources.getResource(fileName).openStream())) {
                //get usernames
                XMLStreamReader reader = processor2.getReader();
                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLEvent.START_ELEMENT) {
                        int attributeCount = reader.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if (reader.getAttributeValue(i).contains(elementValue)) {
                                do {
                                    event = reader.next();
                                    if (event == XMLEvent.START_ELEMENT) {
                                        System.out.println(reader.getElementText());
                                    }
                                } while(event != XMLEvent.START_ELEMENT);
                            }
                        }
                    }
                }


            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}