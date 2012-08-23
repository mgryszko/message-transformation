package com.grysz

import spock.lang.Specification

class MethodAsMessageSpec extends Specification {
    def container = new MethodContainer()

    def 'adds a named arguments signature based on the standard method signature will all but first argument'() {
        expect:
        container.metaClass.respondsTo(container, 'singleParameterMethod', object())
        container.metaClass.respondsTo(container, 'singleParameterMethod', map())
        container.singleParameterMethod(object()) == 'result'
        container.singleParameterMethod(param1: object()) == 'result'
    }

    // TODO transformation on a method with multiple parameters
    // TODO first parameter is untouched, further parameters are converted to map
    // TODO transformation on a method with default parameter values (if applicable)
    // TODO transformation on parameterless method
    // TODO transformation on class?

    private object() { new Object() }

    private map() { [:] }
}
