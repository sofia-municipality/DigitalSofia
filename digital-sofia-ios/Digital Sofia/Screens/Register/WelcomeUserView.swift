//
//  WelcomeUserView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 17.08.23.
//

import SwiftUI

struct WelcomeUserView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @State var viewModel: WelcomeUserViewModel
    
    @State private var loginViewModel: LoginViewModel?
    @State private var showHomeScreen = false
    
    init(viewModel: WelcomeUserViewModel) {
        _viewModel = State(initialValue: viewModel)
    }
    
    var body: some View {
        VStack(spacing: AppConfig.Dimensions.Padding.XXL) {
            navigation()
            
            loginViewModel?.welcomeView()
                .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
            
            stateImage()
            
            titleLabel()
            
            detailsLabel()
            
            Spacer()
            Spacer()
            
            viewModel.state == .success ? AnyView(goToHomeButton()) : AnyView(startAgainButton())
            
            Spacer()
        }
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXL)
        .onAppear {
            self.loginViewModel = LoginViewModel()
            
            if viewModel.state == .success {
                NotificationCenter.default.post(name: NSNotification.Name.checkPendingDocumentsNotification,
                                                object: nil,
                                                userInfo: [:])
            }
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: TabbarView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
            
        }
    }
    
    private func stateImage() -> some View {
        HStack {
            Spacer()
            Image(viewModel.state == .success ? ImageProvider.statusOK : ImageProvider.statusError)
            Spacer()
        }
        .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
    }
    
    private func titleLabel() -> some View {
        Text(viewModel.state == .success ? AppConfig.UI.Text.welcomeUserOkTitle.localized : AppConfig.UI.Alert.welcomeUserErrorTitle.localized)
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XLarge))
            .foregroundColor(DSColors.Text.indigoDark)
            .frame(maxWidth: .infinity, alignment: .center)
    }
    
    private func detailsLabel() -> some View {
        var message = AppConfig.UI.Text.welcomeUserOkDetails.localized
        
        switch viewModel.state {
        case .error(description: let error):
            message = error
        default: break
        }
        
        return Text(message)
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.large))
            .foregroundColor(DSColors.Text.indigoDark)
            .multilineTextAlignment(.center)
            .lineSpacing(AppConfig.Dimensions.Padding.large)
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.top, AppConfig.Dimensions.Padding.large)
    }
    
    private func startAgainButton() -> some View {
        Button(action: {
            NotificationCenter.default.post(name: NSNotification.Name.logoutUserNotification,
                                            object: nil,
                                            userInfo: [:])
        }) {
            Text(AppConfig.UI.Titles.Button.restartRegistration.localized)
                .foregroundColor(DSColors.Blue.blue)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
        }
    }
    
    private func goToHomeButton() -> some View {
        Button(action: {
            showHomeScreen = true
        }) {
            HStack {
                Text(AppConfig.UI.Titles.Tabbar.service.localized)
                    .foregroundColor(.white)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXL))
            }
        }
        .buttonStyle(PlainButtonStyle())
        .padding([.top, .bottom], AppConfig.Dimensions.Padding.XXL)
        .padding([.leading, .trailing], UIScreen.main.bounds.width * 0.2)
        .background(DSColors.Blue.blue6)
        .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
    }
}

struct WelcomeUserView_Previews: PreviewProvider {
    static var previews: some View {
        WelcomeUserView(viewModel: WelcomeUserViewModel())
    }
}

@MainActor class WelcomeUserViewModel: ObservableObject {
    enum WelcomeUserViewState: Equatable {
        case success, error(description: String)
    }
    
    @Published var state: WelcomeUserViewState?
}
