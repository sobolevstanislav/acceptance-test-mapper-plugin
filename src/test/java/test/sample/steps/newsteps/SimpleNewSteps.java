package test.sample.steps.newsteps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import test.sample.SimpleException;

import java.util.List;

public class SimpleNewSteps {

    @Given("I have JBehave scenario")
    public void givenIHaveAnImplementedJBehaveScenario() {
        System.out.println("This is jbehave simple step execution.");
    }

    @Given("I have JBehave scenario with $parameter")
    public void givenIHaveAnImplementedJBehaveScenarioWithOneParameter(String parameter) {
        System.out.println("I have an implemented JBehave scenario with one " + parameter);
    }

    @Given("scenario with $first and $second also works")
    public void givenIHaveAnImplementedJBehaveScenarioWithTwoParameters(String first, String second) {
        System.out.println("I have an implemented JBehave scenario with two " + first + ", " + second + " parameters.");
    }

    @Then("user should see: $titleTable")
    public void thenUserShouldSeeTitleWithText(String titleText) throws SimpleException {
        System.out.println("User see page with title - " + titleText);
    }
}
