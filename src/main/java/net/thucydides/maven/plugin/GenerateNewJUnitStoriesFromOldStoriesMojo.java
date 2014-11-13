package net.thucydides.maven.plugin;

import net.thucydides.maven.plugin.model.ScenarioStepsClassModel;
import net.thucydides.maven.plugin.xml.steps.AcceptanceSteps;
import net.thucydides.maven.plugin.xml.unmarshaller.XMLUnmarshaller;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;

/**
 * @goal change-stories-to-new
 * @requiresProject false
 * @execute phase="compile"
 */
public class GenerateNewJUnitStoriesFromOldStoriesMojo extends AbstractMojo {

    /**
     * Directory with oldsteps story files
     *
     * @parameter expression="${project.oldsteps.stories.directory}"
     * @required
     */
    public File oldStoriesDirectory;

    /**
     * Directory with new story files
     *
     * @parameter expression="${project.new.stories.directory}"
     * @required
     */
    public File newStoriesDirectory;

    /**
     * Package name for Scenario jbehave steps
     *
     * @parameter expression="${project.scenario.acceptance.base.steps.package}"
     * @required
     */
    public String packageForOldScenarioSteps;

    /**
     * Package name for Scenario jbehave steps
     *
     * @parameter expression="${project.scenario.satisfy.web.steps.package}" default-value="${project.groupId}"
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
     * @parameter expression="${project.junit.stories.directory}"
     * @required
     */
    public File outputDirectory;

    private StoryParser storyParser;
    ScenarioStepsFactory scenarioStepsFactory;
    private List<ScenarioStepsClassModel> scenarioStepsClassModels = new ArrayList<ScenarioStepsClassModel>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("EXECUTE() started in:" + new Date().toString());
        storyParser = new RegexStoryParser(new LocalizedKeywords());
        scenarioStepsFactory = new ScenarioStepsFactory(packageForOldScenarioSteps, new GetClassloaderWithCustomClasspath(project, classesDirectory, testClassesDirectory).getClassLoader());
        File oldStoriesDir = oldStoriesDirectory;
        File newStoriesDir = newStoriesDirectory;
        AcceptanceSteps acceptanceSteps = new AcceptanceSteps();
        XMLUnmarshaller.unmarshal(acceptanceSteps);
        findStoryFilesAndGenerateNewStories(oldStoriesDir);
    }

    private ClassLoader getClassLoader() throws MojoFailureException, MojoExecutionException {
        ClassLoader pluginClassLoader = getClass().getClassLoader();
        Set<URL> projectClasspathList = getUrlsForCustomClasspath();
        ClassLoader projectClassLoader = new URLClassLoader(
                projectClasspathList.toArray(
                        new URL[projectClasspathList.size()]), pluginClassLoader);
        return projectClassLoader;
    }

    private Set<URL> getUrlsForCustomClasspath() throws MojoFailureException, MojoExecutionException {
        Set<URL> classpath = new HashSet<URL>(2 + getProject().getArtifacts().size());

        try {
            classpath.add(getTestClassesDirectory().getAbsoluteFile().toURI().toURL());
            classpath.add(getClassesDirectory().getAbsoluteFile().toURI().toURL());

            @SuppressWarnings("unchecked") Set<Artifact> classpathArtifacts = getProject().getArtifacts();

            for (Artifact artifact : classpathArtifacts) {
                if (artifact.getArtifactHandler().isAddedToClasspath() && !artifact.getGroupId().startsWith("org.apache.maven")) {
                    File file = artifact.getFile();
                    if (file != null) {
                        classpath.add(file.toURI().toURL());
                    }
                }
            }
        } catch (MalformedURLException e) {
            getLog().error(e.getMessage(), e);
        }
        return classpath;
    }

    public MavenProject getProject() {
        return project;
    }

    public File getTestClassesDirectory() {
        return testClassesDirectory;
    }

    public File getClassesDirectory() {
        return classesDirectory;
    }

    private void findStoryFilesAndGenerateNewStories(File oldStoriesDir) throws MojoExecutionException {
        System.out.println("Finding and generating stories started");
        if (oldStoriesDir.exists() && oldStoriesDir.isDirectory()) {
            for (File file : getFiles(oldStoriesDir)) {
                System.out.println("Story " + file.getName() + " found");
                if (file.isFile() && getExtension(file.getPath()).equals("story")) {
                    Story parsedOldStory = generateStubFromStoryFile(file);
                    createConnectionBetweenStepCandidates(parsedOldStory);
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

    private Story generateStubFromStoryFile(File story) throws MojoExecutionException {
        System.out.println("Generating stub from story " + story.getName());
        String className = getClassNameFrom(story.getName());
        Story parsedOldStory;
        try {
            String storyText = FileUtils.readFileToString(story);
            parsedOldStory = storyParser.parseStory(storyText);
            parsedOldStory.namedAs(story.getName());
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + className, e);
        }
        scenarioStepsClassModels.add(scenarioStepsFactory.createScenarioStepsClassModelFrom(parsedOldStory));
        return parsedOldStory;
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

    private void createConnectionBetweenStepCandidates(Story parsedOldStory) {
        Story parsedNewStory = parsedOldStory;
        String newStoryName = parsedOldStory.getName().substring(0, parsedOldStory.getName().indexOf(".")) + "_new";
        generateNewStoryFiles(parsedNewStory, newStoryName);
    }

    private void generateNewStoryFiles(Story parsedNewStory, String newStoryName) {
        newStoriesDirectory = new File("./src/test/stories/new/" + newStoryName + ".story");
        try {
            newStoriesDirectory.createNewFile();
        } catch (IOException ex) {

        }
    }

}
