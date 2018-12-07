package com.devloper.joker.rdt_jpa_test.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JsonUtils implements CommandLineRunner {

    @Resource
    private FastJsonConfig fastJsonConfig;

    private static FastJsonConfig globalFastJsonConfig;

    @Override
    public void run(String... args) throws Exception {
        globalFastJsonConfig = fastJsonConfig;
    }

    public static String toJson(Object o) {
        FastJsonConfig fastJsonConfig = globalFastJsonConfig;
        if (fastJsonConfig == null) {
            return JSON.toJSONString(o);
        }
        SerializeFilter[] globalFilters = fastJsonConfig.getSerializeFilters();
        List<SerializeFilter> allFilters = new ArrayList(Arrays.asList(globalFilters));
        return JSON.toJSONString(o, fastJsonConfig.getSerializeConfig(), allFilters.toArray(new SerializeFilter[allFilters.size()]), fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, fastJsonConfig.getSerializerFeatures());
    }
}
