package com.grysz

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement

import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION

class MessageMethod {
    private static final NAMED_PARAMS = '$_args'

    private transformedMethod
    private preserveFirstParameter

    MessageMethod(TransformedMethod transformedMethod, boolean preserveFirstParameter) {
        this.transformedMethod = transformedMethod
        this.preserveFirstParameter = preserveFirstParameter
    }

    MethodNode create() {
        new MethodNode(
            transformedMethod.name,
            transformedMethod.modifiers,
            transformedMethod.returnType,
            createParameters(),
            transformedMethod.exceptions,
            createBody(),
        )
    }

    private createParameters() {
        def params = [new Parameter(new ClassNode(Map), NAMED_PARAMS)]
        if (preserveFirstParameter) {
            params << transformedMethod.cloneFirstParameter()
        }
        if (transformedMethod.lastParameterIsClosure()) {
            params << transformedMethod.cloneLastParameter()
        }
        params as Parameter[]
    }

    private createBody() {
        new BlockStatement([
            new ExpressionStatement(
                new MethodCallExpression(
                    THIS_EXPRESSION,
                    transformedMethod.name,
                    argsToCallTransformedMethod()
                )
            )], new VariableScope()
        )
    }

    private argsToCallTransformedMethod() {
        def args = []
        if (preserveFirstParameter) {
            args << firstArg()
        }
        args += argsToBeTakenFromNamedParameters()
        if (transformedMethod.lastParameterIsClosure()) {
            args << lastArg()
        }

        new ArgumentListExpression(args)
    }

    private firstArg() {
        new VariableExpression(transformedMethod.firstParameter.name)
    }

    private lastArg() {
        new VariableExpression(transformedMethod.lastParameter.name)
    }

    private argsToBeTakenFromNamedParameters() {
        def map = new MapAccessor(new VariableExpression(NAMED_PARAMS))

        def params = paramsTransformedToNamedParams()
        params.collect { param ->
            if (param.hasInitialExpression()) {
                new TernaryExpression(
                    new BooleanExpression(map.containsKey(param.name)), // if
                    getAndCastToArray(map, param), // then
                    param.initialExpression // else
                )
            } else {
                getAndCastToArray(map, param)
            }
        }
    }

    private paramsTransformedToNamedParams()
    {
        def start = preserveFirstParameter ? 1 : 0
        def end = transformedMethod.lastParameterIsClosure() ?
            transformedMethod.parameters.size() - 2 :
            transformedMethod.parameters.size() - 1
        transformedMethod.parameters[start..end]
    }

    private getAndCastToArray(map, param) {
        def expr = map.get(param.name)
        param.type.typeClass.array ? CastExpression.asExpression(param.type, expr) : expr
    }
}
