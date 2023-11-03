package com.digitall.digital_sofia.di

import com.digitall.digital_sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digitall.digital_sofia.mappers.documents.DocumentsUiMapper
import com.digitall.digital_sofia.mappers.forms.UnsignedDocumentUiMapper
import com.digitall.digital_sofia.ui.fragments.main.documents.list.DocumentsAdapter
import com.digitall.digital_sofia.ui.fragments.main.documents.list.DocumentsDelegate
import com.digitall.digital_sofia.ui.fragments.main.signing.list.SigningAdapter
import com.digitall.digital_sofia.ui.fragments.main.signing.list.SigningDelegate
import com.digitall.digital_sofia.utils.CurrentContext
import org.koin.dsl.module

/**
 * single<Class>(named("name")){Class()} -> for creating a specific instance in module
 * single<Class1>{Class1(get<Class2>(named("name")))} -> for creating a specific instance in module
 * val nameOfVariable: Class by inject(named("name")) -> for creating a specific instance in class
 * get<Class>{parametersOf("param")} -> parameter passing in module
 * single<Class>{(param: String)->Class(param)} -> parameter passing in module
 * val name: Class by inject(parameters={parametersOf("param")}) -> parameter passing in class
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

val presentationModule = module {
    single<SigningAdapter> {
        SigningAdapter(
            signingDelegate = get<SigningDelegate>()
        )
    }
    single<SigningDelegate> {
        SigningDelegate()
    }
    single<DocumentsAdapter> {
        DocumentsAdapter(
            documentsDelegate = get<DocumentsDelegate>()
        )
    }
    single<DocumentsDelegate> {
        DocumentsDelegate()
    }
    single<CreateCodeResponseErrorToStringMapper> {
        CreateCodeResponseErrorToStringMapper(
            currentContext = get<CurrentContext>(),
        )
    }
    single<DocumentsUiMapper>{
        DocumentsUiMapper()
    }
    single<UnsignedDocumentUiMapper>{
        UnsignedDocumentUiMapper()
    }
}