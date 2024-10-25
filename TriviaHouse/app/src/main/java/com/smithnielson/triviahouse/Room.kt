package com.smithnielson.triviahouse

import java.io.Serializable

class Room:Serializable {

    var answer:String? = null
    var attendant:String? = null
    var give: String? = null
    var look: String? = null
    var move: String? = null
    var name: String? = null
    var use: String? = null

    private var lookObject:Look? = null
    private var questionObject:Question? = null

    fun getMoveToRooms(): Array<String>?{
        return move?.split("|")?.toTypedArray()
    }

    fun getLookObject(): Look?{
       if(lookObject == null){
           lookObject = Look(look)
       }
        return lookObject
    }

    fun getQuestionObject(): Question?{
        if(questionObject == null){
            questionObject = Question(answer)
        }
        return questionObject

    }



}