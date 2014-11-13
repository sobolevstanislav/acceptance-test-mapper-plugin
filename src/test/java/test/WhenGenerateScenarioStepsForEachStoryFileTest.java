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

    File pluginOutputDirectory;
    File newStoriesDirectory;
    File oldStoriesDirectory;
    File classesDirectory;
    File testClassesDirectory;

    private MavenProject project = null;

    @Before
    public void setupPlugin() throws IOException {
        plugin = new GenerateNewJUnitStoriesFromOldStoriesMojo();
        project = new MavenProject();

        pluginOutputDirectory = new File("target/generated");
        pluginOutputDirectory.mkdir();
        oldStoriesDirectory = new File("src/test/resources/stories/old");
        newStoriesDirectory = new File("src/test/resources/stories/new");
        classesDirectory = new File("target/classes");
        testClassesDirectory = new File("target/test-classes");

        plugin.outputDirectory = pluginOutputDirectory;
        plugin.oldStoriesDirectory = oldStoriesDirectory;
        plugin.newStoriesDirectory = newStoriesDirectory;
        plugin.classesDirectory = classesDirectory;
        plugin.testClassesDirectory = testClassesDirectory;

        plugin.packageForOldScenarioSteps = "test.sample.steps.oldsteps";
        plugin.packageForNewScenarioSteps = "test.sample.steps.newsteps";
        plugin.project = project;
    }

    @Test
    public void should_create_scenario_steps_class_for_each_story_file() throws MojoExecutionException, MojoFailureException, IOException {
        System.out.println(pluginOutputDirectory.getAbsolutePath());
        System.out.println(newStoriesDirectory.getAbsolutePath());
        System.out.println(oldStoriesDirectory.getAbsolutePath());
        plugin.execute();
    }
}