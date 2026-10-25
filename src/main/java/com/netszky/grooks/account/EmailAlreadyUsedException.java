package com.netszky.grooks.account;

import com.netszky.grooks.common.error.BusinessException;
import org.springframework.http.HttpStatus;

class EmailAlreadyUsedException extends BusinessException {
    public EmailAlreadyUsedException() {
        super("EMAIL_ALREADY_USED", "Un compte avec cet email existe déjà.", HttpStatus.CONFLICT);
    }
}

