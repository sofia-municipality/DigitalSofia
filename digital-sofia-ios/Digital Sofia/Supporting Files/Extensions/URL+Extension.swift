//
//  URL+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 25.10.23.
//

import Foundation
import UniformTypeIdentifiers

extension URL {
    static let defaultURL = URL(string: "https://www.google.com/")!
}

extension URL {
    public func mimeType() -> String {
        if let mimeType = UTType(filenameExtension: self.pathExtension)?.preferredMIMEType {
            return mimeType
        }
        else {
            return "application/octet-stream"
        }
    }
}
