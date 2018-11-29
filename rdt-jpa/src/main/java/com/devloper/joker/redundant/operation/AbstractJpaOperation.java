package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.core.RdtConfiguration;
import com.devloper.joker.redundant.fill.FillOneKeyModel;
import com.devloper.joker.redundant.model.*;
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
                sb.append(property + "=:" + property + ", ");
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
                sb.append(property + " =:" + property + andText);
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
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo,  Column relyColumn, int group, ModifyRelyDescribe describe) {
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



    protected Predicate modelTypeCriteriaProcessing(ModifyRelyDescribe describe, String relyProperty, CriteriaPredicateBuilder builder, Root root) {
        List<Object> unknownNotExistValList = describe.getUnknownNotExistValList();
        List<Object> valList = describe.getValList();
        Path relyPropertyPath = root.get(relyProperty);
        Predicate predicate = null;
        if (!valList.isEmpty()) {
            if (unknownNotExistValList.isEmpty()) {
                predicate = builder.criteriaIn(relyPropertyPath, valList);
            } else {
                //满足在valList 或 非unknownNotExistValList时
                predicate = builder.or(builder.criteriaIn(relyPropertyPath, valList), builder.criteriaNotIn(relyPropertyPath, unknownNotExistValList));
            }
        } else {
            if (!unknownNotExistValList.isEmpty()) {
                predicate = builder.criteriaNotIn(relyPropertyPath, unknownNotExistValList);
            }
        }
        return predicate;
    }

    @Override
    protected <T> List<T> findByFillKeyModelExecute(FillOneKeyModel fillOneKeyModel) {
        Class<T> entityClass = fillOneKeyModel.getEntityClass();
        CriteriaPredicateBuilder criteriaBuilder = CriteriaPredicateBuilder.of(entityManager);
        try {
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root root = criteriaQuery.from(entityClass);

            Set<Column> columnSet = fillOneKeyModel.getColumnValues();

            List<Selection<?>> selectionList = new ArrayList<Selection<?>>(16);
            selectionList.add(root.get(fillOneKeyModel.getKey()));

            for (Column column : columnSet) {
                selectionList.add(root.get(column.getProperty()));
            }

            criteriaQuery.select(criteriaBuilder.getCriteriaBuilder().construct(Object[].class, selectionList.toArray(new Selection[selectionList.size()])));

            List<Predicate> predicateList = new ArrayList<Predicate>();
            predicateList.add(criteriaBuilder.criteriaIn(root.get(fillOneKeyModel.getKey()), fillOneKeyModel.getKeyValues()));

            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
            List<Object[]> list = entityManager.createQuery(criteriaQuery).getResultList();
            return parseResults(entityClass, list, Arrays.asList(fillOneKeyModel.getKeyColumn()), columnSet);
        } catch (Exception e) {
            logger.warn("rdt fill get " + entityClass.getName() + " data list by used property has error, will use all property, \n cause by exception :", e);
        }

        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root root = criteriaQuery.from(entityClass);

        List<Predicate> predicateList = new ArrayList<Predicate>(16);
        predicateList.add(criteriaBuilder.criteriaIn(root.get(fillOneKeyModel.getKey()), fillOneKeyModel.getKeyValues()));

        criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    protected <T> List<T> parseResults(Class<T> entityClass, List<Object[]> list, Collection<Column> conditions, Collection<Column> columns) throws Exception {
        List<T> results = new ArrayList<T>(16);
        for (Object[] objects : list) {
            T data = entityClass.newInstance();
            int i = 0;

            for (Column column : conditions) {
                String property = column.getProperty();
                rdtResolver.setPropertyValue(data, property, objects[i ++]);
            }

            for (Column column : columns) {
                String property = column.getProperty();
                rdtResolver.setPropertyValue(data, property, objects[i ++]);
            }

            results.add(data);
        }
        return results;
    }


    @Override
    protected <T> List<T> findByFillManyKeyExecute(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue) {
        CriteriaPredicateBuilder criteriaBuilder = CriteriaPredicateBuilder.of(entityManager);
        try {
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root root = criteriaQuery.from(entityClass);
            List<Selection<?>> selectionList = new ArrayList<Selection<?>>(16);
            List<Predicate> predicateList = new ArrayList<Predicate>();

            int index = 0;
            for (Column column : conditionColumnValues) {
                Path path = root.get(column.getProperty());
                selectionList.add(path);
                predicateList.add(criteriaBuilder.eq(path, conditionGroupValue.get(index ++)));
            }

            for (Column column : columnValues) {
                selectionList.add(root.get(column.getProperty()));
            }

            criteriaQuery.select(criteriaBuilder.getCriteriaBuilder().construct(Object[].class, selectionList.toArray(new Selection[selectionList.size()])));

            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
            List<Object[]> list = entityManager.createQuery(criteriaQuery).getResultList();
            return parseResults(entityClass, list, conditionColumnValues, columnValues);
        } catch (Exception e) {
            logger.warn("rdt fill get " + entityClass.getName() + " data list by used property has error, will use all property, \n cause by exception :", e);
        }

        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root root = criteriaQuery.from(entityClass);

        List<Predicate> predicateList = new ArrayList<Predicate>(16);
        int index = 0;
        for (Column column : conditionColumnValues) {
            Path path = root.get(column.getProperty());
            predicateList.add(criteriaBuilder.eq(path, conditionGroupValue.get(index ++)));
        }
        criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
