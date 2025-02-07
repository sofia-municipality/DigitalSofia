//
//  ETSdkAuthenticationFailedViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.01.25.
//

import Foundation
import EvrotrustSDK

class ETSdkAuthenticationFailedViewModel: NSObject, ObservableObject, EvrotrustSubscribeForUserStatusCallbackDelegate {
    @Published var networkError: String?
    @Published var userReadyToSign: Bool = false
    
    fileprivate var user: User? {
        return UserProvider.currentUser
    }
    
    func subscribeToUserUpdates() {
        let callbackUrl = NetworkConfig.Addresses.baseServer + NetworkConfig.EP.API.sdkAuthFailedCallback
        Evrotrust.sdk().subscribe(forUserStatusCallback: callbackUrl,
                                  withSecurityContext: user?.securityContext,
                                  andDelegate: self)
    }
    
    func evrotrustSubscribe(forUserStatusCallbackDelegateDidFinish result: EvrotrustSubscribeForUserStatusCallbackResult!) {
        if result.identified == true && result.readyToSign == true {
            userReadyToSign = true
        } else {
            subscribeToETUserUpdates()
        }
    }
    
    private func subscribeToETUserUpdates() {
        NetworkManager.subscribeToETUserUpdates() { _ in }
    }
}
