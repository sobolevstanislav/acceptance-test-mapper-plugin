package net.thucydides.maven.plugin.xml.unmarshaller;


import net.thucydides.maven.plugin.xml.steps.AcceptanceSteps;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class XMLUnmarshaller {

    public static AcceptanceSteps unmarshal(AcceptanceSteps stepPairsFromXML) {
        try {
            File file = new File("resources/steps/acceptance-steps.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(AcceptanceSteps.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            stepPairsFromXML = (AcceptanceSteps) jaxbUnmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return stepPairsFromXML;
    }
}
