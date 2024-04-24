//
//  FirebaseNotificationHelper.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 10.01.24.
//

import Foundation

enum FirebaseNotificationType {
    case login, pendingDocument, signedDocument, expiredDocument
}

class FirebaseNotificationHelper {
    private init() {}
    
    static func sendNotifiactionFor(type: FirebaseNotificationType, payload: String) {
        switch type {
        case .login:
            NotificationCenter.default.post(name: NSNotification.Name.loginFromPortalNotification,
                                            object: nil,
                                            userInfo: [AppConfig.Notifications.payload: payload])
        case .pendingDocument:
            NotificationCenter.default.post(name: NSNotification.Name.pendingDocumentNotification,
                                            object: nil,
                                            userInfo: [AppConfig.Notifications.payload: payload])
        case .signedDocument:
            NotificationCenter.default.post(name: NSNotification.Name.signedDocumentNotification,
                                            object: nil,
                                            userInfo: [AppConfig.Notifications.payload: payload])
        case .expiredDocument:
            NotificationCenter.default.post(name: NSNotification.Name.expiredDocumentNotification,
                                            object: nil,
                                            userInfo: [AppConfig.Notifications.payload: payload])
        }
    }
}
