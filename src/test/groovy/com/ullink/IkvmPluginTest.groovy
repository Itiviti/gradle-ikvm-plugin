package com.ullink

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.*

class IkvmPluginTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    void ikvmPluginAddsIkvmTasksToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        assertTrue(project.tasks.ikvm instanceof Ikvm)
        assertTrue(project.tasks.ikvmDoc instanceof IkvmDoc)
        project.ikvm {
            ikvmHome = 'abc'
        }
        assertEquals('abc', project.tasks.ikvm.ikvmHome.get())
    }

    @Test
    void ensureIkvmTaskdependsOnJarByDefault() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        project.evaluate()
        assertTrue(project.ikvm.dependsOn.contains(project.jar))
    }

    @Test
    void shouldDependOnOtherTaskWhenTargetingAnotherJar() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        project.task('otherJar', type: Jar) {
            baseName = 'foo'
        }
        project.ikvm {
            jars = [ project.otherJar.archiveFile.get() ]
        }
        project.evaluate()
        assertFalse(project.ikvm.dependsOn.contains(project.jar))
        assertTrue(project.ikvm.dependsOn.contains(project.otherJar))
    }

    @Test
    void commandLineContainsJar() {
        def home = temporaryFolder.newFolder()
        new File(home, 'bin').mkdir()
        new File(home, 'bin/ikvmc.exe').createNewFile()

        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        project.ikvm {
            ikvmHome = home.path
        }

        def cmd = project.ikvm.commandLineArgs
        def jarFile = project.jar.archiveFile.get().toString()
        assertTrue(cmd.contains(jarFile))
    }
}
