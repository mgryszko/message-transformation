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

    @AsMessage
    def untypedVarargs(String param1, ...varargs) {
        "untypedVarargs($param1, $varargs)"
    }

    @AsMessage
    def typedVarargs(String param1, int[] varargs) {
        "typedVarargs($param1, $varargs)"
    }

    @AsMessage
    def arrayParameter(String param1, int[] param2, String param3) {
        "arrayParameter($param1, $param2, $param3)"
    }

    @AsMessage
    def arrayParameterWithDefaultValue(String param1, int[] param2 = [2, 3, 4] as int[], String param3) {
        "arrayParameterWithDefaultValue($param1, $param2, $param3)"
    }

    @AsMessage
    def lastClosureParameter(String param1, param2, Closure param3) {
        "lastClosureParameter($param1, $param2, ${param3()})"
    }

    @AsMessage
    def lastClosureParameter(String param1, Closure param2) {
        "lastClosureParameter($param1, ${param2()})"
    }

    @AsMessage
    def lastClosureParameterWithDefaultValue(String param1, param2, Closure param3 = { '3' }) {
        "lastClosureParameterWithDefaultValue($param1, $param2, ${param3()})"
    }
}
