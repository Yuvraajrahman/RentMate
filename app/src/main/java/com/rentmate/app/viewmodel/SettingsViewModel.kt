package com.rentmate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rentmate.app.data.AppDatabase
import com.rentmate.app.data.Flat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsViewModel(private val db: AppDatabase) : ViewModel() {
	var flatName: String = ""; private set
	var address: String = ""; private set
	var dueDay: Int = 10; private set
	var bkash: String = ""; private set

	init {
		runBlocking {
			val f = db.flatDao().get() ?: Flat()
			flatName = f.name
			address = f.address
			dueDay = f.dueDay
			bkash = f.bkashNumber
		}
	}

	fun save(flat: Flat) {
		viewModelScope.launch(Dispatchers.IO) {
			db.flatDao().upsert(flat.copy(key = 1))
		}
		flatName = flat.name
		address = flat.address
		dueDay = flat.dueDay
		bkash = flat.bkashNumber
	}
}

class SettingsViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return SettingsViewModel(db) as T
	}
}
