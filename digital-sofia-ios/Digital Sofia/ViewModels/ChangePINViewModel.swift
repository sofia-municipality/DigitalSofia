//
//  ChangePINViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 18.08.23.
//

import SwiftUI
import EvrotrustSDK

class ChangePINViewModel: NSObject, EvrotrustChangeSecurityContextDelegate {
    private var _pin: String = ""
    private var _completion: ((EvrotrustError?) -> ())?
    
    func change(pin: String, completion: @escaping (EvrotrustError?) -> ()) {
        _pin = pin
        _completion = completion
        
        let currentContext = UserProvider.shared.getUser()?.securityContext
        let pinHash = pin.getHashedPassword
        Evrotrust.sdk().changeSecurityContext(currentContext, withNewSecurityContext: pinHash, andDelegate: self)
    }
    
    func evrotrustChangeSecurityContextDelegateDidFinish(_ result: EvrotrustChangeSecurityContextResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            _completion?(EvrotrustError.sdkNotSetUp)
        case EvrotrustResultStatus.errorInput:
            _completion?(EvrotrustError.errorInput)
        case EvrotrustResultStatus.userNotSetUp:
            _completion?(EvrotrustError.userNotSetUp)
        case EvrotrustResultStatus.OK:
            if result.changed {
                var user = UserProvider.shared.getUser()
                user?.pin = _pin
                user?.securityContext = result.securityContext
                UserProvider.shared.save(user: user)
                _completion?(nil)
            }
            
        default: break
        }
    }
}
