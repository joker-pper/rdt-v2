package com.joker17.redundant.utils;

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
    public static List<Class> getClassList(String packageName) {
        Set<Class> classSet = new LinkedHashSet<Class>();
        Set<String> packageNames = new HashSet<String>();//最终匹配到的全部的包
        Set<URL> urls = getClassPathUrl();
        Set<String> jarPaths = new HashSet<String>();
        Set<String> filePaths = new HashSet<String>();
        for (URL url : urls) {
            String urlPath = url.getPath();
            String jarPath = getJarPath(url);
            List<String> currents;
            if (jarPath != null) {
                currents = getPackageNamesByJar(jarPath, packageName);
            } else {
                currents = getPackageNamePaths(urlPath, packageName);
            }
            if (!currents.isEmpty()) {
                if (jarPath != null) {
                    jarPaths.add(jarPath);
                } else {
                    filePaths.add(urlPath);
                }
                for (String current : currents) {
                    if (separator.equals("\\") && urlPath.startsWith("/")) {
                        //windows
                        urlPath = urlPath.substring(1).replace("/", separator);
                    }
                    String actualPackageName = current.replace(urlPath, "").replace(separator, ".");
                    packageNames.add(actualPackageName);
                }
            }
        }
        for (String actualPackageName : packageNames) {
            for (String jarPath : jarPaths) {
                classSet.addAll(classNamesToClassList(getJarClassNames(jarPath, actualPackageName)));
            }
            for (String filePath : filePaths) {
                classSet.addAll(getClassList(filePath, actualPackageName));
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
     * 获取该路径下关于包名的全部class列表
     * @param filePath  文件夹路径
     * @param packageName 具体包名
     * @return
     */
    public  static List<Class> getClassList(String filePath, String packageName) {
        if (filePath == null || "".equals(filePath)) {
            throw new IllegalArgumentException("file path can't blank");
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
        if (!filePath.endsWith(separator)) {
            filePath += separator;
        }
        File file = new File(filePath + packageDirName);
        String currentFileName;
        String[] allFileNames = file.list();
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
                    List<Class> currentClasses = getClassList(filePath, currPath);
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
        return classes;
    }


    /**
     * 通过包名匹配到对应的路径
     *
     * @param jarPath     jar文件所在路径
     * @param packageName e.g ["", com.devloper.*.*.annotation]
     * @return 只包含文件夹名称 e.g com/devloper
     */
    public static List<String> getPackageNamePathsByJar(String jarPath, String packageName) {
        String[] packageNames = packageName.split(",", -1);
        Set<String> set = new LinkedHashSet<String>();
        for (String result : packageNames) {
            set.addAll(getPackageResultByJar(jarPath, result, true));
        }
        return new ArrayList<String>(set);
    }

    /**
     * 通过包名匹配到对应的包名
     * @param jarPath
     * @param packageName
     * @return
     */
    public static List<String> getPackageNamesByJar(String jarPath, String packageName) {
        String[] packageNames = packageName.split(",", -1);
        Set<String> set = new LinkedHashSet<String>();
        for (String result : packageNames) {
            set.addAll(getPackageResultByJar(jarPath, result, false));
        }
        return new ArrayList<String>(set);
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
                                String currentPackageName = entryName.replace("/", ".");
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
     * @param path 指定文件夹路径
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
                    String current = currentPath.replace(separator, ".");
                    String allRegex = ".*";

                    Set<String> regexSet = new LinkedHashSet<String>();
                    if (packageName != null && !packageName.trim().equals("") && !packageName.trim().equals("*")) {
                        String[] packageNames = packageName.split(",", -1);
                        for (String currentPackageName: packageNames) {
                            if ("".equals(currentPackageName)) continue;
                            if ("*".equals(currentPackageName)) {
                                regexSet.clear();
                                regexSet.add(allRegex);
                                break;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append(allRegex + currentPackageName);
                                //移除当前末尾的.*
                                for (int len = sb.length(); len > 2 && sb.lastIndexOf(allRegex) == len - 2; ) {
                                    sb.delete(len - 2, len);
                                }
                                regexSet.add(sb.toString());
                            }
                        }
                    } else {
                        regexSet.add(allRegex);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (String regex : regexSet) {
                        sb.append(regex + "|");
                    }
                    int len = sb.length();
                    sb.delete(len - 1, len);
                    String regex = sb.toString();

                    if (regex.equals(allRegex)) {
                        result.add(currentPath);
                    } else if (current.matches(regex)) {
                        result.add(currentPath);
                    } else {
                        result.addAll(getPackageNamePaths(currentPath, packageName));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取jar文件的具体路径
     * @param urlPath
     * @return
     */
    public static String getJarPath(String urlPath) {
        String jarFilePath = null;
        if (urlPath != null && urlPath.contains("!") && BY_JAR) {
            String[] allFileNames = urlPath.split("!");
            if (allFileNames.length >= 2) {
                String fileName = allFileNames[0];
                int index = fileName.indexOf("/");
                if (index >= 0) {
                    if (separator.equals("\\")) {
                        index ++;
                    }
                    jarFilePath = fileName.substring(index);
                }
            }
        }
        return jarFilePath;
    }

    public static String getJarPath(URL url) {
       return getJarPath(url.getPath());
    }

    public static List<Class> classNamesToClassList(Collection<String> classNames) {
        List<Class> classList = new ArrayList<Class>(16);
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    if (StringUtils.isEmpty(className)) {
                        continue;
                    }
                    classList.add(Class.forName(className));
                } catch (Exception e) {
                    log.debug(className + " not found");
                }
            }
        }
        return classList;
    }

    /**
     * 获取jar文件中指定包名的所有类名
     * @param jarPath jar所在具体路径
     * @param packageName 具体包名 e.g com.devloper
     * @return
     */
    public static List<String> getJarClassNames(String jarPath, String packageName) {
        List<String> classNames = new ArrayList<String>();
        try {
            if (jarPath != null) {
                JarFile jarFile = new JarFile(jarPath);
                Enumeration entrys = jarFile.entries();
                String packageDirectory = packageName.replace(".", "/");
                while (entrys.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) entrys.nextElement();
                    String entryName = jarEntry.getName();

                    if (entryName.contains(packageDirectory) && entryName.endsWith(".class")) {
                        String className = entryName.substring(entryName.indexOf(packageDirectory)).replace(".class", "").replace("/", ".");
                        classNames.add(className);
                    }
                }

            }
        } catch (Exception e) {
            log.warn("", e);
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
