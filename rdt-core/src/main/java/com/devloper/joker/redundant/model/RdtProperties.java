package com.devloper.joker.redundant.model;

import com.devloper.joker.redundant.resolver.RdtResolver;
import com.devloper.joker.redundant.resolver.RdtPropertiesResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RdtProperties {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 扫描的包
     */
    private String basePackage;

    /**
     * 是否启用获取列名
     */
    private Boolean enableColumnName = false;

    /**
     * 是否启用获取表名
     */
    private Boolean enableEntityName = false;

    /**
     * 是否深度克隆修改列和修改条件数据
     */
    private Boolean deepCloneChangedModify = false;

    /**
     * 如果更新需要用到查询数据时的分页数量,-1为全部
     */
    private Long pageSize = -1L;

    /**
     * 是否使用saveAll方法还是通过update语句更新(用于mongo子文档数组数据的更新操作)
     */
    private Boolean complexBySaveAll = true;

    /**
     * base类未找到primary id时默认使用该字段
     */
    private String defaultIdKey = "id";

    /**
     * 保存操作出错时是否抛出异常
     */
    private Boolean throwException = false;

    /**
     * 当前类的数据
     */
    private Map<Class, ClassModel> classModelMap = new LinkedHashMap<Class, ClassModel>(16);

    /**
     * 在指定包的class列表
     */
    private List<Class> packageClassList = new ArrayList<Class>(16);

    /**
     * target class不在指定包的数据
     */
    private Set<Class> extraClassSet = new HashSet<Class>(16);

    /**
     * 关联类所对应的多个关联信息
     */
    private Map<Class, List<ComplexModel>> classComplexModelsMap = new LinkedHashMap<Class, List<ComplexModel>>(16);

    public RdtProperties() {
        super();
    }

    public RdtProperties(String basePackage) {
        this.basePackage = basePackage;
    }

    public RdtSupport builder(RdtResolver rdtResolver) {
        if (rdtResolver == null) throw new IllegalArgumentException("rdt resolver must be not null");
        RdtPropertiesResolver propertiesResolver = new RdtPropertiesResolver(rdtResolver, this);
        //获取该包下的所有类文件
        List<Class> classList = rdtResolver.getClasses(basePackage);
        if (classList != null) packageClassList.addAll(classList);

        for (Class currentClass : packageClassList) {
            propertiesResolver.builderClass(currentClass);
        }
        logger.debug("rdt load package class complete, package class size {}, extra class size {}.", packageClassList.size(), extraClassSet.size());
        return new RdtSupport(this, rdtResolver);
    }


    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Boolean getEnableColumnName() {
        return enableColumnName;
    }

    public void setEnableColumnName(Boolean enableColumnName) {
        this.enableColumnName = enableColumnName;
    }

    public Boolean getEnableEntityName() {
        return enableEntityName;
    }

    public void setEnableEntityName(Boolean enableEntityName) {
        this.enableEntityName = enableEntityName;
    }

    public boolean hasPackageContainsClass(Class current) {
        return packageClassList.contains(current);
    }

    public Boolean getDeepCloneChangedModify() {
        return deepCloneChangedModify;
    }

    public void setDeepCloneChangedModify(Boolean deepCloneChangedModify) {
        this.deepCloneChangedModify = deepCloneChangedModify;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        if (pageSize == 0 || pageSize < -1) throw new IllegalArgumentException("page size not allowed, page > 0 || page = -1");
        this.pageSize = pageSize;
    }

    public Boolean getComplexBySaveAll() {
        return complexBySaveAll;
    }

    public void setComplexBySaveAll(Boolean complexBySaveAll) {
        this.complexBySaveAll = complexBySaveAll;
    }

    public String getDefaultIdKey() {
        return defaultIdKey;
    }

    public void setDefaultIdKey(String defaultIdKey) {
        this.defaultIdKey = defaultIdKey;
    }

    public Boolean getThrowException() {
        return throwException;
    }

    public void setThrowException(Boolean throwException) {
        this.throwException = throwException;
    }

    public Map<Class, ClassModel> getClassModelMap() {
        return classModelMap;
    }

    public List<Class> getPackageClassList() {
        return packageClassList;
    }

    public Set<Class> getExtraClassSet() {
        return extraClassSet;
    }

    public Map<Class, List<ComplexModel>> getClassComplexModelsMap() {
        return classComplexModelsMap;
    }

    public ClassModel getClassModel(Class currentClass) {
        return classModelMap.get(currentClass);
    }

}
