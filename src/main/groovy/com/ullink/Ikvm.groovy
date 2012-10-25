package com.ullink

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class Ikvm extends ConventionTask {
    def home
    def destinationDir
    String assemblyName
    boolean debug = true
    def keyFile
    String version
    String fileVersion
    def srcPath
    boolean removeAssertions = true
    boolean compressResources = true
    boolean generateDoc = true
    boolean delaySign = false
    String classloader
    String target
    String main
    
    Ikvm() {
        description = 'Generates a .Net assembly from the project jar'
        conventionMapping.map "destinationDir", { project.jar.destinationDir }
        conventionMapping.map "assemblyName", { project.name }
        conventionMapping.map "version", { project.version }
        conventionMapping.map "home", { System.getenv()['IKVM_HOME'] }
        dependsOn(project.tasks.jar)
    }
    
    @InputFile
    def getIkvmc(){
        assert home, "You must install Ikvm and set ikvm.home property or IKVM_HOME env variable"
        def ikvmExec = new File(project.file(home), 'bin/ikvmc.exe')
        assert ikvmExec.exists(), "You must install Ikvm and set ikvm.home property or IKVM_HOME env variable"
        ikvmExec
    } 
    
    @InputFiles
    def getReferences() {
        project.configurations.ikvmCompile.collect()
    } 
    
    @InputFile
    def getKeyFileObj() {
        if (getKeyFile()) {
            return project.file(getKeyFile())
        }
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
        def commandLineArgs = [ getIkvmc() ]

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
            getInputs().file(new File(getDestDir(), getAssemblyName() + ".pdb"))
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
        if (main) {
            commandLineArgs += "-target:${main}"
        }
        if (delaySign) {
            commandLineArgs += "-delaysign"
        }

        commandLineArgs += project.jar.archivePath
        commandLineArgs += getReferences().collect{"-reference:${it}"}
        project.exec {
            commandLine = commandLineArgs
        }
        if (generateDoc && !project.gradle.taskGraph.hasTask(project.tasks.ikvmDoc)) {
            project.tasks.ikvmDoc.generate()
        }
    }
}
