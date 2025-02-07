//
//  ETSdkAuthenticationFailedView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.01.25.
//

import SwiftUI

struct ETSdkAuthenticationFailedView: View {
    // MARK: Proprties
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel = ETSdkAuthenticationFailedViewModel()
    var shouldDismiss = false
    var onReadyToSign: (() -> ())?
    private let verticalPadding = AppConfig.Dimensions.Padding.XXXL
    
    // MARK: Body
    var body: some View {
        VStack {
            Text(AppConfig.UI.Titles.Screens.authentication.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                            weight: DSFonts.FontWeight.regular,
                                            size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.top, .bottom], verticalPadding)
            
            Image(ImageProvider.statusError)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: UIScreen.main.bounds.width * 0.15, height: UIScreen.main.bounds.width * 0.15)
                .padding([.top, .bottom], verticalPadding)
            
            Text(AppConfig.UI.Evrotrust.sdkAuthenticationFailedError.localized)
                .multilineTextAlignment(.center)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                            weight: DSFonts.FontWeight.regular,
                                            size: DSFonts.FontSize.XL))
                .foregroundColor(DSColors.Text.indigoDark)
                .lineSpacing(6)
                .frame(maxWidth: .infinity, alignment: .center)
            
            ProgressView()
                .padding([.top, .bottom], verticalPadding)
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                viewModel.subscribeToUserUpdates()
            }
        }
        .onChange(of: viewModel.userReadyToSign) { userReadyToSign in
            if userReadyToSign {
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    onReadyToSign?()
                }
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name.evrotrustUserStatusNotification)) { userInfo in
            if let userStatus = userInfo.userInfo?.values.first as? UserStatusNotificationModel {
                if userStatus.isIdentified == true && userStatus.isReadyToSign == true {
                    if shouldDismiss || UserProvider.shouldContinueResetPasswordFlow {
                        dismiss()
                        viewModel.userReadyToSign = true
                    } else {
                        let vc = UIApplication.topMostViewController()
                        if UserProvider.isVerified {
                            (vc as? ViewController)?.authenticateUser()
                        } else {
                            (vc as? ViewController)?.showConfirmDataShareView()
                        }
                    }
                }
            }
        }
    }
}
