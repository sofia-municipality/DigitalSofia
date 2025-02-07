//
//  Error+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 8.01.25.
//

extension Error {
    var baseDescription: String {
        if let etError = self as? EvrotrustError {
            return etError.description
        } else if let netError = self as? NetworkError {
            return netError.description
        }
        return ""
    }
}
