//
//  ChangePINViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 18.08.23.
//

import SwiftUI
import EvrotrustSDK

class ChangePINViewModel: NSObject {
    private var viewModel = IdentityRequestViewViewModel()
    private var _pin: String = ""
    private var _completion: ((NetworkError?) -> ())?
    
    func change(pin: String, completion: @escaping (NetworkError?) -> ()) {
        _pin = pin
        _completion = completion
        
        changePinOnServer(pin: _pin) { [weak self] pinHash in
            let currentContext = UserProvider.currentUser?.securityContext ?? ""
            
            self?.viewModel.completion = { error in
                if let error = error {
                    self?.changeOldPinOnServer()
                    self?._completion?(NetworkError.message(error.description))
                } else {
                    UserProvider.shared.update(pin: pin)
                    self?._completion?(nil)
                }
            }
            
            self?.viewModel.changeSecurityContext(old: currentContext, to: pinHash)
        }
    }
    
    private func changePinOnServer(pin: String, success: ((String) -> ())? = nil) {
        let hash = pin.getHashedPassword ?? ""
        NetworkManager.change(pin: hash) { [weak self] response in
            switch response {
            case .success(_):
                success?(hash)
            case .failure(let error):
                self?._completion?(error)
            }
        }
    }
    
    private func changeOldPinOnServer() {
        changePinOnServer(pin: UserProvider.currentUser?.pin ?? "")
    }
}
