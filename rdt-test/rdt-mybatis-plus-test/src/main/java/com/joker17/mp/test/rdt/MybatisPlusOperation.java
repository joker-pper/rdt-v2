package com.joker17.mp.test.rdt;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.operation.AbstractMybatisPlusOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class MybatisPlusOperation extends AbstractMybatisPlusOperation {

    @Resource
    private ApplicationContext applicationContext;

    public MybatisPlusOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Map<?, BaseMapper> getEntityMapperMap() {
        return applicationContext.getBeansOfType(BaseMapper.class);
    }

}
