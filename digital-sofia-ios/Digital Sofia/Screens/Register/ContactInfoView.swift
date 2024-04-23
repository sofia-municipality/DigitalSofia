//
//  ContactInfoView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import SwiftUI

struct ContactInfoView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var config: KeyboardInactivityHandlerConfig
    
    @FocusState private var phoneIsFocused: Bool
    @State private var email = ""
    @State private var phone = ""
    @State private var acceptedTerms = false
    @State private var showPIN = false
    
    private var phoneCodeLength = 4
    private var phoneLength = 13
    private let bgPhoneCode = "+359"
    private let allowedCharacters = "+0123456789"
    
    private var emailIsValid: Bool {
        return email.isValid(rules: [ValidEmailRule()])
    }
    
    private var phoneIsValid: Bool {
        return phone.isValid(rules: [ValidPhoneRule()])
    }
    
    var body: some View {
        KeyboardInactivityHandler {
            VStack {
                LogoHeaderView()
                
                navigation()
                
                Text(AppConfig.UI.Register.contactInformation.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
                
                addEmail()
                addPhone()
                
                Spacer()
                
                let buttonIsDisabled = email.isEmpty || phone.isEmpty
                BlueBackgroundButton(title: AppConfig.UI.Titles.Button.forward.localized, disabled: buttonIsDisabled) {
                    forward()
                }
            }
        }
        .environmentObject(config)
        .contentShape(Rectangle())
        .onTapGesture {
            config.stopTimer = true
            hideKeyboard()
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .padding([.leading, .trailing], RegisterFlowConstants.padding)
        .backgroundAndNavigation()
        .ignoresSafeArea(.keyboard)
    }
    
    private func addEmail() -> some View {
        VStack {
            Text(AppConfig.UI.Text.emailLabel.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            TextField("", text: $email)
                .keyboardType(.emailAddress)
                .submitLabel(.done)
                .autocapitalization(.none)
                .disableAutocorrection(true)
                .placeholder(when: email.isEmpty) {
                    Text(AppConfig.UI.Text.emailPlaceholder.localized)
                        .font(RegisterFlowConstants.placeholderFont)
                        .foregroundColor(RegisterFlowConstants.placeholderColor)
                }
                .onChange(of: email, perform: { newValue in
                    handleKeyboardInactivity(for: newValue, type: .email)
                })
                .padding(.all, AppConfig.Dimensions.Padding.medium)
                .overlay(
                    RoundedRectangle(cornerRadius: AppConfig.Dimensions.CornerRadius.mini)
                        .stroke(DSColors.Indigo.regular.opacity(0.2), lineWidth: AppConfig.Dimensions.Standart.lineHeight / 2)
                )
                .frame(width: RegisterFlowConstants.fieldWidth)
                .padding(.bottom, AppConfig.Dimensions.Padding.XL)
        }
    }
    
    private func addPhone() -> some View {
        VStack {
            Text(AppConfig.UI.Text.phoneLabel.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            TextField("", text: $phone)
                .keyboardType(.asciiCapableNumberPad)
                .submitLabel(.done)
                .focused($phoneIsFocused)
                .onChange(of: phoneIsFocused) { isFocused in
                    if isFocused {
                        if phone.isEmpty {
                            phone = bgPhoneCode
                        }
                    }
                }
                .onChange(of: phone, perform: { newValue in
                    handleKeyboardInactivity(for: newValue, type: .phone)
                    let filtered = newValue.filter { allowedCharacters.contains($0) }
                    if filtered != newValue {
                        phone = filtered
                    }
                    
                    if newValue.count < phoneCodeLength {
                        phone = bgPhoneCode
                    } else if newValue.count > phoneLength {
                        phone = String(phone.prefix(phoneLength))
                    } else if newValue.count == phoneCodeLength + 1 {
                        if newValue.contains("0") {
                            phone = bgPhoneCode
                        }
                    }
                })
                .placeholder(when: phone.isEmpty) {
                    Text(AppConfig.UI.Text.phonePlaceholder.localized)
                        .font(RegisterFlowConstants.placeholderFont)
                        .foregroundColor(RegisterFlowConstants.placeholderColor)
                }
                .padding(.all, AppConfig.Dimensions.Padding.medium)
                .overlay(
                    RoundedRectangle(cornerRadius: AppConfig.Dimensions.CornerRadius.mini)
                        .stroke(DSColors.Indigo.regular.opacity(0.2), lineWidth: AppConfig.Dimensions.Standart.lineHeight / 2)
                )
                .frame(width: RegisterFlowConstants.fieldWidth)
                .padding(.bottom, AppConfig.Dimensions.Padding.XL)
        }
    }
    
    private func forward() {
        if email.isEmpty ||  phone.isEmpty {
            appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.registerMissingInfoAlertText.localized)
        } else if emailIsValid == false {
            appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.enterValidEmailAlertText.localized)
        } else if phoneIsValid == false {
            appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.enterValidPhoneAlertText.localized)
        } else {
            var user = UserProvider.currentUser
            user?.email = email
            user?.phone = phone
            UserProvider.shared.save(user: user)
            
            showPIN = true
        }
    }
    
    private func handleKeyboardInactivity(for text: String, type: TextFieldType) {
        switch type {
        case .email:
            if text.count > 20 {
                config.startTimer = true
            } else {
                config.stopTimer = true
            }
        case .phone:
            if text.count > 11 {
                config.startTimer = true
            } else {
                config.stopTimer = true
            }
            
            if text.count == 13 {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    hideKeyboard()
                }
            }
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: RegisterPINView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showPIN) { EmptyView() }
        }
    }
}

#Preview {
    ContactInfoView()
}

enum TextFieldType {
    case email, phone
}
