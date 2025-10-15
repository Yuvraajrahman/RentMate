package com.rentmate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rentmate.app.data.AppDatabase
import com.rentmate.app.data.Flat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsViewModel(private val db: AppDatabase) : ViewModel() {
	var flatName: String = ""; private set
	var address: String = ""; private set
	var dueDay: Int = 10; private set
	var bkash: String = ""; private set
	
	private val _isSaving = MutableStateFlow(false)
	val isSaving = _isSaving.asStateFlow()
	
	private val _saveMessage = MutableStateFlow<String?>(null)
	val saveMessage = _saveMessage.asStateFlow()

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
			_isSaving.value = true
			try {
				db.flatDao().upsert(flat.copy(key = 1))
				flatName = flat.name
				address = flat.address
				dueDay = flat.dueDay
				bkash = flat.bkashNumber
				_saveMessage.value = "Settings saved successfully!"
			} catch (e: Exception) {
				_saveMessage.value = "Error saving settings: ${e.message}"
			} finally {
				_isSaving.value = false
			}
		}
	}
	
	fun clearMessage() {
		_saveMessage.value = null
	}
}

class SettingsViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
			return SettingsViewModel(db) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
	}
}
