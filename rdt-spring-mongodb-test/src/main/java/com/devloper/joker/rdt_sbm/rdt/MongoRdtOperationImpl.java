package com.devloper.joker.rdt_sbm.rdt;

import com.devloper.joker.redundant.operation.MongoRdtOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;
import java.util.List;

public class MongoRdtOperationImpl extends MongoRdtOperation {

    @Override
    protected Pageable getPageable(long page, long size) {
        return null;
    }

    @Override
    public <T> Collection<T> findByIdIn(Class<T> entityClass, String idKey, Collection<Object> ids) {
        Query query = new Query(Criteria.where(idKey).in(ids));
        List<T> list = mongoTemplate.find(query, entityClass);
        return list;
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
