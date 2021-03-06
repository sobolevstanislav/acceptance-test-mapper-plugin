package net.thucydides.maven.plugin.xml.steps;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class StepsPairBean {

    @XmlElement(name = "old-step")
    private String oldStep;

    @XmlElement(name = "new-step")
    private NewStep newStep;

    public String getOldStep() {
        return oldStep;
    }

    public void setOldStep(String oldStep) {
        this.oldStep = oldStep;
    }

    public NewStep getNewStep() {
        return newStep;
    }

    public void setNewStep(NewStep newStep) {
        this.newStep = newStep;
    }
}
