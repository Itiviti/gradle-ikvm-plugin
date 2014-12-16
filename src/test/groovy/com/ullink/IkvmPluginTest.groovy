package com.ullink

import static org.junit.Assert.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class IkvmPluginTest {
    @Test
    public void ikvmPluginAddsIkvmTasksToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'ikvm'
        assertTrue(project.tasks.ikvm instanceof Ikvm)
        assertTrue(project.tasks.ikvmDoc instanceof IkvmDoc)
        project.ikvm {
            ikvmHome = 'abc'
        }
        assertEquals('abc', project.tasks.ikvm.ikvmHome)
    }
}
