//
//  ProfileView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.06.23.
//

import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    var body: some View {
        VStack() {
            CustomNavigationBar()
            
            ProfileTiltleView(title: SettingsType.profile.description)
            
            VStack(alignment: .leading) {
                Image(ImageProvider.person)
                    .padding(.bottom, AppConfig.Dimensions.Padding.medium)
                
                profileInfoView()
                
                Spacer()
            }
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXXL)
            .padding(.top, AppConfig.Dimensions.Padding.XXL)
        }
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func profileInfoView() -> some View {
        let user = UserProvider.currentUser
        let name = LanguageProvider.shared.appLanguage == .bulgarian ? user?.fullName : user?.fullLatinName
        
        return Text(name?.capitalized ?? "")
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, 
                                        weight: DSFonts.FontWeight.regular,
                                        size: DSFonts.FontSize.XL))
            .foregroundColor(DSColors.Text.indigoDark)
            .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileView()
    }
}
