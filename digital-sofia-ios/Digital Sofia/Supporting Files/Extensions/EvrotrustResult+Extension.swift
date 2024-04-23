//
//  EvrotrustResult+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.01.24.
//

import EvrotrustSDK

extension EvrotrustChangeSecurityContextResult {
    var dictionary: [String: Any] {
        return ["status": status.rawValue,
                "changed": changed,
                "securityContext": securityContext]
    }
}

extension EvrotrustEditPersonalDataResult {
    var dictionary: [String: Any] {
        return ["status": status.rawValue,
                "identified": identified,
                "supervised": supervised,
                "readyToSign": readyToSign,
                "rejected": rejected,
                "editPersonalData": editPersonalData,
                "rejectReason": rejectReason.rawValue]
    }
}

extension EvrotrustCheckUserStatusResult {
    var dictionary: [String: Any] {
        return ["status": status.rawValue,
                "successfulCheck": successfulCheck,
                "identified": identified,
                "supervised": supervised,
                "readyToSign": readyToSign,
                "rejected": rejected,
                "confirmedPhone": confirmedPhone,
                "confirmedEmail": confirmedEmail,
                "rejectReason": rejectReason.rawValue]
    }
}

extension EvrotrustSetupProfileResult {
    var dictionary: [String: Any] {
        return ["status": status.rawValue,
                "userSetUp": userSetUp,
                "identified": identified,
                "supervised": supervised,
                "readyToSign": readyToSign,
                "rejected": rejected,
                "rejectReason": rejectReason.rawValue,
                "securityContext": securityContext ?? "",
                "pinCode": pinCode ?? "",
                "countryCode2": countryCode2 ?? "",
                "countryCode3": countryCode3 ?? "",
                "personalIdentificationNumber": personalIdentificationNumber ?? "",
                "firstName": firstName ?? "",
                "middleName": middleName ?? "",
                "lastName": lastName ?? "",
                "firstLatinName": firstLatinName ?? "",
                "middleLatinName": middleLatinName ?? "",
                "lastLatinName": lastLatinName ?? "",
                "phone": phone ?? ""]
    }
}

extension EvrotrustOpenDocumentResult {
    var dictionary: [String: Any] {
        return ["status": status.rawValue,
                "userDecision": userDecision.rawValue]
    }
}
