package com.grysz

class MethodContainer {
    @AsMessage
    def parameterlessMethod() { 'parameterlessMethod' }

    @AsMessage
    def singleParameterMethod(param1) { "singleParameterMethod($param1)" }

    @AsMessage
    def twoParameterMethod(param1, param2) { "twoParameterMethod($param1, $param2)" }
}
