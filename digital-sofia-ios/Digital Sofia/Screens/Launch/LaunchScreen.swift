//
//  LaunchScreen.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.04.23.
//

import SwiftUI

struct LaunchScreen: View {
    @ObservedObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var hideProgressView = false
    @State private var showInitialScreen = false
    @State private var showHomeScreen = false
    
    var body: some View {
        LaunchScreenBackground(content: VStack {
            navigation()
            
            Spacer()
            
            HStack(alignment: .center) {
               ProgressView().opacity(hideProgressView ? 0 : 1)
            }
            
            Spacer()
        })
        .onChange(of: appState.state) { newValue in
            hideProgressView = true
            
            switch newValue {
            case .initial:
                showInitialScreen = true
            case .home:
                showHomeScreen = true
            case .wasLoggedOut, .tokenExpired:
                showInitialScreen = false
                showHomeScreen = false
            default: break
            }
        }
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: InitialView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showInitialScreen) { EmptyView() }
            
            NavigationLink(destination: TabbarView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
        }
    }
}

struct LaunchScreen_Previews: PreviewProvider {
    static var previews: some View {
        LaunchScreen(appState: AppState())
    }
}



