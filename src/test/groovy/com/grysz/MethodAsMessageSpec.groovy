package com.grysz

import spock.lang.Specification

class MethodAsMessageSpec extends Specification {
    private container = new MethodContainer()

    def 'parameterless method is not transformed'() {
        expect:
        container.metaClass.respondsTo(container, 'parameterlessMethod')
        !container.metaClass.respondsTo(container, 'parameterlessMethod', map())
    }

    def 'adds a method with named arguments calling the original method with a single parameter'() {
        expect:
        container.metaClass.respondsTo(container, 'singleParameterMethod', namedObject())
        container.metaClass.respondsTo(container, 'singleParameterMethod', map())
        container.singleParameterMethod(namedObject('o1')) == container.singleParameterMethod(param1: namedObject('o1'))
    }

    def 'adds a method with named arguments calling the original method with two parameters'() {
        expect:
        container.metaClass.respondsTo(container, 'twoParameterMethod', namedObject(), namedObject())
        container.metaClass.respondsTo(container, 'twoParameterMethod', map())
        container.twoParameterMethod(namedObject('o1'), namedObject('o2')) ==
            container.twoParameterMethod(param1: namedObject('o1'), param2: namedObject('o2'))
    }

    // TODO MethodContainer methods should return a string made of concatenated string representation of arguments
    // TODO first parameter is untouched, further parameters are converted to map
    // TODO transformation on parameterless method
    // TODO transformation on a method with default parameter values (if applicable)
    // TODO control if all parameters are passed
    // TODO transformation on class?

    private namedObject(name) {
        new Object() {
            String toString() { name }
        }
    }

    private map() { [:] }
}
