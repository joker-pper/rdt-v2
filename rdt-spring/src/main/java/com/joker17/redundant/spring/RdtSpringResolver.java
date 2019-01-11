package com.joker17.redundant.spring;

import com.joker17.redundant.core.RdtResolver;
import com.joker17.redundant.utils.PackageClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class RdtSpringResolver extends RdtResolver implements BeanDefinitionRegistryPostProcessor/*, InitializingBean, ApplicationContextAware*/ {

    @javax.annotation.Resource
    private ResourcePatternResolver resourcePatternResolver;

    @javax.annotation.Resource
    private MetadataReaderFactory metadataReaderFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //https://blog.csdn.net/qq_20597727/article/details/82713306
        //https://blog.csdn.net/flashflight/article/details/43464383
        /*
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.scan() */

    }

    @Override
    public List<Class> getClasses(String packageName) {
        String[] basePackages = StringUtils.tokenizeToStringArray(packageName, ",; \t\n");
        List<String> classNames = new ArrayList<String>();

        for (String basePackage : basePackages) {
            String packageSearchPath = "classpath*:" + basePackage.replace(".", "/") + '/' + "**/*.class";
            try {
                Resource[] resources = this.getResourcePatternResolver().getResources(packageSearchPath);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        try {
                            MetadataReader metadataReader = this.getMetadataReaderFactory().getMetadataReader(resource);
                            ClassMetadata classMetadata = metadataReader.getClassMetadata();
                            if (classMetadata != null) {
                                classNames.add(classMetadata.getClassName());
                            }
                        } catch (Throwable throwable) {
                            throw new BeanDefinitionStoreException("Failed to read class: " + resource, throwable);
                        }
                    } else if (logger.isTraceEnabled()) {
                        logger.trace("Ignored because not readable: " + resource);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O failure during classpath scanning", e);
            }
        }
        return PackageClassUtils.classNamesToClassList(classNames);
    }

    protected ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }

    protected MetadataReaderFactory getMetadataReaderFactory() {
        if (this.metadataReaderFactory == null) {
            this.metadataReaderFactory = new CachingMetadataReaderFactory();
        }
        return this.metadataReaderFactory;
    }

}
