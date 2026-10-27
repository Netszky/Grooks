package com.netszky.grooks.account;

import com.netszky.grooks.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class CurrentUserNotFoundException extends BusinessException {
    public CurrentUserNotFoundException(){
        super("UNAUTHENTICATED", "La session n'est plus valide", HttpStatus.UNAUTHORIZED);
    }
}
