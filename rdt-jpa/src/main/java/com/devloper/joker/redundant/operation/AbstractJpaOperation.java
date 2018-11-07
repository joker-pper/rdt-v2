package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.fill.FillOneKeyModel;
import com.devloper.joker.redundant.model.*;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public abstract class AbstractJpaOperation extends AbstractOperation {

    public EntityManager entityManager;

    public AbstractJpaOperation(RdtSupport rdtSupport) {
        super(rdtSupport);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public abstract CrudRepository getCrudRepository(Class entityClass);

    public abstract CrudRepository getActualCrudRepository(Class entityClass);

    @Override
    protected <T> T save(T entity, Class<T> entityClass) {
        return (T)getCrudRepository(entityClass).save(entity);
    }

    @Override
    protected <T> Collection<T> saveAll(Collection<T> data, Class<T> entityClass) {
        return (Collection<T>)getCrudRepository(entityClass).saveAll(data);
    }

    @Override
    protected <T> List<T> findByIdIn(Class<T> entityClass, String idKey, Collection<Object> ids) {
        return new ArrayList<T>((Collection) getCrudRepository(entityClass).findAllById(ids));
    }

    @Override
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap) {

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE " + modifyClassModel.getClassName() + " SET ");

        for (String property: updateValMap.keySet()) {
            sb.append(property + "=:" + property + ", ");
        }
        int index = sb.lastIndexOf(", ");
        int length = sb.length();

        if (index == length - 2) {
            sb.delete(index, length - 1);
        }

        if (!conditionValMap.isEmpty()) {
            sb.append("WHERE ");
            String andText = " AND ";
            for (String property: conditionValMap.keySet()) {
                sb.append(property + " =:" + property + andText);
            }

            index = sb.lastIndexOf(andText);
            length = sb.length();

            if (index == length - andText.length()) {
                sb.delete(index, length);
            }
        }
        Query query = entityManager.createQuery(sb.toString());
        for (String property: updateValMap.keySet()) {
            query.setParameter(property, updateValMap.get(property));
        }

        for (String property: conditionValMap.keySet()) {
            query.setParameter(property, conditionValMap.get(property));
        }
        query.executeUpdate();
    }

    @Override
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap, Column relyColumn, int group, ModifyRelyDescribe describe, RdtLog rdtLog) {
        Class entityClass = modifyClassModel.getCurrentClass();
        CriteriaPredicateBuilder builder = CriteriaPredicateBuilder.of(entityManager);
        CriteriaUpdate criteriaUpdate = builder.createCriteriaUpdate(entityClass);
        Root root = criteriaUpdate.from(entityClass);
        for (String property: updateValMap.keySet()) {
            criteriaUpdate.set(property, updateValMap.get(property));
        }
        List<Predicate> predicateList = new ArrayList<Predicate>();
        for (String property: conditionValMap.keySet()) {
            predicateList.add(builder.eq(root.get(property), conditionValMap.get(property)));
        }

        String relyProperty = relyColumn.getProperty();
        Predicate processingPredicate = modelTypeCriteriaProcessing(describe, relyProperty, builder, root, rdtLog);
        if (processingPredicate != null) {
            predicateList.add(processingPredicate);
        }

        if (!predicateList.isEmpty()) {
            criteriaUpdate.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        entityManager.createQuery(criteriaUpdate).executeUpdate();
    }



    protected Predicate modelTypeCriteriaProcessing(ModifyRelyDescribe describe, String relyProperty, CriteriaPredicateBuilder builder, Root root, RdtLog rdtLog) {
        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
        List<Object> valList = describe.getValList();
        Path relyPropertyPath = root.get(relyProperty);
        Predicate predicate = null;
        Map allMap = new HashMap();
        if (!valList.isEmpty()) {
            if (unknowNotExistValList.isEmpty()) {
                predicate = builder.criteriaIn(relyPropertyPath, valList);
                allMap.put(relyProperty, valList);
            } else {
                //满足在valList 或 非unknowNotExistValList时
                predicate = builder.or(builder.criteriaIn(relyPropertyPath, valList), builder.criteriaNotIn(relyPropertyPath, unknowNotExistValList));

                Map notValMap = new HashMap();
                notValMap.put(relyProperty, unknowNotExistValList);

                Map notMap = new HashMap();
                notMap.put("not", notValMap);

                Map inMap = new HashMap();
                inMap.put(relyProperty, valList);

                allMap.put("or", Arrays.asList(inMap, notMap));

            }
        } else {
            if (!unknowNotExistValList.isEmpty()) {
                predicate = builder.criteriaNotIn(relyPropertyPath, unknowNotExistValList);
                Map notMap = new HashMap();
                notMap.put(relyProperty, unknowNotExistValList);
                allMap.put("not", notMap);
            }
        }
        rdtLog.putConditionTop(allMap);
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
