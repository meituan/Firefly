package com.meituan

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

/**
 * Created by zhangmeng on 15/11/21.
 */
class FireflyArgs {
    @InputDirectory
    File inputDir
    @OutputDirectory
    File outputDir
    @Input
    boolean rxStyle = false
    @Input
    boolean android = false

    FireflyArgs(Project project){
        inputDir = new File("${project.projectDir}/src/main/idl")
        outputDir = new File("${project.buildDir}/generated/source/firefly")
    }

}
