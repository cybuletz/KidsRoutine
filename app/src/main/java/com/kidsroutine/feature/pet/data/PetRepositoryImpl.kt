package com.kidsroutine.feature.pet.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.PetEvolutionStage
import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetSpecies
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PetRepository {

    private val petsCollection get() = firestore.collection("pets")

    override suspend fun getPet(userId: String): PetModel? {
        return try {
            Log.d(TAG, "Getting pet for user: $userId")
            val snapshot = petsCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d(TAG, "No pet found for user: $userId")
                return null
            }

            val doc = snapshot.documents.first()
            val data = doc.data ?: return null
            val pet = mapToPetModel(doc.id, data)
            Log.d(TAG, "Loaded pet '${pet.name}' (${pet.species}) for user: $userId")
            pet
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pet for user: $userId", e)
            null
        }
    }

    override suspend fun savePet(pet: PetModel) {
        try {
            Log.d(TAG, "Saving pet '${pet.name}' (id=${pet.petId})")
            val data = petToFirestoreMap(pet)
            petsCollection.document(pet.petId).set(data).await()
            Log.d(TAG, "Pet saved successfully: ${pet.petId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving pet: ${pet.petId}", e)
            throw e
        }
    }

    override suspend fun adoptPet(userId: String, species: PetSpecies, name: String): PetModel {
        Log.d(TAG, "Adopting new $species pet named '$name' for user: $userId")
        val docRef = petsCollection.document()
        val now = System.currentTimeMillis()
        val pet = PetModel(
            petId = docRef.id,
            userId = userId,
            species = species,
            name = name,
            stage = PetEvolutionStage.EGG,
            happiness = 70,
            energy = 80,
            style = 0,
            totalFed = 0,
            daysAlive = 0,
            longestHappyStreak = 0,
            hatchedAt = now,
            lastFedAt = now,
            lastInteractedAt = now,
            accessoryId = null,
            isPremium = false
        )
        val data = petToFirestoreMap(pet)
        docRef.set(data).await()
        Log.d(TAG, "Pet adopted successfully: ${pet.petId}")
        return pet
    }

    private fun petToFirestoreMap(pet: PetModel): Map<String, Any?> = mapOf(
        "userId" to pet.userId,
        "species" to pet.species.name,
        "name" to pet.name,
        "stage" to pet.stage.name,
        "happiness" to pet.happiness,
        "energy" to pet.energy,
        "style" to pet.style,
        "totalFed" to pet.totalFed,
        "totalPlayed" to pet.totalPlayed,
        "totalTrained" to pet.totalTrained,
        "totalGroomed" to pet.totalGroomed,
        "totalAdventures" to pet.totalAdventures,
        "totalNaps" to pet.totalNaps,
        "totalTreats" to pet.totalTreats,
        "totalTreasureHunts" to pet.totalTreasureHunts,
        "daysAlive" to pet.daysAlive,
        "longestHappyStreak" to pet.longestHappyStreak,
        "hatchedAt" to pet.hatchedAt,
        "lastFedAt" to pet.lastFedAt,
        "lastInteractedAt" to pet.lastInteractedAt,
        "accessoryId" to pet.accessoryId,
        "isPremium" to pet.isPremium
    )

    private fun mapToPetModel(docId: String, data: Map<String, Any?>): PetModel = PetModel(
        petId = docId,
        userId = data["userId"] as? String ?: "",
        species = runCatching {
            PetSpecies.valueOf(data["species"] as? String ?: "DRAGON")
        }.getOrDefault(PetSpecies.DRAGON),
        name = data["name"] as? String ?: "",
        stage = runCatching {
            PetEvolutionStage.valueOf(data["stage"] as? String ?: "EGG")
        }.getOrDefault(PetEvolutionStage.EGG),
        happiness = (data["happiness"] as? Number)?.toInt() ?: 80,
        energy = (data["energy"] as? Number)?.toInt() ?: 80,
        style = (data["style"] as? Number)?.toInt() ?: 0,
        totalFed = (data["totalFed"] as? Number)?.toInt() ?: 0,
        totalPlayed = (data["totalPlayed"] as? Number)?.toInt() ?: 0,
        totalTrained = (data["totalTrained"] as? Number)?.toInt() ?: 0,
        totalGroomed = (data["totalGroomed"] as? Number)?.toInt() ?: 0,
        totalAdventures = (data["totalAdventures"] as? Number)?.toInt() ?: 0,
        totalNaps = (data["totalNaps"] as? Number)?.toInt() ?: 0,
        totalTreats = (data["totalTreats"] as? Number)?.toInt() ?: 0,
        totalTreasureHunts = (data["totalTreasureHunts"] as? Number)?.toInt() ?: 0,
        daysAlive = (data["daysAlive"] as? Number)?.toInt() ?: 0,
        longestHappyStreak = (data["longestHappyStreak"] as? Number)?.toInt() ?: 0,
        hatchedAt = (data["hatchedAt"] as? Number)?.toLong() ?: 0L,
        lastFedAt = (data["lastFedAt"] as? Number)?.toLong() ?: 0L,
        lastInteractedAt = (data["lastInteractedAt"] as? Number)?.toLong() ?: 0L,
        accessoryId = data["accessoryId"] as? String,
        isPremium = data["isPremium"] as? Boolean ?: false
    )

    companion object {
        private const val TAG = "PetRepository"
    }
}
