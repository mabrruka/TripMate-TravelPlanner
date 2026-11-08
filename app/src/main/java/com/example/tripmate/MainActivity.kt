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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.tripmate.data.Expense
import com.example.tripmate.data.PackingItem
import com.example.tripmate.data.TripActivity
import com.example.tripmate.data.TripDao
import com.example.tripmate.data.TripDatabase
import com.example.tripmate.ui.theme.TripMateTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tripDao = TripDatabase.getDatabase(applicationContext).tripDao()

        setContent {
            TripMateTheme {
                TripMateApp(tripDao = tripDao)
            }
        }
    }
}

@Composable
fun TripMateApp(tripDao: TripDao) {
    var selectedTripId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf("Home") }

    val selectedTrip by tripDao
        .getTripById(selectedTripId ?: -1)
        .collectAsState(initial = null)

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = { selectedTab = "Home" },
                    icon = { Text("⌂") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == "Trips",
                    onClick = { selectedTab = "Trips" },
                    icon = { Text("✈") },
                    label = { Text("Trips") }
                )

                NavigationBarItem(
                    selected = selectedTab == "Budget",
                    onClick = { selectedTab = "Budget" },
                    icon = { Text("$") },
                    label = { Text("Budget") }
                )

                NavigationBarItem(
                    selected = selectedTab == "Packing",
                    onClick = { selectedTab = "Packing" },
                    icon = { Text("✓") },
                    label = { Text("Packing") }
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "Home" -> {
                    TripHomeScreen(
                        tripDao = tripDao,
                        onTripClick = { tripId ->
                            selectedTripId = tripId
                            selectedTab = "Trips"
                        }
                    )
                }

                "Trips" -> {
                    if (selectedTripId != null && selectedTrip != null) {
                        TripDetailsScreen(
                            trip = selectedTrip!!,
                            tripDao = tripDao,
                            onBack = { selectedTripId = null }
                        )
                    } else {
                        TripListScreen(
                            tripDao = tripDao,
                            title = "My trips",
                            onTripClick = { tripId ->
                                selectedTripId = tripId
                            }
                        )
                    }
                }

                "Budget" -> {
                    if (selectedTripId != null && selectedTrip != null) {
                        BudgetScreen(
                            trip = selectedTrip!!,
                            tripDao = tripDao,
                            onBack = {
                                selectedTripId = null
                                selectedTab = "Trips"
                            }
                        )
                    } else {
                        TripListScreen(
                            tripDao = tripDao,
                            title = "Choose a trip for your budget",
                            onTripClick = { tripId ->
                                selectedTripId = tripId
                            }
                        )
                    }
                }

                "Packing" -> {
                    if (selectedTripId != null && selectedTrip != null) {
                        PackingScreen(
                            trip = selectedTrip!!,
                            tripDao = tripDao,
                            onBack = {
                                selectedTripId = null
                                selectedTab = "Trips"
                            }
                        )
                    } else {
                        TripListScreen(
                            tripDao = tripDao,
                            title = "Choose a trip for packing",
                            onTripClick = { tripId ->
                                selectedTripId = tripId
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripListScreen(
    tripDao: TripDao,
    title: String,
    onTripClick: (Int) -> Unit
) {
    val trips by tripDao.getAllTrips().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            if (trips.isEmpty()) {
                item {
                    Text("No trips yet. Go to Home and tap + to create your first trip.")
                }
            } else {
                items(trips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) },
                        onDelete = {
                            coroutineScope.launch {
                                tripDao.deleteTripCompletely(trip)
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun TripHomeScreen(
    tripDao: TripDao,
    onTripClick: (Int) -> Unit
) {
    val trips by tripDao.getAllTrips().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showAddTripDialog by remember { mutableStateOf(false) }

    val totalBudget = trips.sumOf { it.budget }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTripDialog = true }) {
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
                            Text("My trips", style = MaterialTheme.typography.titleMedium)
                            Text(
                                trips.size.toString(),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Column {
                            Text("Total budget", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "%.2f".format(totalBudget),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "My upcoming trips",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (trips.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("No trips yet", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap the + button to create your first trip.")
                        }
                    }
                }
            } else {
                items(trips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) },
                        onDelete = {
                            coroutineScope.launch {
                                tripDao.deleteTripCompletely(trip)
                            }
                        }
                    )
                    @Composable
                    fun TripCard(
                        trip: Trip,
                        onClick: () -> Unit,
                        onDelete: () -> Unit
                    ) {
                        var showDeleteConfirmation by remember(trip.id) {
                            mutableStateOf(false)
                        }

                        Card(
                            onClick = onClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    trip.name,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    trip.destination,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("${trip.startDate} to ${trip.endDate}")
                                Text("Budget: %.2f".format(trip.budget))

                                if (trip.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(trip.notes)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Tap to view itinerary →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                TextButton(
                                    onClick = {
                                        showDeleteConfirmation = true
                                    }
                                ) {
                                    Text(
                                        "Delete trip",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = {
                                    showDeleteConfirmation = false
                                },
                                title = {
                                    Text("Delete trip?")
                                },
                                text = {
                                    Text(
                                        "Are you sure you want to delete \"${trip.name}\"? " +
                                                "This will also permanently delete its itinerary, expenses, and packing items."
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDelete()
                                            showDeleteConfirmation = false
                                        }
                                    ) {
                                        Text(
                                            "Delete",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteConfirmation = false
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }

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
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember(trip.id) {
        mutableStateOf(false)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                trip.name,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                trip.destination,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("${trip.startDate} to ${trip.endDate}")
            Text("Budget: %.2f".format(trip.budget))

            if (trip.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(trip.notes)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tap to view itinerary →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            TextButton(
                onClick = {
                    showDeleteConfirmation = true
                }
            ) {
                Text(
                    "Delete trip",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Delete trip?")
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${trip.name}\"? " +
                            "This will also permanently delete its itinerary, expenses, and packing items."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun TripDetailsScreen(
    trip: Trip,
    tripDao: TripDao,
    onBack: () -> Unit
) {
    var showBudgetScreen by remember(trip.id) { mutableStateOf(false) }
    var showPackingScreen by remember(trip.id) { mutableStateOf(false) }

    if (showBudgetScreen) {
        BudgetScreen(
            trip = trip,
            tripDao = tripDao,
            onBack = { showBudgetScreen = false }
        )
    } else if (showPackingScreen) {
        PackingScreen(
            trip = trip,
            tripDao = tripDao,
            onBack = { showPackingScreen = false }
        )
    } else {
        val activities by tripDao.getActivities(trip.id)
            .collectAsState(initial = emptyList())
        val coroutineScope = rememberCoroutineScope()
        var showAddActivityDialog by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddActivityDialog = true }) {
                    Text("+")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TextButton(onClick = onBack) {
                        Text("← Back to trips")
                    }
                    Text(trip.name, style = MaterialTheme.typography.headlineLarge)
                    Text(trip.destination, style = MaterialTheme.typography.titleLarge)
                    Text("${trip.startDate} to ${trip.endDate}")
                    Text("Trip budget: %.2f".format(trip.budget))

                    if (trip.notes.isNotBlank()) {
                        Text("Notes: ${trip.notes}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showBudgetScreen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open trip budget")
                    }

                    Button(
                        onClick = { showPackingScreen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open packing checklist")
                    }
                }

                item {
                    Text("Your itinerary", style = MaterialTheme.typography.titleLarge)
                    Text("Tap + to add an activity to this trip.")
                }

                if (activities.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "No activities added yet.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(activities, key = { it.id }) { activity ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        activity.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Day ${activity.dayNumber} • ${activity.time}")
                                }
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            tripDao.deleteActivity(activity)
                                        }
                                    }
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddActivityDialog) {
            AddActivityDialog(
                tripId = trip.id,
                onDismiss = { showAddActivityDialog = false },
                onSave = { activity ->
                    coroutineScope.launch {
                        tripDao.insertActivity(activity)
                    }
                    showAddActivityDialog = false
                }
            )
        }
    }
}

@Composable
fun BudgetScreen(
    trip: Trip,
    tripDao: TripDao,
    onBack: () -> Unit
) {
    val expenses by tripDao.getExpenses(trip.id)
        .collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val spent = expenses.sumOf { it.amount }
    val remaining = trip.budget - spent
    val progress = if (trip.budget > 0) {
        (spent / trip.budget).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExpenseDialog = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TextButton(onClick = onBack) {
                    Text("← Back to trip")
                }
                Text("Trip budget", style = MaterialTheme.typography.headlineLarge)
                Text(trip.name, style = MaterialTheme.typography.titleLarge)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Total budget")
                        Text(
                            "%.2f".format(trip.budget),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text("Spent: %.2f".format(spent))
                        Text("Remaining: %.2f".format(remaining))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("${(progress * 100).toInt()}% of budget used")

                        if (remaining < 0) {
                            Text(
                                "You are over budget!",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Text("Your expenses", style = MaterialTheme.typography.titleLarge)
                Text("Tap + to record a new expense.")
            }

            if (expenses.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No expenses recorded yet.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    expense.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(expense.category)
                                Text("%.2f".format(expense.amount))
                            }

                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        tripDao.deleteExpense(expense)
                                    }
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            tripId = trip.id,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { expense ->
                coroutineScope.launch {
                    tripDao.insertExpense(expense)
                }
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun AddExpenseDialog(
    tripId: Int,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add an expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Food, Transport)") },
                    singleLine = true
                )
                if (errorMessage.isNotBlank()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    if (title.isBlank() || category.isBlank()) {
                        errorMessage = "Enter an expense name and category."
                    } else if (value == null || value <= 0) {
                        errorMessage = "Enter an amount greater than zero."
                    } else {
                        onSave(
                            Expense(
                                tripId = tripId,
                                title = title.trim(),
                                amount = value,
                                category = category.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PackingScreen(
    trip: Trip,
    tripDao: TripDao,
    onBack: () -> Unit
) {
    val packingItems by tripDao.getPackingItems(trip.id)
        .collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    val packedCount = packingItems.count { it.isPacked }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TextButton(onClick = onBack) {
                    Text("← Back to trip")
                }
                Text("Packing checklist", style = MaterialTheme.typography.headlineLarge)
                Text(trip.name, style = MaterialTheme.typography.titleLarge)
                Text("$packedCount of ${packingItems.size} items packed")
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (packingItems.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Your packing list is empty. Tap + to add your first item.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(packingItems, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isPacked,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            tripDao.updatePackingItem(
                                                item.copy(isPacked = checked)
                                            )
                                        }
                                    }
                                )
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (item.isPacked) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        tripDao.deletePackingItem(item)
                                    }
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPackingItemDialog(
            tripId = trip.id,
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                coroutineScope.launch {
                    tripDao.insertPackingItem(item)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddPackingItemDialog(
    tripId: Int,
    onDismiss: () -> Unit,
    onSave: (PackingItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add packing item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name") },
                    singleLine = true
                )
                if (errorMessage.isNotBlank()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter an item name."
                    } else {
                        onSave(
                            PackingItem(
                                tripId = tripId,
                                name = name.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Add item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddActivityDialog(
    tripId: Int,
    onDismiss: () -> Unit,
    onSave: (TripActivity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dayNumber by remember { mutableStateOf("1") }
    var time by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add itinerary activity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = dayNumber,
                    onValueChange = { dayNumber = it },
                    label = { Text("Day number") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 10:30 AM)") },
                    singleLine = true
                )
                if (errorMessage.isNotBlank()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val day = dayNumber.toIntOrNull()
                    if (title.isBlank() || time.isBlank()) {
                        errorMessage = "Enter an activity name and time."
                    } else if (day == null || day < 1) {
                        errorMessage = "Day number must be 1 or greater."
                    } else {
                        onSave(
                            TripActivity(
                                tripId = tripId,
                                title = title.trim(),
                                dayNumber = day,
                                time = time.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save activity")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
        title = { Text("Add a new trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        errorMessage,
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
