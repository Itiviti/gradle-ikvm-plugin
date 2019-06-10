package com.ullink

import org.gradle.api.tasks.bundling.Jar

import static org.junit.Assert.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class IkvmPluginTest {
    @Test
    void ikvmPluginAddsIkvmTasksToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        assertTrue(project.tasks.ikvm instanceof Ikvm)
        assertTrue(project.tasks.ikvmDoc instanceof IkvmDoc)
        project.ikvm {
            ikvmHome = 'abc'
        }
        assertEquals('abc', project.tasks.ikvm.ikvmHome)
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
            jars = [ project.otherJar.archivePath ]
        }
        project.evaluate()
        assertFalse(project.ikvm.dependsOn.contains(project.jar))
        assertTrue(project.ikvm.dependsOn.contains(project.otherJar))
    }

    @Test
    void commandLineContainsJar() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        def cmd = project.ikvm.commandLineArgs
        assertTrue(cmd.contains(project.jar.archivePath))
    }
}
