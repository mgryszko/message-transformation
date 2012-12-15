package com.grysz

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class AsMessageASTTransformation implements ASTTransformation {
    private static final String NAMED_PARAMS = '$_args'

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            addError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: ${nodes.toList()}", nodes[0], source);
        }
        MethodNode method = nodes[1]
        if (shouldTransform(method)) {
            method.declaringClass.addMethod new MethodNode(
                method.name,
                method.modifiers,
                method.returnType,
                messageMethodParametersFrom(method.parameters),
                method.exceptions,
                messageMethodBodyCalling(method),
            )
        }
    }

    private addError(msg, expr, source) {
        def syntaxEx = new SyntaxException("$msg\n", expr.getLineNumber(), expr.getColumnNumber())
        source.getErrorCollector().addError new SyntaxErrorMessage(syntaxEx, source)
    }

    private shouldTransform(MethodNode method) {
        method.parameters.size() > 1 && !(method.parameters.size() == 2 && lastParameterIsClosure(method.parameters))
    }

    private messageMethodParametersFrom(originalParams) {
        def messageParams = [
            new Parameter(new ClassNode(Map), NAMED_PARAMS),
            new Parameter(originalParams[0].type, originalParams[0].name, originalParams[0].initialExpression),
        ]
        if (lastParameterIsClosure(originalParams)) {
            messageParams << new Parameter(originalParams.last().type, originalParams.last().name, originalParams.last().initialExpression)
        }
        messageParams as Parameter[]
    }

    private messageMethodBodyCalling(originalMethod) {
        new BlockStatement([
            new ExpressionStatement(
                new MethodCallExpression(THIS_EXPRESSION, originalMethod.name, argsToCallOriginalMethod(originalMethod.parameters))
            )], new VariableScope()
        )
    }

    private argsToCallOriginalMethod(originalParams) {
        def args = [firstArg(originalParams)] + argsToBeTakenFromNamedParameters(originalParams)
        if (lastParameterIsClosure(originalParams)) {
            args << lastArg(originalParams)
        }

        new ArgumentListExpression(args)
    }

    private firstArg(originalParams) {
        new VariableExpression(originalParams.first().name)
    }

    private argsToBeTakenFromNamedParameters(originalParams) {
        def argsParam = new VariableExpression(NAMED_PARAMS)

        def originalParamsToTakeFromNamedParams = lastParameterIsClosure(originalParams) ?
            allButFirstAndLast(originalParams) :
            allButFirst(originalParams)
        originalParamsToTakeFromNamedParams.collect { originalParam ->
            if (originalParam.hasInitialExpression()) {
                new TernaryExpression(
                    new BooleanExpression(containsKey(argsParam, originalParam.name)), // if
                    getAndCastToArray(argsParam, originalParam), // then
                    originalParam.initialExpression // else
                )
            } else {
                getAndCastToArray(argsParam, originalParam)
            }
        }
    }

    private lastArg(originalParams) {
        new VariableExpression(originalParams.last().name)
    }

    private lastParameterIsClosure(params) {
        params.last().type == new ClassNode(Closure)
    }

    private allButFirst(c) {
        c[1..c.size() - 1]
    }

    private allButFirstAndLast(c) {
        c[1..c.size() - 2]
    }

    private getAndCastToArray(argsParam, param) {
        def expr = get(argsParam, param.name)
        param.type.typeClass.array ? CastExpression.asExpression(param.type, expr) : expr
    }

    private get(map, key) {
        new PropertyExpression(map, key)
    }

    private containsKey(map, key) {
        new MethodCallExpression(map, 'containsKey', new ArgumentListExpression(new ConstantExpression(key)))
    }
}
