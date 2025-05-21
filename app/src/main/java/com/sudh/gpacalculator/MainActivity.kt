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

    // Simplified grade system using a list
    private val gradeSystem = listOf(
        Grade(90, 10.0, "S"),
        Grade(80, 9.0, "A"),
        Grade(70, 8.0, "B"),
        Grade(60, 7.0, "C"),
        Grade(50, 6.0, "D"),
        Grade(40, 5.0, "E"),
        Grade(0, 0.0, "F")
    )

    data class Grade(val minMarks: Int, val point: Double, val letter: String)

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
            } else {
                generateRows(number)
            }
        }
    }

    private fun generateRows(count: Int) {
        containerLayout.removeAllViews()

        for (i in 1..count) {
            val rowView = layoutInflater.inflate(R.layout.row_layout, containerLayout, false)

            rowView.background = ContextCompat.getDrawable(this, R.drawable.box_background)
            rowView.setPadding(32, 32, 32, 32)

            val inputField = rowView.findViewById<EditText>(R.id.inputField)
            val subjectSpinner = rowView.findViewById<Spinner>(R.id.spinner1)
            val creditSpinner = rowView.findViewById<Spinner>(R.id.spinner2)

            inputField.setTextColor(Color.WHITE)
            inputField.setHintTextColor(Color.LTGRAY)
            inputField.hint = "Enter marks (0-100)"

            subjectSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Subject") + subjectOptions
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            creditSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Credits") + creditOptions
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            subjectSpinner.setPopupBackgroundResource(android.R.color.black)
            creditSpinner.setPopupBackgroundResource(android.R.color.black)

            containerLayout.addView(rowView)
        }

        val resultButton = layoutInflater.inflate(R.layout.button_result, containerLayout, false) as Button
        resultButton.setOnClickListener {
            if (validateInputs()) {
                showResultsDialog()
            }
        }

        containerLayout.addView(resultButton)
    }

    private fun validateInputs(): Boolean {
        var valid = true
        val error = StringBuilder()

        for (i in 0 until containerLayout.childCount - 1) {
            val row = containerLayout.getChildAt(i) as? LinearLayout ?: continue
            val inputField = row.findViewById<EditText>(R.id.inputField)
            val subjectSpinner = row.findViewById<Spinner>(R.id.spinner1)
            val creditSpinner = row.findViewById<Spinner>(R.id.spinner2)

            if (subjectSpinner.selectedItemPosition == 0) {
                valid = false
                error.append("• Row ${i + 1}: Please select a subject\n")
            }

            if (creditSpinner.selectedItemPosition == 0) {
                valid = false
                error.append("• Row ${i + 1}: Please select credit value\n")
            }

            val marksText = inputField.text.toString()
            val marks = marksText.toIntOrNull()

            if (marksText.isBlank() || marks == null || marks < 0 || marks > 100) {
                valid = false
                error.append("• Row ${i + 1}: Marks must be between 0 and 100\n")
            }
        }

        if (!valid) {
            Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
        }

        return valid
    }

    private fun showResultsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_result, null)
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)

        var totalPoints = 0.0
        var totalCredits = 0.0

        for (i in 0 until containerLayout.childCount - 1) {
            val row = containerLayout.getChildAt(i) as LinearLayout
            val inputField = row.findViewById<EditText>(R.id.inputField)
            val creditSpinner = row.findViewById<Spinner>(R.id.spinner2)

            val marks = inputField.text.toString().toInt()
            val credits = creditSpinner.selectedItem.toString().toDouble()

            val gradePoint = getGradePoint(marks)
            totalPoints += gradePoint * credits
            totalCredits += credits
        }

        val gpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0

        title.text = "GPA Results"
        message.text = "Your GPA: %.2f".format(gpa)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getGradePoint(marks: Int): Double {
        for (grade in gradeSystem) {
            if (marks >= grade.minMarks) {
                return grade.point
            }
        }
        return 0.0
    }
}
