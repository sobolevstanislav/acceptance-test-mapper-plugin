package net.thucydides.maven.plugin.xml.steps;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class NewStep {

    @XmlElement(name = "step-as-string")
    private String stepAsString;

    @XmlElement(name = "params")
    private String[] params;

    public String getStepAsString() {
        return stepAsString;
    }

    public void setStepAsString(String stepAsString) {
        this.stepAsString = stepAsString;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
