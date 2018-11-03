package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.model.*;

import java.util.*;

public abstract class AbstractOperationComplexResolver extends AbstractOperationResolver {

    public AbstractOperationComplexResolver(RdtSupport rdtSupport) {
        super(rdtSupport);
    }

    @Override
    protected void updateMultiCore(ClassModel classModel, ChangedVo changedVo) throws Exception {
        super.updateMultiCore(classModel, changedVo);
        updateModifyComplex(classModel, changedVo);
    }


    /**
     * 复杂数据的处理
     *
     * @param classModel
     * @param vo
     */
    protected void updateModifyComplex(final ClassModel classModel, final ChangedVo vo) throws Exception {
        //获取classModel相关的非base关系类
        Set<Class> complexClassSet = classModel.getChangedComplexClassSet();
        if (!complexClassSet.isEmpty()) {
            final List<String> changedPropertys = vo.getChangedPropertys();
            for (Class complexClass : complexClassSet) {
                //获取complexClass所拥有的复杂关系对象组合
                List<ComplexAnalysis> complexAnalysisList = rdtSupport.getComplexAnalysisList(complexClass);
                ClassModel complexClassModel = getClassModel(complexClass);
                for (final ComplexAnalysis complexAnalysis : complexAnalysisList) {
                    if (complexAnalysis.getHasMany()) { //包含many时

                        rdtSupport.doModifyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe) throws Exception {
                                ModifyDescribe currentDescribe = rdtSupport.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                                if (currentDescribe != null) {
                                    updateModifyDescribeMany(classModel, currentClassModel, complexAnalysis, currentDescribe, vo);
                                }
                            }
                        });

                        rdtSupport.doModifyRelyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) throws Exception {
                                ModifyRelyDescribe currentDescribe = rdtSupport.getModifyRelyDescribe(describe, changedPropertys);
                                if (currentDescribe != null) {
                                    updateModifyRelyDescribeMany(classModel, currentClassModel, complexAnalysis, currentDescribe, vo, relyColumn, group);
                                }
                            }
                        });

                    } else { //全部为one时

                        rdtSupport.doModifyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe) throws Exception {
                                ModifyDescribe currentDescribe = rdtSupport.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                                if (currentDescribe != null) {
                                    updateModifyDescribeOne(classModel, currentClassModel, complexAnalysis, currentDescribe, vo);
                                }
                            }
                        });
                        rdtSupport.doModifyRelyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) throws Exception {
                                ModifyRelyDescribe currentDescribe = rdtSupport.getModifyRelyDescribe(describe, changedPropertys);
                                if (currentDescribe != null) {
                                    updateModifyRelyDescribeOne(classModel, currentClassModel, complexAnalysis, currentDescribe, vo, relyColumn, group);
                                }
                            }
                        });
                    }
                }

            }
        }
    }


    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑
     *
     * @param classModel        触发更新的实体
     * @param complexClassModel 当前处理的complexClassModel
     * @param complexAnalysis
     * @param describe          对应的修改信息
     * @param vo
     */
    protected void updateModifyDescribeOne(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo) throws Exception {

        ClassModel modifyClassModel = getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis); //获取当前要修改的base model

        final Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

        rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal) {
                String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                conditionDataMap.put(property, targetPropertyVal); //用作查询条件

                if (logDetail) {
                    conditionLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    conditionLogMap.put(property, targetPropertyVal);
                }
            }
        });

        rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal) {
                String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                updateDataMap.put(property, targetPropertyVal); //用作更新值
                if (logDetail) {
                    updateLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(property + targetProperty, targetPropertyVal);
                }
            }
        });

        try {
            updateModifyDescribeOneImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, conditionDataMap, updateDataMap);

            logger.trace("{} modify about {}【{}={}】data with complex【{}】, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(conditionLogMap), rdtResolver.toJson(updateLogMap));
        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with complex【{}】 has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(conditionLogMap), rdtResolver.toJson(updateLogMap));
            logger.warn("rdt update field has error", e);
            handlerThrowException(e);
        }
    }

    protected abstract ClassModel getModifyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition);

    protected abstract String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyColumn column);

    protected abstract void updateModifyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap);

    protected void updateModifyRelyDescribeOne(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group) throws Exception {
        ClassModel modifyClassModel = getModifyRelyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);

        final Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

        rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal) {
                String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                conditionDataMap.put(property, targetPropertyVal);

                if (logDetail) {
                    conditionLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    conditionLogMap.put(property, targetPropertyVal);
                }
            }
        });


        rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal) {
                String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                updateDataMap.put(property, targetPropertyVal);
                if (logDetail) {
                    updateLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(property, targetPropertyVal);
                }
            }
        });

        RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

        try {
            updateModifyRelyDescribeOneImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, conditionDataMap, updateDataMap, relyColumn, group, rdtLog);

            logger.trace("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】has error , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

            logger.warn("rdt update field has error", e);
            handlerThrowException(e);
        }
    }

    protected abstract ClassModel getModifyRelyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyColumn modifyColumn);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column);

    protected abstract void updateModifyRelyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap, final Column relyColumn, final int group, RdtLog rdtLog);

    protected void updateModifyDescribeMany(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo) throws Exception {
        ClassModel modifyClassModel = getModifyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);

        Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
        Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

        try {
            updateModifyDescribeManyImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, rdtLog);

            logger.trace("{} modify about {}【{}={}】data with complex【{}】, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with complex【{}】has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

            logger.warn("rdt update field has error", e);
            handlerThrowException(e);
        }
    }

    protected abstract ClassModel getModifyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract void updateModifyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, RdtLog rdtLog);


    protected void updateModifyRelyDescribeMany(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group) throws Exception {
        ClassModel modifyClassModel = getModifyRelyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);

        Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
        Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

        try {
            updateModifyRelyDescribeManyImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, relyColumn, group, rdtLog);

            logger.trace("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

            logger.warn("rdt update field has error", e);
            handlerThrowException(e);
        }
    }

    protected abstract ClassModel getModifyRelyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract void updateModifyRelyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group, RdtLog rdtLog);
}