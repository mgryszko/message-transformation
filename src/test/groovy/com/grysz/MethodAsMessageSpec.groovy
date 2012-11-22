package com.grysz

import spock.lang.Specification

class MethodAsMessageSpec extends Specification {
    private container = new MethodContainer()

    def 'parameterless method is not transformed'() {
        expect:
        container.metaClass.respondsTo(container, 'parameterlessMethod')
        !container.metaClass.respondsTo(container, 'parameterlessMethod', Map)
    }

    def 'single parameter method is not transformed'() {
        expect:
        container.metaClass.respondsTo(container, 'singleParameterMethod', String)
        !container.metaClass.respondsTo(container, 'singleParameterMethod', Map)
    }

    def 'adds a method with named arguments calling the original method with two parameters'() {
        expect:
        container.metaClass.respondsTo(container, 'twoParameterMethod', String, Number)
        container.metaClass.respondsTo(container, 'twoParameterMethod', Map, String)
        container.twoParameterMethod('p1', 2) == container.twoParameterMethod('p1', param2: 2)
    }

    def 'adds a method with named arguments calling the original method with three parameters'() {
        expect:
        container.metaClass.respondsTo(container, 'threeParameterMethod', String, Number, List)
        container.metaClass.respondsTo(container, 'threeParameterMethod', Map, String)
        container.threeParameterMethod('p1', 2, ['p3']) ==
            container.threeParameterMethod('p1', param2: 2, param3: ['p3'])
    }

    def 'if method has default parameter values, these are used as default key values'() {
        expect:
        container.methodWithDefaultParameterValues('11', 22, 31..33, '44') ==
            container.methodWithDefaultParameterValues('11', param2: 22, param3: 31..33, param4: '44')
        container.methodWithDefaultParameterValues('11', 22, 31..33) ==
            container.methodWithDefaultParameterValues('11', param2: 22, param3: 31..33)
        container.methodWithDefaultParameterValues('11', 22) ==
            container.methodWithDefaultParameterValues('11', param2: 22)
        container.methodWithDefaultParameterValues('11') == container.methodWithDefaultParameterValues([:], '11')
    }

    def 'transformed method first parameter default value is taken from the original method'() {
        expect:
        container.firstParameterHasDefaultValue(22, 31..33) ==
            container.firstParameterHasDefaultValue(param2: 22, param3: 31..33)
    }

    // TODO transformation on a method with named parameters
    // TODO transformation on a method with varargs
    // TODO transformation on a method with closure as last parameter
    // TODO control if all parameters are passed
    // TODO choose if first parameter should be preserved
}
