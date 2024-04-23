//
//  TabbarView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.23.
//

import SwiftUI
import Combine

internal enum HomeTabBarItemType: Int {
    case service, myServices, docments, pendingDocuments
}

struct TabbarView: View, KeyboardReadable {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showMenu = false
    @State private var isKeyboardVisible = false
    @State private var showTagAlert = false
    @StateObject private var viewModel = LoginCodeViewModel()
    
    @AppStorage(AppConfig.UserDefaultsKeys.selectedTab) private var selectedTab: Int = HomeTabBarItemType.service.rawValue
    
    var body: some View {
        BetaTagScreenCover(isPresented: $showTagAlert) {
            VStack(spacing: 0) {
                menuToolbar()
                
                ZStack {
                    TabView(selection: $selectedTab) {
                        ServiceView()
                            .environmentObject(appState)
                            .environmentObject(networkMonitor)
                            .tabItem {
                                Image(selectedTab == HomeTabBarItemType.service.rawValue ? ImageProvider.TabView.editFilled : ImageProvider.TabView.edit)
                                Text(AppConfig.UI.Titles.Tabbar.service.localized)
                            }
                            .tag(HomeTabBarItemType.service.rawValue)
                        
                        MyServicesView()
                            .environmentObject(appState)
                            .environmentObject(networkMonitor)
                            .tabItem {
                                Image(selectedTab == HomeTabBarItemType.myServices.rawValue ? ImageProvider.TabView.myServicesFilled : ImageProvider.TabView.myServices)
                                Text(AppConfig.UI.Titles.Tabbar.myServices.localized)
                            }
                            .tag(HomeTabBarItemType.myServices.rawValue)
                        
                        DocumentsHistoryView()
                            .environmentObject(appState)
                            .tabItem {
                                Image(selectedTab == HomeTabBarItemType.docments.rawValue ? ImageProvider.TabView.editDocumentFilled : ImageProvider.TabView.editDocument)
                                Text(AppConfig.UI.Titles.Tabbar.docs.localized)
                            }
                            .tag(HomeTabBarItemType.docments.rawValue)
                        
                        PendingDocumentsView()
                            .environmentObject(appState)
                            .tabItem {
                                Image(selectedTab == HomeTabBarItemType.pendingDocuments.rawValue ? ImageProvider.TabView.homeFilled : ImageProvider.TabView.home)
                                Text(AppConfig.UI.Titles.Tabbar.home.localized)
                            }
                            .tag(HomeTabBarItemType.pendingDocuments.rawValue)
                            .badge(appState.hasPendingDocuments ? " " : nil)
                    }
                    
                    if isKeyboardVisible == false {
                        VStack {
                            Spacer()
                            Rectangle()
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: DSColors.TabbarGradient.list),
                                        startPoint: .leading,
                                        endPoint: .trailing)
                                )
                                .frame(width: UIScreen.main.bounds.width, height:  1)
                                .padding(.bottom, UITabBarController().height)
                        }
                    }
                }
            }
        }
        .onAppear {
            showMenu = false
        }
        .onChange(of: selectedTab) { newValue in
            showMenu = false
        }
        .onLoad {
            if appState.loginRequestCode == nil {
                viewModel.getAuthenticationCode { response, _ in
                    if response?.codeExists == true {
                        if let code = response?.code {
                            appState.loginRequestCode = code
                        }
                    }
                }
            }
        }
        .customMenuSheet(isPresented: $showMenu, frame: CGSize(width: UIScreen.main.bounds.width * 0.8, height: UIScreen.main.bounds.height * 0.35)) {
            CustomMenuView(presentView: $showMenu)
                .environmentObject(appState)
                .environmentObject(networkMonitor)
        }
        .onReceive(keyboardPublisher) { newIsKeyboardVisible in
            isKeyboardVisible = newIsKeyboardVisible
        }
        .log(view: self)
        .alert()
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .accentColor(DSColors.Blue.blue6)
        .navigationBarHidden(true)
    }
    
    private func menuToolbar() -> some View {
        let shouldAddLogo = selectedTab != HomeTabBarItemType.pendingDocuments.rawValue
        let color = shouldAddLogo ? Color.white : DSColors.background
        
        return VStack(spacing: 0) {
            HStack() {
                if shouldAddLogo {
                    Image(ImageProvider.logo)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(height: AppConfig.Dimensions.Standart.rowHeight * 1.2)
                } else {
                    EmptyView()
                }
                
                Spacer()
                
                if selectedTab == HomeTabBarItemType.service.rawValue {
                    BetaTagView(perform: {
                        showTagAlert = true
                    })
                    .padding([.trailing], AppConfig.Dimensions.Padding.XL * 3)
                    Spacer()
                }
                
                Button {
                    showMenu = true
                } label: {
                    Image(ImageProvider.menu)
                        .foregroundColor(DSColors.Text.indigoDark)
                }
            }
            .frame(maxWidth: .infinity)
            .padding([.top, .bottom], AppConfig.Dimensions.Padding.large)
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XL)
            .background(color)
            
            if shouldAddLogo {
                Rectangle()
                    .fill(DSColors.toggleDeselected)
                    .frame(width: UIScreen.main.bounds.width, height:  1)
            }
        }
    }
}

#Preview {
    TabbarView()
}
