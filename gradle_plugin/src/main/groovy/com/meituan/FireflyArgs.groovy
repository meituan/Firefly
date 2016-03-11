package com.meituan

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

/**
 * Created by zhangmeng on 15/11/21.
 */
class FireflyArgs {
    @InputDirectory
    File inputDir = new File('${project.projectDir}/src/main/idl')
    @OutputDirectory
    File outputDir = new File('${project.buildDir}/generated/source/firefly')
    @Input
    boolean rxStyle = false
    @Input
    boolean android = false

}
