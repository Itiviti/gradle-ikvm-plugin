package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project

class IkvmBasePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.withType(Ikvm).whenTaskAdded { Ikvm task ->
            task.ikvmVersion.convention('7.2.4630.5')
            task.ikvmHome.convention(project.provider {
                if (System.getenv()['IKVM_HOME']) {
                    return System.getenv()['IKVM_HOME']
                }
                def version = task.ikvmVersion.get()
                return "http://downloads.sourceforge.net/project/ikvm/ikvm/${version}/ikvmbin-${version}.zip"
            })
        }
    }
}
