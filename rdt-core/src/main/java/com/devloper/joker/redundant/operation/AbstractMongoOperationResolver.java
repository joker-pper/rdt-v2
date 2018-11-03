package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.model.*;


public abstract class AbstractMongoOperationResolver extends AbstractOperationComplexResolver {

    public AbstractMongoOperationResolver(RdtSupport rdtSupport) {
        super(rdtSupport);
    }

    @Override
    protected ClassModel getModifyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getClassModel(complexAnalysis.getRootClass());
    }


    @Override
    protected String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition) {
        return complexAnalysis.getPrefix() + "." + modifyCondition.getProperty();
    }

    @Override
    protected String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column) {
        return complexAnalysis.getPrefix() + "." + column.getProperty();
    }


    @Override
    protected ClassModel getModifyRelyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);
    }

    @Override
    protected String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition) {
        return getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
    }

    @Override
    protected String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column) {
        return getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, column);
    }

    @Override
    protected ClassModel getModifyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);
    }

    @Override
    protected ClassModel getModifyRelyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getModifyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);
    }
}
