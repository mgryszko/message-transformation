package com.grysz

class MethodContainer {
    @AsMessage
    def parameterlessMethod() {
        'parameterlessMethod'
    }

    @AsMessage
    def singleParameterMethod(String param1) {
        "singleParameterMethod($param1)"
    }

    @AsMessage
    def twoParameterMethod(String param1, Number param2) {
        "twoParameterMethod($param1, $param2)"
    }

    @AsMessage
    def threeParameterMethod(String param1, Number param2, List param3) {
        "threeParameterMethod($param1, $param2, $param3)"
    }
}
