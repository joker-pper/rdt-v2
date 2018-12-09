package com.joker17.redundant.operation;

import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.model.*;

import java.util.*;

public abstract class AbstractComplexOperation extends AbstractOperation {

    public AbstractComplexOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void updateMultiCore(ClassModel classModel, ChangedVo changedVo) {
        super.updateMultiCore(classModel, changedVo);
        updateModifyComplex(classModel, changedVo);
    }


    /**
     * 复杂数据的处理
     *
     * @param classModel
     * @param vo
     */
    protected void updateModifyComplex(final ClassModel classModel, final ChangedVo vo) {
        //获取classModel相关的非base关系类
        Set<Class> complexClassSet = classModel.getChangedComplexClassSet();
        if (!complexClassSet.isEmpty()) {
            final List<String> changedPropertys = vo.getChangedPropertys();
            for (Class complexClass : complexClassSet) {
                //获取complexClass所拥有的复杂关系对象组合
                List<ComplexAnalysis> complexAnalysisList = configuration.getComplexAnalysisList(complexClass);
                ClassModel complexClassModel = getClassModel(complexClass);
                for (final ComplexAnalysis complexAnalysis : complexAnalysisList) {
                    if (complexAnalysis.getHasMany()) {
                        //包含many时,即存在数组/集合的子对象

                        configuration.doModifyDescribeHandle(classModel, complexClassModel, new RdtConfiguration.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe) {
                                ModifyDescribe currentDescribe = configuration.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                                if (currentDescribe != null) {
                                    updateModifyDescribeMany(classModel, modifyClassModel, complexAnalysis, currentDescribe, vo);
                                }
                            }
                        });

                        configuration.doModifyRelyDescribeHandle(classModel, complexClassModel, new RdtConfiguration.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel modifyClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                                ModifyRelyDescribe currentDescribe = configuration.getModifyRelyDescribe(describe, changedPropertys);

                                if (currentDescribe != null) {
                                    updateModifyRelyDescribeMany(classModel, modifyClassModel, complexAnalysis, currentDescribe, vo, relyColumn, group);
                                }
                            }
                        });

                    } else { //全部为one时

                        configuration.doModifyDescribeHandle(classModel, complexClassModel, new RdtConfiguration.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe) {
                                ModifyDescribe currentDescribe = configuration.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                                if (currentDescribe != null) {
                                    updateModifyDescribeOne(classModel, modifyClassModel, complexAnalysis, currentDescribe, vo);
                                }
                            }
                        });
                        configuration.doModifyRelyDescribeHandle(classModel, complexClassModel, new RdtConfiguration.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel modifyClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                                ModifyRelyDescribe currentDescribe = configuration.getModifyRelyDescribe(describe, changedPropertys);
                                if (currentDescribe != null) {
                                    updateModifyRelyDescribeOne(classModel, modifyClassModel, complexAnalysis, currentDescribe, vo, relyColumn, group);
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
    protected void updateModifyDescribeOne(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo) {

        ClassModel modifyClassModel = getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis); //获取当前要修改的base model

        Exception exception = null;
        try {
            updateModifyDescribeOneImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo);
        } catch (Exception e) {
            exception = e;
        }
        updateModifyDescribeOneLogOutput(classModel, complexClassModel, modifyClassModel, complexAnalysis, describe, vo, exception);
        if (exception != null) {
            handlerUpdateThrowException(exception);
        }


    }

    protected abstract ClassModel getModifyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition);

    protected abstract String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyColumn column);

    protected abstract void updateModifyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo);

    protected void updateModifyRelyDescribeOne(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group) {
        ClassModel modifyClassModel = getModifyRelyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);

        Exception exception = null;
        try {
            updateModifyRelyDescribeOneImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, relyColumn, group);
        } catch (Exception e) {
            exception = e;
        }
        updateModifyRelyDescribeOneLogOutput(classModel, complexClassModel, modifyClassModel, complexAnalysis, describe, vo, relyColumn, group, exception);
        if (exception != null) {
            handlerUpdateThrowException(exception);
        }
    }



    protected void updateModifyDescribeOneLogOutput(final ClassModel classModel, final ClassModel complexClassModel, final ClassModel modifyClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo, Exception exception) {
        boolean hasException = exception != null;
        if (isLoggerSupport() || hasException) {
            RdtLog rdtLog = new RdtLog();
            final Map<String, Object> conditionLogMap = rdtLog.getCondition();
            final Map<String, Object> updateLogMap = rdtLog.getUpdate();

            configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                    String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                    conditionLogMap.put(getPropertyMark(property, targetProperty), targetPropertyVal);
                }
            });
            configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
                @Override
                public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                    String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                    updateLogMap.put(getPropertyMark(property, targetProperty), targetPropertyVal);
                }
            });

            if (hasException) {
                logger.warn("{} modify about {}【{}={}】data with complex【{}】 has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()), exception);
            } else {
                loggerSupport("{} modify about {}【{}={}】data with complex【{}】, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
            }
        }

    }

    protected void updateModifyRelyDescribeOneLogOutput(final ClassModel classModel, final ClassModel complexClassModel, final ClassModel modifyClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group, final Exception exception) {

        boolean hasException = exception != null;
        if (isLoggerSupport() || hasException) {
            RdtLog rdtLog = new RdtLog();
            final Map<String, Object> conditionLogMap = rdtLog.getCondition();
            final Map<String, Object> updateLogMap = rdtLog.getUpdate();

            configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                    String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                    conditionLogMap.put(getPropertyMark(property, targetProperty), targetPropertyVal);
                }
            });

            configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
                @Override
                public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                    String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                    updateLogMap.put(getPropertyMark(property, targetProperty), targetPropertyVal);
                }
            });

            String relyProperty = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, relyColumn);
            rdtLog.putConditionTop(getModelTypeProcessingCriteriaMap(describe, relyProperty));

            if (hasException) {
                logger.warn("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】has error , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()), exception);

            } else {
                loggerSupport("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
            }
        }

    }

    protected abstract ClassModel getModifyRelyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyColumn modifyColumn);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column);

    protected abstract void updateModifyRelyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group);

    protected void updateModifyDescribeMany(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo) {
        ClassModel modifyClassModel = getModifyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);
        RdtLog rdtLog = new RdtLog();

        try {
            updateModifyDescribeManyImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, rdtLog);

            loggerSupport("{} modify about {}【{}={}】data with complex【{}】, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with complex【{}】has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()), e);
            handlerUpdateThrowException(e);
        }
    }

    protected abstract ClassModel getModifyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract void updateModifyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, RdtLog rdtLog);


    protected void updateModifyRelyDescribeMany(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group) {
        ClassModel modifyClassModel = getModifyRelyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);
        RdtLog rdtLog = new RdtLog();

        try {
            updateModifyRelyDescribeManyImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, relyColumn, group, rdtLog);

            loggerSupport("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()), e);
            handlerUpdateThrowException(e);
        }
    }

    protected abstract ClassModel getModifyRelyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract void updateModifyRelyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group, RdtLog rdtLog);
}