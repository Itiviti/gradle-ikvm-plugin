package com.ullink

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

class Ikvm extends ConventionTask {
    def ikvmHome
    def ikvmVersion
    def destinationDir
    String assemblyName
    boolean debug = true
    def keyFile
    String version
    String fileVersion
    def srcPath
    boolean removeAssertions = true
    boolean compressResources = true
    boolean generateDoc = false
    boolean delaySign = false
    String classloader
    String target
    String main
    String platform
    
    Ikvm() {
        conventionMapping.map "destinationDir", { project.jar.destinationDir }
        conventionMapping.map "assemblyName", { project.name }
        conventionMapping.map "version", { project.version }
        dependsOn(project.tasks.jar)
        outputs.files {
            if (generateDoc) {
                return project.tasks.ikvmDoc.getDestinationFile()
            }
        }
        outputs.files {
            if (debug) {
                return new File(getDestDir(), getAssemblyName() + ".pdb")
            }
        }
        
        Configuration compileConfiguration = (Configuration)project.configurations.findByName(getCompileConfigurationName());
        if (compileConfiguration == null) {
            compileConfiguration = project.configurations.maybeCreate(getCompileConfigurationName());
        }
        compileConfiguration.transitive = true
        compileConfiguration.description = this.name + ' compile classpath'
    }
    
    String getCompileConfigurationName() {
        return StringUtils.uncapitalize(String.format("%sCompile", this.name ));
    }
    
    @InputFile
    def getIkvmc(){
        assert getIkvmHome(), "You must install Ikvm and set ikvm.home property or IKVM_HOME env variable"
        File ikvmExec = new File(project.file(getIkvmHome()), 'bin/ikvmc.exe')
        assert ikvmExec.exists(), "You must install Ikvm and set ikvm.home property or IKVM_HOME env variable"
        return ikvmExec
    }

    def ikvmcOptionalOnMono(){
        if (!OperatingSystem.current().windows){
            project.logger.info "Using Mono for IKVM"
            return ["mono",getIkvmc()]
        }
        return [getIkvmc()]
    }

    
    @InputFiles
    def getReferences() {
        project.configurations.findByName(getCompileConfigurationName()).collect()
    } 
    
    @InputFiles
    def getKeyFileObj() {
        if (getKeyFile()) {
            return project.file(getKeyFile())
        }
        return new File[0]
    } 
    
    def getDestDir() {
        project.file(getDestinationDir())
    }
    
    @OutputFile
    def getDestFile() {
        String extension = ".dll"
        if (target != null)
        {
            switch (target)
            {
                case "library":
                    extension = ".dll"
                    break;
                case "module":
                    extension = ".netmodule"
                    break;
                case "exe":
                case "winexe":
                default:
                    extension = ".exe"
                    break;
            }
        }
        new File(getDestDir(), getAssemblyName() + extension)
    }
    
    @TaskAction
    def build() {
        def commandLineArgs = ikvmcOptionalOnMono()

        def destFile = getDestFile()
        commandLineArgs += "-out:${destFile}"
        
        def version = getVersion().replaceAll("[^0-9.]+", "")
        commandLineArgs += "-version:${version}"
        
        if (fileVersion) {
            commandLineArgs += "-fileversion:${fileVersion}"
        }
        def keyFile = getKeyFileObj()
        if (keyFile) {
            commandLineArgs += "-keyfile:${keyFile}"
        }
        if (debug) {
            commandLineArgs += "-debug"
        }
        if (removeAssertions) {
            commandLineArgs += "-removeassertions"
        }
        if (compressResources) {
            commandLineArgs += "-compressresources"
        }
        if (srcPath) {
            def srcPath = project.file(srcPath)
            commandLineArgs += "-srcpath:${srcPath}"
        }
        if (classloader) {
            def classloader = classloader
            if (classloader == "AppDomainAssembly" || classloader == "Assembly" || classloader == "ClassPathAssembly") {
                classloader = "ikvm.runtime."+classloader+"ClassLoader"
            }
            commandLineArgs += "-classloader:${classloader}"
        }
        if (target) {
            commandLineArgs += "-target:${target}"
        }
        if (platform) {
            commandLineArgs += "-platform:${platform}"
        }
        if (main) {
            commandLineArgs += "-target:${main}"
        }
        if (delaySign) {
            commandLineArgs += "-delaysign"
        }

        commandLineArgs += project.jar.archivePath
        commandLineArgs += getReferences().collect{"-reference:${it}"}
        
        File debugFile = new File(getDestDir(), getAssemblyName() + ".pdb")
        if (debug && debugFile.isFile()) {
            debugFile.delete();
        }
        project.exec {
            commandLine = commandLineArgs
        }
        if (debug && !debugFile.isFile()) {
            // bug in IKVM 0.40
            File shitFile = new File(getAssemblyName() + ".pdb")
            if (shitFile.isFile()) {
                FileUtils.moveFile(shitFile, debugFile)
            }
        }
        if (generateDoc && !project.gradle.taskGraph.hasTask(project.tasks.ikvmDoc)) {
            project.tasks.ikvmDoc.generate()
        }
    }
}
