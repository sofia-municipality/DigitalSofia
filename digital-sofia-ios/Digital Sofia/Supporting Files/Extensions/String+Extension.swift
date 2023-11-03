//
//  String+Extension .swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation
import CryptoKit

extension String {
    var localized: String {
        let appLanguage = LanguageProvider.shared.appLanguage
        
        guard let path = Bundle.main.path(forResource: appLanguage?.rawValue, ofType: "lproj") else {
            return ""
        }
        
        guard let bundle = Bundle(path: path) else {
            return ""
        }
        
        return NSLocalizedString(self, tableName: nil, bundle: bundle, value: "", comment: "")
    }
}


extension String {
    var isoDate: Date? {
        return Date.iso8601Formatter.date(from: self)
    }
}

extension String {
    var digitallWebLink: String {
        let user = UserProvider.shared.getUser()
        let token = user?.token ?? ""
        let refreshToken = user?.refreshToken ?? ""
        
        let language = LanguageProvider.shared.appLanguage?.short ?? ""
        
        let urlString = self + "?token=\(token)&refreshToken=\(refreshToken)&hideNav=true&lang=\(language)"
        
#if DEBUG
        //        print(urlString)
#endif
        return urlString
    }
}

extension String {
    func format(_ arguments: CVarArg...) -> String {
        let args = arguments.map {
            if let arg = $0 as? Int { return String(arg) }
            if let arg = $0 as? Float { return String(arg) }
            if let arg = $0 as? Double { return String(arg) }
            if let arg = $0 as? Int64 { return String(arg) }
            if let arg = $0 as? String { return String(arg) }
            
            return "(null)"
        } as [CVarArg]
        
        return String.init(format: self, arguments: args)
    }
}

extension String {
    func slice(from: String, to: String) -> String? {
        return (range(of: from)?.upperBound).flatMap { substringFrom in
            (range(of: to, range: substringFrom..<endIndex)?.lowerBound).map { substringTo in
                String(self[substringFrom..<substringTo])
            }
        }
    }
}

extension String {
    var getHashedPassword: String? {
        let pinData = Data(self.utf8)
        let hashed = SHA256.hash(data: pinData)
        let hashString = hashed.compactMap { String(format: "%02x", $0) }.joined()
        
        return hashString
    }
}

extension String {
    var isValidEmail: Bool {
        let emailRegEx = #"^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$"#
        
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailPred.evaluate(with: self)
    }
    
    var isValidPhone: Bool {
        let emailRegEx = #"(\+)?(359|0)8[789]\d{1}(|-| )\d{3}(|-| )\d{3}"#
        
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailPred.evaluate(with: self)
    }
}

/// Matches a 10-digit number of the format NNNNNNNNNN, where second and third number starts from [01–12 or 21–32 or 41–52], and the fourth and fifth number starts from [01–31]
extension String {
    var isValidEGN: Bool {
        let emailRegEx = #"\b[0-9]{2}(?:0[1-9]|1[0-2]|2[1-9]|3[0-2]|4[1-9]|5[0-2])(?:0[1-9]|[1-2][0-9]|3[0-1])[0-9]{4}\b"#
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        let passedRegex = emailPred.evaluate(with: self)
        
        if passedRegex {
            let checksums = [2,4,8,5,10,9,7,3,6]
            var sum = 0
            for (index, numberString) in self.enumerated() {
                if index != self.count - 1 {
                    let weight = checksums[index]
                    let number = Int(String(numberString)) ?? 0
                    sum += number * weight
                }
            }
            
            let division = sum.quotientAndRemainder(dividingBy: 11)
            let checksum = sum - (division.quotient * 11)
            
            if let checkNumberString = self.last {
                let checkNumber = Int(String(checkNumberString)) ?? 0
                let passChecksum = checkNumber == checksum
                return passChecksum
            }
        }
        
        return false
    }
}
