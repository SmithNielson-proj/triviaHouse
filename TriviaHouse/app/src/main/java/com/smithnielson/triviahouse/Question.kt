package com.smithnielson.triviahouse

class Question (input: String?) {

    var question:String? = null
    var answers: List<String>? = null
    var correctAnswer:Int? = null
    var rewardType:String? = null
    var reward:String? = null

    init{
        var qaReward: List<String>? = input?.split("|")
        var qa:List<String>? = qaReward?.get(0)?.split("?")
        question = qa?.get(0)
        answers = qa?.get(1)?.split("+")
        correctAnswer = Integer.parseInt(qaReward?.get(1)!!)
        rewardType = qaReward[2]
        reward = qaReward[3]
    }

}