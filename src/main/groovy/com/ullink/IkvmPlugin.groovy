package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project

class IkvmPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.configurations  {
            ikvmCompile {
                description = 'IKVM compile classpath'
                transitive = true
            }
        }
        project.apply plugin: 'java'
        project.task('ikvm', type: Ikvm)
        project.task('ikvmDoc', type: IkvmDoc)
    }
}

