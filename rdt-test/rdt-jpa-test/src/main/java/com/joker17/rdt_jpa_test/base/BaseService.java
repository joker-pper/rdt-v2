package com.joker17.rdt_jpa_test.base;

import com.joker17.rdt_jpa_test.repository.BaseRepository;
import com.joker17.redundant.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@NoRepositoryBean
public class BaseService<T, ID> implements IBaseService<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected BaseRepository<T, ID> baseRepository;

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
        return baseRepository.findAll();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return baseRepository.findAll(sort);
    }

    @Override
    public List<T> findAllById(Iterable<ID> iterable) {
        return baseRepository.findAllById(iterable);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        return baseRepository.saveAll(iterable);
    }

    @Override
    public void flush() {
        baseRepository.flush();
    }

    @Override
    public <S extends T> S saveAndFlush(S s) {
        return baseRepository.saveAndFlush(s);
    }

    @Override
    public void deleteInBatch(Iterable<T> iterable) {
        baseRepository.deleteInBatch(iterable);
    }

    @Override
    public void deleteAllInBatch() {
        baseRepository.deleteAllInBatch();
    }


    @Override
    public T getOne(ID id) {
        /*try {
            return baseRepository.getOne(id);
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
        return baseRepository.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return baseRepository.findAll(example, sort);
    }

    public Page<T> findAll(Pageable pageable) {
        return baseRepository.findAll(pageable);
    }

    @Override
    public <S extends T> S save(S s) {
        return baseRepository.save(s);
    }

    @Override
    public Optional<T> findById(ID id) {
        return baseRepository.findById(id);
    }

    @Override
    public boolean existsById(ID id) {
        return baseRepository.existsById(id);
    }

    @Override
    public long count() {
        return baseRepository.count();
    }

    @Override
    public void deleteById(ID id) {
        baseRepository.deleteById(id);
    }

    @Override
    public void delete(T t) {
        baseRepository.delete(t);
    }

    @Override
    public void deleteAll(Iterable<? extends T> iterable) {
        baseRepository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
        baseRepository.deleteAll();
    }

    public <S extends T> Optional<S> findOne(Example<S> example) {
        return baseRepository.findOne(example);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return baseRepository.findAll(example, pageable);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return baseRepository.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return baseRepository.exists(example);
    }
}
