//
//  PermissionProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import UIKit

struct PermissionProvider {
    static func getNotificationsPermission(completion: @escaping (Bool)->Void) {
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { (success, error) in
            guard success else {
                completion(false)
                return
            }
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
                completion(success)
            }
        }
    }
}
