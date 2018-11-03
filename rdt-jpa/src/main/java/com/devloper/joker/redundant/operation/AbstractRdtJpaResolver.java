package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.model.*;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public abstract class AbstractRdtJpaResolver extends AbstractOperationResolver {

    public EntityManager entityManager;

    public AbstractRdtJpaResolver(RdtSupport rdtSupport) {
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
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap) throws Exception {

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
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate criteriaUpdate = criteriaBuilder.createCriteriaUpdate(entityClass);
        Root root = criteriaUpdate.from(entityClass);
        for (String property: updateValMap.keySet()) {
            criteriaUpdate.set(property, updateValMap.get(property));
        }
        List<Predicate> predicateList = new ArrayList<Predicate>();
        for (String property: conditionValMap.keySet()) {
            predicateList.add(criteriaBuilder.equal(root.get(property), conditionValMap.get(property)));
        }

        String relyProperty = relyColumn.getProperty();
        Predicate processingPredicate = modelTypeCriteriaProcessing(describe, relyProperty, criteriaBuilder, root, rdtLog);
        if (processingPredicate != null) {
            predicateList.add(processingPredicate);
        }

        if (!predicateList.isEmpty()) {
            criteriaUpdate.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        entityManager.createQuery(criteriaUpdate).executeUpdate();
    }

    protected Predicate criteriaIn(CriteriaBuilder criteriaBuilder, Path path, Collection<?> vals) {
        if (vals != null && vals.size() == 1) {
            return criteriaBuilder.equal(path, vals.iterator().next());
        }
        return path.in(vals);
    }


    protected Predicate criteriaNotIn(CriteriaBuilder criteriaBuilder, Path path, Collection<?> vals) {
        if (vals != null && vals.size() == 1) {
            return criteriaBuilder.notEqual(path, vals.iterator().next());
        }
        return criteriaBuilder.not(path.in(vals));
    }


    protected Predicate modelTypeCriteriaProcessing(ModifyRelyDescribe describe, String relyProperty, CriteriaBuilder criteriaBuilder, Root root, RdtLog rdtLog) {
        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
        List<Object> valList = describe.getValList();
        Path relyPropertyPath = root.get(relyProperty);
        Predicate predicate = null;
        Map allMap = new HashMap();
        if (!valList.isEmpty()) {
            if (unknowNotExistValList.isEmpty()) {
                predicate = criteriaIn(criteriaBuilder, relyPropertyPath, valList);
                allMap.put(relyProperty, valList);
            } else {
                //满足在valList 或 非unknowNotExistValList时
                predicate = criteriaBuilder.or(criteriaIn(criteriaBuilder, relyPropertyPath, valList), criteriaNotIn(criteriaBuilder, relyPropertyPath, unknowNotExistValList));

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
                predicate = criteriaNotIn(criteriaBuilder, relyPropertyPath, unknowNotExistValList);
                Map notMap = new HashMap();
                notMap.put(relyProperty, unknowNotExistValList);
                allMap.put("not", notMap);
            }
        }
        rdtLog.putConditionTop(allMap);
        return predicate;
    }
}
