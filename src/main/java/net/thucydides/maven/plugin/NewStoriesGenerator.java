package net.thucydides.maven.plugin;

import org.jbehave.core.model.Story;

import java.io.File;
import java.io.IOException;

public class NewStoriesGenerator {

    File newStoriesDirectory;
    File oldStoriesDirectory;
    Story oldStory;
    Story newStory;

    public NewStoriesGenerator(File oldStoriesDirectory, File newStoriesDirectory) {
        this.newStoriesDirectory = newStoriesDirectory;
    }

    private void createConnectionBetweenStepCandidates(Story parsedOldStory) {
        Story parsedNewStory = parsedOldStory;
        String newStoryName = parsedOldStory.getName().substring(0, parsedOldStory.getName().indexOf(".")) + "_new";
        generateNewStoryFiles(parsedNewStory, newStoryName);
    }

    private void generateNewStoryFiles(Story parsedNewStory, String newStoryName) {
        newStoriesDirectory = new File("src/test/resources/stories/new/" + newStoryName + ".story");
        try {
            newStoriesDirectory.createNewFile();
        } catch (IOException ex) {

        }
    }
}
