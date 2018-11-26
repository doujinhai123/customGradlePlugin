package com.iqiyi.paopao.methodtime.gradle

import org.gradle.api.Project

import java.util.regex.Pattern


/**
 * Created by LiYong on 2018/9/19
 *
 * Email:liyong@qiyi.com / lee131483@gmail.com
 *
 */
class ExtensionHelper {

    static void initExtensionInfo(Project project) {
        PluginExtension extension = project.extensions.findByName(MethodTimePlugin.EXT_NAME) as PluginExtension
        project.logger.error(MethodTimePlugin.EXT_NAME + ' configuration info is:\n' + extension.toString())
        if (extension == null)
            extension = new PluginExtension(project)
        if (extension.scanInclude == null)
            extension.scanInclude = new ArrayList<>()
        if (extension.scanExclude == null)
            extension.scanExclude = new ArrayList<>()
        if (extension.autoInjectClass)
            extension.autoInjectClass = extension.autoInjectClass.replaceAll('\\.', '/').intern()
        if (!extension.scanExclude.contains(extension.autoInjectClass))
            extension.scanExclude.add(extension.autoInjectClass)
        PluginExtension.DEFAULT_EXCLUDE.each { exclude ->
            if (!extension.scanExclude.contains(exclude))
                extension.scanExclude.add(exclude)
        }
        initPattern(extension.scanInclude, extension.includePatterns)
        initPattern(extension.scanExclude, extension.excludePatterns)
        MethodTimeProcessor.extension = extension
    }

    private static void initPattern(ArrayList<String> list, ArrayList<Pattern> patterns) {
        list.each { s ->
            patterns.add(Pattern.compile(s))
        }
    }
}