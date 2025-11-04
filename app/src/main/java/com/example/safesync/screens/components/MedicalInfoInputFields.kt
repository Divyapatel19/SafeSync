package com.example.safesync.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MedicalInfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
fun HeightInputField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var numericValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("cm") }

    LaunchedEffect(value) {
        val parts = value.split(" ")
        if (parts.size == 2) {
            numericValue = parts[0]
            unit = parts[1]
        } else {
            numericValue = value
        }
    }

    var heightUnitExpanded by remember { mutableStateOf(false) }
    val heightUnits = listOf("cm", "in")

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        OutlinedTextField(
            value = numericValue,
            onValueChange = {
                numericValue = it
                onValueChange("$it $unit")
            },
            label = { Text("Height") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.padding(start = 8.dp).width(100.dp)) {
            OutlinedTextField(
                value = unit,
                onValueChange = { },
                label = { Text("Unit") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select Unit",
                        Modifier.clickable { heightUnitExpanded = true })
                },
                modifier = Modifier.clickable { heightUnitExpanded = true }
            )
            DropdownMenu(
                expanded = heightUnitExpanded,
                onDismissRequest = { heightUnitExpanded = false }
            ) {
                heightUnits.forEach { unitItem ->
                    DropdownMenuItem(
                        text = { Text(unitItem) },
                        onClick = {
                            unit = unitItem
                            onValueChange("$numericValue $unitItem")
                            heightUnitExpanded = false
                        }
                    )
                }
            }
        }
    }
}
