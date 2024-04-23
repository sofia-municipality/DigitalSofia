//
//  BuildConfiguration.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 16.11.23.
//

import Foundation

enum BuildConfiguration {
    case debug, release, debugDev, releaseDev
    
    static func getConfiguration() -> BuildConfiguration? {
        guard let schemeName = Bundle.main.infoDictionary!["CURRENT_SCHEME_NAME"] as? String else {
            return nil
        }
        
        switch schemeName {
        case "DEBUG":
            return .debug
        case "DDEBUG":
            return .debugDev
        case "RELEASE":
            return .release
        case "DRELEASE":
            return .releaseDev
        default:
            return nil
        }
    }
    
    static var plistName: String {
        switch getConfiguration() {
        case .debug, .release:
            return "GoogleService-Info"
        case .debugDev, .releaseDev:
            return "GoogleService-Info-Debug"
        default:
            return ""
        }
    }
}
