package com.example.nokos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nokos.data.model.NokosOrderEntity
import com.example.nokos.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NokosDao {
    @Query("SELECT * FROM nokos_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<NokosOrderEntity>>

    @Query("SELECT * FROM nokos_orders WHERE status = 'WAITING_SMS' OR status = 'RECEIVED' ORDER BY createdAt DESC")
    fun getActiveOrders(): Flow<List<NokosOrderEntity>>

    @Query("SELECT * FROM nokos_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): NokosOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: NokosOrderEntity)

    @Update
    suspend fun updateOrder(order: NokosOrderEntity)

    @Query("UPDATE nokos_orders SET status = :status, smsCode = :code, smsText = :text WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String, code: String?, text: String?)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM nokos_orders WHERE id = :id")
    suspend fun deleteOrder(id: String)
}
