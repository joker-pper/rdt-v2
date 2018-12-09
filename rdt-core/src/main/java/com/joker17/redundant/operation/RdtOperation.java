package com.joker17.redundant.operation;

import com.joker17.redundant.fill.FillNotAllowedDataException;
import com.joker17.redundant.fill.FillNotAllowedValueException;
import com.joker17.redundant.fill.FillType;
import com.joker17.redundant.model.ClassModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RdtOperation {
    /**
     * 通过id列表查找对应数据
     * @param entityClass
     * @param ids
     * @param <T>
     * @return
     */
    <T> List<T> findByIdIn(Class<T> entityClass, Collection<Object> ids);

    /**
     * 通过id查找对应数据
     * @param entityClass
     * @param id
     * @param <T>
     * @return
     */
    <T> T findById(Class<T> entityClass, Object id);

    /**
     * 保存实体数据,非核心,根据具体是否需要实现
     * @param entity
     * @param <T>
     * @return
     */
    <T> T save(T entity);

    /**
     * 保存实体数据,非核心,根据具体是否需要实现
     * @param collection
     * @param <T>
     * @return
     */
    <T> Collection<T> saveAll(Collection<T> collection);


    /**
     * 获取当前entity class的 id字段名称
     * @param entityClass
     * @return
     */
    String getPrimaryId(Class entityClass);


    /**
     * 获取当前指定数据类型集合中以key属性值为key的map数据
     * @param data
     * @param key
     * @param <T>
     * @return
     */
    <T> Map<Object, T> getKeyMap(Collection<T> data, String key);


    ClassModel getClassModel(Class entityClass);

    /**
     * 根据数据列表的id值获取当前持久化的map数据
     * @param data 单实体/array/list数据
     * @param check 如果为true时仅在有use property时查询
     * @return key: id, val: domain data
     */
    Map<Object, Object> getCurrentMapData(Object data, boolean check);


    /**
     *  getCurrentMapData(data, true)
     */
    Map<Object, Object> getCurrentMapData(Object data);


    /**
     * 更新当前对象的所有相关冗余字段数据
     * @param current
     */
    void updateMulti(Object current);

    /**
     * 根据当前对象与之前对象数据对比后,更新被引用字段值所发生改变后的相关冗余字段数据
     * @param current 当前单实体数据
     * @param before 之前单实体数据,为null时将被忽略
     */
    void updateMulti(Object current, Object before);

    /**
     *  提供单对象/array/list数据的更新支持,解析后调用updateMulti(Object current)操作,
     *  更新所有相关冗余字段数据
      * @param data 当前单实体/array/list数据
     */
    void updateRelevant(Object data);

    /**
     * 通过当前数据以及更新数据前的数据,对比后去更新被引用字段值所发生改变后的相关冗余
     * 字段数据
     * @param data 当前单实体/array/list数据
     * @param beforeKeyDataMap 之前实体id值所对应实体的map数据
     */
    void updateRelevant(Object data, Map<Object, Object> beforeKeyDataMap);

    /**
     * fill(collection, allowedNullValue, checkValue, clear, FillType.ALL);
     */
    void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear);

    /**
     * 填充数据列表的核心方法,根据当前集合数据、参数以及关系填充所引用target持久化类的字段值
     * @param collection 当前需要进行填充的数据列表(支持不同类型的数据)
     * @param allowedNullValue 是否允许条件列值为null,为false时存在null值会抛出 FillNotAllowedValueException 异常
     * @param checkValue 为true时对应条件值的个数必须等于所匹配的结果个数,反之抛出 FillNotAllowedDataException 异常
     * @param clear 为true时会清除未匹配到数据的字段值
     * @param fillType 填充类型,为TRANSIENT时只填充为transient的column(用于填充展示时),ALL时为全部(填充条件下所有的column),PERSISTENT只填充为持久化的column(主要用于填充保存时)
     *
     * 异常类:
     * @see     FillNotAllowedValueException
     * @see     FillNotAllowedDataException
     */
    void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear, FillType fillType);


    /**
     * fillForShow(collection, false),默认只填充transient的列
     * @param collection
     */
    void fillForShow(Collection<?> collection);


    /**
     *  fillForShow(collection, clear, FillType.TRANSIENT);
     */
    void fillForShow(Collection<?> collection, boolean clear);

    /**
     * 用于填充展示数据列表的方法,忽略约束性
     * fill(collection, true, false, clear, fillType);
     * @param collection
     * @param clear 是否清除未找到的值
     * @param fillType
     */
    void fillForShow(Collection<?> collection, boolean clear, FillType fillType);



    /**
     * fillForSave(collection, false)
     * @param collection
     */
    void fillForSave(Collection<?> collection);

    /**
     *  fillForSave(collection, allowedNullValue, FillType.PERSISTENT);
     */
    void fillForSave(Collection<?> collection, boolean allowedNullValue);

    /**
     * 对持久化数据保存的填充,默认check, 条件值的个数必须不等于所匹配的结果个数时会抛出FillNotAllowedDataException 异常
     * fill(collection, allowedNullValue, true, true, fillType);
     * @param collection
     * @param allowedNullValue 是否允许条件列值为null,为false时存在null值会抛出 FillNotAllowedValueException 异常
     * @param fillType
     *
     */
    void fillForSave(Collection<?> collection, boolean allowedNullValue, FillType fillType);
}
