package net.thucydides.maven.plugin.xml.steps;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "acceptance-steps")
public class AcceptanceSteps {

    @XmlElement(name = "steps-pair", type = StepsPairBean.class)
    private List<StepsPairBean> stepsPairBeanList = new ArrayList<StepsPairBean>();

    public List<StepsPairBean> getStepsBeanList() {
        return stepsPairBeanList;
    }

    public void setStepsBeanList(List<StepsPairBean> stepsBeanList) {
        this.stepsPairBeanList = stepsBeanList;
    }
}
