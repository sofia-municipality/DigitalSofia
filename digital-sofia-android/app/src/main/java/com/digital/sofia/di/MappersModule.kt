package com.digital.sofia.di

import com.digital.sofia.data.mappers.network.user.request.UserProfileStatusChangesRequestBodyMapper
import org.koin.dsl.module

val mappersModule = module {

    single<UserProfileStatusChangesRequestBodyMapper> {
        UserProfileStatusChangesRequestBodyMapper()
    }
    
}