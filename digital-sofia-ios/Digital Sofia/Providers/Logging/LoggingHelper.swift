//
//  LoggingHelper.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 26.01.24.
//

import EvrotrustSDK
import os.log

final class LoggingHelper {
    static let logger = Logger.init(subsystem: "com.Digital-Sofia", category: "main")
    static var defaultDictionary: [String : Any] {
        let user = UserProvider.currentUser
        let egn = user?.personalIdentificationNumber ?? ""
        
        return [AppConfig.FirebaseAnalytics.Parameters.timestamp: Date().utcTimestamp,
                AppConfig.FirebaseAnalytics.Parameters.deviceId: UIDevice.logDeviceId,
                AppConfig.FirebaseAnalytics.Parameters.personalIdentificationNumber: egn]
    }
    
    private init() { }
    
    private static var libraryDirectory: String {
        return NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first ?? ""
    }
    
    private static var logFileName: String {
        let date = Date().getFormattedDate(format: .log)
        return "\(date).log"
    }
    
    static func checkUserDebugMode() {
        let filesToDelete = getOldLogFiles()
        if filesToDelete.isEmpty == false {
            sendOldSessionLogToServer(filesToDelete: filesToDelete)
        }
        
        NetworkManager.checkDebugMode { response in
            switch response {
            case .success(let response):
                if response.debugMode {
                    setupNewSessionLogFile()
                }
            case .failure(_): break
            }
        }
    }
    
    private static func getOldLogFiles() -> [String] {
        var filesToDelete: [String] = []
        
        do {
            let items = try FileManager.default.contentsOfDirectory(atPath: libraryDirectory)
            items.forEach({ i in
                let pathExtention = (i as NSString).pathExtension
                if pathExtention == "log" {
                    let itemPath = libraryDirectory.appending("/\(i)")
                    filesToDelete.append(itemPath)
                }
            })
            
        } catch let error {
            print("getOldLogFiles() error: \(error)")
        }
        
        return filesToDelete
    }
    
    private static func sendOldSessionLogToServer(filesToDelete: [String]) {
        NetworkManager.sendDebugFile(files: filesToDelete) { response in
            switch response {
            case .success(let success):
                if success {
                    do {
                        try filesToDelete.forEach { file in
                            let _ = try FileManager.default.removeItem(atPath: file)
                        }
                    } catch let error {
                        print("sendOldSessionLogToServer() error: \(error)")
                    }
                }
            case .failure(_): break
            }
        }
    }
    
    private static func setupNewSessionLogFile() {
        let logFilePath = (libraryDirectory as NSString).appendingPathComponent(logFileName)
        freopen(logFilePath.cString(using: String.Encoding.ascii)!, "a+", stderr)
    }
}
