package com.ullink

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar

import java.nio.file.Files

class Ikvm extends DefaultTask {
    public static final String IKVM_EXE = 'bin/ikvmc.exe'

    Property<String> ikvmHome
    Property<String> ikvmVersion
    DirectoryProperty destinationDir
    Property<String> assemblyName
    boolean debug = true
    def keyFile
    Property<String> version
    String fileVersion
    def srcPath
    boolean removeAssertions = true
    boolean compressResources = true
    boolean generateDoc = false
    boolean delaySign = false
    boolean nojni = false
    boolean nostdlib = false
    String classloader
    String target
    String main
    String platform
    String remap
    def warnAsError

    @InputFiles
    ListProperty<RegularFile> jars

    Ikvm() {
        ikvmHome = project.objects.property(String)
        ikvmVersion = project.objects.property(String)
        destinationDir = project.objects.directoryProperty()
        jars = project.objects.listProperty(RegularFile)
        assemblyName = project.objects.property(String)
        version = project.objects.property(String)

        def jarTask = (Jar)project.jar
        destinationDir.convention(jarTask.destinationDirectory)
        jars.convention(project.provider { [ jarTask.archiveFile.get() ] })
        assemblyName.convention(project.name)
        version.convention(project.version)

        project.afterEvaluate {
            def src = jars.get()
            project.tasks.withType(Jar).find {
                src.contains(it.archiveFile.get())
            }.each {
                dependsOn it
            }
            if (generateDoc) {
                outputs.files {
                    return project.tasks.ikvmDoc.getDestinationFile()
                }
            }
            if (debug) {
                outputs.files {
                    getDestinationDebugFile()
                }
            }
        }

        Configuration compileConfiguration = (Configuration) project.configurations.findByName(getCompileConfigurationName())
        if (compileConfiguration == null) {
            compileConfiguration = project.configurations.maybeCreate(getCompileConfigurationName())
        }
        compileConfiguration.transitive = true
        compileConfiguration.description = this.name + ' compile classpath'
    }
    
    String getCompileConfigurationName() {
        String.format("%sCompile", this.name).toLowerCase()
    }
    
    def getIkvmc(){
        def home = resolveIkvmHome()
        assert home, "You must install Ikvm and set ikvm.home property or IKVM_HOME env variable"
        File ikvmExec = new File(home, IKVM_EXE)
        assert ikvmExec.exists(), "You must install Ikvm and set ikvm.home property or IKVM_HOME env variable"
        return ikvmExec
    }

    File resolveIkvmHome() {
        def home = getIkvmHome()
        URL url
        if (home instanceof URL)
            url = (URL)home
        else if (home?.toString()?.startsWith("http"))
            url = new URL(home.toString())
        if (url) {
            def dest = new File(project.gradle.gradleUserHomeDir, 'ikvm')
            if (!dest.exists()) {
                dest.mkdirs()
            }
            def urlSha1 = url.toString().digest('SHA-1')
            def ret = new File(dest, urlSha1)
            if (!ret.exists()) {
                project.logger.info "Downloading & Unpacking Ikvm ${url}"
                def dlFile = new File(dest, "${urlSha1}.zip")
                if (!dlFile.exists()) {
                    dlFile.withOutputStream { out ->
                        out << url.openStream()
                    }
                }
                project.ant.unzip(src: dlFile, dest: ret)
            }
            if (new File(ret, IKVM_EXE).exists())
                return ret
            def sub = ret.listFiles().find {
                new File(it, IKVM_EXE).exists()
            }
            assert sub, "${IKVM_EXE} not found in downloaded archive"
            return sub
        }
        return project.file(home)
    }

    def ikvmcOptionalOnMono (){
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
        project.file(destinationDir.get())
    }

    def getDestinationDebugFile() {
        return new File(getDestDir(), assemblyName.get() + ".pdb")
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
                    break
                case "module":
                    extension = ".netmodule"
                    break
                case "exe":
                case "winexe":
                default:
                    extension = ".exe"
                    break
            }
        }
        new File(getDestDir(), assemblyName.get() + extension)
    }

    def getCommandLineArgs() {
        def commandLineArgs = ikvmcOptionalOnMono()

        def destFile = getDestFile()
        commandLineArgs += "-out:${destFile}"

        def version = version.get().replaceAll("[^0-9.]+", "")
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
        if (nojni) {
            commandLineArgs += "-nojni"
        }
        if (nostdlib) {
            commandLineArgs += "-nostdlib"
        }
        if (remap) {
            commandLineArgs += "-remap:${remap}"
        }
        if (warnAsError.any()) {
            warnAsError.each {
                commandLineArgs += "-warnaserror:$it"
            }
        }
        else if (warnAsError) {
            commandLineArgs += "-warnaserror"
        }

        commandLineArgs += jars.get().collect { it.asFile.path }
        commandLineArgs += getReferences().collect{"-reference:${it}"}

        return commandLineArgs
    }
    
    @TaskAction
    def build() {
        File debugFile = getDestinationDebugFile()
        if (debug && debugFile.isFile()) {
            debugFile.delete()
        }
        project.exec {
            commandLine(commandLineArgs)
        }
        if (debug && !debugFile.isFile()) {
            // bug in IKVM 0.40
            File shitFile = new File(assemblyName.get() + ".pdb")
            if (shitFile.isFile()) {
                Files.move(shitFile.toPath(), debugFile.toPath())
            }
        }
        if (generateDoc && !project.gradle.taskGraph.hasTask(project.tasks.ikvmDoc)) {
            project.tasks.ikvmDoc.generate()
        }
    }
}
