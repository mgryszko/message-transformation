Like Smalltalk/Objective-C message passing? Want to have a more human-readable code? Annotate your method with ``@AsMessage``

```
@AsMessage
def mockBlowOn(whistle, times, expects, returns)
```

and instead of:

```
mockBlowOn whistle, 1, 'fififi', 'pufff'
```

you call:

```
mockBlowOn whistle, times: 1, expects: 'fififi', returns: 'pufff'
```

# How it works

``@AsMessage`` adds a new method to the declaring class, preserving by default the first parameter and adding a named parameter map. The body of the message method calls the transformed method by passing the arguments extracted from the named parameters:

```
@AsMessage
def mockBlowOn(whistle, times, expects, returns)
```

becomes:

```
def mockBlowOn(Map args, whistle) { 
	mockBlowOn(whistle, args.times, args.expects, args.returns)
}
```

Parameterless and single parameter methods are not transformed.

## What happens with default parameter values?
They are used as default key values if not provided in named args. First parameter's default parameter value is preserved too.

You can call:

```
@AsMessage
def mockBlowOn(whistle, times = 1, expects, returns)
```

as:

```
mockBlowOn whistle, expects: 'fififi', returns: 'pufff'
```

## What if the transformed method has already a closure as last parameter?
It is preserved (as last parameter), with or without a default value:

```
@AsMessage
def mockBlowOn(whistle, times, expects, Closure returns)
```

is called as:

```
mockBlowOn whistle, times: 1, expects: 'fififi', { 
    ['pufff', 'pafff', 'bufff']
}
```

## And named parameters?
They must be provided as an explicit map in the first parameter after transformation:

```
@AsMessage
def methodWithNamedParameters(args, param1, param2)
```

should be called as:

```
methodWithNamedParameters([namedParam1: 'first', namedParam2: 'second'], param1: '1', param2: 2)
```

## And a varargs parameter?
Varargs are passed as a named parameter, either as array or list:

```
@AsMessage
def mockBlowOn(whistle, times, expects, ...returns)
```

is called either as

```
mockBlowOn whistle, times: 1, expects: 'fififi', returns: ['pufff', 'pafff', 'bufff']
```

or with more ceremony

```
mockBlowOn whistle, times: 1, expects: 'fififi', returns: ['pufff', 'pafff', 'bufff'] as Object[]
```

## Extras
### Don't preserve first parameter
First parameter can optionally removed and its value taken from the named parameters. You do this by setting the ``preserveFirstParameter`` annotation parameter to ``false`` (the default value is ``true``):

```
@AsMessage(preserveFirstParameter = false)
def mockBlowOn(whistle, times, expects, returns)
```

becomes:

```
def mockBlowOn(Map args) { 
	mockBlowOn(args.whistle, args.times, args.expects, args.returns)
}
```

### Handling of array parameters
Any array parameter of the transformed method can be provided either as array or list after transformation.

## Transformation doesn't...
* check if the it makes sense (e.g. with methods already having named parameters)
* validate if the map with named parameters contains all arguments required to call the original method (al least not yet)