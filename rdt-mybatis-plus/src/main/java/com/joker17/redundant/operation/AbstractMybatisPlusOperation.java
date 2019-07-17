package com.joker17.redundant.operation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.model.*;

import java.util.*;

public abstract class AbstractMybatisPlusOperation extends AbstractOperation {

    public AbstractMybatisPlusOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    /**
     * 获取当前实体类所对应的mapper
     * @param entityClass
     * @param <T>
     * @return
     */
    protected abstract <T> BaseMapper<T> getEntityMapperExecute(Class<T> entityClass);


    public <T> BaseMapper<T> getEntityMapper(Class<T> entityClass) {
        BaseMapper<T> mapper = getEntityMapperExecute(entityClass);
        if (mapper == null) {
            throw new IllegalArgumentException(String.format("%s mapper must be not null", entityClass.getName()));
        }
        return mapper;
    }

    @Override
    protected <T> T saveExecute(T entity, Class<T> entityClass) {
        throw new IllegalStateException("not support save");
    }

    @Override
    protected <T> Collection<T> saveAllExecute(Collection<T> collection, Class<T> entityClass) {
        throw new IllegalStateException("not support saveAll");
    }

    protected String getColumn(Class entityClass, String property) {
        return property;
    }

    @Override
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {
        final Class modifyClass = modifyClassModel.getCurrentClass();
        BaseMapper mapper = getEntityMapperExecute(modifyClass);

        Object modifyModel = rdtResolver.newInstance(modifyClass);

        final UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                updateWrapper.set(getColumn(modifyClass, property), targetPropertyVal);
            }
        });

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                updateWrapper.eq(getColumn(modifyClass, property), targetPropertyVal);

            }
        });

        configuration.doLogicalModelHandle(modifyClassModel, properties.getUpdateMultiWithLogical(), new RdtConfiguration.LogicalModelCallBack() {
            @Override
            public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                String property = logicalModel.getColumn().getProperty();
                List<Object> values = logicalModel.getValues();
                boolean batch = values.size() != 1;
                if (batch) {
                    updateWrapper.in(getColumn(modifyClass, property), values);
                } else {
                    updateWrapper.eq(getColumn(modifyClass, property), values.get(0));
                }

            }
        });

        mapper.update(modifyModel, updateWrapper);
    }

    @Override
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Column relyColumn, int group, ModifyRelyDescribe describe) {
        final Class modifyClass = modifyClassModel.getCurrentClass();
        BaseMapper mapper = getEntityMapperExecute(modifyClass);

        Object modifyModel = rdtResolver.newInstance(modifyClass);

        final UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                updateWrapper.set(getColumn(modifyClass, property), targetPropertyVal);
            }
        });

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                updateWrapper.eq(getColumn(modifyClass, property), targetPropertyVal);

            }
        });

        final String relyProperty = relyColumn.getProperty();

        configuration.matchedTypeHandle(describe, new RdtConfiguration.MatchedTypeCallback() {
            @Override
            public void in(List<Object> inValList) {
                boolean batch = inValList.size() != 1;
                if (batch) {
                    updateWrapper.in(getColumn(modifyClass, relyProperty), inValList);
                } else {
                    updateWrapper.eq(getColumn(modifyClass, relyProperty), inValList.get(0));
                }
            }

            @Override
            public void or(List<Object> inValList, List<Object> notInValList) {
                final boolean inIsBatch = inValList.size() != 1;
                boolean notIsBatch = notInValList.size() != 1;
                updateWrapper.nested(
                        it -> inIsBatch ? it.in(getColumn(modifyClass, relyProperty), inValList) : it.eq(getColumn(modifyClass, relyProperty), inValList.get(0))
                                .or()
                                .notIn(notIsBatch, getColumn(modifyClass, relyProperty), notInValList)
                                .ne(!notIsBatch, getColumn(modifyClass, relyProperty), notInValList.get(0))
                );
            }

            @Override
            public void notIn(List<Object> notInValList) {
                boolean batch = notInValList.size() != 1;
                if (batch) {
                    updateWrapper.notIn(getColumn(modifyClass, relyProperty), notInValList);
                } else {
                    updateWrapper.ne(getColumn(modifyClass, relyProperty), notInValList.get(0));
                }
            }
        }, true);

        configuration.doLogicalModelHandle(modifyClassModel, properties.getUpdateMultiWithLogical(), new RdtConfiguration.LogicalModelCallBack() {
            @Override
            public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                String property = logicalModel.getColumn().getProperty();
                List<Object> values = logicalModel.getValues();
                boolean batch = values.size() != 1;
                if (batch) {
                    updateWrapper.in(getColumn(modifyClass, property), values);
                } else {
                    updateWrapper.eq(getColumn(modifyClass, property), values.get(0));
                }

            }
        });
        mapper.update(modifyModel, updateWrapper);
    }

    @Override
    protected <T> List<T> findByConditionsExecute(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        BaseMapper mapper = getEntityMapperExecute(entityClass);
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        if (conditionPropertys != null) {
            for (int i = 0; i < conditionPropertys.size(); i++) {
                String property = conditionPropertys.get(i);
                Object conditionValue = conditionValues.get(i);
                if (conditionValue instanceof Collection) {
                    queryWrapper.in(getColumn(entityClass, property), (Collection)conditionValue);
                } else {
                    queryWrapper.eq(getColumn(entityClass, property), conditionValue);
                }
            }
        }
        return mapper.selectList(queryWrapper);
    }
}
