//
//  SettingsView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 26.04.23.
//

import SwiftUI

enum SettingsType: CaseIterable {
    case profile, language, security, pin
    
    var description: String {
        switch self {
        case .profile:
            return AppConfig.UI.Profile.profile.localized
        case .language:
            return AppConfig.UI.Profile.language.localized
        case .security:
            return AppConfig.UI.Profile.security.localized
        case .pin:
            return AppConfig.UI.Profile.pin.localized
        }
    }
    
    @ViewBuilder
    var destination: some View {
        switch self {
        case .profile:
            ProfileView()
        case .language:
            ProfileLanguageView()
        case .security:
            BiometricAuthenticationView()
        case .pin:
            ChangePINView(state: .old)
        }
    }
}

struct SettingsView: View {
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showProfileOptionView = false
    @State private var showProfileDeleteView = false
    @State private var selectedItem: SettingsType?
    
    var body: some View {
        VStack(spacing: 0) {
            NavigationLink(destination: selectedItem?.destination.environmentObject(appState).environmentObject(networkMonitor),
                           isActive: $showProfileOptionView) { EmptyView() }
            
            NavigationLink(destination: ProfileDeleteView().environmentObject(appState).environmentObject(networkMonitor),
                           isActive: $showProfileDeleteView) { EmptyView() }
            
            CustomNavigationBar()
            CustomNavigationBar(title: AppConfig.UI.Titles.Screens.settings.localized, image: ImageProvider.settings)
            
            Spacer()
            Spacer()
            
            VStack(spacing: AppConfig.Dimensions.Padding.large) {
                ForEach(SettingsType.allCases, id: \.self) { item in
                    createListRow(item: item)
                        .onTapGesture {
                            selectedItem = item
                            showProfileOptionView = true
                        }
                }
            }
            
            Spacer()
            
            Button {
                showProfileDeleteView = true
            } label: {
                HStack {
                    Image(ImageProvider.personRemove)
                        .foregroundColor(DSColors.Text.indigoDark)
                    Text(AppConfig.UI.Titles.Button.deleteProfile.localized)
                        .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                        .foregroundColor(DSColors.Text.indigoDark)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.standart + AppConfig.Dimensions.Padding.large)
            .padding(.bottom, AppConfig.Dimensions.Padding.XXXL)
        }
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func createListRow(item: SettingsType) -> some View {
        SettingsListRow(title: item.description, subTitle: item == .language
                        ? LanguageProvider.shared.appLanguage?.description ?? ""
                        : item == .security ? AppConfig.UI.Profile.Security.faceID.localized : "")
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
