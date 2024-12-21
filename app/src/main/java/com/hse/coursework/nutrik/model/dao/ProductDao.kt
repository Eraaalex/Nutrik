package com.hse.coursework.nutrik.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hse.coursework.nutrik.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name LIMIT 30 OFFSET :offset")
    suspend fun getRecentProducts(offset: Int): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(products: Product)

    @Query("DELETE FROM products")
    suspend fun clearProducts()


    @Query(
        """
        SELECT * FROM products 
        WHERE (:query IS NULL OR name LIKE :query)
        ORDER BY name
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun searchProducts(query: String?, limit: Int, offset: Int): List<Product>


    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): Product?

    @Query("SELECT * FROM products WHERE id IN (:ids)")
    fun getProductsByIds(ids: List<String>): Flow<List<Product>>


}
