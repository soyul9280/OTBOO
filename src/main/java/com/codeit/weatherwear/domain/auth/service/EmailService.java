package com.codeit.weatherwear.domain.auth.service;

public interface EmailService {

  void sendTempPasswordEmail(String toEmail, String tempPassword);

}
