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
        container.metaClass.respondsTo(container, 'twoParameterMethod', Map)
        container.twoParameterMethod('p1', 1) == container.twoParameterMethod(param1: 'p1', param2: 1)
    }

    def 'adds a method with named arguments calling the original method with three parameters'() {
        expect:
        container.metaClass.respondsTo(container, 'threeParameterMethod', String, Number, List)
        container.metaClass.respondsTo(container, 'threeParameterMethod', Map)
        container.threeParameterMethod('p1', 1, ['p3']) ==
            container.threeParameterMethod(param1: 'p1', param2: 1, param3: ['p3'])
    }

    // TODO MethodContainer methods should return a string made of concatenated string representation of arguments
    // TODO first parameter is untouched, further parameters are converted to map
    // TODO transformation on a method with default parameter values (if applicable)
    // TODO control if all parameters are passed
    // TODO transformation on class?
}
