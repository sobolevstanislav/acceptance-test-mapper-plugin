package test;

import net.thucydides.maven.plugin.GenerateNewJUnitStoriesFromOldStoriesMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class WhenGenerateScenarioStepsForEachStoryFileTest {

    private GenerateNewJUnitStoriesFromOldStoriesMojo plugin;

//    @Rule
//    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    File newStoriesDirectory;
    File oldStoriesDirectory;
    File classesDirectory;
    File testClassesDirectory;
    File outputDirectory;

    private MavenProject project = null;

    @Before
    public void setupPlugin() throws IOException {
        plugin = new GenerateNewJUnitStoriesFromOldStoriesMojo();
        project = new MavenProject();

        oldStoriesDirectory = new File("src/test/resources/stories/old");
        newStoriesDirectory = new File("src/test/resources/stories/new");
        classesDirectory = new File("target/classes");
        testClassesDirectory = new File("target/test-classes");
        outputDirectory = new File("target/generated-test-sources");

        plugin.oldStoriesDirectory = oldStoriesDirectory;
        plugin.classesDirectory = classesDirectory;
        plugin.testClassesDirectory = testClassesDirectory;
        plugin.outputDirectory = outputDirectory;

        plugin.packageForOldScenarioSteps = "test.sample.steps.oldsteps";
        plugin.packageForNewScenarioSteps = "test.sample.steps.newsteps";
        plugin.project = project;
    }

    @Test
    public void should_create_scenario_steps_class_for_each_story_file() throws MojoExecutionException, MojoFailureException, IOException {
        plugin.execute();
    }
}