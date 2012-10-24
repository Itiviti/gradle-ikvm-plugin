package com.ullink

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc

class IkvmDoc extends Javadoc {
    def assemblyFile
    IkvmDoc() {
        description = 'Generates an xml documentation file for IKVM generated assembly (from javadoc)'
        conventionMapping.map "classpath", { project.configurations.compile }
        conventionMapping.map "source", { project.sourceSets.main.allJava }
        conventionMapping.map "destinationDir", { project.tasks.ikvm.getDestDir() }
        conventionMapping.map "assemblyFile", { project.tasks.ikvm.getDestFile() }
        options.doclet = IKVMDocLet.class.getName()
        options.docletpath = project.buildscript.configurations.classpath.files.asType(List)
        dependsOn(project.tasks.ikvm)
    }
    
    @Override
    @TaskAction
    protected void generate()
    {
        options.addStringOption("assembly", getDestFile().toString())
        super.generate();
    }
    
    @InputFile
    File getDestFile() {
        project.file(getAssemblyFile())
    }
}
