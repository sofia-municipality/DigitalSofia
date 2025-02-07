//
//  Notification+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 3.08.23.
//

import Foundation

extension Notification.Name {
    static let evrotrustSDKSetupReceivedNotification = Notification.Name("evrotrust-sdk-setup-received-notification")
    static let tokenExpiredNotification = Notification.Name("token-expired-notification")
    static let logoutUserNotification = Notification.Name("logout-user-notification")
    static let checkPendingDocumentsNotification = Notification.Name("check-pending-documents-notification")
    static let lockScreenNotification = Notification.Name("lock-screen-notification")
    static let openDocumentNotification = Notification.Name("open-document-notification")
    static let loginFromPortalNotification = Notification.Name("login-from-portal-notification")
    static let pendingDocumentNotification = Notification.Name("pending-document-notification")
    static let signedDocumentNotification = Notification.Name("signed-document-notification")
    static let expiredDocumentNotification = Notification.Name("expired-document-notification")
    static let pendingDeliveryDocumentNotification = Notification.Name("pending-delivery-document-notification")
    static let tokenRefreshedNotification = Notification.Name("token-refreshed-notification")
    static let evrotrustSDKNotSetupNotification = Notification.Name("evrotrust-sdk-not-setup-notification")
    static let evrotrustUserStatusNotification = Notification.Name("evrotrust-sdk-user-status-notification")
}
