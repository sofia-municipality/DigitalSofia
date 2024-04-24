//
//  IdentityRequestViewViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 16.01.24.
//

import SwiftUI
import EvrotrustSDK

class IdentityRequestViewViewModel: NSObject, EvrotrustChangeSecurityContextDelegate {
    var completion: ((EvrotrustError?) -> ())?
    
    func changeSecurityContext(old: String, to new: String) {
        Evrotrust.sdk().changeSecurityContext(old, withNewSecurityContext: new, andDelegate: self)
        LoggingHelper.logSDKChangeSecurityContextStart(old: old, new: new)
    }
    
    internal func evrotrustChangeSecurityContextDelegateDidFinish(_ result: EvrotrustChangeSecurityContextResult!) {
        LoggingHelper.logSDKChangeSecurityContextResult(result: result)
        switch (result.status) {
        case EvrotrustResultStatus.OK:
            if result.changed {
                completion?(nil)
            } else {
                completion?(EvrotrustError.userCancelled)
            }
        default:
            completion?(EvrotrustError.getError(for: result.status))
        }
    }
}
