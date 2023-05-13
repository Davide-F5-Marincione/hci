package com.example.mobiliteam

import android.R.attr.text
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.listOut)
        val textInput = findViewById<TextInputEditText>(R.id.textIn)


        // initialize an array adapter
        val adapter:ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line
        )


        // attach the array adapter with list view
        listView.adapter = adapter


        // list view item click listener
//        listView.onItemClickListener = AdapterView.OnItemClickListener {
//                parent, view, position, id ->
//            val selectedItem = parent.getItemAtPosition(position)
//            textView.text = "Selected : $selectedItem"
//        }


        // add list view items programmatically
        val txtwt = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) return

                if (s.last() == '\n') {
                    adapter.add(s.toString())
                    textInput.removeTextChangedListener(this) //after this line you do the editing code
                    textInput.setText("")
                    Log.i("REACHES ON", "AO")
                    textInput.addTextChangedListener(this) // you register again for listener callbacks
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }
        }

        textInput.addTextChangedListener(txtwt);
    }
}