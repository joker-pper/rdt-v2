package com.devloper.joker.redundant.utils;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class PackageClassUtilsTest {

    private String userDir = System.getProperty("user.dir");
    private String jarFilePath = userDir + "/target/" + "rdt-core-1.0.jar";

    @Test
    public void getClasses() throws Exception {
        System.out.println(PackageClassUtils.getClasses("*"));
        System.out.println(PackageClassUtils.getClasses("com.devloper.joker.redundant.utils"));
    }

    @Test
    public void getPackageNamePaths() {
        System.out.println(PackageClassUtils.getPackageNamePaths(userDir,"*"));
    }

    @Test
    public void getPackageNamePathsByJar() {
        List<String> results = PackageClassUtils.getPackageNamePathsByJar(jarFilePath, "com");
        System.out.println(results);

        results = PackageClassUtils.getPackageNamePathsByJar(jarFilePath, "*");
        System.out.println(results);
    }

    @Test
    public void getJarClassNames() {
        List<String> classList = PackageClassUtils.getJarClassNames(jarFilePath, "com");
        System.out.println(classList);
    }
}