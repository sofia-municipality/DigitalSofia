//
//  Logging+Evrotrust.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 30.01.24.
//

import FirebaseCrashlytics
import EvrotrustSDK

extension LoggingHelper {
    static func logSDKSetupStart() {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkSetupStart
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKSetupResult(_ result: EvrotrustSetupSDKResult) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkSetupResult
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkIsSetUp] = result.isSetUp
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkHasNewVersion] = result.hasNewVersion
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkIsInMaintenance] = result.isInMaintenance
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKSetLanguage() {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkSetLanguage
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkLanguage] = LanguageProvider.shared.appLanguage?.short ?? ""
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKEditUserStart(securityContext: String) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkEditUserStart
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkSecurityContext] = securityContext
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKEditUserResult(result: EvrotrustEditPersonalDataResult) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkEditUserResult
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkResult] = result.dictionary
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKCheckUserStatusStart() {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkCheckUserStatusStart
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKCheckUserStatusResult(result: EvrotrustCheckUserStatusResult) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkCheckUserStatusResult
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkResult] = result.dictionary
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKChangeSecurityContextStart(old: String, new: String) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkChangeSecurityContextStart
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkOldSecurityContext] = old
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkSecurityContext] = new
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKChangeSecurityContextResult(result: EvrotrustChangeSecurityContextResult) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkChangeSecurityContextResult
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkResult] = result.dictionary
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKUserSetupStart(securityContext: String, personalIdentificationNumber: String) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkUserSetupStart
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkSecurityContext] = securityContext
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkPersonalIdentificationNumber] = personalIdentificationNumber
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKUserSetupResult(result: EvrotrustSetupProfileResult) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkUserSetupResult
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkResult] = result.dictionary
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKOpenDocumentStart(securityContext: String, transactionId: String) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkOpenDocumentStart
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkSecurityContext] = securityContext
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkTransactionId] = transactionId
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logSDKOpenDocumentResult(result: EvrotrustOpenDocumentResult) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.etEvent] = AppConfig.FirebaseAnalytics.Events.Evrotrust.sdkOpenDocumentResult
        logInfo[AppConfig.FirebaseAnalytics.Parameters.Evrotrust.sdkResult] = result.dictionary
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
}
