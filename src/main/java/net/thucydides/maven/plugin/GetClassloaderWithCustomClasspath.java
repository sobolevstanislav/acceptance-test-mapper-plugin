package net.thucydides.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class GetClassloaderWithCustomClasspath {

    public MavenProject project = null;
    public File classesDirectory;
    public File testClassesDirectory;

    public GetClassloaderWithCustomClasspath(MavenProject project, File testClassesDirectory, File classesDirectory) {
        this.project = project;
        this.classesDirectory = classesDirectory;
        this.testClassesDirectory = testClassesDirectory;
    }

    public ClassLoader getClassLoader() throws MojoFailureException, MojoExecutionException {
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
}
