//
//  PrivacyPolicyView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import SwiftUI

struct PrivacyPolicyView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    
    var body: some View {
        VStack(spacing: 0) {
            CustomNavigationBar(bigTitle: true)
            
            DSWebView(type: .privacyPolicy)
                .environmentObject(appState)
                .environmentObject(networkMonitor)
        }
        .log(view: self)
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
}

struct PrivacyPolicyView_Previews: PreviewProvider {
    static var previews: some View {
        PrivacyPolicyView()
    }
}
