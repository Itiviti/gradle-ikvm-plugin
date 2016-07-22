package com.ullink
import org.gradle.api.Plugin
import org.gradle.api.Project

class IkvmBasePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.tasks.withType(Ikvm).whenTaskAdded { Ikvm task ->
            task.conventionMapping.map "ikvmVersion", { '7.2.4630.5' }
            task.conventionMapping.map "ikvmHome", {
                if (System.getenv()['IKVM_HOME']) {
                    return System.getenv()['IKVM_HOME']
                }
                return tryToFindUrl(task.getIkvmVersion())
            }
        }
    }

    String tryToFindUrl(String version){
        String defaultUrl = "http://downloads.sourceforge.net/project/ikvm/ikvm/${version}/ikvmbin-${version}.zip"
        String betaUrl = "http://www.frijters.net/ikvmbin-${version}.zip"

        HttpURLConnection connection = new URL(defaultUrl).openConnection()
        connection.setRequestMethod("HEAD")
        if (connection.responseCode == 200) {
            return defaultUrl
        }else{
            return betaUrl
        }
    }
}
