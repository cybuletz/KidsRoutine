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
    val userLevel: Int = 1,
    val error: String? = null,
    val showShop: Boolean = false,
    val ownedAccessoryIds: List<String> = emptyList()
) {
    companion object {
        const val FEED_COST = 5

        val SHOP_ITEMS = listOf(
            // Permanent accessories (cost more)
            PetAccessory("hat_crown", "Royal Crown", "👑", "A crown fit for royalty!", 25, PetAccessoryCategory.HAT, happinessBoost = 5),
            PetAccessory("hat_wizard", "Wizard Hat", "🧙", "Magical style!", 20, PetAccessoryCategory.HAT, happinessBoost = 3),
            PetAccessory("collar_star", "Star Collar", "⭐", "Shine bright!", 15, PetAccessoryCategory.COLLAR, happinessBoost = 2),
            PetAccessory("collar_bell", "Jingle Bell", "🔔", "Ding-a-ling!", 10, PetAccessoryCategory.COLLAR, energyBoost = 2),
            PetAccessory("toy_ball", "Bouncy Ball", "🎾", "Hours of fun!", 8, PetAccessoryCategory.TOY, happinessBoost = 3),
            PetAccessory("toy_bone", "Chew Bone", "🦴", "Yummy to chew!", 12, PetAccessoryCategory.TOY, energyBoost = 3),
            PetAccessory("bed_cloud", "Cloud Bed", "☁️", "Fluffy dreams!", 30, PetAccessoryCategory.BED, energyBoost = 5),
            // Consumable treats (cheaper, temporary — lasts durationMinutes then disappears)
            PetAccessory("snack_cookie", "Magic Cookie", "🍪", "Instant energy! (lasts 1 hour)", 3, PetAccessoryCategory.SNACK, energyBoost = 2, durationMinutes = 60),
            PetAccessory("snack_cake", "Birthday Cake", "🎂", "Party time! (lasts 2 hours)", 8, PetAccessoryCategory.SNACK, happinessBoost = 5, energyBoost = 3, durationMinutes = 120)
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
                    // Check evolution based on user level
                    val userLevel = _uiState.value.userLevel
                    val evolvedPet = petEngine.checkEvolution(pet, userLevel)
                    if (evolvedPet.stage != pet.stage) {
                        Log.d(TAG, "Pet evolved from ${pet.stage} to ${evolvedPet.stage}!")
                        petRepository.savePet(evolvedPet)
                    }
                    Log.d(TAG, "Loaded pet '${evolvedPet.name}' — mood: ${evolvedPet.mood}")
                    _uiState.value = _uiState.value.copy(
                        pet = evolvedPet,
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
        // Observe user XP and level
        viewModelScope.launch {
            userRepository.observeUser(userId)
                .collect { user ->
                    val previousLevel = _uiState.value.userLevel
                    _uiState.value = _uiState.value.copy(
                        currentXp = user.xp,
                        userLevel = user.level
                    )
                    // If level increased, check evolution
                    if (user.level > previousLevel) {
                        val currentPet = _uiState.value.pet
                        if (currentPet != null) {
                            val evolvedPet = petEngine.checkEvolution(currentPet, user.level)
                            if (evolvedPet.stage != currentPet.stage) {
                                Log.d(TAG, "Level up! Pet evolved to ${evolvedPet.stage}")
                                petRepository.savePet(evolvedPet)
                                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                            }
                        }
                    }
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
                val evolvedPet = checkAndApplyEvolution(fedPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Pet fed — happiness: ${evolvedPet.happiness}, energy: ${evolvedPet.energy}")
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
                val evolvedPet = checkAndApplyEvolution(updatedPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Interaction complete — happiness: ${evolvedPet.happiness}")
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
                val evolvedPet = checkAndApplyEvolution(trainedPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Pet trained — happiness: ${evolvedPet.happiness}, energy: ${evolvedPet.energy}")
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
                val evolvedPet = checkAndApplyEvolution(groomedPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Grooming complete — happiness: ${evolvedPet.happiness}")
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
                val evolvedPet = checkAndApplyEvolution(adventurePet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Adventure complete — happiness: ${evolvedPet.happiness}, energy: ${evolvedPet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error on adventure", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to go on adventure")
            }
        }
    }

    fun napPet() {
        val currentPet = _uiState.value.pet ?: return
        viewModelScope.launch {
            try {
                Log.d(TAG, "Putting pet '${currentPet.name}' down for a nap")
                val nappedPet = petEngine.napPet(currentPet)
                val evolvedPet = checkAndApplyEvolution(nappedPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Nap complete — energy: ${evolvedPet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error napping pet", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to nap pet")
            }
        }
    }

    fun treatPet(userId: String = currentUserId) {
        val currentPet = _uiState.value.pet ?: return
        val currentXp = _uiState.value.currentXp

        if (currentXp < TREAT_COST) {
            _uiState.value = _uiState.value.copy(
                error = "Not enough XP! You need $TREAT_COST XP for a treat. (You have $currentXp XP)"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Giving pet '${currentPet.name}' a treat — costing $TREAT_COST XP")
                userRepository.updateUserXp(userId, -TREAT_COST)
                val treatedPet = petEngine.treatPet(currentPet)
                val evolvedPet = checkAndApplyEvolution(treatedPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Treat given — happiness: ${evolvedPet.happiness}, energy: ${evolvedPet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error treating pet", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to give treat")
            }
        }
    }

    fun treasureHuntWithPet(userId: String = currentUserId) {
        val currentPet = _uiState.value.pet ?: return
        val currentXp = _uiState.value.currentXp

        if (currentXp < TREASURE_HUNT_COST) {
            _uiState.value = _uiState.value.copy(
                error = "Not enough XP! You need $TREASURE_HUNT_COST XP for a treasure hunt. (You have $currentXp XP)"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Treasure hunting with pet '${currentPet.name}' — costing $TREASURE_HUNT_COST XP")
                userRepository.updateUserXp(userId, -TREASURE_HUNT_COST)
                val huntPet = petEngine.treasureHuntWithPet(currentPet)
                val evolvedPet = checkAndApplyEvolution(huntPet)
                petRepository.savePet(evolvedPet)
                _uiState.value = _uiState.value.copy(pet = evolvedPet)
                Log.d(TAG, "Treasure hunt complete — happiness: ${evolvedPet.happiness}, energy: ${evolvedPet.energy}")
            } catch (e: Exception) {
                Log.e(TAG, "Error on treasure hunt", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to treasure hunt")
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

    /**
     * After any activity, check if the pet should evolve based on user level.
     * Returns the (possibly evolved) pet. Caller is responsible for saving.
     */
    private fun checkAndApplyEvolution(pet: PetModel): PetModel {
        val userLevel = _uiState.value.userLevel
        val evolved = petEngine.checkEvolution(pet, userLevel)
        if (evolved.stage != pet.stage) {
            Log.d(TAG, "Pet evolved to ${evolved.stage} after activity!")
        }
        return evolved
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
                // Update style based on number of permanent accessories owned
                val currentPet = state.pet
                if (currentPet != null) {
                    val permanentIds = PetUiState.SHOP_ITEMS
                        .filter { it.isPermanent }
                        .map { it.id }
                        .toSet()
                    val permanentCount = updatedOwned.count { it in permanentIds }
                    val styledPet = petEngine.updateStyle(currentPet, permanentCount)
                    petRepository.savePet(styledPet)
                    _uiState.value = state.copy(
                        ownedAccessoryIds = updatedOwned,
                        pet = styledPet
                    )
                } else {
                    _uiState.value = state.copy(ownedAccessoryIds = updatedOwned)
                }
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
        const val TREAT_COST = 3
        const val TREASURE_HUNT_COST = 10
    }
}
