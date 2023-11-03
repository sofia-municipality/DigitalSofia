//
//  NetworkResponse.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Alamofire

enum NetworkResponse<T> {
  case success(T)
  case failure(Error)
}
