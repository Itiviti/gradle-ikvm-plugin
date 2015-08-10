package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;

class IkvmPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.withType(Ikvm).whenTaskAdded { Ikvm task ->
            task.conventionMapping.map "ikvmVersion", { '7.2.4630.5' }
            task.conventionMapping.map "ikvmHome", {
                if (System.getenv()['IKVM_HOME']) {
                    return System.getenv()['IKVM_HOME']
                }
                def version = task.getIkvmVersion()
                return "http://downloads.sourceforge.net/project/ikvm/ikvm/${version}/ikvmbin-${version}.zip"
            }
        }

        project.apply plugin: 'java'

		Task ikvm = project.task('ikvm', type: Ikvm)
		ikvm.group = BasePlugin.BUILD_GROUP
		ikvm.description = 'Compiles the project jar into a .Net assembly.'

        Task ikvmDoc = project.task('ikvmDoc', type: IkvmDoc)
		ikvmDoc.group = JavaBasePlugin.DOCUMENTATION_GROUP
		ikvmDoc.description = 'Generates .Net API documentation (XML) for the main source code.'

    }
}
