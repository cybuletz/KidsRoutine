package com.kidsroutine.feature.community.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.UserModel

@Composable
fun RatingDialog(
    contentId: String,
    contentType: String,
    onRate: (rating: Int, review: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRating by remember { mutableStateOf(0) }
    var review by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rate this ${if (contentType == "task") "task" else "challenge"}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Star rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { selectedRating = index + 1 }
                                .padding(4.dp),
                            tint = if (index < selectedRating) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }

                // Rating text
                Text(
                    text = when (selectedRating) {
                        0 -> "Rate this content"
                        1 -> "Poor"
                        2 -> "Fair"
                        3 -> "Good"
                        4 -> "Very Good"
                        5 -> "Excellent"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )

                // Review text
                OutlinedTextField(
                    value = review,
                    onValueChange = { review = it },
                    label = { Text("Review (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRating > 0) {
                        onRate(selectedRating, review)
                        onDismiss()
                    }
                },
                enabled = selectedRating > 0
            ) {
                Text("Submit Rating")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}