package com.rentmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rentmate.app.data.AppDatabase
import com.rentmate.app.data.Expense
import com.rentmate.app.data.Flat
import com.rentmate.app.data.User
import com.rentmate.app.viewmodel.AuthViewModel
import com.rentmate.app.viewmodel.AuthViewModelFactory
import com.rentmate.app.viewmodel.SettingsViewModel
import com.rentmate.app.viewmodel.SettingsViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			MaterialTheme(colorScheme = glassColorScheme()) {
				val nav = rememberNavController()
				val context = LocalContext.current
				val db = remember { AppDatabase.getInstance(context) }
				val authVm: AuthViewModel = viewModel(factory = AuthViewModelFactory(db))
				val currentUser by authVm.currentUser.collectAsState()

				if (currentUser == null) {
					LoginScreen(onLogin = {}, viewModel = authVm)
				} else {
					MainApp(nav = nav, db = db, authVm = authVm, currentUser = currentUser!!)
				}
			}
		}
	}
}

private fun glassColorScheme() = darkColorScheme(
	primary = Color(0xFF5E8BFF),
	secondary = Color(0xFF80D0C7),
	background = Color(0xFF0F172A),
	surface = Color.White.copy(alpha = 0.08f),
	onSurface = Color.White,
	onPrimary = Color.White,
	onBackground = Color.White
)

@Composable
private fun GlassBackground(content: @Composable BoxScope.() -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.linearGradient(
					colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
				)
			),
		contentAlignment = Alignment.Center
	) {
		content()
	}
}

@Composable
private fun GlassCard(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit
) {
	Card(
		modifier = modifier,
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		elevation = CardDefaults.cardElevation(0.dp)
	) {
		Column(content = content)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(onLogin: () -> Unit, viewModel: AuthViewModel) {
	GlassBackground {
		var username by remember { mutableStateOf("") }
		var password by remember { mutableStateOf("") }
		var error by remember { mutableStateOf<String?>(null) }

		GlassCard(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp)
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					Icons.Default.Home,
					contentDescription = null,
					modifier = Modifier.size(64.dp),
					tint = MaterialTheme.colorScheme.primary
				)
				Spacer(Modifier.height(16.dp))
				Text(
					"RentMate",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
				)
				Spacer(Modifier.height(24.dp))
				OutlinedTextField(
					value = username,
					onValueChange = { username = it; error = null },
					label = { Text("Username") },
					singleLine = true
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					value = password,
					onValueChange = { password = it; error = null },
					label = { Text("Password") },
					visualTransformation = PasswordVisualTransformation(),
					singleLine = true
				)
				if (error != null) {
					Spacer(Modifier.height(8.dp))
					Text(error!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
				}
				Spacer(Modifier.height(20.dp))
				Button(
					onClick = {
						val success = viewModel.login(username, password)
						if (!success) error = "Invalid credentials"
					},
					modifier = Modifier.fillMaxWidth()
				) {
					Text("Login")
				}
				Spacer(Modifier.height(16.dp))
				Text(
					"Demo accounts:\nAdmin/Admin • Emon/emon123\nBokhtiar/bokhtiar123 • Mahir/mahir123",
					color = Color.LightGray,
					style = MaterialTheme.typography.bodySmall
				)
			}
		}
	}
}

@Composable
private fun MainApp(nav: NavHostController, db: AppDatabase, authVm: AuthViewModel, currentUser: User) {
	val navBackStackEntry by nav.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route

	Scaffold(
		bottomBar = {
			if (currentRoute in listOf("home", "expenses", "summary", "profile")) {
				BottomNav(nav, currentRoute ?: "home")
			}
		}
	) { padding ->
		Box(modifier = Modifier.padding(padding)) {
			NavHost(navController = nav, startDestination = "home") {
                composable("home") {
                    HomeScreen(nav, db, authVm, currentUser)
                }
				composable("expenses") {
					ExpensesScreen(nav, db, currentUser)
				}
				composable("summary") {
					SummaryScreen(db, currentUser)
				}
				composable("profile") {
					ProfileScreen(authVm, currentUser)
				}
				composable("settings") {
					val vm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(db))
					SettingsScreen(vm, nav)
				}
				composable("addExpense") {
					AddExpenseScreen(db, currentUser, nav)
				}
			}
		}
	}
}

@Composable
private fun BottomNav(nav: NavHostController, currentRoute: String) {
	NavigationBar(containerColor = Color(0xFF1E293B)) {
		BottomNavItem(Icons.Default.Home, "Home", "home", currentRoute, nav)
		BottomNavItem(Icons.Default.List, "Expenses", "expenses", currentRoute, nav)
		BottomNavItem(Icons.Default.Info, "Summary", "summary", currentRoute, nav)
		BottomNavItem(Icons.Default.Person, "Profile", "profile", currentRoute, nav)
	}
}

@Composable
private fun RowScope.BottomNavItem(
	icon: ImageVector,
	label: String,
	route: String,
	currentRoute: String,
	nav: NavHostController
) {
	NavigationBarItem(
		selected = currentRoute == route,
		onClick = { nav.navigate(route) { launchSingleTop = true } },
		icon = { Icon(icon, contentDescription = label) },
		label = { Text(label, style = MaterialTheme.typography.labelSmall) },
		colors = NavigationBarItemDefaults.colors(
			selectedIconColor = MaterialTheme.colorScheme.primary,
			selectedTextColor = MaterialTheme.colorScheme.primary,
			indicatorColor = MaterialTheme.colorScheme.surface
		)
	)
}

@Composable
private fun HomeScreen(nav: NavHostController, db: AppDatabase, authVm: AuthViewModel, currentUser: User) {
	var recentExpenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

	LaunchedEffect(Unit) {
		recentExpenses = db.expenseDao().getAll().take(5)
	}

	GlassBackground {
        Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Text(
				"Welcome, ${currentUser.username}!",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				modifier = Modifier.padding(vertical = 16.dp)
			)

			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				GlassCard(modifier = Modifier.weight(1f)) {
					Column(modifier = Modifier.padding(16.dp)) {
						Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
						Spacer(Modifier.height(8.dp))
						TextButton(onClick = { nav.navigate("addExpense") }) {
							Text("Add Expense")
						}
					}
				}
				if (currentUser.isAdmin) {
					GlassCard(modifier = Modifier.weight(1f)) {
						Column(modifier = Modifier.padding(16.dp)) {
							Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(32.dp))
							Spacer(Modifier.height(8.dp))
							TextButton(onClick = { nav.navigate("settings") }) {
								Text("Settings")
							}
						}
					}
				}
			}

			Spacer(Modifier.height(24.dp))

			Text(
				"Recent Expenses",
				style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
			)
			Spacer(Modifier.height(12.dp))

			if (recentExpenses.isEmpty()) {
				GlassCard(modifier = Modifier.fillMaxWidth()) {
					Text(
						"No expenses yet. Tap 'Add Expense' to get started!",
						modifier = Modifier.padding(16.dp),
						color = Color.LightGray
					)
				}
			} else {
				LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					items(recentExpenses) { expense ->
						ExpenseCard(expense, db)
					}
				}
			}

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { authVm.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
		}
	}
}

@Composable
private fun ExpenseCard(expense: Expense, db: AppDatabase) {
	var paidByName by remember { mutableStateOf("") }

	LaunchedEffect(expense.paidBy) {
		paidByName = db.userDao().getById(expense.paidBy)?.username ?: "Unknown"
	}

	GlassCard(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.padding(16.dp)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Column {
				Text(expense.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
				if (expense.description.isNotEmpty()) {
					Text(expense.description, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
				}
				Text(
					"Paid by $paidByName • ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date))}",
					style = MaterialTheme.typography.bodySmall,
					color = Color.Gray
				)
			}
			Text(
				"৳${String.format("%.2f", expense.amount)}",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.primary
			)
		}
	}
}

@Composable
private fun ExpensesScreen(nav: NavHostController, db: AppDatabase, currentUser: User) {
	var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

	LaunchedEffect(Unit) {
		expenses = db.expenseDao().getAll()
	}

	GlassBackground {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					"All Expenses",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
				)
				IconButton(onClick = { nav.navigate("addExpense") }) {
					Icon(Icons.Default.Add, contentDescription = "Add")
				}
			}
			Spacer(Modifier.height(16.dp))

			if (expenses.isEmpty()) {
				GlassCard(modifier = Modifier.fillMaxWidth()) {
					Column(
						modifier = Modifier
							.padding(32.dp)
							.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp))
						Spacer(Modifier.height(16.dp))
						Text("No expenses yet", style = MaterialTheme.typography.titleMedium)
						Spacer(Modifier.height(8.dp))
						Button(onClick = { nav.navigate("addExpense") }) {
							Text("Add First Expense")
						}
					}
				}
			} else {
				LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					items(expenses) { expense ->
						ExpenseCard(expense, db)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseScreen(db: AppDatabase, currentUser: User, nav: NavHostController) {
	var amount by remember { mutableStateOf("") }
	var category by remember { mutableStateOf("Rent") }
	var description by remember { mutableStateOf("") }
	var expanded by remember { mutableStateOf(false) }
	var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
	var selectedParticipants by remember { mutableStateOf<Set<Long>>(emptySet()) }
	var saving by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		allUsers = db.userDao().getAll()
		selectedParticipants = allUsers.map { it.id }.toSet()
	}

	GlassBackground {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = { nav.popBackStack() }) {
					Icon(Icons.Default.ArrowBack, contentDescription = "Back")
				}
				Text(
					"Add Expense",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
				)
			}
			Spacer(Modifier.height(16.dp))

			GlassCard(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(20.dp)) {
					OutlinedTextField(
						value = amount,
						onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
						label = { Text("Amount (৳)") },
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(Modifier.height(12.dp))

					ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
						OutlinedTextField(
							value = category,
							onValueChange = {},
							readOnly = true,
							label = { Text("Category") },
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor()
						)
						ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
							listOf("Rent", "Utilities", "Meals", "Extras").forEach { cat ->
								DropdownMenuItem(
									text = { Text(cat) },
									onClick = { category = cat; expanded = false }
								)
							}
						}
					}
					Spacer(Modifier.height(12.dp))

					OutlinedTextField(
						value = description,
						onValueChange = { description = it },
						label = { Text("Description (optional)") },
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(Modifier.height(16.dp))

					Text("Split among:", style = MaterialTheme.typography.titleSmall)
					Spacer(Modifier.height(8.dp))

					allUsers.forEach { user ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable {
									selectedParticipants = if (selectedParticipants.contains(user.id)) {
										selectedParticipants - user.id
									} else {
										selectedParticipants + user.id
									}
								}
								.padding(vertical = 4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Checkbox(
								checked = selectedParticipants.contains(user.id),
								onCheckedChange = null
							)
							Text(user.username)
						}
					}

					Spacer(Modifier.height(20.dp))

					Button(
						onClick = {
							saving = true
							GlobalScope.launch(Dispatchers.IO) {
								val exp = Expense(
									amount = amount.toDoubleOrNull() ?: 0.0,
									category = category,
									description = description,
									date = System.currentTimeMillis(),
									paidBy = currentUser.id,
									participants = selectedParticipants.joinToString(","),
									createdBy = currentUser.id
								)
								db.expenseDao().insert(exp)
								withContext(Dispatchers.Main) {
									nav.popBackStack()
								}
							}
						},
						modifier = Modifier.fillMaxWidth(),
						enabled = !saving && amount.toDoubleOrNull() != null && selectedParticipants.isNotEmpty()
					) {
						if (saving) {
							CircularProgressIndicator(modifier = Modifier.size(16.dp))
						} else {
							Text("Save Expense")
						}
					}
				}
			}
		}
	}
}

@Composable
private fun SummaryScreen(db: AppDatabase, currentUser: User) {
	var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
	var users by remember { mutableStateOf<List<User>>(emptyList()) }
	var userTotals by remember { mutableStateOf<Map<Long, Double>>(emptyMap()) }

	LaunchedEffect(Unit) {
		expenses = db.expenseDao().getAll()
		users = db.userDao().getAll()

		val totals = mutableMapOf<Long, Double>()
		users.forEach { user -> totals[user.id] = 0.0 }

		expenses.forEach { expense ->
			val participantIds = expense.participants.split(",").mapNotNull { it.toLongOrNull() }
			val share = expense.amount / participantIds.size
			participantIds.forEach { id ->
				totals[id] = (totals[id] ?: 0.0) + share
			}
		}
		userTotals = totals
	}

	GlassBackground {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Text(
				"Monthly Summary",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				modifier = Modifier.padding(vertical = 16.dp)
			)

			val total = expenses.sumOf { it.amount }

			GlassCard(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(20.dp)) {
					Text("Total Expenses", style = MaterialTheme.typography.titleMedium, color = Color.LightGray)
					Text(
						"৳${String.format("%.2f", total)}",
						style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.primary
					)
				}
			}

			Spacer(Modifier.height(16.dp))
			Text("Per Person Breakdown", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
			Spacer(Modifier.height(12.dp))

			LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				items(users) { user ->
					GlassCard(modifier = Modifier.fillMaxWidth()) {
						Row(
							modifier = Modifier
								.padding(16.dp)
								.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(user.username, style = MaterialTheme.typography.titleMedium)
							Text(
								"৳${String.format("%.2f", userTotals[user.id] ?: 0.0)}",
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.secondary
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ProfileScreen(authVm: AuthViewModel, currentUser: User) {
	GlassBackground {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Text(
				"Profile",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				modifier = Modifier.padding(vertical = 16.dp)
			)

			GlassCard(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(20.dp)) {
					Icon(
						Icons.Default.Person,
						contentDescription = null,
						modifier = Modifier
							.size(80.dp)
							.align(Alignment.CenterHorizontally),
						tint = MaterialTheme.colorScheme.primary
					)
					Spacer(Modifier.height(16.dp))
					Text(
						currentUser.username,
						style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
						modifier = Modifier.align(Alignment.CenterHorizontally)
					)
					Text(
						currentUser.email,
						style = MaterialTheme.typography.bodyMedium,
						color = Color.LightGray,
						modifier = Modifier.align(Alignment.CenterHorizontally)
					)
					if (currentUser.isAdmin) {
						Spacer(Modifier.height(8.dp))
						Text(
							"Admin",
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.primary,
							modifier = Modifier.align(Alignment.CenterHorizontally)
						)
					}
				}
			}

			Spacer(Modifier.height(24.dp))

			Button(
				onClick = { authVm.logout() },
				modifier = Modifier.fillMaxWidth(),
				colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
			) {
				Icon(Icons.Default.ExitToApp, contentDescription = null)
				Spacer(Modifier.width(8.dp))
				Text("Logout")
			}
		}
	}
}

@Composable
private fun SettingsScreen(viewModel: SettingsViewModel, nav: NavHostController) {
	var name by remember { mutableStateOf(viewModel.flatName) }
	var address by remember { mutableStateOf(viewModel.address) }
	var dueDay by remember { mutableStateOf(viewModel.dueDay.toString()) }
	var bkash by remember { mutableStateOf(viewModel.bkash) }

	val isSaving by viewModel.isSaving.collectAsState()
	val saveMessage by viewModel.saveMessage.collectAsState()

	GlassBackground {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = { nav.popBackStack() }) {
					Icon(Icons.Default.ArrowBack, contentDescription = "Back")
				}
				Text(
					"Flat Settings",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
				)
			}
			Spacer(Modifier.height(16.dp))

			GlassCard(modifier = Modifier.fillMaxWidth()) {
				Column(Modifier.padding(20.dp)) {
					OutlinedTextField(name, { name = it }, label = { Text("Flat name") }, modifier = Modifier.fillMaxWidth())
					Spacer(Modifier.height(12.dp))
					OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
					Spacer(Modifier.height(12.dp))
					OutlinedTextField(
						dueDay,
						{ dueDay = it.filter { ch -> ch.isDigit() }.take(2) },
						label = { Text("Billing due day (e.g., 10)") },
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(Modifier.height(12.dp))
					OutlinedTextField(bkash, { bkash = it }, label = { Text("Bkash number") }, modifier = Modifier.fillMaxWidth())
					Spacer(Modifier.height(16.dp))

					Button(
						onClick = {
							viewModel.save(Flat(name = name, address = address, dueDay = dueDay.toIntOrNull() ?: 10, bkashNumber = bkash))
						},
						enabled = !isSaving,
						modifier = Modifier.fillMaxWidth()
					) {
						if (isSaving) {
							CircularProgressIndicator(modifier = Modifier.size(16.dp))
						} else {
							Text("Save")
						}
					}

					if (saveMessage != null) {
						Spacer(Modifier.height(8.dp))
						Text(
							text = saveMessage!!,
							color = if (saveMessage!!.startsWith("Error")) Color.Red else Color.Green,
							style = MaterialTheme.typography.bodySmall
						)
						LaunchedEffect(saveMessage) {
							delay(3000)
							viewModel.clearMessage()
						}
					}
				}
			}
		}
	}
}
