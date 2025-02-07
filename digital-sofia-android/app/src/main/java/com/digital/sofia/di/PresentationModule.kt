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
package com.digital.sofia.di

import com.digital.sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digital.sofia.mappers.documents.DocumentsUiMapper
import com.digital.sofia.mappers.forms.PendingDocumentUiMapper
import com.digital.sofia.ui.fragments.main.documents.list.DocumentsAdapter
import com.digital.sofia.ui.fragments.main.documents.list.DocumentsDelegate
import com.digital.sofia.ui.fragments.main.documents.list.DocumentsHeaderDelegate
import com.digital.sofia.ui.fragments.main.pending.list.PendingAdapter
import com.digital.sofia.ui.fragments.main.pending.list.PendingDelegate
import com.digital.sofia.utils.CurrentContext
import org.koin.dsl.module

val presentationModule = module {
    single<PendingAdapter> {
        PendingAdapter(
            pendingDelegate = get<PendingDelegate>()
        )
    }
    single<PendingDelegate> {
        PendingDelegate()
    }
    single<DocumentsAdapter> {
        DocumentsAdapter(
            documentsHeaderDelegate = get<DocumentsHeaderDelegate>(),
            documentsDelegate = get<DocumentsDelegate>()
        )
    }
    single<DocumentsHeaderDelegate> {
        DocumentsHeaderDelegate()
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
        DocumentsUiMapper(
            currentContext = get<CurrentContext>(),
        )
    }
    single<PendingDocumentUiMapper>{
        PendingDocumentUiMapper(
            currentContext = get<CurrentContext>(),
        )
    }
}