package com.iqiyi.paopao.methodtime.gradle

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*
import org.objectweb.asm.commons.LocalVariablesSorter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Pattern
import java.util.zip.ZipEntry

/**
 * Created by LiYong on 2018/9/19
 *
 * Email:liyong@qiyi.com / lee131483@gmail.com
 */
class MethodTimeProcessor {
    static PluginExtension extension

    static void processJar(File jarFile) {
        if (!jarFile || !shouldProcessPreDexJar(jarFile.absolutePath))
            return
        def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
        if (optJar.exists())
            optJar.delete()
        def file = new JarFile(jarFile)
        Enumeration enumeration = file.entries()
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            ZipEntry zipEntry = new ZipEntry(entryName)
            InputStream inputStream = file.getInputStream(jarEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (shouldProcessClass(entryName)) {
                println('entryName:' + entryName)
                def bytes = injectCode(inputStream)
                jarOutputStream.write(bytes)
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()

        if (jarFile.exists()) {
            jarFile.delete()
        }
        optJar.renameTo(jarFile)
    }

    static boolean shouldProcessPreDexJar(String path) {
        if (!extension || !extension.autoInjectClass )
            return false
        return path.endsWith("classes.jar") && !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    static boolean shouldProcessClass(String entryName) {
        if (entryName == null || !entryName.endsWith(".class") || entryName.startsWith("android/support"))
            return false
        entryName = entryName.substring(0, entryName.lastIndexOf('.'))
        if (extension != null) {
            def list = extension.includePatterns
            if (list) {
                def exlist = extension.excludePatterns
                Pattern pattern, p
                for (int i = 0; i < list.size(); i++) {
                    pattern = list.get(i)
                    if (pattern.matcher(entryName).matches()) {
                        if (exlist) {
                            for (int j = 0; j < exlist.size(); j++) {
                                p = exlist.get(j)
                                if (p.matcher(entryName).matches())
                                    return false
                            }
                        }
                        return true
                    }
                }
            } else {
                def exlist = extension.excludePatterns
                if (exlist) {
                    Pattern p
                    for (int j = 0; j < exlist.size(); j++) {
                        p = exlist.get(j)
                        if (p.matcher(entryName).matches())
                            return false
                    }
                }
                return true
            }
        }
        return false
    }

    static boolean shouldProcessMethod(String name) {
        if ("<initExtensionInfo>".equals(name) || "<clinit>".equals(name) || name.startsWith('access$') || "<init>".equals(name))
            return false
        if (name.startsWith("get") || name.startsWith("set"))
            return false
        return true
    }

    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    static void processClass(File file, String root) {
        if (!file || !file.isFile())
            return

        def path = file.absolutePath.replace(root, '')
        def shouldProcessClass = shouldProcessClass(path)
        if(!shouldProcessClass)
            return

        def optClass = new File(file.getParent(), file.name + ".opt")
        FileInputStream inputStream = new FileInputStream(file);
        FileOutputStream outputStream = new FileOutputStream(optClass)

        def bytes = injectCode(inputStream)
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
    }

    private static byte[] injectCode(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    static class ScanClassVisitor extends ClassVisitor {
        String className;
        String superName;
        String[] interfaces;

        public ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (shouldProcessMethod(name)) {
                println('insert method:' + name + desc)
                mv = new InsertMethodVisitor(Opcodes.ASM5, mv, access, name, desc, signature, exceptions, className);
            }
            return mv;
        }
    }

    static class InsertMethodVisitor extends LocalVariablesSorter {
        int access;
        String name, desc, signature, className;
        String[] exceptions;
        int aopVar;

        public InsertMethodVisitor(final int api, final MethodVisitor mv
                                   , int access, String name, String desc, String signature, String[] exceptions, String className) {
            super(api, access, desc, mv);
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
            this.className = className;
        }

        @Override
        void visitCode() {
            super.visitCode()
            if (extension.autoInjectClass && extension.autoInjectMethodStart) {
                mv.visitLdcInsn(className);//类名
                mv.visitLdcInsn(name)//方法名
                mv.visitLdcInsn(desc)//参数列表及返回值类型
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, extension.autoInjectClass, extension.autoInjectMethodStart, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)L${extension.autoInjectClass};", false);
                aopVar = newLocal(Type.getObjectType(extension.autoInjectClass))
                mv.visitVarInsn(Opcodes.ASTORE, aopVar)
            }
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                    || opcode == Opcodes.ATHROW) {
                if (extension.autoInjectClass && extension.autoInjectMethodEnd) {
                    //在返回之前插入代码。
                    if (aopVar >= 0) {
                        mv.visitVarInsn(Opcodes.ALOAD, aopVar)
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, extension.autoInjectClass, extension.autoInjectMethodEnd, "(L${extension.autoInjectClass};)V", false);
                    } else {
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, extension.autoInjectClass, extension.autoInjectMethodEnd, "()V", false);
                    }
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }
}