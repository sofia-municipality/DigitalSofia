//
//  TabbarView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.23.
//

import SwiftUI

internal enum HomeTabBarItemType: Int {
    case service, myServices, docments, home
}

struct TabbarView: View {
    @State private var selection = 0
    @State private var showMenu = false
    
    @ObservedObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    var body: some View {
        VStack(spacing: 0) {
            menuToolbar()
            
            TabView(selection: $selection) {
                ServiceView()
                    .environmentObject(appState)
                    .environmentObject(networkMonitor)
                    .tabItem {
                        Image(selection == HomeTabBarItemType.service.rawValue ? ImageProvider.TabView.editFilled : ImageProvider.TabView.edit)
                        Text(AppConfig.UI.Titles.Tabbar.service.localized)
                    }
                    .tag(HomeTabBarItemType.service.rawValue)
                
                MyServicesView()
                    .environmentObject(appState)
                    .environmentObject(networkMonitor)
                    .tabItem {
                        Image(selection == HomeTabBarItemType.myServices.rawValue ? ImageProvider.TabView.myServicesFilled : ImageProvider.TabView.myServices)
                        Text(AppConfig.UI.Titles.Tabbar.myServices.localized)
                    }
                    .tag(HomeTabBarItemType.myServices.rawValue)
                
                DocumentsHistoryView()
                    .environmentObject(appState)
                    .tabItem {
                        Image(selection == HomeTabBarItemType.docments.rawValue ? ImageProvider.TabView.editDocumentFilled : ImageProvider.TabView.editDocument)
                        Text(AppConfig.UI.Titles.Tabbar.docs.localized)
                    }
                    .tag(HomeTabBarItemType.docments.rawValue)
                
                PendingDocumentsView()
                    .environmentObject(appState)
                    .tabItem {
                        Image(selection == HomeTabBarItemType.home.rawValue ? ImageProvider.TabView.homeFilled : ImageProvider.TabView.home)
                        Text(AppConfig.UI.Titles.Tabbar.home.localized)
                    }
                    .tag(HomeTabBarItemType.home.rawValue)
                    .badge(appState.hasPendingDocuments ? " " : nil)
            }
        }
        .onAppear {
            showMenu = false
        }
        .onChange(of: selection) { newValue in
            showMenu = false
        }
        .customMenuSheet(isPresented: $showMenu, frame: CGSize(width: UIScreen.main.bounds.width * 0.8, height: UIScreen.main.bounds.height * 0.35)) {
            CustomMenuView(presentView: $showMenu)
                .environmentObject(appState)
                .environmentObject(networkMonitor)
        }
        .accentColor(DSColors.Blue.blue6)
        .navigationBarHidden(true)
    }
    
    private func menuToolbar() -> some View {
        let isServices = selection == HomeTabBarItemType.service.rawValue || selection == HomeTabBarItemType.myServices.rawValue
        
        return HStack {
            if isServices {
                Image(ImageProvider.logo)
            } else {
                EmptyView()
            }
            
            Spacer()
            
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
        .background(isServices ? Color.white : DSColors.background)
    }
}

#Preview {
    TabbarView(appState: AppState())
}
