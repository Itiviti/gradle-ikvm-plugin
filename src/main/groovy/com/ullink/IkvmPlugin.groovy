package com.ullink

import org.gradle.api.GradleException
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
        
        project.tasks.withType(Ikvm).whenTaskAdded { Ikvm task ->
            task.conventionMapping.map "ikvmVersion", { '7.2.4630.4' }
            task.conventionMapping.map "ikvmHome", {
                if (System.getenv()['IKVM_HOME']) {
                    return System.getenv()['IKVM_HOME']
                }
                setupIkvmRepositories(project)
                def version = task.getIkvmVersion()
                downloadIkvm(project, version)
            }
        }
        
        project.apply plugin: 'repositories'
        project.apply plugin: 'java'
        project.task('ikvm', type: Ikvm)
        project.task('ikvmDoc', type: IkvmDoc)
    }
    
    File downloadIkvm(Project project, String version) {
        def dest = new File(project.gradle.gradleUserHomeDir, 'ikvm')
        if (!dest.exists()) {
            dest.mkdirs()
        }
        def ret = new File(dest, "ikvm-${version}")
        if (!ret.exists()) {
            project.logger.info "Downloading & Unpacking Ikvm ${version}"
            def dep = project.dependencies.create(group: 'ikvm', name: 'ikvm', version: version) {
                artifact {
                    name = 'ikvmbin'
                    type = 'zip'
                }
            }
            File zip = project.configurations.detachedConfiguration(dep).singleFile
            if (!zip.isFile()) {
                throw new GradleException("IKVM zip file '${zip}' doesn't exist")
            }
            project.ant.unzip(src: zip, dest: dest)
        }
        ret
    }
    
    void setupIkvmRepositories(Project project) {
        if (!project.repositories.findByName('sourceforge-ikvm')) {
            project.repositories.sourceforge('ikvm', '[module]/[revision]/[artifact]-[revision].[ext]') {
                // this one is where Jeroen deploys beta/rc
                addArtifactPattern('http://www.frijters.net/[artifact]-[revision].[ext]')
            }
        }
    }
}

