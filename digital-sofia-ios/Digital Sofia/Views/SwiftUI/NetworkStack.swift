//
//  NetworkStack.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.02.24.
//

import SwiftUI

struct NetworkStack<T: View>: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    private var content: T
    private var centered = false
    
    init(@ViewBuilder content: () -> T,
         centered: Bool = false) {
        self.content = content()
        self.centered = centered
    }
    
    var body: some View {
        if networkMonitor.isConnected {
            content
        } else {
            VStack {
                if centered {
                    Spacer()
                }
                
                NoNetworkConnetionView()
                
                if centered {
                    Spacer()
                }
            }
        }
    }
}
