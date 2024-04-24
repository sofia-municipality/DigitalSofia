//
//  NetworkManager.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Combine
import UIKit

class NetworkManager {
    internal static let provider = AlamofireProvider()
    internal static var cancellables = Set<AnyCancellable>()
    
    internal static var fcm: String {
        return (UIApplication.shared.delegate as? AppDelegate)?.fcmToken ?? ""
    }
    
    internal static var user: User? {
        return UserProvider.currentUser
    }
}
