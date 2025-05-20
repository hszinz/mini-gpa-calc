package com.sudh.gpacalculator

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var numberInput: EditText
    private lateinit var generateButton: Button
    private lateinit var containerLayout: LinearLayout

    private val subjectOptions = listOf("Differential", "Python", "OOPS", "Calculus", "Java")
    private val creditOptions = listOf("1", "1.5", "2", "3", "4")
    private val gradeSystem = mapOf(
        Pair(90, 10.0 to "S"),
        Pair(80, 9.0 to "A"),
        Pair(70, 8.0 to "B"),
        Pair(60, 7.0 to "C"),
        Pair(50, 6.0 to "D"),
        Pair(40, 5.0 to "E"),
        Pair(0, 0.0 to "F")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberInput = findViewById(R.id.numberInput)
        generateButton = findViewById(R.id.generateButton)
        containerLayout = findViewById(R.id.containerLayout)

        generateButton.setOnClickListener {
            val numText = numberInput.text.toString()
            val number = numText.toIntOrNull()

            if (number == null || number < 1 || number > 5) {
                Toast.makeText(this, "Please enter a number between 1 and 5", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateRows(number)
        }
    }

    private fun generateRows(count: Int) {
        containerLayout.removeAllViews()

        for (i in 1..count) {
            val rowView = layoutInflater.inflate(R.layout.row_layout, containerLayout, false)

            rowView.background = ContextCompat.getDrawable(this, R.drawable.box_background)
            rowView.setPadding(32, 32, 32, 32)

            val inputField = rowView.findViewById<EditText>(R.id.inputField)
            val spinner1 = rowView.findViewById<Spinner>(R.id.spinner1)
            val spinner2 = rowView.findViewById<Spinner>(R.id.spinner2)

            inputField.setTextColor(Color.WHITE)
            inputField.setHintTextColor(Color.LTGRAY)
            inputField.hint = "Enter marks (0-100)"

            val subjectAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Subject") + subjectOptions
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            val creditAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Credits") + creditOptions
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinner1.adapter = subjectAdapter
            spinner2.adapter = creditAdapter

            spinner1.setPopupBackgroundResource(android.R.color.black)
            spinner2.setPopupBackgroundResource(android.R.color.black)

            containerLayout.addView(rowView)
        }

        val resultButtonView = layoutInflater.inflate(R.layout.button_result, containerLayout, false) as Button
        resultButtonView.setOnClickListener {
            if (validateAndShowResults()) {
                showResultsDialog()
            }
        }
        containerLayout.addView(resultButtonView)
    }

    private fun validateAndShowResults(): Boolean {
        var allValid = true
        val errorMessage = StringBuilder()

        for (i in 0 until containerLayout.childCount - 1) {
            val row = containerLayout.getChildAt(i) as? LinearLayout ?: continue
            val inputField = row.findViewById<EditText>(R.id.inputField)
            val spinner1 = row.findViewById<Spinner>(R.id.spinner1)
            val spinner2 = row.findViewById<Spinner>(R.id.spinner2)

            if (spinner1.selectedItemPosition == 0) {
                allValid = false
                errorMessage.append("• Row ${i+1}: Please select a subject\n")
            }

            if (spinner2.selectedItemPosition == 0) {
                allValid = false
                errorMessage.append("• Row ${i+1}: Please select credit value\n")
            }

            val marksText = inputField.text.toString()
            if (marksText.isBlank()) {
                allValid = false
                errorMessage.append("• Row ${i+1}: Please enter marks\n")
            } else {
                val marks = marksText.toIntOrNull()
                if (marks == null || marks < 0 || marks > 100) {
                    allValid = false
                    errorMessage.append("• Row ${i+1}: Marks must be between 0-100\n")
                }
            }
        }

        if (!allValid) {
            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_LONG).show()
        }
        return allValid
    }

    private fun showResultsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_result, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)

        var totalGradePoints = 0.0
        var totalCredits = 0.0

        for (i in 0 until containerLayout.childCount - 1) {
            val row = containerLayout.getChildAt(i) as? LinearLayout ?: continue
            val inputField = row.findViewById<EditText>(R.id.inputField)
            val creditSpinner = row.findViewById<Spinner>(R.id.spinner2)

            val marks = inputField.text.toString().toIntOrNull() ?: 0
            val credit = creditSpinner.selectedItem.toString().toDouble()

            val (gradePoint, _) = getGradePoint(marks)
            totalGradePoints += gradePoint * credit
            totalCredits += credit
        }

        val gpa = if (totalCredits > 0) totalGradePoints / totalCredits else 0.0

        dialogTitle.text = "GPA Results"
        dialogMessage.text = "Your GPA: ${"%.2f".format(gpa)}"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getGradePoint(marks: Int): Pair<Double, String> {
        return gradeSystem.entries.firstOrNull { marks >= it.key }?.value
            ?: (0.0 to "F")
    }
}