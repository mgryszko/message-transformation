package com.grysz

import org.codehaus.groovy.ast.expr.*

class MapAccessor {
    private map

    MapAccessor(VariableExpression map) {
        this.map = map
    }

    def containsKey(key) {
        new MethodCallExpression(map, 'containsKey', new ArgumentListExpression(new ConstantExpression(key)))
    }

    def get(key) {
        new PropertyExpression(map, key)
    }
}
