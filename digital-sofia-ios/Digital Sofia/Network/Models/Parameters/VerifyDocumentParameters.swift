//
//  VerifyDocumentParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.10.23.
//

import Foundation

struct VerifyDocumentParameters {
    func getDictionary() -> [String: Any] {
        let user = UserProvider.shared.getUser()
        return ["refreshToken": user?.refreshToken ?? ""]
    }
}
