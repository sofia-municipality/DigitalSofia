//
//  RegisterView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.09.23.
//

import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var config: KeyboardInactivityHandlerConfig
    
    @StateObject private var viewModel = RegisterViewModel()
    @State private var personalNumber = ""
    @State private var acceptedTerms = false
    @State private var showPP = false
    @State private var showContactInfo = false
    @State private var showVerifyPIN = false
    @State private var showCreatePIN = false
    @State private var isLoading = false
    
    private let padding = AppConfig.Dimensions.Padding.XXXL
    private var egnIsValid: Bool {
        return personalNumber.isValid(rules: [ValidEGNRule()])
    }
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            KeyboardInactivityHandler {
                VStack {
                    LogoHeaderView()
                    
                    navigation()
                    
                    Text(AppConfig.UI.Register.personalIdLabelText.localized)
                        .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                        .foregroundColor(DSColors.Text.indigoDark)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
                    
                    addPersonalNumber()
                    
                    Spacer()
                    
                    addTC()
                        .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
                    
                    let buttonIsDisabled = personalNumber.isEmpty || acceptedTerms == false
                    BlueBackgroundButton(title: AppConfig.UI.Titles.Button.forward.localized, disabled: buttonIsDisabled) {
                        forward()
                    }
                }
            }
            .environmentObject(config)
            .contentShape(Rectangle())
            .padding([.leading, .trailing], RegisterFlowConstants.padding)
        }
        .onChange(of: viewModel.nextScreen) { nextScreen in
            switch nextScreen {
            case .none: break
            case .contactInfo:
                showContactInfo = true
            case .verifyPin:
                showVerifyPIN = true
            case .createPin:
                showCreatePIN = true
            }
        }
        .onTapGesture {
            config.stopTimer = true
            hideKeyboard()
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
        .ignoresSafeArea(.keyboard)
    }
    
    private func addPersonalNumber() -> some View {
        VStack {
            Text(AppConfig.UI.Text.idNumberLabel.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            TextField("", text: $personalNumber)
                .keyboardType(.asciiCapableNumberPad)
                .placeholder(when: personalNumber.isEmpty) {
                    Text(AppConfig.UI.Text.idNumberPlaceholder.localized)
                        .font(RegisterFlowConstants.placeholderFont)
                        .foregroundColor(RegisterFlowConstants.placeholderColor)
                }
                .onChange(of: personalNumber, perform: { newValue in
                    if newValue.count > 8 {
                        config.startTimer = true
                    } else {
                        config.stopTimer = true
                    }
                    
                    if newValue.count == 10 {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            hideKeyboard()
                        }
                    }
                })
                .submitLabel(.done)
                .padding(.all, AppConfig.Dimensions.Padding.medium)
                .overlay(
                    RoundedRectangle(cornerRadius: AppConfig.Dimensions.CornerRadius.mini)
                        .stroke(DSColors.Indigo.regular.opacity(0.2), lineWidth: AppConfig.Dimensions.Standart.lineHeight / 2)
                )
                .frame(width: RegisterFlowConstants.fieldWidth)
                .padding(.bottom, AppConfig.Dimensions.Padding.XL)
        }
        .padding(.bottom, AppConfig.Dimensions.Padding.XL)
    }
    
    private func addTC() -> some View {
        HStack() {
            Toggle("", isOn: $acceptedTerms).labelsHidden()
                .toggleStyle(iOSCheckboxToggleStyle())
            Text(AppConfig.UI.Register.termsAndConditions.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Blue.blue6)
                .underline()
                .frame(maxWidth: .infinity, alignment: .leading)
                .onTapGesture {
                    showPP = true
                }
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: PrivacyPolicyView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showPP) { EmptyView() }
            
            NavigationLink(destination: ContactInfoView()
                .environmentObject(appState)
                .environmentObject(networkMonitor)
                .environmentObject(KeyboardInactivityHandlerConfig(inactivityDelay: 3)),
                           isActive: $showContactInfo) { EmptyView() }
            
            NavigationLink(destination: VerifyPINView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showVerifyPIN) { EmptyView() }
            
            NavigationLink(destination: RegisterPINView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showCreatePIN) { EmptyView() }
        }
    }
    
    private func forward() {
        if personalNumber.isEmpty {
            appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.registerMissingInfoAlertText.localized)
        } else if egnIsValid == false {
            appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.enterValidEgnAlertText.localized)
        } else if acceptedTerms == false {
            appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.registerAcceptTermsAlertText.localized)
        } else {
            isLoading = true
            viewModel.check(personalNumber: personalNumber) { error in
                isLoading = false
                
                if let error = error {
                    appState.alertItem = AlertProvider.errorAlert(message: error.description)
                }
            }
        }
    }
}

#Preview {
    RegisterView()
}

struct RegisterFlowConstants {
    static let padding = AppConfig.Dimensions.Padding.XXXL
    static let placeholderFont = DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.large)
    static let placeholderColor = DSColors.Text.placeholder.opacity(0.3)
    static var fieldWidth: CGFloat {
        return UIScreen.main.bounds.width - RegisterFlowConstants.padding * 2
    }
}

enum RegisterFlowNextScreen {
    case none, contactInfo, createPin, verifyPin
}
