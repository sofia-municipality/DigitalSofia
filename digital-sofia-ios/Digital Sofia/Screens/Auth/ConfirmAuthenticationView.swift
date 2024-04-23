//
//  ConfirmAuthenticationView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.01.24.
//

import SwiftUI

enum ConfirmAuthenticationViewType {
    case accept, decline, error
    
    var buttonTitle: (yes: String, no: String) {
        switch self {
        case .accept:
            return (AppConfig.UI.Titles.Button.accept.localized, AppConfig.UI.Titles.Button.decline.localized)
        case .decline:
            return (AppConfig.UI.Titles.Screens.authentication.localized, AppConfig.UI.Titles.Button.back.localized)
        case .error:
            return (AppConfig.UI.Titles.Screens.authentication.localized, "")
        }
    }
    
    var image: String? {
        switch self {
        case .accept:
            return nil
        case .decline, .error:
            return ImageProvider.statusError
        }
    }
    
    var details: String {
        switch self {
        case .accept:
            return AppConfig.UI.Text.authenticationConfirmDetailsText.localized
        case .decline:
            return AppConfig.UI.Text.authenticationRejectDetailsText.localized
        case .error:
            return AppConfig.UI.Text.somethingWentWrongErrorText.localized
        }
    }
}

struct ConfirmAuthenticationView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    
    @State private var type: ConfirmAuthenticationViewType = .accept
    @State private var isLoading = false
    
    @State private var showETSetup = false
    @State private var showConfirmDataShare = false
    @State private var showTabbar = false
    @StateObject private var viewModel = ConfirmAuthenticationViewModel()
    
    private let verticalPadding = AppConfig.Dimensions.Padding.XXXL
    private let horizontalPadding = AppConfig.Dimensions.Padding.XXXL
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack(spacing: verticalPadding) {
                navigation()
                
                Text(AppConfig.UI.Titles.Screens.authentication.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.XXXL))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding([.top, .bottom], verticalPadding)
                
                if let image = type.image {
                    Image(image)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: UIScreen.main.bounds.width * 0.1, height: UIScreen.main.bounds.width * 0.1)
                        .padding([.top, .bottom], verticalPadding)
                }
                
                Text(type.details)
                    .multilineTextAlignment(.center)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.XL))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .lineSpacing(6)
                    .frame(maxWidth: .infinity, alignment: .center)
                
                Spacer()
                
                HStack() {
                    BlueTextButton(title: type.buttonTitle.no) {
                        switch type {
                        case .decline:
                            dismiss()
                        case .error, .accept:
                            type = .decline
                        }
                    }
                    
                    BlueBackgroundButton(title: type.buttonTitle.yes, action: {
                        showETSetup = true
                    })
                }
                .padding(.bottom, verticalPadding)
            }
            .padding([.leading, .trailing], horizontalPadding)
            .backgroundAndNavigation()
        }
        .log(view: self)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: EvrotrustFullSetupView(shouldAddUserInformation: true, completion: { success, error in
                if let error = error {
                    showETSetup = false
                    type = error == .userCancelled ? .decline : .error
                } else {
                    if success == true {
                        showETSetup = false
                        isLoading = true
                        showConfirmDataShareScreen()
                    } else {
                        UserProvider.shared.logout()
                    }
                }
            })
                .ignoresSafeArea(),
                           isActive: $showETSetup) { EmptyView() }
            
            NavigationLink(destination: TabbarView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showTabbar) { EmptyView() }
            
            NavigationLink(destination: ConfirmDataShareView()
                .environmentObject(appState)
                .environmentObject(networkMonitor)
                .environmentObject(IdentityRequestConfig()),
                           isActive: $showConfirmDataShare) { EmptyView() }
        }
    }
    
    private func showConfirmDataShareScreen() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            if UserProvider.isVerified {
                viewModel.register { error in
                    isLoading = false
                    if error != nil {
                        type = .error
                    } else {
                        showTabbar = true
                    }
                }
            } else {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    isLoading = false
                    showConfirmDataShare = true
                }
            }
        }
    }
}

#Preview {
    ConfirmAuthenticationView()
}
