package com.kidsroutine.feature.pet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.engine.pet_engine.PetEngine
import com.kidsroutine.core.model.PetAccessory
import com.kidsroutine.core.model.PetAccessoryCategory
import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetSpecies
import com.kidsroutine.feature.pet.data.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetUiState(
    val pet: PetModel? = null,
    val isLoading: Boolean = false,
    val adoptionMode: Boolean = false,
    val selectedSpecies: PetSpecies? = null,
    val currentXp: Int = 0,
    val error: String? = null,
    val showShop: Boolean = false,
    val ownedAccessoryIds: List<String> = emptyList()
) {
    companion object {
        const val FEED_COST = 5

        val SHOP_ITEMS = listOf(
            PetAccessory("hat_crown", "Royal Crown", "👑", "A crown fit for royalty!", 25, PetAccessoryCategory.HAT, happinessBoost = 5),
            PetAccessory("hat_wizard", "Wizard Hat", "🧙", "Magical style!", 20, PetAccessoryCategory.HAT, happinessBoost = 3),
            PetAccessory("collar_star", "Star Collar", "⭐", "Shine bright!", 15, PetAccessoryCategory.COLLAR, happinessBoost = 2),
            PetAccessory("collar_bell", "Jingle Bell", "🔔", "Ding-a-ling!", 10, PetAccessoryCategory.COLLAR, energyBoost = 2),
            PetAccessory("toy_ball", "Bouncy Ball", "🎾", "Hours of fun!", 8, PetAccessoryCategory.TOY, happinessBoost = 3),
            PetAccessory("toy_bone", "Chew Bone", "🦴", "Yummy to chew!", 12, PetAccessoryCategory.TOY, energyBoost = 3),
            PetAccessory("bed_cloud", "Cloud Bed", "☁️", "Fluffy dreams!", 30, PetAccessoryCategory.BED, energyBoost = 5),
            PetAccessory("snack_cookie", "Magic Cookie", "🍪", "Instant energy!", 5, PetAccessoryCategory.SNACK, energyBoost = 2),
            PetAccessory("snack_cake", "Birthday Cake", "🎂", "Party time!", 15, PetAccessoryCategory.SNACK, happinessBoost = 5, energyBoost = 3)
        )
    }
}

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val petEngine: PetEngine,
    private val userRepository: com.kidsroutine.feature.daily.data.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetUiState())
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""

    fun loadPet(userId: String) {
        currentUserId = userId
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading pet for user: $userId")
                val pet = petRepository.getPet(userId)
                if (pet != null) {
                    Log.d(TAG, "Loaded pet '${pet.name}' — mood: ${pet.mood}")
                    _uiState.value = _uiState.value.copy(
                        pet = pet,
                        isLoading = false,
                        adoptionMode = false
                    )
                } else {
                    Log.d(TAG, "No pet found, entering adoption mode")
                    _uiState.value = _uiState.value.copy(
                        pet = null,
                        isLoading = false,
                        adoptionMode = true
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading pet", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load pet"
                )
            }
        }
        // Observe user XP
        viewModelScope.launch {
            userRepository.observeUser(userId)
                .collect { user ->
                    _uiState.value = _uiState.value.copy(currentXp = user.xp)
                }
        }
    }

    fun feedPet(userId: String = currentUserId) {
        val currentPet = _uiState.value.pet ?: return
        val currentXp = _uiState.value.currentXp

        if (currentXp < PetUiState.FEED_COST) {
            _uiState.value = _uiState.value.copy(
                error = "Not enough XP! You need ${PetUiState.FEED_COST} XP to feed your pet. (You have $currentXp XP)"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Feeding pet '${currentPet.name}' — costing ${PetUiState.FEED_COST} XP")
                // Deduct XP first
                userRepository.updateUserXp(userId, -PetUiState.FEED_COST)
                val fedPet = petEngine.feedPet(currentPet, xpEarned = 50)
                petRepository.savePet(fedPet)
                _uiState.value = _uiState.value.copy(pet = fedPet)
                Log.d(TAG, "Pet fed — happiness: ${fedPet.happiness}, energy: ${fedPet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error feeding pet", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to feed pet"
                )
            }
        }
    }

    fun interactWithPet() {
        val currentPet = _uiState.value.pet ?: return
        viewModelScope.launch {
            try {
                Log.d(TAG, "Interacting with pet '${currentPet.name}'")
                val updatedPet = petEngine.interactWithPet(currentPet)
                petRepository.savePet(updatedPet)
                _uiState.value = _uiState.value.copy(pet = updatedPet)
                Log.d(TAG, "Interaction complete — happiness: ${updatedPet.happiness}")
            } catch (e: Exception) {
                Log.e(TAG, "Error interacting with pet", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to interact with pet"
                )
            }
        }
    }

    fun trainPet(userId: String = currentUserId) {
        val currentPet = _uiState.value.pet ?: return
        val currentXp = _uiState.value.currentXp

        if (currentXp < TRAIN_COST) {
            _uiState.value = _uiState.value.copy(
                error = "Not enough XP! You need $TRAIN_COST XP to train. (You have $currentXp XP)"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Training pet '${currentPet.name}' — costing $TRAIN_COST XP")
                userRepository.updateUserXp(userId, -TRAIN_COST)
                val trainedPet = petEngine.trainPet(currentPet)
                petRepository.savePet(trainedPet)
                _uiState.value = _uiState.value.copy(pet = trainedPet)
                Log.d(TAG, "Pet trained — happiness: ${trainedPet.happiness}, energy: ${trainedPet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error training pet", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to train pet")
            }
        }
    }

    fun groomPet() {
        val currentPet = _uiState.value.pet ?: return
        viewModelScope.launch {
            try {
                Log.d(TAG, "Grooming pet '${currentPet.name}'")
                val groomedPet = petEngine.groomPet(currentPet)
                petRepository.savePet(groomedPet)
                _uiState.value = _uiState.value.copy(pet = groomedPet)
                Log.d(TAG, "Grooming complete — happiness: ${groomedPet.happiness}")
            } catch (e: Exception) {
                Log.e(TAG, "Error grooming pet", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to groom pet")
            }
        }
    }

    fun adventureWithPet(userId: String = currentUserId) {
        val currentPet = _uiState.value.pet ?: return
        val currentXp = _uiState.value.currentXp

        if (currentXp < ADVENTURE_COST) {
            _uiState.value = _uiState.value.copy(
                error = "Not enough XP! You need $ADVENTURE_COST XP for an adventure. (You have $currentXp XP)"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Going on adventure with pet '${currentPet.name}' — costing $ADVENTURE_COST XP")
                userRepository.updateUserXp(userId, -ADVENTURE_COST)
                val adventurePet = petEngine.adventureWithPet(currentPet)
                petRepository.savePet(adventurePet)
                _uiState.value = _uiState.value.copy(pet = adventurePet)
                Log.d(TAG, "Adventure complete — happiness: ${adventurePet.happiness}, energy: ${adventurePet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error on adventure", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to go on adventure")
            }
        }
    }

    fun selectSpecies(species: PetSpecies) {
        _uiState.value = _uiState.value.copy(selectedSpecies = species)
    }

    fun adoptPet(userId: String, species: PetSpecies, name: String) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a name for your pet")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adopting $species pet named '$name' for user: $userId")
                val pet = petRepository.adoptPet(userId, species, name)
                _uiState.value = _uiState.value.copy(
                    pet = pet,
                    isLoading = false,
                    adoptionMode = false,
                    selectedSpecies = null
                )
                Log.d(TAG, "Pet adopted successfully: ${pet.petId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adopting pet", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to adopt pet"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleShop() {
        _uiState.value = _uiState.value.copy(showShop = !_uiState.value.showShop)
    }

    fun purchaseAccessory(accessory: PetAccessory) {
        val state = _uiState.value
        if (state.currentXp < accessory.xpCost) {
            _uiState.value = state.copy(error = "Not enough XP! Need ${accessory.xpCost} XP (you have ${state.currentXp})")
            return
        }
        if (accessory.id in state.ownedAccessoryIds) {
            equipAccessory(accessory.id)
            return
        }
        viewModelScope.launch {
            try {
                userRepository.updateUserXp(currentUserId, -accessory.xpCost)
                val updatedOwned = state.ownedAccessoryIds + accessory.id
                _uiState.value = state.copy(ownedAccessoryIds = updatedOwned)
                equipAccessory(accessory.id)
                Log.d(TAG, "Purchased accessory: ${accessory.name} for ${accessory.xpCost} XP")
            } catch (e: Exception) {
                Log.e(TAG, "Error purchasing accessory", e)
                _uiState.value = _uiState.value.copy(error = "Failed to purchase")
            }
        }
    }

    fun equipAccessory(accessoryId: String) {
        val currentPet = _uiState.value.pet ?: return
        val updatedPet = currentPet.copy(accessoryId = accessoryId)
        viewModelScope.launch {
            try {
                petRepository.savePet(updatedPet)
                _uiState.value = _uiState.value.copy(pet = updatedPet)
                Log.d(TAG, "Equipped accessory: $accessoryId")
            } catch (e: Exception) {
                Log.e(TAG, "Error equipping accessory", e)
            }
        }
    }

    companion object {
        private const val TAG = "PetViewModel"
        const val TRAIN_COST = 8
        const val ADVENTURE_COST = 15
    }
}
