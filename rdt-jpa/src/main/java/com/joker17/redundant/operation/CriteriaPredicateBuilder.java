package com.joker17.redundant.operation;


import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Collection;

public class CriteriaPredicateBuilder {

    private CriteriaBuilder criteriaBuilder;

    public CriteriaPredicateBuilder(CriteriaBuilder criteriaBuilder) {
        if (criteriaBuilder == null) {
            throw new IllegalArgumentException("criteriaBuilder must be not null");
        }
        this.criteriaBuilder = criteriaBuilder;
    }


    public static CriteriaPredicateBuilder of(CriteriaBuilder criteriaBuilder) {
        return new CriteriaPredicateBuilder(criteriaBuilder);
    }

    public static CriteriaPredicateBuilder of(EntityManager entityManager) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        return new CriteriaPredicateBuilder(criteriaBuilder);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public <T> CriteriaUpdate createCriteriaUpdate(Class<T> entityClass) {
        return criteriaBuilder.createCriteriaUpdate(entityClass);
    }

    public <T> CriteriaQuery createQuery(Class<T> entityClass) {
        return criteriaBuilder.createQuery(entityClass);
    }


    public Predicate criteriaIn(Path path, Collection<?> vals) {
        if (vals != null && vals.size() == 1) {
            return eq(path, vals.iterator().next());
        }
       /* CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
        for (Object val : vals) {
            in.value(val);
        }
        return criteriaBuilder.and(in);*/
        return path.in(vals);
    }

    public Predicate criteriaIn(Path path, Object val) {
        if (val instanceof Collection) {
            return criteriaIn(path, (Collection) val);
        }
        return eq(path, val);
    }



    public Predicate criteriaNotIn(Path path, Collection<?> vals) {
        if (vals != null && vals.size() == 1) {
            return ne(path, vals.iterator().next());
        }
        return criteriaBuilder.not(path.in(vals));
    }

    public Predicate criteriaNotIn(Path path, Object val) {
        if (val instanceof Collection) {
            return criteriaNotIn(path, (Collection) val);
        }
        return ne(path, val);
    }

    public Predicate eq(Path path, Object o) {
        return criteriaBuilder.equal(path, o);
    }


    public Predicate ne(Path path, Object o) {
        return criteriaBuilder.notEqual(path, o);
    }

    public Predicate or(Predicate... params) {
        return criteriaBuilder.or(params);
    }


}
