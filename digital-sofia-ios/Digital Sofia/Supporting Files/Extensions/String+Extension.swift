//
//  String+Extension .swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation
import CryptoKit
import UniformTypeIdentifiers

extension String {
    var localized: String {
        guard let bundle = Bundle.getLanguageBundle() else { return "" }
        return NSLocalizedString(self, tableName: nil, bundle: bundle, value: "", comment: "")
    }
    
    var evrotrustLocalized: String {
        guard let bundle = Bundle.getLanguageBundle() else { return "" }
        return NSLocalizedString(self, tableName: "Evrotrust", bundle: bundle, value: "", comment: "")
    }
    
    var faceTechLocalized: String {
        guard let bundle = Bundle.getLanguageBundle() else { return "" }
        return NSLocalizedString(self, tableName: "FaceTec", bundle: bundle, value: "", comment: "")
    }
}

extension String {
    var isoDate: Date? {
        return Date.iso8601Formatter.date(from: self)
    }
    
    func dateFor(format: DateFormat) -> Date? {
        let dateformat = DateFormatter()
        dateformat.dateFormat = format.rawValue
        return dateformat.date(from: self)
    }
}

extension String {
    var digitallWebLink: String {
        let user = UserProvider.currentUser
        let token = user?.token ?? ""
        let refreshToken = user?.refreshToken ?? ""
        
        let language = LanguageProvider.shared.appLanguage?.short ?? ""
        
        var urlString = self + "?token=\(token)&refreshToken=\(refreshToken)&hideNav=true&lang=\(language)"
        
        if self == AppConfig.WebViewPages.services {
            urlString.append("&showRequestServiceLink=true")
        }
        
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

extension NSString {
    public func mimeType() -> String {
        if let mimeType = UTType(filenameExtension: self.pathExtension)?.preferredMIMEType {
            return mimeType
        }
        else {
            return "application/octet-stream"
        }
    }
}

extension String {
    public func mimeType() -> String {
        return (self as NSString).mimeType()
    }
}

extension String {
    var fileName: String {
        return (self as NSString).deletingPathExtension
    }
    
    var fileExtension: String {
        return (self as NSString).pathExtension
    }
}
