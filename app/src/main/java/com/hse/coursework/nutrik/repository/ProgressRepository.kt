package com.hse.coursework.nutrik.repository
import com.hse.coursework.nutrik.model.ProgressItem;
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ProgressRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getProgressData(): Flow<List<ProgressItem>> {
        return localDataSource.getProgressData()
            .flatMapLatest { localData ->
                if (localData.isNotEmpty()) {
                    flowOf(localData)
                } else {
                    remoteDataSource.fetchProgressData()
                        .onEach { fetchedData ->
                            localDataSource.saveProgressData(fetchedData)
                        }
                }
            }
    }
}