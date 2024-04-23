//
//  DataShareViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

@MainActor class DataShareViewModel: ObservableObject {
    func getIdentityRequest(completion: @escaping (String?, NetworkError?) -> ()) {
        let personIdentifier = UserProvider.currentUser?.personalIdentificationNumber ?? ""
        NetworkManager.requestIdentity(personIdentifier: personIdentifier) { response in
            switch response {
            case .success(let document):
                completion(document.evrotrustTransactionId ?? "", nil)
            case .failure(let networkError):
                completion(nil, networkError)
            }
        }
    }
}
