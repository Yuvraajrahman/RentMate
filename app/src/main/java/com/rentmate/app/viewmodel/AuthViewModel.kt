package com.rentmate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rentmate.app.data.AppDatabase
import kotlinx.coroutines.runBlocking

class AuthViewModel(private val db: AppDatabase) : ViewModel() {
	fun login(username: String, password: String): Boolean = runBlocking {
		val user = db.userDao().find(username.trim(), password.trim())
		user?.isAdmin == true
	}
}

class AuthViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return AuthViewModel(db) as T
	}
}
