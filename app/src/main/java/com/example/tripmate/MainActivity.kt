package com.example.tripmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tripmate.data.Trip
import com.example.tripmate.data.TripDao
import com.example.tripmate.data.TripDatabase
import com.example.tripmate.ui.theme.TripMateTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tripDao = TripDatabase.getDatabase(applicationContext).tripDao()

        setContent {
            TripMateTheme {
                TripHomeScreen(tripDao = tripDao)
            }
        }
    }
}

@Composable
fun TripHomeScreen(tripDao: TripDao) {
    val trips by tripDao.getAllTrips().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showAddTripDialog by remember { mutableStateOf(false) }

    val totalBudget = trips.sumOf { it.budget }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTripDialog = true }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "TripMate",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "Your next adventure starts here.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "My trips",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = trips.size.toString(),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        Column {
                            Text(
                                text = "Total budget",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "%.2f".format(totalBudget),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "My upcoming trips",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (trips.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "No trips yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button to create your first trip."
                            )
                        }
                    }
                }
            } else {
                items(trips, key = { it.id }) { trip ->
                    TripCard(trip = trip)
                }
            }
        }
    }

    if (showAddTripDialog) {
        AddTripDialog(
            onDismiss = { showAddTripDialog = false },
            onSave = { trip ->
                coroutineScope.launch {
                    tripDao.insertTrip(trip)
                }
                showAddTripDialog = false
            }
        )
    }
}

@Composable
fun TripCard(trip: Trip) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trip.name,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = trip.destination,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "${trip.startDate} to ${trip.endDate}")
            Text(text = "Budget: %.2f".format(trip.budget))

            if (trip.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = trip.notes)
            }
        }
    }
}

@Composable
fun AddTripDialog(
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add a new trip")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Trip name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start date (YYYY-MM-DD)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End date (YYYY-MM-DD)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budgetAmount = budget.toDoubleOrNull()

                    if (
                        name.isBlank() ||
                        destination.isBlank() ||
                        startDate.isBlank() ||
                        endDate.isBlank()
                    ) {
                        errorMessage = "Please fill in all required fields."
                    } else if (budgetAmount == null || budgetAmount < 0) {
                        errorMessage = "Please enter a valid budget."
                    } else {
                        onSave(
                            Trip(
                                name = name.trim(),
                                destination = destination.trim(),
                                startDate = startDate.trim(),
                                endDate = endDate.trim(),
                                budget = budgetAmount,
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
