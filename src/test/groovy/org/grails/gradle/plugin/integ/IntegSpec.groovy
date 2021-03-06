package org.grails.gradle.plugin.integ

import org.gradle.GradleLauncher
import org.gradle.StartParameter
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class IntegSpec extends Specification {
    @Rule final TemporaryFolder dir = new TemporaryFolder()

    static class ExecutedTask {
        Task task
        TaskState state
    }

    List<ExecutedTask> executedTasks = []

    GradleLauncher launcher(String... args) {
        StartParameter startParameter = GradleLauncher.createStartParameter(args)
        startParameter.setProjectDir(dir.root)
        GradleLauncher launcher = GradleLauncher.newInstance(startParameter)
        executedTasks.clear()
        launcher.addListener(new TaskExecutionListener() {
            void beforeExecute(Task task) {
                IntegSpec.this.executedTasks << new ExecutedTask(task: task)
            }

            void afterExecute(Task task, TaskState taskState) {
                IntegSpec.this.executedTasks.last().state = taskState
                taskState.metaClass.upToDate = taskState.skipMessage == "UP-TO-DATE"
            }
        })
        launcher
    }

    File getBuildFile() {
        file("build.gradle")
    }

    File file(String path) {
        def parts = path.split("/")
        if (parts.size() > 1) {
            dir.newFolder(*parts[0..-2])
        }
        dir.newFile(path)
    }

    ExecutedTask task(String name) {
        executedTasks.find { it.task.name == name }
    }

    def setup() {
        buildFile << """
            GrailsPlugin = project.class.classLoader.loadClass("org.grails.gradle.plugin.GrailsPlugin")
            GrailsTask = project.class.classLoader.loadClass("org.grails.gradle.plugin.GrailsTask")
            version = "1.0"

            repositories {
                maven { url "http://repo.grails.org/grails/core" }
            }
        """
    }

    def applyPlugin(String grailsVersion = "2.0.0") {
        buildFile << """
            grailsVersion = "$grailsVersion"
            apply plugin: GrailsPlugin
        """
    }

}