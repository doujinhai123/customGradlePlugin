package com.iqiyi.paopao.methodtime.gradle

import com.android.build.api.transform.*
import com.android.build.api.transform.Context
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.apache.commons.codec.digest.*

/**
 * Created by LiYong on 2018/9/19
 *
 * Email:liyong@qiyi.com / lee131483@gmail.com
 */
class MethodTimeTransform extends Transform {

    Project project

    MethodTimeTransform(Project project) {
        this.project = project
    }

    /**
     * transform的名称
     * transformClassesWithMyClassTransformForDebug 运行时的名字
     * transformClassesWith + getName() + For + Debug或Release
     *
     * @return String
     */
    @Override
    String getName() {
        return "methodTime"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
     *
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指Transform要操作内容的范围，官方文档Scope有7种类型：
     * EXTERNAL_LIBRARIES        只有外部库
     * PROJECT                   只有项目内容
     * PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * PROVIDED_ONLY             只提供本地或远程依赖项
     * SUB_PROJECTS              只有子项目。
     * SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * TESTED_CODE               由当前变量(包括依赖项)测试的代码
     *
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 指明当前Transform是否支持增量编译
     * If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * Transform中的核心方法
     * transformInvocation.getInputs() 中是传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式。
     * transformInvocation.getOutputProvider() 获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译会报错
     *
     * @param transformInvocation
     * @throws TransformException
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs
                   , Collection<TransformInput> referencedInputs
                   , TransformOutputProvider outputProvider
                   , boolean isIncremental) throws IOException, TransformException, InterruptedException {

        project.logger.error("start ${MethodTimePlugin.EXT_NAME} transform...")

        long time = System.currentTimeMillis()

        if (!isIncremental) {
            outputProvider.deleteAll()
        }


        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.jarInputs) {
                String destName = jarInput.name
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                File src = jarInput.file
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                MethodTimeProcessor.processJar(src)
                FileUtils.copyFile(src, dest)
                project.logger.error "Copying\t${src.absolutePath} \nto\t\t${dest.absolutePath}"
            }

            for (DirectoryInput directoryInput : input.directoryInputs) {
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                directoryInput.file.eachFileRecurse { File file ->
                    MethodTimeProcessor.processClass(file,root)
                }
                FileUtils.copyDirectory(directoryInput.file, dest)
                project.logger.error "Copying\t${directoryInput.file.absolutePath} \nto\t\t${dest.absolutePath}"
            }
        }

        long endTime = System.currentTimeMillis()
        project.logger.error("${MethodTimePlugin.EXT_NAME} plugin inject  code cost time: " + (endTime - time) + " ms")
        project.logger.error("end ${MethodTimePlugin.EXT_NAME} transform...")

    }

}
