package com.digital.sofia.domain.repository.local.base

import kotlinx.coroutines.flow.Flow

interface BaseLocalRepositoryWithDataMethods<T> : BaseLocalRepository<T> {

    fun getAll(): T?

    fun addAll(data: T)

    fun replaceAll(data: T)

    fun subscribeToAll(): Flow<T?>

}