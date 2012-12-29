package com.grysz

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter

class TransformedMethod {
    private node

    TransformedMethod(MethodNode node) {
        this.node = node
    }

    boolean shouldBeTransformed() {
        node.parameters.size() > 1 && !(node.parameters.size() == 2 && lastParameterIsClosure())
    }

    boolean lastParameterIsClosure() {
        node.parameters.last().type == new ClassNode(Closure)
    }

    String getName() {
        node.name
    }

    int getModifiers() {
        node.modifiers
    }

    Parameter[] getParameters() {
        node.parameters
    }

    Parameter cloneFirstParameter() {
        cloneParameter firstParameter
    }

    Parameter cloneLastParameter() {
        cloneParameter lastParameter
    }

    private cloneParameter(original) {
        new Parameter(original.type, original.name, original.initialExpression)
    }

    Parameter getFirstParameter() {
        parameters.first()
    }

    Parameter getLastParameter() {
        parameters.last()
    }

    ClassNode getReturnType() {
        node.returnType
    }

    ClassNode[] getExceptions() {
        node.exceptions
    }

    void addMethodToClass(MethodNode method) {
        node.declaringClass.addMethod method
    }
}
