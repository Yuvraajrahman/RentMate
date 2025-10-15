package com.rentmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rentmate.app.data.AppDatabase
import com.rentmate.app.data.Flat
import com.rentmate.app.data.User
import com.rentmate.app.viewmodel.AuthViewModel
import com.rentmate.app.viewmodel.AuthViewModelFactory
import com.rentmate.app.viewmodel.SettingsViewModel
import com.rentmate.app.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			MaterialTheme(colorScheme = glassColorScheme()) {
				val nav = rememberNavController()
				NavGraph(nav)
			}
		}
	}
}

private fun glassColorScheme() = lightColorScheme(
	primary = Color(0xFF5E8BFF),
	secondary = Color(0xFF80D0C7),
	background = Color(0xFF0F172A),
	surface = Color.White.copy(alpha = 0.08f),
	onSurface = Color.White,
	onPrimary = Color.White
)

@Composable
private fun GlassBackground(content: @Composable BoxScope.() -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.linearGradient(
					colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
				)
			),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.blur(30.dp)
				.alpha(0.9f)
		)
		content()
	}
}

@Composable
private fun NavGraph(nav: NavHostController) {
	NavHost(navController = nav, startDestination = "login") {
		composable("login") {
			val db = remember { AppDatabase.getInstance(LocalContext.current) }
			val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(db))
			LoginScreen(
				onLogin = { nav.navigate("settings") },
				viewModel = vm
			)
		}
		composable("settings") {
			val db = remember { AppDatabase.getInstance(LocalContext.current) }
			val vm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(db))
			SettingsScreen(vm)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(onLogin: () -> Unit, viewModel: AuthViewModel) {
	GlassBackground {
		var username by remember { mutableStateOf("") }
		var password by remember { mutableStateOf("") }
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp),
			shape = RoundedCornerShape(24.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
		) {
			Column(
				modifier = Modifier.padding(20.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					"RentMate",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
				)
				Spacer(Modifier.height(16.dp))
				OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					label = { Text("Password") },
					visualTransformation = PasswordVisualTransformation()
				)
				Spacer(Modifier.height(20.dp))
				Button(onClick = {
					if (viewModel.login(username, password)) onLogin()
				}) { Text("Login") }
				Spacer(Modifier.height(6.dp))
				Text("Tip: Admin/Admin", color = Color.LightGray)
			}
		}
	}
}

@Composable
private fun SettingsScreen(viewModel: SettingsViewModel) {
	GlassBackground {
		var name by remember { mutableStateOf(viewModel.flatName) }
		var address by remember { mutableStateOf(viewModel.address) }
		var dueDay by remember { mutableStateOf(viewModel.dueDay.toString()) }
		var bkash by remember { mutableStateOf(viewModel.bkash) }

		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp),
			shape = RoundedCornerShape(24.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
		) {
			Column(Modifier.padding(20.dp)) {
				Text("Flat Settings", style = MaterialTheme.typography.titleLarge)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(name, { name = it }, label = { Text("Flat name") })
				Spacer(Modifier.height(8.dp))
				OutlinedTextField(address, { address = it }, label = { Text("Address") })
				Spacer(Modifier.height(8.dp))
				OutlinedTextField(dueDay, { dueDay = it.filter { ch -> ch.isDigit() }.take(2) }, label = { Text("Billing due day (e.g., 10)") })
				Spacer(Modifier.height(8.dp))
				OutlinedTextField(bkash, { bkash = it }, label = { Text("Bkash number") })
				Spacer(Modifier.height(16.dp))
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					Button(onClick = {
						viewModel.save(Flat(name = name, address = address, dueDay = dueDay.toIntOrNull() ?: 10, bkashNumber = bkash))
					}) { Text("Save") }
				}
			}
		}
	}
}
