package com.smithnielson.triviahouse

class Look (input:String?){
    var result:String? = null
    var resultType:String? = null

    init{
        var look:List<String>? = input?.split("|")
        resultType = look?.get(0)
        result = look?.get(1)
    }
}