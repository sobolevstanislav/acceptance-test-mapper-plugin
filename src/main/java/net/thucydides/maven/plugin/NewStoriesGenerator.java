package net.thucydides.maven.plugin;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class NewStoriesGenerator {

    public void createNewStory(Story parsedNewStory, String newStoryName, File outputDirectory) {

        File newStoriesDirectory  = new File(outputDirectory.getAbsolutePath() + "/generated-stories");
        newStoriesDirectory.mkdirs();
        File newStoryFile = new File(newStoriesDirectory.getAbsolutePath() + "/" + newStoryName);
        try {
            PrintWriter printWriter = new PrintWriter(newStoryFile);
            for(Scenario scenario : parsedNewStory.getScenarios()) {
                printWriter.write("Scenario: " + scenario.getTitle() + "\n");
                for (String step : scenario.getSteps()) {
                    printWriter.write(step + "\n");
                }
                printWriter.write("\n");
            }
            printWriter.close();
            newStoryFile.createNewFile();
        } catch (IOException ex) {
        }
    }
}
