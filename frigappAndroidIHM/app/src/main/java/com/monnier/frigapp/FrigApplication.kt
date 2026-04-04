package com.monnier.frigapp

import android.app.Application
import com.monnier.frigapp.data.local.TokenRepository
import com.monnier.frigapp.data.repository.AuthRepository
import com.monnier.frigapp.data.repository.FridgeRepository
import com.monnier.frigapp.data.repository.ItemRepository
import com.monnier.frigapp.data.repository.MembersRepository
import com.monnier.frigapp.data.repository.ProductRepository
import com.monnier.frigapp.network.ApiService

/**
 * Classe Application centrale de FrigApp.
 *
 * Joue le rôle de conteneur de dépendances léger (sans Hilt pour l'instant).
 * Les repositories sont créés en lazy pour n'être initialisés qu'au premier accès.
 *
 * Utilisation dans un ViewModel :
 *   val authRepository    = (application as FrigApplication).authRepository
 *   val fridgeRepository  = (application as FrigApplication).fridgeRepository
 */
class FrigApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialise le TokenAuthenticator avant tout appel API
        ApiService.setup(tokenRepository)
    }

    /** Repository pour la persistance locale du JWT (DataStore). */
    val tokenRepository: TokenRepository by lazy {
        TokenRepository(applicationContext)
    }

    /** Repository d'authentification (login, register, logout, refresh). */
    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenRepository)
    }

    /** Repository des frigos (CRUD + quitter). */
    val fridgeRepository: FridgeRepository by lazy {
        FridgeRepository()
    }

    /** Repository du référentiel produits (recherche par barcode via le backend). */
    val productRepository: ProductRepository by lazy {
        ProductRepository()
    }

    /** Repository des items (produits dans un frigo). */
    val itemRepository: ItemRepository by lazy {
        ItemRepository()
    }

    /** Repository des membres d'un frigo. */
    val membersRepository: MembersRepository by lazy {
        MembersRepository()
    }
}
