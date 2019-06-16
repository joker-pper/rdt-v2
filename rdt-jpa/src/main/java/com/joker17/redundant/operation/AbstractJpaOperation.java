package com.joker17.redundant.operation;

import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.fill.FillOneKeyModel;

import com.joker17.redundant.model.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public abstract class AbstractJpaOperation extends AbstractOperation {

    public EntityManager entityManager;

    public AbstractJpaOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {
        final StringBuilder sb = new StringBuilder();
        sb.append("UPDATE " + modifyClassModel.getClassName());

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

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
                //sb.append(property + " =:" + property + ", ");
                updateDataMap.put(property, targetPropertyVal);
            }
        });

        int index = sb.lastIndexOf(", ");
        int length = sb.length();

        if (index == length - 2) {
            sb.delete(index, length - 1);
        }
        final String andText = " AND ";

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                if (position == 0) {
                    sb.append(" WHERE ");
                }
                String property = modifyCondition.getColumn().getProperty();
                sb.append(property);
                sb.append(" =:");
                sb.append(property);
                sb.append(andText);
                //sb.append(property + " =:" + property + andText);
                conditionDataMap.put(property, targetPropertyVal);
            }
        });


        index = sb.lastIndexOf(andText);
        length = sb.length();

        if (index == length - andText.length()) {
            sb.delete(index, length);
        }

        Query query = entityManager.createQuery(sb.toString());
        for (String property : updateDataMap.keySet()) {
            query.setParameter(property, updateDataMap.get(property));
        }

        for (String property : conditionDataMap.keySet()) {
            query.setParameter(property, conditionDataMap.get(property));
        }
        query.executeUpdate();
    }

    @Override
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Column relyColumn, int group, ModifyRelyDescribe describe) {
        Class entityClass = modifyClassModel.getCurrentClass();
        final CriteriaPredicateBuilder builder = CriteriaPredicateBuilder.of(entityManager);
        final CriteriaUpdate criteriaUpdate = builder.createCriteriaUpdate(entityClass);
        final Root root = criteriaUpdate.from(entityClass);

        //设置更新值
        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                criteriaUpdate.set(property, targetPropertyVal);
            }
        });


        //设置查询条件
        final List<Predicate> predicateList = new ArrayList<Predicate>(16);
        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                predicateList.add(builder.eq(root.get(property), targetPropertyVal));
            }
        });

        String relyProperty = relyColumn.getProperty();
        Predicate processingPredicate = modelTypeCriteriaProcessing(describe, relyProperty, builder, root);
        if (processingPredicate != null) {
            predicateList.add(processingPredicate);
        }
        if (!predicateList.isEmpty()) {
            criteriaUpdate.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        entityManager.createQuery(criteriaUpdate).executeUpdate();
    }


    protected Predicate modelTypeCriteriaProcessing(ModifyRelyDescribe describe, final String relyProperty, final CriteriaPredicateBuilder builder, Root root) {
        final Path relyPropertyPath = root.get(relyProperty);
        final Predicate[] predicates = new Predicate[]{null};

        configuration.matchedTypeHandle(describe, new RdtConfiguration.MatchedTypeCallback() {
            @Override
            public void in(List<Object> inValList) {
                predicates[0] = builder.criteriaIn(relyPropertyPath, inValList);
            }

            @Override
            public void or(List<Object> inValList, List<Object> notInValList) {
                predicates[0] = builder.or(builder.criteriaIn(relyPropertyPath, inValList), builder.criteriaNotIn(relyPropertyPath, notInValList));
            }

            @Override
            public void notIn(List<Object> notInValList) {
                predicates[0] = builder.criteriaNotIn(relyPropertyPath, notInValList);

            }
        }, true);
        return predicates[0];
    }

    @Override
    protected <T> List<T> findByFillKeyModelExecute(FillOneKeyModel fillOneKeyModel) {
        Class<T> entityClass = fillOneKeyModel.getEntityClass();
        List<String> conditionPropertys = Arrays.asList(fillOneKeyModel.getKey());
        List<Object> conditionValues = Arrays.asList((Object) fillOneKeyModel.getKeyValues());

        List<String> selectPropertys = new ArrayList<String>(16);
        Set<Column> columnSet = fillOneKeyModel.getColumnValues();
        for (Column column : columnSet) {
            selectPropertys.add(column.getProperty());
        }
        return findByConditions(entityClass, conditionPropertys, conditionValues, selectPropertys.toArray(new String[selectPropertys.size()]));
    }


    @Override
    protected <T> List<T> findByFillManyKeyExecute(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue) {
        List<String> conditionPropertys = new ArrayList<String>(16);
        for (Column column : conditionColumnValues) {
            conditionPropertys.add(column.getProperty());
        }
        List<String> selectPropertys = new ArrayList<String>(16);
        for (Column column : columnValues) {
            selectPropertys.add(column.getProperty());
        }
        return findByConditions(entityClass, conditionPropertys, conditionGroupValue, selectPropertys.toArray(new String[selectPropertys.size()]));
    }


    @Override
    public <T> List<T> findByConditions(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        try {
            return findByPropertyResults(entityClass, conditionPropertys, conditionValues, selectPropertys);
        } catch (Exception e) {
            logger.warn("rdt fill get " + entityClass.getName() + " data list by used property has error, will use all property, \n cause by exception :", e);
        }
        return findByConditions(entityClass, conditionPropertys, conditionValues);
    }


    /**
     * 通过条件值获取数据列表
     * @param entityClass
     * @param conditionPropertys
     * @param conditionValues
     * @param <T>
     * @return
     */
    protected <T> List<T> findByConditions(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues) {
        CriteriaPredicateBuilder criteriaBuilder = CriteriaPredicateBuilder.of(entityManager);
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
        if (conditionPropertys != null && !conditionPropertys.isEmpty()) {
            Root root = criteriaQuery.from(entityClass);
            List<Predicate> predicateList = new ArrayList<Predicate>(16);
            int index = 0;
            for (String property : conditionPropertys) {
                Path path = root.get(property);
                predicateList.add(criteriaBuilder.criteriaIn(path, conditionValues.get(index++)));
            }
            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    protected <T> List<T> findByPropertyResults(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        List<String> finalQueryPropertys = new ArrayList<String>(16);
        List<Object[]> queryResults = findPropertyResultsByConditions(entityClass, finalQueryPropertys, conditionPropertys, conditionValues, selectPropertys);
        return convertPropertyResults(entityClass, queryResults, finalQueryPropertys);
    }

    /**
     * 通过查询指定列及条件获取结果列表
     * @param entityClass
     * @param finalQueryPropertys 空数组,用于访问最终查询列的数据
     * @param selectPropertys
     * @param conditionPropertys
     * @param conditionValues
     * @param <T>
     * @return
     */
    protected <T> List<Object[]> findPropertyResultsByConditions(Class<T> entityClass, List<String> finalQueryPropertys, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        if (finalQueryPropertys == null || !finalQueryPropertys.isEmpty()) {
            throw new IllegalArgumentException("finalQueryPropertys must be empty list");
        }
        CriteriaPredicateBuilder criteriaBuilder = CriteriaPredicateBuilder.of(entityManager);
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);

        List<Selection<?>> selectionList = new ArrayList<Selection<?>>(16);
        Root root = criteriaQuery.from(entityClass);
        if (conditionPropertys != null && !conditionPropertys.isEmpty()) {
            //添加限定条件
            List<Predicate> predicateList = new ArrayList<Predicate>(16);
            int index = 0;
            for (String property : conditionPropertys) {
                Path path = root.get(property);
                predicateList.add(criteriaBuilder.criteriaIn(path, conditionValues.get(index++)));

                //已存在的属性不再添加
                if (!finalQueryPropertys.contains(property)) {
                    finalQueryPropertys.add(property);
                    selectionList.add(path);
                }
            }
            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        if (selectPropertys != null) {
            for (String property : selectPropertys) {
                if (!finalQueryPropertys.contains(property)) {
                    finalQueryPropertys.add(property);
                    Path path = root.get(property);
                    selectionList.add(path);
                }
            }
        }
        criteriaQuery.select(criteriaBuilder.getCriteriaBuilder().construct(Object[].class, selectionList.toArray(new Selection[selectionList.size()])));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

}
