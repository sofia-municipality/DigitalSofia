//
//  NetworkMonitor.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.08.23.
//

import Network
import SwiftUI

class NetworkMonitor: ObservableObject {
    private let networkMonitor = NWPathMonitor()
    private let workerQueue = DispatchQueue(label: "Monitor")
    var isConnected = false
    
    init() {
        networkMonitor.pathUpdateHandler = { path in
            self.isConnected = path.status == .satisfied
            Task {
                await MainActor.run {
                    self.objectWillChange.send()
                }
            }
        }
        
        networkMonitor.start(queue: workerQueue)
    }
}
