package com.meituan

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task

/***
 * plugin uses idl files in inputDir to generate java files in outputDir via using generator in project firefly
 * http://git.sankuai.com/projects/AOPEN/repos/firefly/browse
 * the plugin must be declared before plugin com.android.library or android
 */
class FireflyPlugin implements Plugin<Project> {
    private static final String ANDROID_DEPENDENCY_TASK_NAME = 'preBuild'
    private static final String JAVA_DEPENDENCY_TASK_NAME = 'compileJava'
    private static  String DEPENDENCY_TASK_NAME;
    void apply(Project target) {

        target.extensions.create("firefly", FireflyArgs)
        target.afterEvaluate {
            if (target.plugins.hasPlugin('com.android.library') || target.plugins.hasPlugin('android')) {
                DEPENDENCY_TASK_NAME=ANDROID_DEPENDENCY_TASK_NAME;
                target.android.sourceSets.main.java.srcDir target.firefly.outputDir.absolutePath
            } else if (target.plugins.hasPlugin('java')) {
                DEPENDENCY_TASK_NAME=JAVA_DEPENDENCY_TASK_NAME;
                target.sourceSets.main.java.srcDirs target.firefly.outputDir.absolutePath
            } else {
                throw new Exception('firefly plugin must used in projects which  use com.android.library , android or java plugin ')
            }
            FireflyTask fireflyTask = target.tasks.create("thrift2java", FireflyTask)
            def Task dependentTask = target.tasks.findByName(DEPENDENCY_TASK_NAME)
            if (dependentTask != null) {
                fireflyTask.dependsOn dependentTask.taskDependencies.getDependencies(dependentTask)
                dependentTask.dependsOn fireflyTask
            } else {
                throw new Exception('cannot find depenpent task ' + DEPENDENCY_TASK_NAME)
            }

        }
    }

}

