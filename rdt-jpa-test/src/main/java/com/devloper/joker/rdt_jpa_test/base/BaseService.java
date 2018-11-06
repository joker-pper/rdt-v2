package com.devloper.joker.rdt_jpa_test.base;

import com.devloper.joker.rdt_jpa_test.repository.BaseRepository;
import com.devloper.joker.redundant.utils.ClassUtils;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@NoRepositoryBean
public class BaseService<T, ID> implements IBaseService<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected BaseRepository<T, ID> repository;

    protected Class<T> entityClass;

    public BaseService() {
        entityClass = (Class) ClassUtils.getActualTypeArgumentClass(getClass(), 0);
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    @Override
    public List<T> findAllById(Iterable<ID> iterable) {
        return repository.findAllById(iterable);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        return repository.saveAll(iterable);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public <S extends T> S saveAndFlush(S s) {
        return repository.saveAndFlush(s);
    }

    @Override
    public void deleteInBatch(Iterable<T> iterable) {
        repository.deleteInBatch(iterable);
    }

    @Override
    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }


    @Override
    public T getOne(ID id) {
        /*try {
            return repository.getOne(id);
        } catch (LazyInitializationException e) {
        }*/
        Optional<T> optional = findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }


    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return repository.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return repository.findAll(example, sort);
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public <S extends T> S save(S s) {
        return repository.save(s);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(T t) {
        repository.delete(t);
    }

    @Override
    public void deleteAll(Iterable<? extends T> iterable) {
        repository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    public <S extends T> Optional<S> findOne(Example<S> example) {
        return repository.findOne(example);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return repository.findAll(example, pageable);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return repository.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return repository.exists(example);
    }
}
