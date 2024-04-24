//
//  ServicesView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 26.04.23.
//

import SwiftUI

struct MyServicesView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    var body: some View {
        DSWebView(type: .myServices)
            .environmentObject(appState)
            .environmentObject(networkMonitor)
            .log(view: self)
    }
}

struct ServicesView_Previews: PreviewProvider {
    static var previews: some View {
        MyServicesView()
    }
}


