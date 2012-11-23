package com.grysz

class MethodContainer {
    @AsMessage
    def parameterless() {
        'parameterless'
    }

    @AsMessage
    def singleParameter(String param1) {
        "singleParameter($param1)"
    }

    @AsMessage
    def twoParameters(String param1, Number param2) {
        "twoParameters($param1, $param2)"
    }

    @AsMessage
    def threeParameters(String param1, Number param2, List param3) {
        "threeParameters($param1, $param2, $param3)"
    }

    @AsMessage
    def defaultParameterValues(String param1 = '1', Number param2 = 2, List param3 = 1..3, param4 = '4') {
        "defaultParameterValues($param1, $param2, $param3, $param4)"
    }

    @AsMessage
    def firstParameterHasDefaultValue(String param1 = '1', Number param2, List param3) {
        "firstParameterHasDefaultValue($param1, $param2, $param3)"
    }

    @AsMessage
    def namedParameters(Map args, String param1, Number param2) {
        "namedParameters($args, $param1, $param2)"
    }
}
