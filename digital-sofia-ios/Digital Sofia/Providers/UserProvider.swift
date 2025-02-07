//
//  UserProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.08.23.
//

import EvrotrustSDK

final class UserProvider {
    private init() { }
    private static let USER_ACCESS_TOKEN = "DIGITAL_SOFIA_KEYCHAIN_USER"
    
    static let shared = UserProvider()
    static var loginInitiated = false
    
    static var hasActiveUser: Bool {
        return currentUser?.token != nil && currentUser?.verified == true && currentUser?.securityContext != nil
    }
    
    static var isVerified: Bool {
        return currentUser?.verified == true
    }
    
    static var currentUser: User? {
        return shared.getUser()
    }
    
    static var biometricsAvailable: Bool {
        return currentUser?.useBiometrics == true && BiometricProvider.biometricsAvailable
    }
    
    static var shouldContinueResetPasswordFlow: Bool {
        return UserDefaults.standard.bool(forKey: AppConfig.UserDefaultsKeys.userInitiatedForgottenPasswordFlow)
    }
    
    func save(user: User?) {
        if let encodedUser = JSONUtilities.shared.encode(object: user) {
            KeychainDatastore.standard.save(data: encodedUser, key: UserProvider.USER_ACCESS_TOKEN)
        }
    }
    
    func updateFirebaseToken() {
        var user = getUser()
        user?.firebaseToken = (UIApplication.shared.delegate as? AppDelegate)?.fcmToken ?? ""
        UserProvider.shared.save(user: user)
    }
    
    func haveAuthTokens() -> Bool {
        let user = getUser()
        return (user?.token?.isEmpty == false)
        && (user?.refreshToken?.isEmpty == false)
    }
    
    func updateUserWithETInfo(result: EvrotrustSetupProfileResult) {
        var user = getUser()
        
        user?.firstName = result.firstName
        user?.middleName = result.middleName
        user?.lastName = result.lastName
        
        user?.firstLatinName = result.firstLatinName
        user?.middleLatinName = result.middleLatinName
        user?.lastLatinName = result.lastLatinName
        
        user?.securityContext = result.securityContext
        
        if result.phone != nil {
            user?.phone = result.phone
        }
        
        user?.personalIdentificationNumber = result.personalIdentificationNumber
        user?.firebaseToken = (UIApplication.shared.delegate as? AppDelegate)?.fcmToken ?? ""
        
        UserProvider.shared.save(user: user)
    }
    
    func update(pin: String) {
        var user = getUser()
        user?.pin = pin
        user?.securityContext = pin.getHashedPassword
        UserProvider.shared.save(user: user)
    }
    
    func updateUserToken(info: TokenInfo, shouldVerify: Bool) {
        var user = getUser()
        user?.token = info.accessToken
        user?.refreshToken = info.refreshToken
        
        let now = Date.getFormattedNow(format: .tokenFormat)
        user?.tokenExpireDateString = now.adding(seconds: info.expiresIn).getFormattedDate(format: .tokenFormat)
        
        if shouldVerify == true {
            user?.verified = true
            user?.exists = true
        }
        
        UserProvider.shared.save(user: user)
        WebtokenRefreshHelper.shared.startTimer(with: info)
    }
    
    func deleteUser() {
        KeychainDatastore.standard.delete(key: UserProvider.USER_ACCESS_TOKEN)
        let _ = KeychainWrapper.standard.removeAllKeys()
    }
    
    func logout() {
        invalidateOldUserSession()
        NotificationCenter.default.post(name: NSNotification.Name.logoutUserNotification,
                                        object: nil,
                                        userInfo: [:])
    }
    
    func tokenExpire() {
        invalidateOldUserSession()
        NotificationCenter.default.post(name: NSNotification.Name.tokenExpiredNotification,
                                        object: nil,
                                        userInfo: [:])
    }
    
    func invalidateOldUserSession() {
        LanguageProvider.shared.appLanguage = .bulgarian
        LockScreenHelper.removeLastTouchTime()
        LockScreenHelper.removeEnterBackgroundTime()
        WebtokenRefreshHelper.shared.invalidateTimers()
        deleteUser()
    }
    
    func forceRefreshToken() {
        var user = getUser()
        user?.token?.removeLast()
        save(user: user)
    }
    
    func login(completion: @escaping (_ isSuccess: Bool) -> Void) {
        UserProvider.loginInitiated = true
        NetworkManager.registerUser(isLogin: true) { response in
            switch response {
            case .failure(let error):
                print("Error when refreshing token from register: \(error)")
                completion(false)
            case .success(let success):
                completion(success)
                
                if success {
                    NotificationCenter.default.post(name: NSNotification.Name.tokenRefreshedNotification,
                                                    object: nil,
                                                    userInfo: [:])
                }
            }
        }
    }
    
    private func getUser() -> User? {
        guard let encodedUser: String = KeychainDatastore.standard.read(key: UserProvider.USER_ACCESS_TOKEN) else {
            let user = User()
            save(user: user)
            return user
        }
        
        if let user: User = JSONUtilities.shared.decode(jsonString: encodedUser) {
            return user
        }
        
        return nil
    }
    
    func updateKeychainUser() {
        let key = "degital_sofia_user"
        if let encodedUser = KeychainWrapper.standard.string(forKey: key, withAccessibility: .whenUnlocked) {
            KeychainWrapper.standard.removeObject(forKey: key)
            KeychainDatastore.standard.save(data: encodedUser, key: UserProvider.USER_ACCESS_TOKEN)
        }
    }
}
