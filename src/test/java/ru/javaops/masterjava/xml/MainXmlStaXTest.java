package ru.javaops.masterjava.xml;

import org.junit.Test;

/**
 * @author dkorolev
 * Date: 7/9/2019
 * Time: 4:08 PM
 */
public class MainXmlStaXTest {


    @Test
    public void extractParticipantsEmailAndNameListFromProject() {
        new MainXmlStaX().extractParticipantsEmailAndNameListFromProject("masterjava","payload.xml");
    }
}