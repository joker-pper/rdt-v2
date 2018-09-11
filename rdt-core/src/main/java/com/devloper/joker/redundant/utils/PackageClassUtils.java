package com.devloper.joker.redundant.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//用于获取当前包路径下的class工具类
public class PackageClassUtils {

    private static final Logger log = LoggerFactory.getLogger(PackageClassUtils.class);
    private static final boolean BY_JAR = isRunByJar();
    private static final String separator = File.separator;

    //获取该包下的所有class
    public static List<Class> getClasses(String packageName) {
        Set<Class> classSet = new LinkedHashSet<Class>();
        Set<String> packageNames = new HashSet<String>();//最终匹配到的全部的包

        Set<URL> urls = getClassPathUrl();
        for (URL url : urls) {
            String urlPath = url.getPath();
            String jarPath = getJarPath(urlPath);
            List<String> currents;
            if (jarPath != null) {
                currents = getPackageNamesByJar(jarPath, packageName);
            } else {
                currents = getPackageNamePaths(urlPath, packageName);
            }
            if (!currents.isEmpty()) {
                for (String current : currents) {
                    packageNames.add(current.replace(urlPath, "").replace("/", "."));
                }
            }
        }
        for (URL url : urls) {
            String urlPath = url.getPath();
            for (String val : packageNames) {
                classSet.addAll(getClassesHandle(urlPath, val));
            }
        }
        if (log.isDebugEnabled()) {
            if (packageName.contains("*")) {
                log.debug("read package({}-{}) all class: {}", packageName, packageNames, classSet);
            } else {
                log.debug("read package({}) all class: {}", packageName, classSet);
            }
        }
        return new ArrayList<Class>(classSet);
    }


    /**
     * 通过包名匹配到对应的路径
     *
     * @param jarPath     jar文件所在路径
     * @param packageName e.g ["", com.devloper.*.*.annotation]
     * @return 只包含文件夹名称 e.g com/devloper
     */
    public static List<String> getPackageNamePathsByJar(String jarPath, String packageName) {
        return getPackageResultByJar(jarPath, packageName, true);
    }

    /**
     * 通过包名匹配到对应的包名
     * @param jarPath
     * @param packageName
     * @return
     */
    public static List<String> getPackageNamesByJar(String jarPath, String packageName) {
        return getPackageResultByJar(jarPath, packageName, false);
    }

    /**
     *
     * @param jarPath
     * @param packageName
     * @param isPath 获取path/packageName
     * @return
     */
    public static List<String> getPackageResultByJar(String jarPath, String packageName, boolean isPath) {
        List<String> result = new ArrayList<String>();
        if (jarPath != null) {
            try {
                if (!isPath) {
                    if (packageName.equals("") || packageName.equals("*"))
                        throw new IllegalArgumentException("get jar package names should use explicit directory");
                }

                JarFile jarFile = new JarFile(jarPath);
                Enumeration entrys = jarFile.entries();
                while (entrys.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) entrys.nextElement();
                    if (!jarEntry.isDirectory()) continue;

                    String entryName = jarEntry.getName();
                    entryName = entryName.substring(0, entryName.length() - 1);
                    String current = entryName.replace("/", ".");

                    String regex = ".*"; //.* or .*com.devloper.*.service
                    if (packageName != null && !packageName.trim().equals("") && !packageName.trim().equals("*")) {
                        regex += packageName;
                        //移除末尾的.*
                        StringBuilder sb = new StringBuilder(regex);
                        for (int len = sb.length(); len > 2 && sb.lastIndexOf(".*") == len - 2; ) {
                            sb.delete(len - 2, len);
                        }
                        regex = sb.toString();
                    }
                    if (current.matches(regex)) {
                        boolean flag = true;
                        for (String val : result) {
                            if (entryName.startsWith(val)) {
                                flag = false;
                                break;
                            }

                        }
                        if (flag) {
                            if (isPath) result.add(entryName);
                            else {
                                String temp = null;
                                if (regex.startsWith(".*")) temp = regex.substring(2);
                                int index = temp.indexOf(".*");
                                if (index > -1) temp = temp.substring(0, index);
                                String currentPackageName = entryName.replace(separator, ".");
                                result.add(currentPackageName.substring(currentPackageName.indexOf(temp)));
                            }

                        }
                    }
                }
            } catch (IOException e) {
            }
        }
        return result;
    }


    /**
     * 通过包名匹配到对应的路径
     *
     * @param path
     * @param packageName e.g ["",com.devloper.*.*.annotation]
     * @return 文件夹所在的具体路径 e.g xx/target/classes/com/devloper
     */
    public static List<String> getPackageNamePaths(String path, String packageName) {
        List<String> result = new ArrayList<String>();
        if (path != null) {
            File file = new File(path);
            if (file != null && file.isDirectory()) {
                for (File currFile : file.listFiles()) {
                    if (!currFile.isDirectory()) continue;
                    String currentPath = currFile.getPath();
                    String current = currentPath.replace("/", ".");
                    String regex = ".*";
                    if (packageName != null && !packageName.trim().equals("") && !packageName.trim().equals("*")) {
                        regex += packageName;
                        //移除末尾的.*
                        StringBuilder sb = new StringBuilder(regex);
                        for (int len = sb.length(); len > 2 && sb.lastIndexOf(".*") == len - 2; ) {
                            sb.delete(len - 2, len);
                        }
                        regex = sb.toString();
                    }
                    boolean flag = regex.equals(".*");
                    if (!flag) {
                        if (current.matches(regex)) {
                            //比较后缀是否一致
                            String suffix = current.substring(current.lastIndexOf(".") + 1);  //当前后缀
                            int index = regex.lastIndexOf(".");
                            if (index == 0) index = 1;
                            flag = suffix.equals(regex.substring(index + 1));
                        }
                    }
                    if (!flag) result.addAll(getPackageNamePaths(currentPath, packageName));
                    else result.add(currentPath);
                }
            }
        }
        return result;
    }

    private static String getJarPath(String path) {
        String jarFilePath = null;
        if (path != null && path.contains("!") && BY_JAR) {
            String[] allFileNames = path.split("!");
            if (allFileNames.length >= 2) {
                jarFilePath = allFileNames[0].substring(allFileNames[0].indexOf(File.separator));
            }
        }
        return jarFilePath;
    }

    /**
     * 获取当前class路径下的packageName的class集合
     *
     * @param classPath
     * @param packageName
     * @return
     */
    private static List<Class> getClassesHandle(String classPath, String packageName) {
        if (classPath == null || "".equals(classPath)) {
            throw new IllegalArgumentException("class path can't blank");
        }
        List<Class> classes = new ArrayList<Class>();
        String packageDirName;
        if (packageName == null || "".equals(packageName.trim())) {
            packageName = null;
            packageDirName = "";
        } else {
            packageName = packageName.trim();
            packageDirName = packageName.replace(".", separator);
        }
        String path = classPath + packageDirName;
        try {
            String jarFilePath = getJarPath(path);
            if (jarFilePath != null) {
                List<String> classNames = getJarClassNames(jarFilePath, packageName);
                for (String className : classNames) {
                    try {
                        classes.add(Class.forName(className));
                    } catch (Exception e) {
                        log.debug(className + " not found");
                    }
                }
            } else {
                File file = new File(path);
                String[] allFileNames;
                String currentFileName;

                allFileNames = file.list();
                if (allFileNames != null) {
                    int size = allFileNames.length;
                    for (int i = 0; i < size; ++i) {
                        currentFileName = allFileNames[i];
                        File currentFile = new File(file, currentFileName);
                        if (currentFile.isDirectory()) {
                            String currPath;
                            if (packageName != null) {
                                currPath = packageName + "." + currentFileName;
                            } else {
                                currPath = currentFileName;
                            }
                            List<Class> currentClasses = getClassesHandle(classPath, currPath);
                            if (currentClasses != null && !currentClasses.isEmpty()) {
                                classes.addAll(currentClasses);
                            }
                        } else if (currentFile.isFile() && currentFileName.endsWith(".class")) {
                            String className = packageName + "." + currentFileName.substring(0, currentFileName.length() - 6);
                            try {
                                classes.add(Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                log.debug(className + " not found");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return classes;
    }


    /**
     * 获取jar文件中指定包名的所有类名
     * @param jarPath
     * @param packageName 具体包名 e.g com.devloper
     * @return
     */
    public static List<String> getJarClassNames(String jarPath, String packageName) {
        List<String> classNames = new ArrayList<String>();
        try {
            if (jarPath != null) {
                JarFile jarFile = new JarFile(jarPath);
                Enumeration entrys = jarFile.entries();
                String packageDirectory = packageName.replace(".", separator);
                while (entrys.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) entrys.nextElement();
                    String entryName = jarEntry.getName();

                    if (entryName.contains(packageDirectory) && entryName.endsWith(".class")) {
                        String className = entryName.substring(entryName.indexOf(packageDirectory)).replace(".class", "").replace(separator, ".");
                        classNames.add(className);
                    }
                }

            }
        } catch (Exception e) {
        }
        return classNames;
    }

    //获取当前所运行的环境路径
    public static Set<URL> getClassPathUrl() {
        Set<URL> urls = new LinkedHashSet<URL>();
        String path = "";
        ClassLoader cl = PackageClassUtils.class.getClassLoader();
        try {
            Enumeration resourceUrls = cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path);
            while (resourceUrls.hasMoreElements()) {  //将会分别找到对应的class 路径
                URL url = (URL) resourceUrls.nextElement();
                urls.add(url);
            }
        } catch (IOException e) {
        }
        return urls;
    }

    public static boolean isRunByJar() {
        Class currentClass = PackageClassUtils.class;
        URL url = currentClass.getResource(currentClass.getSimpleName() + ".class");
        //return "jar".equals(url.getProtocol());
        return url.toString().startsWith("jar:file:/");
    }

}
