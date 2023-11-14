//
//  Notification+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 3.08.23.
//

import Foundation

extension Notification.Name {
    static let evrotrustSDKSetupReceivedNotification = Notification.Name("evrotrust-sdk-setup-received")
    static let tokenExpiredNotification = Notification.Name("token-expired")
    static let logoutUserNotification = Notification.Name("logout-user")
    static let openDocumentNotification = Notification.Name("open-document")
    static let checkPendingDocumentsNotification = Notification.Name("check-pending-documents")
}
