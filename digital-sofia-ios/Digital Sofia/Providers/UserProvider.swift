//
//  UserProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.08.23.
//

import EvrotrustSDK

final class UserProvider {
    private static let USER_ACCESS_TOKEN = "degital_sofia_user"
    
    static let shared = UserProvider()
    
    func save(user: User?) {
        if let encodedUser = JSONUtilities.shared.encode(object: user) {
            KeychainDatastore.standard.save(data: encodedUser, key: UserProvider.USER_ACCESS_TOKEN)
        }
    }
    
    func getUser() -> User? {
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
    
    func haveAuthTokens() -> Bool {
        let user = getUser()
        return (user?.token?.isEmpty == false) && (user?.refreshToken?.isEmpty == false)
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
        
        UserProvider.shared.save(user: user)
    }
    
    func deleteUser() {
        KeychainDatastore.standard.delete(key: UserProvider.USER_ACCESS_TOKEN)
    }
    
    func logout() {
        deleteUser()
        LanguageProvider.shared.appLanguage = .bulgarian
        NotificationCenter.default.post(name: NSNotification.Name.logoutUserNotification,
                                        object: nil,
                                        userInfo: [:])
    }
}
