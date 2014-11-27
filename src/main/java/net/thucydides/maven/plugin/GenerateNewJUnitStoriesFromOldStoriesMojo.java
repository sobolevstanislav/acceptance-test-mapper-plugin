package net.thucydides.maven.plugin;

import net.thucydides.maven.plugin.xml.unmarshaller.XMLUnmarshaller;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;

/**
 * @goal map-stories
 * @phase process-test-classes
 * @requiresDependencyResolution test
 */
public class GenerateNewJUnitStoriesFromOldStoriesMojo extends AbstractMojo {

    /**
     * Directory with oldsteps story files
     *
     * @parameter expression="${project.oldsteps.stories.directory}" default-value="${project.basedir}/src/test/resources/stories"
     * @required
     */
    public File oldStoriesDirectory;

    /**
     * Package name for Scenario jbehave steps
     *
     * @parameter expression="${project.scenario.acceptance.base.steps.package}" default-value="com.engagepoint.acceptancetest"
     * @required
     */
    public String packageForOldScenarioSteps;

    /**
     * Package name for Scenario jbehave steps
     *
     * @parameter expression="${project.scenario.satisfy.web.steps.package}" default-value="com.engagepoint.satisfy"
     * @required
     */
    public String packageForNewScenarioSteps;

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    public MavenProject project = null;

    /**
     * The directory containing generated test classes of the project being tested. This will be included at the
     * beginning of the test classpath.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    public File testClassesDirectory;

    /**
     * The directory containing generated classes of the project being tested. This will be included after the test
     * classes in the test classpath.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    public File classesDirectory;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.new.stories.directory}" default-value="${project.build.directory}/generated-test-sources"
     * @required
     */
    public File outputDirectory;

    private StoryParser storyParser;
    private ScenarioStepsFactory scenarioOldStepsFactory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("=======================");
        System.out.println("====MAPPING STARTED====");
        System.out.println("=======================");
        storyParser = new RegexStoryParser(new LocalizedKeywords());
        scenarioOldStepsFactory = new ScenarioStepsFactory(packageForOldScenarioSteps, new CustomClassloaderGenerator(project, classesDirectory, testClassesDirectory).getClassLoader(), new XMLUnmarshaller().unmarshal());
        File oldStoriesDir = oldStoriesDirectory;
        findStoryFilesAndGenerateNewStories(oldStoriesDir);
    }

    private void findStoryFilesAndGenerateNewStories(File oldStoriesDir) throws MojoExecutionException {
        if (oldStoriesDir.exists() && oldStoriesDir.isDirectory()) {
            for (File file : getFiles(oldStoriesDir)) {
                if (file.isFile() && getExtension(file.getPath()).equals("story")) {
                    generateStubFromStoryFile(file);
                } else if (file.isDirectory()) {
                    findStoryFilesAndGenerateNewStories(file);
                }
            }
        }
    }

    private File[] getFiles(File oldStoriesDir) throws MojoExecutionException {
        try {
            return oldStoriesDir.listFiles();
        } catch (NullPointerException e) {
            throw new MojoExecutionException("No files in directory: " + oldStoriesDir.getName(), e);
        }
    }

    public String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private void generateStubFromStoryFile(File story) throws MojoExecutionException {
        String className = getClassNameFrom(story.getName());
        Story parsedOldStory;
        try {
            String storyText = FileUtils.readFileToString(story);
            parsedOldStory = storyParser.parseStory(storyText);
            parsedOldStory.namedAs(story.getName());
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + className, e);
        }
        scenarioOldStepsFactory.createScenarioStepsClassModelFrom(parsedOldStory);
        createNewStoryFiles(scenarioOldStepsFactory.getNewStory(), story.getName());
    }

    private void createNewStoryFiles(Story newStory, String storyName) {
        NewStoriesGenerator storiesGenerator = new NewStoriesGenerator();
        storiesGenerator.createNewStory(newStory, storyName, outputDirectory);
    }

    private static String getClassNameFrom(String name) {
        int extensionIndex = name.lastIndexOf('.');
        String nameWithOutExtension = name.substring(0, extensionIndex);
        String[] words = nameWithOutExtension.split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(Character.toTitleCase(word.charAt(0)))
                    .append(word.substring(1));
        }
        return builder.toString();
    }
}
