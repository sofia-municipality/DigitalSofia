//
//  ServiceView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import SwiftUI

struct ServiceView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    var body: some View {
        DSWebView(type: .services)
            .environmentObject(appState)
            .environmentObject(networkMonitor)
            .log(view: self)
    }
}

struct ServiceView_Previews: PreviewProvider {
    static var previews: some View {
        ServiceView()
    }
}
