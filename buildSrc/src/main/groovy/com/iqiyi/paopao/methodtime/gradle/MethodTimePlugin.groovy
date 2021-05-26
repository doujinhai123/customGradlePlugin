package com.iqiyi.paopao.methodtime.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin;
import org.gradle.api.Project


/**
 * 统计方法执行时间的gradle插件
 */
class MethodTimePlugin implements Plugin<Project> {
    public static final String EXT_NAME = 'methodTime'

    @Override
    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            project.logger.error('project(' + project.name + ') apply method-time plugin')
            project.extensions.create(EXT_NAME, PluginExtension)
            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new MethodTimeTransform(project)
            android.registerTransform(transformImpl)
            project.afterEvaluate {
                ExtensionHelper.initExtensionInfo(project)
            }
        }
    }


}
