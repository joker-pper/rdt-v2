package com.devloper.joker.redundant.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RdtConfigurationBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RdtConfigurationBuilder.class);

    public static RdtConfiguration build(RdtProperties properties, RdtResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("rdt resolver must be not null.");
        }

        if (properties == null) {
            throw new IllegalArgumentException("rdt properties must be not null.");
        }

        RdtPropertiesBuilder propertiesBuilder = new RdtPropertiesBuilder(resolver, properties);

        String basePackage = properties.getBasePackage();
        //获取该包下的所有类文件
        List<Class> classList = resolver.getClasses(basePackage);
        List<Class> packageClassList = properties.getPackageClassList();
        if (classList != null) {
            packageClassList.addAll(classList);
        }

        for (Class currentClass : packageClassList) {
            propertiesBuilder.builderClass(currentClass);
        }

        logger.debug("rdt load package class complete, package class size {}, extra class size {}.", packageClassList.size(), properties.getExtraClassSet().size());
        return new RdtConfiguration(properties, propertiesBuilder, resolver);
    }
}
