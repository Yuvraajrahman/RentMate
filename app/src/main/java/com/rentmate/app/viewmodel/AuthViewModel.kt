package com.rentmate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rentmate.app.data.AppDatabase
import com.rentmate.app.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class AuthViewModel(private val db: AppDatabase) : ViewModel() {
	private val _currentUser = MutableStateFlow<User?>(null)
	val currentUser = _currentUser.asStateFlow()

	fun login(username: String, password: String): Boolean = runBlocking {
		val user = db.userDao().find(username.trim(), password.trim())
		if (user != null) {
			_currentUser.value = user
			true
		} else {
			false
		}
	}

	fun logout() {
		_currentUser.value = null
	}
}

class AuthViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
			return AuthViewModel(db) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
	}
}
