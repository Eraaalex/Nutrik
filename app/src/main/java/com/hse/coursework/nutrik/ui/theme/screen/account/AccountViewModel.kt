package com.hse.coursework.nutrik.ui.theme.screen.account

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.dto.Gender
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = mutableStateOf(User())
    val user: State<User> = _user

    init {
        viewModelScope.launch {
            userRepository.getUser()?.let {
                _user.value = it
            }
        }
    }

    private fun updateAndSave(updatedUser: User) {
        _user.value = updatedUser
        viewModelScope.launch {
            userRepository.saveUser(updatedUser)
        }
    }

    fun updateAge(newAge: Int) {
        updateAndSave(_user.value.copy(age = newAge))
    }

    fun updateGender(newGender: Gender) {
        updateAndSave(_user.value.copy(gender = newGender))
    }

    fun updateRestrictions(newRestrictions: List<Restriction>) {
        updateAndSave(_user.value.copy(restrictions = newRestrictions))
    }

    fun toggleRestriction(restriction: Restriction) {
        val current = _user.value.restrictions
        val updated = if (restriction in current) current - restriction else current + restriction
        updateAndSave(_user.value.copy(restrictions = updated))
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        userRepository.clearCache()
    }
}
