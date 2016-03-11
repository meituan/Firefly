package com.meituan

import org.gradle.api.DefaultTask
import com.meituan.firefly.Main$
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class FireflyTask extends DefaultTask {

    File inputDir = new File('${project.projectDir}/src/main/idl')
    File outputDir = new File('${project.buildDir}/generated/source/firefly')
    boolean rxStyle = false
    boolean android = false

    public static final String CONFIG_MAP = 'config_map.txt'
    public static final String CONFIG_MAP_SIGN = '->'
    File configMapFile

    FireflyTask() {
        inputDir = (getProject().firefly.inputDir)
        outputDir = (getProject().firefly.outputDir)
        rxStyle= (getProject().firefly.rxStyle)
        android=(getProject().firefly.android)
    }
/**
 *
 * for test
 */
    static void main(args) {

        println 'begin'
        def array = ['${project.buildDir}/src/main/idl/PoiComment.thrift', '--output', '/Users/zhangmeng/Desktop/common/', '--rx'] as String[];
        Main$.MODULE$.main(array);
        println 'finish'

    }

    @TaskAction
    def generator(IncrementalTaskInputs inputs) throws Exception {
        if (!outputDir.exists())
            outputDir.mkdirs()

        configMapFile = new File(outputDir, CONFIG_MAP)
        configMapFile.absolutePath
        inputs.outOfDate { change ->
            if (!change.file.name.equals(CONFIG_MAP)) {
                invokeGenerator(change.file.path)
                recordFilePackageName()
            }
        }
        inputs.removed { change ->
            deleteFile(change.file.name)
        }
    }

    def void recordFilePackageName() {
        if (!configMapFile.exists()) {
            configMapFile.createNewFile()
        }
        for (File file : inputDir.listFiles())
            file.eachLine {
                line ->
                    if (line.contains('namespace') && line.contains('java')) {
                        String packageName = line.substring(line.indexOf('java') + 4).trim()
                        if (!configMapFile.text.contains(packageName))
                            configMapFile.append(file.name + CONFIG_MAP_SIGN + packageName + '\n')
                    }

            };
    }

    def void deleteFile(String name) {
        String packageName = getPackageName(name.subSequence(0, name.indexOf('.')))
        if (null == packageName) {
            return
        }
        def File targetFile = new File(outputDir, packageName.replace('.', '/'))
        if (!targetFile.parentFile.deleteDir()) {
            println('delete file encounter error')
        }
        targetFile.delete()
        StringBuilder mapBuilder = new StringBuilder()
        configMapFile.eachLine {
            line ->
                if (!line.contains(packageName)) {
                    mapBuilder.append(line + '\n')
                }
        }
        configMapFile.text = mapBuilder.toString()
    }

    def String getPackageName(String name) {
        if (!configMapFile.exists()) {
            recordFilePackageName();
        }
        String pachageName
        configMapFile.eachLine {
            line ->
                if (line.contains(name)) {
                    pachageName = line.split(CONFIG_MAP_SIGN)[1]
                }
        }
        pachageName
    }

    def invokeGenerator(String thriftFilePath) throws Exception {
        ArrayList<String> thriftArgslist = new ArrayList<String>()
        thriftArgslist.add(thriftFilePath)
        thriftArgslist.add('--output')
        thriftArgslist.add(outputDir.path)
        if (rxStyle)
            thriftArgslist.add('--rx');
        if (android)
            thriftArgslist.add('--android');
        try {
            Main$.MODULE$.main(thriftArgslist as String[]);
        } catch (Exception e) {
            throw e
        }
    }

    @InputDirectory
    File getInputDir() {
        return inputDir
    }

    @OutputDirectory
    File getOutputDir() {
        return outputDir
    }

    @InputDirectory
    void setInputDir(File inputDir) {
        this.inputDir = inputDir
    }

    @OutputDirectory
    void setOutputDir(File outputDir) {
        this.outputDir = outputDir
    }
    @Input
    boolean getAndroid() {
        return android
    }
    @Input
    boolean getRxStyle() {
        return rxStyle
    }
}
