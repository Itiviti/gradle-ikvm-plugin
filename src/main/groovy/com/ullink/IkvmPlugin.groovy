package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;

class IkvmPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'java'
        project.apply plugin: 'ikvm-base'

        Task ikvm = project.task('ikvm', type: Ikvm)
        ikvm.group = BasePlugin.BUILD_GROUP
        ikvm.description = 'Compiles the project jar into a .Net assembly.'

        Task ikvmDoc = project.task('ikvmDoc', type: IkvmDoc)
        ikvmDoc.group = JavaBasePlugin.DOCUMENTATION_GROUP
        ikvmDoc.description = 'Generates .Net API documentation (XML) for the main source code.'

    }
}
