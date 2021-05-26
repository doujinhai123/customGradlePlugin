package com.iqiyi.paopao.methodtime.gradle

import org.gradle.api.Project

import java.util.regex.Pattern

/**
 *
 */
class PluginExtension {
    static final DEFAULT_EXCLUDE = [
            '.*/R(\\\$)?.*'
            , '.*/BuildConfig$'
    ]
    /**
     * 自动注入类
     */
    String autoInjectClass = ''
    /**
     * 自动注入类的方法名，该方法会被注入到被注入类的方法开始处
     */
    String autoInjectMethodStart = ''
    /**
     * 自动注入类的方法名，该方法会被注入到被注入类的方法结束处，在return之前。
     */
    String autoInjectMethodEnd = ''
    /**
     * 扫描类的范围，默认不配置就是全部class待注入
     */
    ArrayList<String> scanInclude = []
    /**
     * 扫描类的黑名单，如果exclude中有该类，扫描的时候会被过滤掉，不会注入code
     */
    ArrayList<String> scanExclude = []
    ArrayList<Pattern> includePatterns = []
    ArrayList<Pattern> excludePatterns = []

    public PluginExtension() {}

    public PluginExtension(Project project) {
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder(MethodTimePlugin.EXT_NAME)
        sb.append(' {')
        sb.append('\n\t').append('autoInjectClass').append(' = \'').append(autoInjectClass).append('\'')
        sb.append('\n\t').append('autoInjectMethodStart').append(' = \'').append(autoInjectMethodStart).append('\'')
        sb.append('\n\t').append('autoInjectMethodEnd').append(' = \'').append(autoInjectMethodEnd).append('\'')
        sb.append('\n\t').append('scanInclude').append(' = ').append('[')
        scanInclude.each { i ->
            sb.append('\n\t\t\'').append(i).append('\'')
        }
        sb.append('\n\t]')
        sb.append('\n\t').append('scanExclude').append(' = ').append('[')
        scanExclude.each { i ->
            sb.append('\n\t\t\'').append(i).append('\'')
        }
        sb.append('\n\t]\n}')
        return sb.toString()
    }
}