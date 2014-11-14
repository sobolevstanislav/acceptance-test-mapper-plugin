package net.thucydides.maven.plugin;

import org.jbehave.core.model.Story;

import java.io.File;
import java.io.IOException;

public class NewStoriesGenerator {

    File newStoriesDirectory;
    File oldStoriesDirectory;

    public NewStoriesGenerator(File oldStoriesDirectory, File newStoriesDirectory) {
        this.newStoriesDirectory = newStoriesDirectory;
        this.oldStoriesDirectory = oldStoriesDirectory;
    }

    private void createNewStory(Story parsedNewStory, String newStoryName) {
        newStoriesDirectory = new File("src/test/resources/stories/new/" + newStoryName + ".story");
        try {
            newStoriesDirectory.createNewFile();
        } catch (IOException ex) {

        }
    }
}
