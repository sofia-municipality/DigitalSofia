//
//  WebViewDownloadError.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.02.24.
//

enum WebViewDownloadError: DSError, Comparable {
    case message(String)
    
    public var description: String {
        switch self {
        case .message(let message):
            return message
        }
    }
}
