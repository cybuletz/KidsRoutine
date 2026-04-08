package com.kidsroutine.feature.pet.data

import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetSpecies

interface PetRepository {
    suspend fun getPet(userId: String): PetModel?
    suspend fun savePet(pet: PetModel)
    suspend fun adoptPet(userId: String, species: PetSpecies, name: String): PetModel
}
