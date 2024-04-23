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
    @State private var showLockScreen = false
    @State private var showPINLockScreen = false
    
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
            case .lockScreen:
                let useBiometrics = UserProvider.biometricsAvailable
                showLockScreen = useBiometrics
                showPINLockScreen = !useBiometrics
            case .home:
                showHomeScreen = true
            case .wasLoggedOut, .tokenExpired:
                deinitNavugation()
            default:
                deinitNavugation()
            }
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: InitialView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showInitialScreen) { EmptyView() }
            
            NavigationLink(destination: TabbarView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
            
            NavigationLink(destination: LoginPINView()
                .environmentObject(networkMonitor)
                .environmentObject(appState),
                           isActive: $showPINLockScreen) { EmptyView() }
            
            NavigationLink(destination: LoginWithBiometricsView()
                .environmentObject(networkMonitor)
                .environmentObject(appState),
                           isActive: $showLockScreen) { EmptyView() }
        }
    }
    
    private func deinitNavugation() {
        showInitialScreen = false
        showHomeScreen = false
        showLockScreen = false
        showPINLockScreen = false
    }
}

struct LaunchScreen_Previews: PreviewProvider {
    static var previews: some View {
        LaunchScreen(appState: AppState())
    }
}



