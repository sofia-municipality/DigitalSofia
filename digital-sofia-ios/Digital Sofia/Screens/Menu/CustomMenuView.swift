//
//  CustomMenuView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import SwiftUI

struct CustomMenuView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showSettings = false
    @State private var showPP = false
    @State private var showFAQ = false
    @State private var showContacts = false
    @Binding var presentView: Bool
    
    var body: some View {
        navigation()
        
        VStack(spacing: AppConfig.Dimensions.Padding.XXXL) {
            HStack {
                Spacer()
                Image(ImageProvider.close)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: AppConfig.Dimensions.Padding.XL, height: AppConfig.Dimensions.Padding.XL)
            }
            .padding(.top, AppConfig.Dimensions.Padding.XL)
            .onTapGesture {
                presentView = false
            }
            
            createSettingsMenuRow(title: AppConfig.UI.Titles.Screens.settings.localized, image: ImageProvider.settings, action: {
                showSettings = true
            })
            
            createSeparator()
            
            createSettingsMenuRow(title: AppConfig.UI.Menu.faq.localized, image: ImageProvider.faqIcon, small: true, action: {
                showFAQ = true
            })
            createSettingsMenuRow(title: AppConfig.UI.Menu.contacts.localized, image: ImageProvider.contactIcon, small: true, action: {
                showContacts = true
            })
            
            Spacer()
            
            Button(action: {
                showPP = true
            }) {
                Text(AppConfig.UI.Menu.privacyPolicy.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(.bottom, AppConfig.Dimensions.Padding.XL)
        }
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXL)
    }
    
    private func createSettingsMenuRow(title: String, image: String, small: Bool = false, action: @escaping () -> ()) -> some View {
        let imageSize = small ? AppConfig.Dimensions.Padding.medium : AppConfig.Dimensions.Padding.large
        
        return HStack() {
            Text(title)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL))
                .minimumScaleFactor(0.01)
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.trailing, AppConfig.Dimensions.Padding.XL)
            
            Image(image)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: imageSize, height: imageSize)
        }
        .onTapGesture {
            action()
        }
    }
    
    private func createSeparator() -> some View {
        Rectangle()
            .fill(DSColors.Text.placeholder).opacity(0.3)
            .frame(height: 1)
            .edgesIgnoringSafeArea(.horizontal)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: SettingsView().id(appState.settingsViewId)
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showSettings) { EmptyView() }
            
            NavigationLink(destination: PrivacyPolicyView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showPP) { EmptyView() }
            
            NavigationLink(destination: FAQView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showFAQ) { EmptyView() }
            
            NavigationLink(destination: ContactsView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showContacts) { EmptyView() }
        }
    }
}

struct CustomMenuView_Previews: PreviewProvider {
    static var previews: some View {
        CustomMenuView(presentView: .constant(true))
    }
}
