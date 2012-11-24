package com.grysz

import spock.lang.Specification

class MethodAsMessageSpec extends Specification {
    private container = new MethodContainer()

    def 'parameterless method is not transformed'() {
        expect:
        container.metaClass.respondsTo(container, 'parameterless')
        !container.metaClass.respondsTo(container, 'parameterless', Map)
    }

    def 'single parameter method is not transformed'() {
        expect:
        container.metaClass.respondsTo(container, 'singleParameter', String)
        !container.metaClass.respondsTo(container, 'singleParameter', Map)
    }

    def 'adds a method with named arguments calling the original method with two parameters'() {
        expect:
        container.metaClass.respondsTo(container, 'twoParameters', String, Number)
        container.metaClass.respondsTo(container, 'twoParameters', Map, String)
        container.twoParameters('1', 2) == container.twoParameters('1', param2: 2)
    }

    def 'adds a method with named arguments calling the original method with three parameters'() {
        expect:
        container.metaClass.respondsTo(container, 'threeParameters', String, Number, List)
        container.metaClass.respondsTo(container, 'threeParameters', Map, String)
        container.threeParameters('p1', 2, ['p3']) == container.threeParameters('p1', param2: 2, param3: ['p3'])
    }

    def 'if method has default parameter values, these are used as default key values'() {
        expect:
        container.defaultParameterValues('11', 22, 31..33, '44') ==
            container.defaultParameterValues('11', param2: 22, param3: 31..33, param4: '44')
        container.defaultParameterValues('11', 22, 31..33) ==
            container.defaultParameterValues('11', param2: 22, param3: 31..33)
        container.defaultParameterValues('11', 22) == container.defaultParameterValues('11', param2: 22)
        container.defaultParameterValues('11') == container.defaultParameterValues([:], '11')
    }

    def 'transformed method first parameter default value is taken from the original method'() {
        expect:
        container.firstParameterHasDefaultValue(22, 31..33) ==
            container.firstParameterHasDefaultValue(param2: 22, param3: 31..33)
    }

    def 'named parameters of the original method must be provided as an explicit map in the first parameter after transformation'() {
        expect:
        container.namedParameters('1', 2, namedParam1: 1..3, namedParam2: 'np2') ==
            container.namedParameters([namedParam1: 1..3, namedParam2: 'np2'], param1: '1', param2: 2)
    }

    // TODO transformation on a method with varargs
    // TODO transformation on a method with closure as last parameter
    // TODO control if all parameters are passed
    // TODO choose if first parameter should be preserved
}
