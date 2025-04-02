package com.hse.coursework.nutrik.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hse.coursework.nutrik.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query(
        """
      SELECT * FROM products 
      ORDER BY name 
      LIMIT :limit OFFSET :offset
    """
    )
    suspend fun getRecentProducts(limit: Int, offset: Int): List<ProductEntity>

    // Для быстрого локального поиска по подстроке (нечёткий LIKE)
    @Query(
        """
      SELECT * FROM products
      WHERE name LIKE :pattern COLLATE NOCASE
      ORDER BY name
    """
    )
    suspend fun searchByName(pattern: String): List<ProductEntity>


    @Query("SELECT * FROM products ORDER BY name LIMIT 30 OFFSET :offset")
    suspend fun getRecentProducts(offset: Int): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(products: ProductEntity)

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
    suspend fun searchProducts(query: String?, limit: Int, offset: Int): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id IN (:ids)")
    fun getProductsByIds(ids: List<String>): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name = :name")
    fun getByName(name: String): ProductEntity?
}
