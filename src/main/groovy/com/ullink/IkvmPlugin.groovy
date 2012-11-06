package com.ullink

import org.apache.ivy.core.module.descriptor.Artifact
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.plugins.resolver.util.ResolvedResource
import org.apache.ivy.plugins.resolver.util.ResourceMDParser
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
        
        project.apply plugin: 'java'
        project.task('ikvm', type: Ikvm)
        project.task('ikvmDoc', type: IkvmDoc)
    }
    
    File downloadIkvm(Project project, String version) {
        def dest = new File(project.gradle.gradleUserHomeDir, 'ikvm')
        if (!dest.exists()) {
            dest.mkDirs()
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
        if (!project.repositories.findByName('ikvm-package-repository')) {
            project.logger.info 'Adding Ikvm repository'
            project.repositories {
                add(new org.apache.ivy.plugins.resolver.URLResolver() {
                    protected ResolvedResource findResourceUsingPattern(ModuleRevisionId mrid, String pattern, Artifact artifact, ResourceMDParser rmdparser, Date date) {
                        if (mrid.organisation == 'ikvm') {
                            pattern = pattern.replace('[timestamp]',String.valueOf((long)(System.currentTimeMillis()/1000)))
                            super.findResourceUsingPattern(mrid, pattern, artifact, rmdparser, date)
                        }
                    }
                }) {
                    name = 'ikvm-package-repository'
                    addArtifactPattern 'http://downloads.sourceforge.net/project/[organization]/[module]/[revision]/[artifact]-[revision].[ext]?r=&ts=[timestamp]'
                    addArtifactPattern('http://www.frijters.net/[artifact]-[revision].[ext]')
                }
            }
        }
    }
}

