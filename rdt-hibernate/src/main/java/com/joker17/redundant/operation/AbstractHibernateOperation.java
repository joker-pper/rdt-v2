package com.joker17.redundant.operation;

import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.model.*;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.*;

public abstract class AbstractHibernateOperation extends AbstractOperation {

    protected final int BATCH_MAX_ROW = 1000;

    protected final String AND_TEXT = " AND ";


    public AbstractHibernateOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    /**
     * 获取当前session,用于查询和更新
     * @return
     */
    public abstract Session getSession();

    @Override
    protected <T> T saveExecute(T entity, Class<T> entityClass) {
        return (T) getSession().save(entity);
    }

    @Override
    protected <T> Collection<T> saveAllExecute(Collection<T> collection, Class<T> entityClass) {
        Session session = getSession();
        int i = 0;
        for (T data : collection) {
            session.save(data);
            if (i++ % BATCH_MAX_ROW == 0) {
                session.flush();
                session.clear();
            }
        }
        return collection;
    }

    @Override
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

        final StringBuilder sb = new StringBuilder();

        resolveCommonUpdateHql(conditionDataMap, updateDataMap, sb, modifyClassModel, describe, vo);

        int index = sb.lastIndexOf(AND_TEXT);
        int length = sb.length();

        if (index == length - AND_TEXT.length()) {
            sb.delete(index, length);
        }

        Session session = getSession();
        Query query = session.createQuery(sb.toString());
        for (String property : updateDataMap.keySet()) {
            query.setParameter(property, updateDataMap.get(property));
        }

        for (String property : conditionDataMap.keySet()) {
            query.setParameter(property, conditionDataMap.get(property));
        }
        query.executeUpdate();
    }

    protected void resolveCommonUpdateHql(final Map<String, Object> conditionDataMap, final Map<String, Object> updateDataMap, final StringBuilder sb, ClassModel modifyClassModel, ModifyDescribe describe, final ChangedVo vo) {
        sb.append(" UPDATE ").append(modifyClassModel.getClassName());

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                if (position == 0) {
                    sb.append(" SET ");
                }
                String property = modifyColumn.getColumn().getProperty();
                sb.append(property);
                sb.append(" =:");
                sb.append(property);
                sb.append(", ");
                updateDataMap.put(property, targetPropertyVal);
            }
        });

        int index = sb.lastIndexOf(", ");
        int length = sb.length();

        if (index == length - 2) {
            sb.delete(index, length - 1);
        }

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                if (position == 0) {
                    sb.append(" WHERE ");
                }
                String property = modifyCondition.getColumn().getProperty();
                sb.append(eq(property, property));

                sb.append(AND_TEXT);
                conditionDataMap.put(property, targetPropertyVal);
            }
        });

        configuration.doLogicalModelHandle(modifyClassModel, properties.getUpdateMultiWithLogical(), new RdtConfiguration.LogicalModelCallBack() {
            @Override
            public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                String property = logicalModel.getColumn().getProperty();
                List<Object> values = logicalModel.getValues();
                boolean batch = values.size() != 1;
                sb.append(eq(batch, property, property));
                sb.append(AND_TEXT);
                conditionDataMap.put(property, values);
            }
        });

    }

    protected String not(boolean batch, String property, String alias) {
        //    batch:  property NOT IN(:alias)      
        //not batch:  property !=:alias        
        StringBuilder builder = new StringBuilder();
        builder.append(property);
        if (batch) {
            builder.append(" NOT IN(:").append(alias).append(")");
        } else {
            builder.append(" !=:").append(alias);
        }
        return builder.toString();
    }


    protected String not(String property, String alias) {
        return not(false, property, alias);
    }

    protected String eq(boolean batch, String property, String alias) {
        //    batch:  property IN(:alias)      
        //not batch:  property =:alias        
        StringBuilder builder = new StringBuilder();
        builder.append(property);
        if (batch) {
            builder.append(" IN(:").append(alias).append(")");
        } else {
            builder.append(" =:").append(alias);
        }
        return builder.toString();
    }


    protected String eq(String property, String alias) {
        return eq(false, property, alias);
    }

    @Override 
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Column relyColumn, int group, ModifyRelyDescribe describe) {

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

        final StringBuilder sb = new StringBuilder();

        resolveCommonUpdateHql(conditionDataMap, updateDataMap, sb, modifyClassModel, describe, vo);

        final String relyProperty = relyColumn.getProperty();

        configuration.matchedTypeHandle(describe, new RdtConfiguration.MatchedTypeCallback() {
            @Override
            public void in(List<Object> inValList) {
                boolean batch = inValList.size() != 1;
                sb.append(eq(batch, relyProperty, relyProperty));
                conditionDataMap.put(relyProperty, inValList);         
            }

            @Override
            public void or(List<Object> inValList, List<Object> notInValList) {
                boolean inIsBatch = inValList.size() != 1;
                boolean notIsBatch = notInValList.size() != 1;
                String inAlias = relyProperty + "InAlias";
                String notInAlias = relyProperty + "NotInAlias";
                sb.append(String.format("( %s OR %s )", eq(inIsBatch, relyProperty, inAlias), not(notIsBatch, relyProperty, notInAlias)));
                conditionDataMap.put(inAlias, inValList);
                conditionDataMap.put(notInAlias, notInValList);
            }

            @Override
            public void notIn(List<Object> notInValList) {
                boolean batch = notInValList.size() != 1;
                sb.append(not(batch, relyProperty, relyProperty));
                conditionDataMap.put(relyProperty, notInValList);

            }
        }, true);

        int index = sb.lastIndexOf(AND_TEXT);
        int length = sb.length();

        if (index == length - AND_TEXT.length()) {
            sb.delete(index, length);
        }

        Session session = getSession();
        Query query = session.createQuery(sb.toString());
        for (String property : updateDataMap.keySet()) {
            query.setParameter(property, updateDataMap.get(property));
        }

        for (String property : conditionDataMap.keySet()) {
            query.setParameter(property, conditionDataMap.get(property));
        }
        query.executeUpdate();
    }

    @Override
    protected <T> List<T> findByConditionsExecute(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        boolean hasSelectPropertys = selectPropertys != null && selectPropertys.length > 0;
        List<String> selectPropertyList = hasSelectPropertys ? new ArrayList<String>(16) : null;
        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);

        StringBuilder builder = new StringBuilder();
        builder.append(" FROM ").append(entityClass.getName()).append(" ");

        if (conditionPropertys != null && !conditionPropertys.isEmpty()) {
            builder.append(" WHERE ");
            for (int i = 0; i < conditionPropertys.size(); i++) {
                String conditionProperty = conditionPropertys.get(i);
                Object conditionValue = conditionValues.get(i);
                boolean batch = conditionValue != null && conditionValue instanceof Collection;
                builder.append(eq(batch, conditionProperty, conditionProperty)).append(AND_TEXT);
                conditionDataMap.put(conditionProperty, conditionValue);

                if (hasSelectPropertys) {
                    //有指定查询属性时,也需要将条件列加入其中
                    if (!selectPropertyList.contains(conditionProperty)) {
                        selectPropertyList.add(conditionProperty);
                    }
                }
            }

            //处理多余的AND
            int index = builder.lastIndexOf(AND_TEXT);
            int length = builder.length();

            if (index == length - AND_TEXT.length()) {
                builder.delete(index, length);
            }
        }
        Session session = getSession();

        String sql = builder.toString();

        if (hasSelectPropertys) {
            //加入查询列
            selectPropertyList.addAll(Arrays.asList(selectPropertys));
            sql = " SELECT " + rdtResolver.join(selectPropertyList, ", ") + sql;
        }

        Query query = session.createQuery(sql);
        for (String property : conditionDataMap.keySet()) {
            query.setParameter(property, conditionDataMap.get(property));
        }
        //查询结果并转换
        List<?> queryList = query.list();
        List<T> resultList = convertPropertyResults(entityClass, queryList, selectPropertyList);
        return resultList;
    }
}
