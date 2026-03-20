package com.kidsroutine.core.network

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ── Safe Firestore wrappers ────────────────────────────────────────────────

suspend fun <T> safeFirestoreCall(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Firestore error", e)
    }

fun Query.asFlow(): Flow<QuerySnapshot> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        if (error != null) { close(error); return@addSnapshotListener }
        snapshot?.let { trySend(it) }
    }
    awaitClose { listener.remove() }
}

fun DocumentReference.asFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        if (error != null) { close(error); return@addSnapshotListener }
        snapshot?.let { trySend(it) }
    }
    awaitClose { listener.remove() }
}
