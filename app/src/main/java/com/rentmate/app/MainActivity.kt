package com.rentmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
				var showSplash by remember { mutableStateOf(true) }
				
				if (showSplash) {
					SplashScreen(
						onAnimationComplete = { showSplash = false }
					)
				} else {
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
}

private fun glassColorScheme() = lightColorScheme(
	primary = Color(0xFF2196F3),
	secondary = Color(0xFF42A5F5),
	background = Color(0xFFF0F8FF),
	surface = Color(0x80FFFFFF),
	surfaceVariant = Color(0x60FFFFFF),
	onSurface = Color(0xFF1A1A1A),
	onPrimary = Color.White,
	onBackground = Color(0xFF1A1A1A),
	onSecondary = Color.White,
	outline = Color(0x802196F3),
	outlineVariant = Color(0x402196F3)
)

@Composable
private fun SplashScreen(
	onAnimationComplete: () -> Unit
) {
	var animationStarted by remember { mutableStateOf(false) }
	
	LaunchedEffect(Unit) {
		animationStarted = true
		delay(2500) // Total animation duration
		onAnimationComplete()
	}
	
	// Logo scale animation
	val logoScale by animateFloatAsState(
		targetValue = if (animationStarted) 1f else 0.3f,
		animationSpec = spring(
			dampingRatio = 0.6f,
			stiffness = 300f
		),
		label = "logoScale"
	)
	
	// Logo rotation animation
	val logoRotation by animateFloatAsState(
		targetValue = if (animationStarted) 0f else 360f,
		animationSpec = spring(
			dampingRatio = 0.8f,
			stiffness = 200f
		),
		label = "logoRotation"
	)
	
	// Logo alpha animation
	val logoAlpha by animateFloatAsState(
		targetValue = if (animationStarted) 1f else 0f,
		animationSpec = tween(800, easing = LinearEasing),
		label = "logoAlpha"
	)
	
	// App name slide animation
	val appNameOffset by animateFloatAsState(
		targetValue = if (animationStarted) 0f else 100f,
		animationSpec = spring(
			dampingRatio = 0.7f,
			stiffness = 400f
		),
		label = "appNameOffset"
	)
	
	// App name alpha animation
	val appNameAlpha by animateFloatAsState(
		targetValue = if (animationStarted) 1f else 0f,
		animationSpec = tween(600, delayMillis = 400),
		label = "appNameAlpha"
	)
	
	// Background gradient animation
	val infiniteTransition = rememberInfiniteTransition(label = "background")
	val backgroundOffset by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(3000, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		),
		label = "backgroundOffset"
	)
	
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(
					colors = listOf(
						Color(0xFFF0F8FF),
						Color(0xFFE6F3FF),
						Color(0xFFCCE7FF),
						Color(0xFFB3DBFF),
						Color(0xFF99CFFF)
					),
					startY = backgroundOffset * 1000f,
					endY = Float.POSITIVE_INFINITY
				)
			),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			// Animated Logo
			Box(
				modifier = Modifier
					.size(120.dp)
					.scale(logoScale)
					.alpha(logoAlpha)
			) {
				// Main logo circle with gradient
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(
							brush = Brush.radialGradient(
								colors = listOf(
									Color(0xFF2196F3),
									Color(0xFF1976D2),
									Color(0xFF1565C0)
								),
								radius = 200f
							),
							shape = androidx.compose.foundation.shape.CircleShape
						)
				)
				
				// Inner icon
				Icon(
					Icons.Default.Home,
					contentDescription = null,
					modifier = Modifier
						.fillMaxSize()
						.padding(32.dp),
					tint = Color.White
				)
				
				// Rotating accent ring
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(
							brush = Brush.sweepGradient(
								colors = listOf(
									Color.Transparent,
									Color(0x40FFFFFF),
									Color.Transparent
								)
							),
							shape = androidx.compose.foundation.shape.CircleShape
						)
						.alpha(0.6f)
				)
			}
			
			Spacer(Modifier.height(32.dp))
			
			// Animated App Name
			Text(
				"RentMate",
				style = MaterialTheme.typography.headlineLarge.copy(
					fontWeight = FontWeight.Bold,
					letterSpacing = 2.dp.value.sp
				),
				color = Color(0xFF1A1A1A),
				modifier = Modifier
					.alpha(appNameAlpha)
					.offset(y = appNameOffset.dp)
			)
			
			Spacer(Modifier.height(8.dp))
			
			// Subtitle
			Text(
				"Smart Expense Management",
				style = MaterialTheme.typography.bodyLarge,
				color = Color(0xFF666666),
				modifier = Modifier
					.alpha(appNameAlpha)
					.offset(y = appNameOffset.dp)
			)
		}
	}
}

@Composable
private fun GlassBackground(content: @Composable BoxScope.() -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(
					colors = listOf(
						Color(0xFFF0F8FF), // Alice Blue (very light blue)
						Color(0xFFE6F3FF), // Light Sky Blue
						Color(0xFFCCE7FF), // Sky Blue with 20% opacity
						Color(0xFFB3DBFF), // Medium Sky Blue
						Color(0xFF99CFFF)  // Deeper Sky Blue
					),
					startY = 0f,
					endY = Float.POSITIVE_INFINITY
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
	var isPressed by remember { mutableStateOf(false) }
	
	val scale by animateFloatAsState(
		targetValue = if (isPressed) 0.98f else 1f,
		animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
		label = "scale"
	)
	
	val alpha by animateFloatAsState(
		targetValue = if (isPressed) 0.8f else 0.9f,
		animationSpec = tween(150),
		label = "alpha"
	)
	
	Card(
		modifier = modifier
			.clip(RoundedCornerShape(24.dp))
			.scale(scale)
			.alpha(alpha)
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) { },
		shape = RoundedCornerShape(24.dp),
		colors = CardDefaults.cardColors(
			containerColor = Color(0x20FFFFFF)
		),
		elevation = CardDefaults.cardElevation(0.dp)
	) {
		Box(
			modifier = Modifier
				.background(
					brush = Brush.verticalGradient(
						colors = listOf(
							Color(0x30FFFFFF),
							Color(0x10FFFFFF)
						)
					)
				)
		) {
			Column(content = content)
		}
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
					tint = Color(0xFF2196F3)
				)
				Spacer(Modifier.height(16.dp))
				Text(
					"RentMate",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
					color = Color(0xFF1A1A1A)
				)
				Spacer(Modifier.height(24.dp))
				OutlinedTextField(
					value = username,
					onValueChange = { username = it; error = null },
					label = { Text("Username", color = Color(0xFF1A1A1A)) },
					singleLine = true,
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color(0xFF1A1A1A),
						unfocusedTextColor = Color(0xFF1A1A1A),
						focusedBorderColor = Color(0xFF2196F3),
						unfocusedBorderColor = Color(0x802196F3)
					)
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					value = password,
					onValueChange = { password = it; error = null },
					label = { Text("Password", color = Color(0xFF1A1A1A)) },
					visualTransformation = PasswordVisualTransformation(),
					singleLine = true,
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color(0xFF1A1A1A),
						unfocusedTextColor = Color(0xFF1A1A1A),
						focusedBorderColor = Color(0xFF2196F3),
						unfocusedBorderColor = Color(0x802196F3)
					)
				)
				if (error != null) {
					Spacer(Modifier.height(8.dp))
					Text(error!!, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
				}
				Spacer(Modifier.height(20.dp))
				Button(
					onClick = {
						val success = viewModel.login(username, password)
						if (!success) error = "Invalid credentials"
					},
					modifier = Modifier.fillMaxWidth(),
					colors = ButtonDefaults.buttonColors(containerColor = Color(0x402196F3)),
					shape = RoundedCornerShape(16.dp)
				) {
					Text("Login", color = Color.White)
				}
				Spacer(Modifier.height(16.dp))
				Text(
					"Demo accounts:\nAdmin/Admin • Emon/emon123\nBokhtiar/bokhtiar123 • Mahir/mahir123",
					color = Color(0xFF666666),
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

	GlassBackground {
		Scaffold(
			containerColor = Color.Transparent,
			bottomBar = {
				if (currentRoute in listOf("home", "expenses", "summary", "profile")) {
					GlassBottomNav(nav, currentRoute ?: "home")
				}
			}
		) { padding ->
			Box(
				modifier = Modifier
					.padding(padding)
					.fillMaxSize()
			) {
				NavHost(
					navController = nav, 
					startDestination = "home",
					enterTransition = {
						slideInHorizontally(
							initialOffsetX = { fullWidth -> fullWidth },
							animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
						) + fadeIn(animationSpec = tween(300))
					},
					exitTransition = {
						slideOutHorizontally(
							targetOffsetX = { fullWidth -> -fullWidth },
							animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
						) + fadeOut(animationSpec = tween(300))
					},
					popEnterTransition = {
						slideInHorizontally(
							initialOffsetX = { fullWidth -> -fullWidth },
							animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
						) + fadeIn(animationSpec = tween(300))
					},
					popExitTransition = {
						slideOutHorizontally(
							targetOffsetX = { fullWidth -> fullWidth },
							animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
						) + fadeOut(animationSpec = tween(300))
					}
				) {
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
}

@Composable
private fun GlassBottomNav(nav: NavHostController, currentRoute: String) {
	NavigationBar(
		containerColor = Color(0x20FFFFFF),
		modifier = Modifier
			.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
			.alpha(0.9f)
	) {
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
			selectedIconColor = Color(0xFF2196F3),
			selectedTextColor = Color(0xFF2196F3),
			unselectedIconColor = Color(0xFF666666),
			unselectedTextColor = Color(0xFF666666),
			indicatorColor = Color(0x202196F3)
		)
	)
}

@Composable
private fun HomeScreen(nav: NavHostController, db: AppDatabase, authVm: AuthViewModel, currentUser: User) {
	var recentExpenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
	var welcomeVisible by remember { mutableStateOf(false) }
	var cardsVisible by remember { mutableStateOf(false) }
	var contentVisible by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		recentExpenses = db.expenseDao().getAll().take(5)
		
		// Staggered animation timing
		welcomeVisible = true
		delay(200)
		cardsVisible = true
		delay(200)
		contentVisible = true
	}

	// Staggered animations for iOS 16-like entrance
	val welcomeAlpha by animateFloatAsState(
		targetValue = if (welcomeVisible) 1f else 0f,
		animationSpec = tween(600),
		label = "welcomeAlpha"
	)
	
	val welcomeOffset by animateFloatAsState(
		targetValue = if (welcomeVisible) 0f else 50f,
		animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
		label = "welcomeOffset"
	)
	
	val cardsAlpha by animateFloatAsState(
		targetValue = if (cardsVisible) 1f else 0f,
		animationSpec = tween(600),
		label = "cardsAlpha"
	)
	
	val cardsOffset by animateFloatAsState(
		targetValue = if (cardsVisible) 0f else 30f,
		animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
		label = "cardsOffset"
	)
	
	val contentAlpha by animateFloatAsState(
		targetValue = if (contentVisible) 1f else 0f,
		animationSpec = tween(600),
		label = "contentAlpha"
	)
	
	val contentOffset by animateFloatAsState(
		targetValue = if (contentVisible) 0f else 20f,
		animationSpec = spring(dampingRatio = 0.9f, stiffness = 600f),
		label = "contentOffset"
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			"Welcome, ${currentUser.username}!",
			style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
			modifier = Modifier
				.padding(vertical = 16.dp)
				.alpha(welcomeAlpha)
				.offset(y = welcomeOffset.dp),
			color = Color(0xFF1A1A1A)
		)

		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier
				.alpha(cardsAlpha)
				.offset(y = cardsOffset.dp)
		) {
			GlassCard(modifier = Modifier.weight(1f)) {
				Column(modifier = Modifier.padding(16.dp)) {
					Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color(0xFF2196F3))
					Spacer(Modifier.height(8.dp))
					TextButton(onClick = { nav.navigate("addExpense") }) {
						Text("Add Expense", color = Color(0xFF1A1A1A))
					}
				}
			}
			if (currentUser.isAdmin) {
				GlassCard(modifier = Modifier.weight(1f)) {
					Column(modifier = Modifier.padding(16.dp)) {
						Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color(0xFF2196F3))
						Spacer(Modifier.height(8.dp))
						TextButton(onClick = { nav.navigate("settings") }) {
							Text("Settings", color = Color(0xFF1A1A1A))
						}
					}
				}
			}
		}

		Spacer(Modifier.height(24.dp))

		Text(
			"Recent Expenses",
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
			color = Color(0xFF1A1A1A),
			modifier = Modifier
				.alpha(contentAlpha)
				.offset(y = contentOffset.dp)
		)
		Spacer(Modifier.height(12.dp))

		if (recentExpenses.isEmpty()) {
			GlassCard(modifier = Modifier.fillMaxWidth()) {
				Text(
					"No expenses yet. Tap 'Add Expense' to get started!",
					modifier = Modifier.padding(16.dp),
					color = Color(0xFF666666)
				)
			}
		} else {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
					.alpha(contentAlpha)
					.offset(y = contentOffset.dp)
			) {
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
				containerColor = Color(0x402196F3),
				contentColor = Color.White
			),
			shape = RoundedCornerShape(16.dp)
		) {
			Icon(Icons.Default.ExitToApp, contentDescription = null)
			Spacer(Modifier.width(8.dp))
			Text("Logout")
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
				Text(expense.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
				if (expense.description.isNotEmpty()) {
					Text(expense.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
				}
				Text(
					"Paid by $paidByName • ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date))}",
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF666666)
				)
			}
			Text(
				"৳${String.format("%.2f", expense.amount)}",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF2196F3)
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
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = Color(0xFF1A1A1A)
			)
				IconButton(onClick = { nav.navigate("addExpense") }) {
					Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF2196F3))
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
					Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF2196F3))
					Spacer(Modifier.height(16.dp))
					Text("No expenses yet", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1A1A1A))
					Spacer(Modifier.height(8.dp))
					Button(
						onClick = { nav.navigate("addExpense") },
						colors = ButtonDefaults.buttonColors(containerColor = Color(0x402196F3)),
						shape = RoundedCornerShape(16.dp)
					) {
						Text("Add First Expense", color = Color.White)
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
				Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
			}
			Text(
				"Add Expense",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = Color.White
			)
		}
		Spacer(Modifier.height(16.dp))

		GlassCard(modifier = Modifier.fillMaxWidth()) {
			Column(modifier = Modifier.padding(20.dp)) {
				OutlinedTextField(
					value = amount,
					onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
					label = { Text("Amount (৳)", color = Color.White) },
					modifier = Modifier.fillMaxWidth(),
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color.White,
						unfocusedTextColor = Color.White,
						focusedBorderColor = Color.White,
						unfocusedBorderColor = Color(0x80FFFFFF)
					)
				)
				Spacer(Modifier.height(12.dp))

				ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
					OutlinedTextField(
						value = category,
						onValueChange = {},
						readOnly = true,
						label = { Text("Category", color = Color.White) },
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
						modifier = Modifier
							.fillMaxWidth()
							.menuAnchor(),
						colors = OutlinedTextFieldDefaults.colors(
							focusedTextColor = Color.White,
							unfocusedTextColor = Color.White,
							focusedBorderColor = Color.White,
							unfocusedBorderColor = Color(0x80FFFFFF)
						)
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
					label = { Text("Description (optional)", color = Color.White) },
					modifier = Modifier.fillMaxWidth(),
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color.White,
						unfocusedTextColor = Color.White,
						focusedBorderColor = Color.White,
						unfocusedBorderColor = Color(0x80FFFFFF)
					)
				)
				Spacer(Modifier.height(16.dp))

				Text("Split among:", style = MaterialTheme.typography.titleSmall, color = Color.White)
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
							onCheckedChange = null,
							colors = CheckboxDefaults.colors(
								checkedColor = Color.White,
								uncheckedColor = Color(0x80FFFFFF)
							)
						)
						Text(user.username, color = Color.White)
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
					enabled = !saving && amount.toDoubleOrNull() != null && selectedParticipants.isNotEmpty(),
					colors = ButtonDefaults.buttonColors(containerColor = Color(0x40FFFFFF)),
					shape = RoundedCornerShape(16.dp)
				) {
					if (saving) {
						CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
					} else {
						Text("Save Expense", color = Color.White)
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

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			"Monthly Summary",
			style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
			modifier = Modifier.padding(vertical = 16.dp),
			color = Color(0xFF1A1A1A)
		)

		val total = expenses.sumOf { it.amount }

		GlassCard(modifier = Modifier.fillMaxWidth()) {
			Column(modifier = Modifier.padding(20.dp)) {
				Text("Total Expenses", style = MaterialTheme.typography.titleMedium, color = Color(0xFF666666))
				Text(
					"৳${String.format("%.2f", total)}",
					style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
					color = Color(0xFF2196F3)
				)
			}
		}

		Spacer(Modifier.height(16.dp))
		Text("Per Person Breakdown", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A1A1A))
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
						Text(user.username, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1A1A1A))
						Text(
							"৳${String.format("%.2f", userTotals[user.id] ?: 0.0)}",
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
							color = Color(0xFF2196F3)
						)
					}
				}
			}
		}
	}
}

@Composable
private fun ProfileScreen(authVm: AuthViewModel, currentUser: User) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			"Profile",
			style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
			modifier = Modifier.padding(vertical = 16.dp),
			color = Color(0xFF1A1A1A)
		)

		GlassCard(modifier = Modifier.fillMaxWidth()) {
			Column(modifier = Modifier.padding(20.dp)) {
				Icon(
					Icons.Default.Person,
					contentDescription = null,
					modifier = Modifier
						.size(80.dp)
						.align(Alignment.CenterHorizontally),
					tint = Color(0xFF2196F3)
				)
				Spacer(Modifier.height(16.dp))
				Text(
					currentUser.username,
					style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
					modifier = Modifier.align(Alignment.CenterHorizontally),
					color = Color(0xFF1A1A1A)
				)
				Text(
					currentUser.email,
					style = MaterialTheme.typography.bodyMedium,
					color = Color(0xFF666666),
					modifier = Modifier.align(Alignment.CenterHorizontally)
				)
				if (currentUser.isAdmin) {
					Spacer(Modifier.height(8.dp))
					Text(
						"Admin",
						style = MaterialTheme.typography.labelSmall,
						color = Color(0xFF2196F3),
						modifier = Modifier.align(Alignment.CenterHorizontally)
					)
				}
			}
		}

		Spacer(Modifier.height(24.dp))

		Button(
			onClick = { authVm.logout() },
			modifier = Modifier.fillMaxWidth(),
			colors = ButtonDefaults.buttonColors(containerColor = Color(0x40FF6B6B)),
			shape = RoundedCornerShape(16.dp)
		) {
			Icon(Icons.Default.ExitToApp, contentDescription = null)
			Spacer(Modifier.width(8.dp))
			Text("Logout", color = Color.White)
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
				Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
			}
			Text(
				"Flat Settings",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = Color.White
			)
		}
		Spacer(Modifier.height(16.dp))

		GlassCard(modifier = Modifier.fillMaxWidth()) {
			Column(Modifier.padding(20.dp)) {
				OutlinedTextField(
					name, 
					{ name = it }, 
					label = { Text("Flat name", color = Color.White) }, 
					modifier = Modifier.fillMaxWidth(),
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color.White,
						unfocusedTextColor = Color.White,
						focusedBorderColor = Color.White,
						unfocusedBorderColor = Color(0x80FFFFFF)
					)
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					address, 
					{ address = it }, 
					label = { Text("Address", color = Color.White) }, 
					modifier = Modifier.fillMaxWidth(),
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color.White,
						unfocusedTextColor = Color.White,
						focusedBorderColor = Color.White,
						unfocusedBorderColor = Color(0x80FFFFFF)
					)
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					dueDay,
					{ dueDay = it.filter { ch -> ch.isDigit() }.take(2) },
					label = { Text("Billing due day (e.g., 10)", color = Color.White) },
					modifier = Modifier.fillMaxWidth(),
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color.White,
						unfocusedTextColor = Color.White,
						focusedBorderColor = Color.White,
						unfocusedBorderColor = Color(0x80FFFFFF)
					)
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					bkash, 
					{ bkash = it }, 
					label = { Text("Bkash number", color = Color.White) }, 
					modifier = Modifier.fillMaxWidth(),
					colors = OutlinedTextFieldDefaults.colors(
						focusedTextColor = Color.White,
						unfocusedTextColor = Color.White,
						focusedBorderColor = Color.White,
						unfocusedBorderColor = Color(0x80FFFFFF)
					)
				)
				Spacer(Modifier.height(16.dp))

				Button(
					onClick = {
						viewModel.save(Flat(name = name, address = address, dueDay = dueDay.toIntOrNull() ?: 10, bkashNumber = bkash))
					},
					enabled = !isSaving,
					modifier = Modifier.fillMaxWidth(),
					colors = ButtonDefaults.buttonColors(containerColor = Color(0x40FFFFFF)),
					shape = RoundedCornerShape(16.dp)
				) {
					if (isSaving) {
						CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
					} else {
						Text("Save", color = Color.White)
					}
				}

				if (saveMessage != null) {
					Spacer(Modifier.height(8.dp))
					Text(
						text = saveMessage!!,
						color = if (saveMessage!!.startsWith("Error")) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
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
