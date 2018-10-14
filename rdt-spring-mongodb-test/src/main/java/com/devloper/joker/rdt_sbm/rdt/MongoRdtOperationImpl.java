package com.devloper.joker.rdt_sbm.rdt;

import com.devloper.joker.redundant.model.RdtSupport;
import com.devloper.joker.redundant.operation.MongoRdtOperation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public class MongoRdtOperationImpl extends MongoRdtOperation {

    public MongoRdtOperationImpl(RdtSupport rdtSupport) {
        super(rdtSupport);
    }

    @Override
    protected Pageable getPageable(long page, long size) {
        return PageRequest.of((int) page, (int)size);
    }

    /**
     * 没有用到,根据需要进行实现
     * @param o
     * @return
     */
    @Override
    public Object save(Object o) {
        return null;
    }

    /**
     * saveAll配置为true时保存子文档数据
     * @param o
     * @return
     */
    @Override
    public Object saveAll(Collection<Object> o) {
         for (Object current: o) {
             mongoTemplate.save(current);
         }
        return o;
    }


}
