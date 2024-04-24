//
//  Logging+Debug.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 30.01.24.
//

import SwiftUI
import FirebaseCrashlytics

extension LoggingHelper {
    static func logView(view: any View) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.viewName] = view.name
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logRequest(response: String) {
        var logInfo = defaultDictionary
        logInfo[AppConfig.FirebaseAnalytics.Parameters.requestResponse] = response
        
        Crashlytics.crashlytics().setCustomKeysAndValues(logInfo)
        logger.debug("\(logInfo.description, privacy: .public)")
    }
    
    static func logGeneral(string: String) {
        Crashlytics.crashlytics().log(string)
        logger.debug("\(string, privacy: .public)")
    }
}
