package com.smithnielson.triviahouse

import android.os.Bundle
import android.provider.ContactsContract.Data
import android.text.method.ScrollingMovementMethod
import android.util.ArrayMap
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

const val OBSERVATION = "OBSERVATION"
const val BACKPACK = "BACKPACK"
const val ADVICE = "ADVICE"

class MainActivity : AppCompatActivity() {

    private var playerTextView: TextView? = null
    private var yourStatusTextView: TextView? = null
    private var gameStatusTextView: TextView? = null
    private var answerButton: Button? = null
    private var moveButton: Button? = null
    private var giveButton: Button? = null
    private var lookButton: Button? = null
    private var useButton: Button? = null

    private val username:String = "smithnielson4"

    private var locationString: String? = null
    private var locationCode: String? = null
    private var newBackPackString: String = ""
    private var backPack: MutableMap<String?, Any?>? = null // was <String, Any>
    private var attendant:String = ""
    private var houseMap: MutableMap<String?, Room?>? = null
    private var currentRoom: Room? = null
    private val roomMap = mapOf(
        "f1r0" to "Front door",
        "f1r1" to "First Floor, entry room",
        "f1r2" to "First floor, dining room",
        "f1r3" to "First floor, living room",
        "f1r4" to "First floor, family room",
        "f1r5" to "First floor, kitchen")

    // db references to connect to listeners
    private val rootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val houseRef: DatabaseReference = rootRef.child("house")
    private val locationRef: DatabaseReference = rootRef.child("users").child(username).child("location")
    private val backPackRef: DatabaseReference = rootRef.child("users").child(username).child("backpack")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerTextView = findViewById(R.id.playerTextView)
        yourStatusTextView = findViewById(R.id.yourStatusTextView)
        yourStatusTextView?.movementMethod = ScrollingMovementMethod()
        gameStatusTextView = findViewById(R.id.gameStatusTextView)
        answerButton = findViewById(R.id.answerButton)
        moveButton = findViewById(R.id.moveButton)
        giveButton = findViewById(R.id.giveButton)
        lookButton = findViewById(R.id.lookButon)
        useButton = findViewById(R.id.useButton)

        backPack = HashMap<String?, Any?>()
        houseMap = HashMap<String?, Room?>()

        //backPack?.put("0", "Crubml Cookie")
        //backPack?.put("1", "Sweet Tooth Fairy cupcake")
        //backPackRef.updateChildren(backPack!!)

        locationRef.setValue("f1r0")

        playerTextView?.text = username

    }


    override fun onStart() {
        super.onStart()

        moveButton?.setOnClickListener(View.OnClickListener { _ ->
            var rooms:Array<String>? = currentRoom?.getMoveToRooms()
            var fullNameRooms: MutableList<String?> = ArrayList()
            for (room in rooms!!){
                fullNameRooms.add(roomMap[room])
            }
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Click on room to enter")
            builder.setItems(fullNameRooms.toTypedArray()) { dialog, which ->
                locationRef.setValue(rooms[which])
            }
            val dialog = builder.create()
            dialog.show()
        })

        lookButton?.setOnClickListener(View.OnClickListener { _ ->
            val look = currentRoom?.getLookObject()
            if(look?.resultType == BACKPACK){
                if(backPack?.containsValue(look.result!!)!!){
                    Toast.makeText(this, "You already have a ${look.result} in your backpack", Toast.LENGTH_LONG).show()
                }
                else {
                    backPack?.put(""+backPack?.size, look.result!!)
                    backPackRef.updateChildren(backPack!!)
                    Toast.makeText(this, "You find a ${look.result}, that is added to your backpack!", Toast.LENGTH_SHORT).show()
                }
            }
            else if (look?.resultType == OBSERVATION){
                Toast.makeText(this, look.result, Toast.LENGTH_LONG).show()
            }
        })

        answerButton?.setOnClickListener(View.OnClickListener { _ ->

            var question:Question? = currentRoom?.getQuestionObject()
            val builder = AlertDialog.Builder(this)
            builder.setTitle(question?.question + "?")
            builder.setItems(question?.answers?.toTypedArray()){_, which ->
                if(which == question?.correctAnswer ){
                    val reward:String? = question.reward!!
                    val rewardType:String? = question.rewardType!!
                    if(rewardType == BACKPACK){
                        if (backPack?.containsValue(reward)!!){
                            Toast.makeText(this, "Correct, However, You already have a $reward in your backpack", Toast.LENGTH_LONG).show()
                        } else{
                            backPack?.put(backPack?.size.toString(), reward)
                            backPackRef.updateChildren(backPack!!)
                            Toast.makeText(this, "Correct, A $reward has been added to your backpack", Toast.LENGTH_LONG).show()
                        }
                    } else if(rewardType == ADVICE){
                        Toast.makeText(this, "Correct, Listen carefully: $reward", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    Toast.makeText(this, "Wrong Answer", Toast.LENGTH_LONG).show()
                }
            }
            val dialog = builder.create()
            dialog.show()
        })

        locationRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                locationCode = snapshot.value as String
                locationString = "You are at the ${roomMap[locationCode]}"
                currentRoom = houseMap?.get(locationCode)
                attendant = "A ${currentRoom?.attendant} is in the room."
                yourStatusTextView?.text = "$locationString\n$attendant\n$newBackPackString"
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        backPackRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                backPack?.clear()
                newBackPackString = ""
                for (item in snapshot.children){
                    backPack?.put(item.key as String, item.value as String)
                    newBackPackString += "Item: ${item.value as String}\n"
                }
                newBackPackString = "Your backpack contents are the following:\n${newBackPackString.trim()}"
                yourStatusTextView?.text = "$locationString\n$attendant\n$newBackPackString"
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        houseRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (item in snapshot.children){
                    houseMap?.put(item.key, item.getValue(Room::class.java))
                }
                currentRoom = houseMap?.get(locationCode)
                attendant = "A ${currentRoom?.attendant} is in the room."
                yourStatusTextView?.text = "$locationString\n$attendant\n${newBackPackString}"

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


    }



}