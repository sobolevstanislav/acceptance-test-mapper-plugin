package net.thucydides.maven.plugin.xml.unmarshaller;


import net.thucydides.maven.plugin.xml.steps.AcceptanceSteps;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class XMLUnmarshaller {

    public AcceptanceSteps unmarshal() {
        System.out.println("UNMARSHALLING STARTED");
        AcceptanceSteps acceptanceSteps = new AcceptanceSteps();
        try {
            XMLUnmarshaller.class.getResource("/acceptance-steps.xml");
            System.out.println("PATH: " + XMLUnmarshaller.class.getResource("/acceptance-steps.xml").getPath());
            JAXBContext jaxbContext = JAXBContext.newInstance(AcceptanceSteps.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            acceptanceSteps = (AcceptanceSteps) jaxbUnmarshaller.unmarshal(XMLUnmarshaller.class.getResource("/acceptance-steps.xml"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return acceptanceSteps;
    }
}
