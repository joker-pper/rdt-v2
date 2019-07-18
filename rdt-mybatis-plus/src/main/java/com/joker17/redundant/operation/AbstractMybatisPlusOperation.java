package com.joker17.redundant.operation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.model.*;

import java.lang.reflect.Proxy;
import java.util.*;

public abstract class AbstractMybatisPlusOperation extends AbstractOperation {

    public final Map<Class, BaseMapper> ENTITY_MAPPER_CACHE_MAP = new HashMap<>(256);

    public AbstractMybatisPlusOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    /**
     * 获取当前实体类的mapper
     * @param entityClass
     * @param <T>
     * @return
     */
    public <T> BaseMapper<T> getEntityMapper(Class<T> entityClass) {
        BaseMapper<T> mapper = getEntityMapperExecute(entityClass);
        if (mapper == null) {
            throw new IllegalArgumentException(String.format("%s mapper must be not null", entityClass.getName()));
        }
        return mapper;
    }


    /**
     * 获取当前所有的mapper对象
     * @return
     */
    protected abstract Map<?, BaseMapper> getEntityMapperMap();

    /**
     * 获取当前实体类所对应的mapper
     * @param entityClass
     * @param <T>
     * @return
     */
    protected <T> BaseMapper<T> getEntityMapperExecute(Class<T> entityClass) {
        BaseMapper entityMapper = ENTITY_MAPPER_CACHE_MAP.get(entityClass);
        if (entityMapper == null && !ENTITY_MAPPER_CACHE_MAP.containsKey(entityClass)) {
            Map<?, BaseMapper> baseMapperMap = getEntityMapperMap();
            if (baseMapperMap != null) {
                for (Object key : baseMapperMap.keySet()) {
                    BaseMapper baseMapper = baseMapperMap.get(key);
                    Class currentClass;
                    if (Proxy.isProxyClass(baseMapper.getClass())) {
                        MybatisMapperProxy mapperProxy = (MybatisMapperProxy) Proxy.getInvocationHandler(baseMapper);
                        currentClass = rdtResolver.getGenericActualTypeClass((Class)rdtResolver.getPropertyValue(mapperProxy, "mapperInterface"), 0);
                    } else {
                        currentClass = rdtResolver.getGenericActualTypeClass(baseMapper, 0);
                    }

                    if (currentClass == entityClass) {
                        entityMapper = baseMapper;
                        break;
                    }
                }
            }
            ENTITY_MAPPER_CACHE_MAP.put(entityClass, entityMapper);
        }
        return entityMapper;
    }


    @Override
    protected <T> T saveExecute(T entity, Class<T> entityClass) {
        throw new IllegalStateException("not support save");
    }

    @Override
    protected <T> Collection<T> saveAllExecute(Collection<T> collection, Class<T> entityClass) {
        throw new IllegalStateException("not support saveAll");
    }

    public String getColumn(Class entityClass, String property) {
        String[] columns = getColumn(entityClass, new String[] {property});
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException(String.format("not found %s property %s column", entityClass.getName(), property));
        }
        return columns[0];
    }


    public String[] getColumn(Class entityClass, String... properties) {
        List<String> columnList = new ArrayList<>(properties.length);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo != null) {
            int propertiesLength = properties.length;

            if (propertiesLength == 1 && tableInfo.getKeyProperty().equals(properties[0])) {
                columnList.add(tableInfo.getKeyColumn());
            } else {

                List<TableFieldInfo> tableInfoFieldList = tableInfo.getFieldList();

                for (String property : properties) {
                    if (tableInfo.getKeyProperty().equals(property)) {
                        columnList.add(tableInfo.getKeyColumn());
                    } else {
                        if (tableInfoFieldList != null) {
                            for (TableFieldInfo tableFieldInfo : tableInfoFieldList) {
                                if (tableFieldInfo.getProperty().equals(property)) {
                                    columnList.add(tableFieldInfo.getColumn());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return columnList.toArray(new String[columnList.size()]);
    }


    @Override
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {
        final Class modifyClass = modifyClassModel.getCurrentClass();
        BaseMapper mapper = getEntityMapper(modifyClass);

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

        mapper.update(null, updateWrapper);
    }

    @Override
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Column relyColumn, int group, ModifyRelyDescribe describe) {
        final Class modifyClass = modifyClassModel.getCurrentClass();
        BaseMapper mapper = getEntityMapper(modifyClass);

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
                updateWrapper.and(
                        it -> it.in(inIsBatch, getColumn(modifyClass, relyProperty), inValList)
                                .eq(!inIsBatch, getColumn(modifyClass, relyProperty), inValList.get(0))
                                .or()
                                .notIn(notIsBatch, getColumn(modifyClass, relyProperty), notInValList)
                                .ne(!notIsBatch, getColumn(modifyClass, relyProperty), notInValList.get(0))

                ) ;


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
        mapper.update(null, updateWrapper);
    }

    @Override
    protected <T> List<T> findByConditionsExecute(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        BaseMapper mapper = getEntityMapper(entityClass);
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();

        boolean hasSelectPropertys = selectPropertys != null && selectPropertys.length > 0;
        List<String> selectPropertyList = hasSelectPropertys ? new ArrayList<>(16) : null;

        if (conditionPropertys != null) {
            for (int i = 0; i < conditionPropertys.size(); i++) {
                String property = conditionPropertys.get(i);
                Object conditionValue = conditionValues.get(i);
                if (conditionValue instanceof Collection) {
                    queryWrapper.in(getColumn(entityClass, property), (Collection)conditionValue);
                } else {
                    queryWrapper.eq(getColumn(entityClass, property), conditionValue);
                }

                if (hasSelectPropertys) {
                    if (!selectPropertyList.contains(property)) {
                        selectPropertyList.add(property);
                    }
                }
            }
        }

        if (hasSelectPropertys) {
            //加入查询列
            selectPropertyList.addAll(Arrays.asList(selectPropertys));
            queryWrapper.select(getColumn(entityClass, selectPropertyList.toArray(new String[selectPropertyList.size()])));
        }

        return mapper.selectList(queryWrapper);
    }
}
